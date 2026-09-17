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
import java.util.WeakHashMap
import java.util.concurrent.TimeUnit

class PerspectivePyStores(
    executionManager: ExecutionManager,
    checkExpiredTaskIntervalMs: Long = 1000,
) : AutoCloseable {

    private val logger = this.getLoggerEx()
    private val lock = Any()
    private val valueResolver = PyStoreValueResolver()

    private val scopes = IdentityHashMap<AbstractLifecycle, MutableMap<String, PyStore>>()
    private val bindingsByToken = WeakHashMap<Any, Binding>()
    private val bindingsByStore = IdentityHashMap<PyStore, MutableSet<Binding>>()

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
        val stores =
            synchronized(lock) {
                detachScope(lifecycle)
            }

        stores.forEach(PyStore::invalidate)
    }

    override fun close() {
        val stores =
            synchronized(lock) {
                if (closed) {
                    return
                }

                closed = true

                scopes.keys.toList().flatMap(::detachScope).also {
                    bindingsByToken.clear()
                    bindingsByStore.clear()
                }
            }

        checkExpiredTask.cancel(false)
        stores.forEach(PyStore::invalidate)
    }

    internal fun bind(
        token: Any,
        store: PyStore,
        path: PyStorePath,
        listener: InteractionListener,
    ) {
        access {
            val binding =
                Binding(
                    WeakReference(token),
                    store,
                    path,
                    WeakReference(listener),
                )

            bindingsByToken.put(token, binding)?.let(::unindex)

            index(binding)
        }
    }

    internal fun unbind(token: Any) {
        synchronized(lock) {
            bindingsByToken.remove(token)?.let(::unindex)
        }
    }

    private fun createStore(name: String): PyStore =
        PyStore(name, valueResolver).also { store ->
            store.subscribe {
                changed(store, it)
            }
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
                requireNotNull(view) {
                    "A view-scoped PyStore requires a current Perspective view"
                }
        }

    private fun checkExpired() {
        val expired =
            synchronized(lock) {
                if (closed) {
                    return
                }

                scopes.keys.filterNot { it.isRunning }.flatMap(::detachScope)
            }

        expired.forEach(PyStore::invalidate)
    }

    private fun detachScope(lifecycle: AbstractLifecycle): List<PyStore> {
        val stores = scopes.remove(lifecycle)?.values?.toList() ?: return emptyList()

        stores.forEach(::removeStoreBindings)

        return stores
    }

    private fun changed(
        store: PyStore,
        path: PyStorePath,
    ) {
        val listeners =
            synchronized(lock) {
                collectListeners(store, path)
            }

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
        changedPath: PyStorePath,
    ): List<InteractionListener> {
        val bindings = bindingsByStore[store] ?: return emptyList()

        val listeners = IdentityHashMap<InteractionListener, Unit>()

        val iterator = bindings.iterator()

        while (iterator.hasNext()) {
            val binding = iterator.next()
            val token = binding.token.get()
            val listener = binding.listener.get()

            if (token == null || listener == null) {
                iterator.remove()

                if (token != null) {
                    bindingsByToken.remove(token, binding)
                }

                continue
            }

            if (binding.path.isRelatedTo(changedPath)) {
                listeners[listener] = Unit
            }
        }

        if (bindings.isEmpty()) {
            bindingsByStore.remove(store)
        }

        return listeners.keys.toList()
    }

    private fun index(binding: Binding) {
        bindingsByStore.getOrPut(binding.store) { LinkedHashSet() }.add(binding)
    }

    private fun unindex(binding: Binding) {
        val bindings = bindingsByStore[binding.store] ?: return

        bindings.remove(binding)

        if (bindings.isEmpty()) {
            bindingsByStore.remove(binding.store)
        }
    }

    private fun removeStoreBindings(store: PyStore) {
        val bindings = bindingsByStore.remove(store) ?: return

        bindings.forEach { binding ->
            binding.token.get()?.let {
                bindingsByToken.remove(it, binding)
            }
        }
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

    private class Binding(
        val token: WeakReference<Any>,
        val store: PyStore,
        val path: PyStorePath,
        val listener: WeakReference<InteractionListener>,
    )

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
