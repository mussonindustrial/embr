package com.mussonindustrial.ignition.embr.periscope.pystore

import com.inductiveautomation.ignition.common.binding.InteractionListener
import com.inductiveautomation.ignition.common.execution.ExecutionManager
import com.inductiveautomation.ignition.common.lifecycle.AbstractLifecycle
import com.inductiveautomation.perspective.gateway.model.PageModel
import com.inductiveautomation.perspective.gateway.session.InternalSession
import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.embr.perspective.gateway.model.ThreadContext
import java.lang.ref.WeakReference
import java.util.IdentityHashMap
import java.util.Locale
import java.util.concurrent.TimeUnit

class PerspectivePyStores(
    executionManager: ExecutionManager,
    checkExpiredTaskIntervalMs: Long = 1000,
) : AutoCloseable {

    private val logger = this.getLoggerEx()
    private val lock = Any()
    private val valueResolver = PyStoreValueResolver()

    private val scopes = IdentityHashMap<AbstractLifecycle, MutableMap<String, PyStore>>()
    private val subscriptionsByStore = IdentityHashMap<PyStore, MutableSet<Subscription>>()

    private var closed = false

    private val checkExpiredTask =
        executionManager.scheduleWithFixedDelay(
            ::checkExpired,
            checkExpiredTaskIntervalMs,
            checkExpiredTaskIntervalMs,
            TimeUnit.MILLISECONDS,
        )

    fun getCurrent(
        name: String,
        scope: String = "page",
    ): PyStore {
        val context = ThreadContext.get()
        val page =
            requireNotNull(context.page.get()) {
                "A $scope-scoped PyStore requires a current Perspective page"
            }

        return get(page, name, scope, context.view.get())
    }

    fun get(
        page: PageModel,
        name: String,
        scope: String = "page",
        view: AbstractLifecycle? = null,
    ): PyStore = get(lifecycleFor(page, scope, view), name)

    fun get(
        lifecycle: AbstractLifecycle,
        name: String,
    ): PyStore {
        require(name.isNotBlank()) {
            "PyStore name cannot be blank"
        }

        check(lifecycle.isRunning) {
            "Cannot access PyStore '$name': Perspective scope is not running"
        }

        val store = access {
            scopes.getOrPut(lifecycle) { LinkedHashMap() }.getOrPut(name) { createStore(name) }
        }

        if (!lifecycle.isRunning) {
            release(lifecycle)
            error("Cannot access PyStore '$name': Perspective scope has ended")
        }

        return store
    }

    fun release(lifecycle: AbstractLifecycle) {
        val stores = synchronized(lock) { detachScope(lifecycle) }
        stores.forEach(PyStore::invalidate)
    }

    override fun close() {
        val stores =
            synchronized(lock) {
                if (closed) return

                closed = true

                scopes.keys.toList().flatMap(::detachScope).also {
                    subscriptionsByStore.clear()
                }
            }

        checkExpiredTask.cancel(false)
        stores.forEach(PyStore::invalidate)
    }

    internal fun subscribe(
        owner: Any,
        store: PyStore,
        path: PyStorePath,
        listener: InteractionListener,
    ): AutoCloseable {
        val subscription =
            Subscription(
                owner,
                store,
                path,
                listener,
            )

        access { index(subscription) }
        return subscription
    }

    private fun createStore(name: String): PyStore =
        PyStore(name, valueResolver).also { store ->
            store.subscribe { changed(store, it) }
        }

    private fun lifecycleFor(
        page: PageModel,
        scope: String,
        view: AbstractLifecycle?,
    ): AbstractLifecycle =
        when (parseScope(scope)) {
            Scope.PAGE -> page
            Scope.SESSION -> page.session as AbstractLifecycle
            Scope.VIEW ->
                requireNotNull(view) { "A view-scoped PyStore requires a current Perspective view" }
        }

    private fun checkExpired() {
        val expired =
            synchronized(lock) {
                if (closed) return

                scopes.keys.filterNot { it.isRunning }.flatMap(::detachScope)
            }

        expired.forEach(PyStore::invalidate)
    }

    private fun detachScope(lifecycle: AbstractLifecycle): List<PyStore> {
        val stores = scopes.remove(lifecycle)?.values?.toList() ?: return emptyList()
        stores.forEach(::removeStoreSubscriptions)
        return stores
    }

    private fun changed(store: PyStore, changeset: PyStoreChangeset) {
        val listeners = synchronized(lock) { collectListeners(store, changeset) }

        listeners.forEach {
            try {
                it.childInteractionUpdated()
            } catch (exception: Exception) {
                logger.warn(
                    "Error updating PyStore listener",
                    exception,
                )
            }
        }
    }

    private fun collectListeners(
        store: PyStore,
        changeset: PyStoreChangeset,
    ): List<InteractionListener> {
        val subscriptions = subscriptionsByStore[store] ?: return emptyList()
        val listeners = IdentityHashMap<InteractionListener, Unit>()
        val iterator = subscriptions.iterator()

        while (iterator.hasNext()) {
            val subscription = iterator.next()
            val listener = subscription.resolveListener()

            if (listener == null) {
                iterator.remove()
                continue
            }

            if (changeset.affects(subscription.path)) {
                listeners[listener] = Unit
            }
        }

        if (subscriptions.isEmpty()) removeStoreSubscriptions(store)

        return listeners.keys.toList()
    }

    private fun index(subscription: Subscription) {
        subscriptionsByStore.getOrPut(subscription.store) { LinkedHashSet() }.add(subscription)
    }

    private fun unindex(subscription: Subscription) {
        val subscriptions = subscriptionsByStore[subscription.store] ?: return

        subscriptions.remove(subscription)

        if (subscriptions.isEmpty()) removeStoreSubscriptions(subscription.store)
    }

    private fun removeStoreSubscriptions(store: PyStore) {
        subscriptionsByStore.remove(store)
    }

    fun entries(): List<Entry> {
        val context = ThreadContext.get()

        val page =
            requireNotNull(context.page.get()) {
                "Current PyStore entries require a current Perspective page"
            }

        return entriesFor(page, context.view.get())
    }

    fun entries(page: PageModel): List<Entry> = entriesFor(page)

    fun entries(session: InternalSession): List<Entry> = access {
        entriesFor(session as AbstractLifecycle, Scope.SESSION)
    }

    private fun entriesFor(page: PageModel, view: AbstractLifecycle? = null): List<Entry> = access {
        buildList {
            view?.let {
                addAll(entriesFor(it, Scope.VIEW))
            }
            addAll(entriesFor(page, Scope.PAGE))
            addAll(entriesFor(page.session as AbstractLifecycle, Scope.SESSION))
        }
    }

    private fun entriesFor(lifecycle: AbstractLifecycle, scope: Scope): List<Entry> =
        scopes[lifecycle]
            ?.keys
            ?.sorted()
            ?.map {
                Entry(name = it, scope = scope.value)
            }
            .orEmpty()

    private inline fun <T> access(block: () -> T): T =
        synchronized(lock) {
            checkOpen()
            block()
        }

    private fun checkOpen() {
        check(!closed) {
            "PerspectivePyStores has been closed"
        }
    }

    private fun parseScope(scope: String): Scope =
        Scope.entries.firstOrNull {
            it.value == scope.lowercase(Locale.ROOT)
        }
            ?: throw IllegalArgumentException(
                "Invalid PyStore scope '$scope'. Expected 'view', 'page', or 'session'."
            )

    private inner class Subscription(
        owner: Any,
        val store: PyStore,
        val path: PyStorePath,
        listener: InteractionListener,
    ) : AutoCloseable {

        private val owner = WeakReference(owner)
        private val listener = WeakReference(listener)

        fun resolveListener(): InteractionListener? {
            owner.get() ?: return null
            return listener.get()
        }

        override fun close() {
            synchronized(lock) { unindex(this) }
        }
    }

    data class Entry(
        val name: String,
        val scope: String,
    )

    private enum class Scope(val value: String) {
        VIEW("view"),
        PAGE("page"),
        SESSION("session"),
    }
}
