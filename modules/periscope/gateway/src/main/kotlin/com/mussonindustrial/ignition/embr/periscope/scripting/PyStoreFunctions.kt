package com.mussonindustrial.ignition.embr.periscope.scripting

import com.inductiveautomation.ignition.common.script.builtin.KeywordArgs
import com.inductiveautomation.ignition.common.script.hints.ScriptFunction
import com.inductiveautomation.perspective.gateway.api.PerspectiveContext
import com.mussonindustrial.embr.common.scripting.PyArgOverloadBuilder
import com.mussonindustrial.embr.perspective.gateway.model.PerspectiveScriptingFunctions
import com.mussonindustrial.ignition.embr.periscope.Meta
import com.mussonindustrial.ignition.embr.periscope.PeriscopeGatewayContext
import com.mussonindustrial.ignition.embr.periscope.pystore.PyStore
import com.mussonindustrial.ignition.embr.periscope.utils.toPyValue
import kotlin.reflect.typeOf
import org.python.core.PyObject

class PyStoreFunctions(private val context: PeriscopeGatewayContext) :
    PerspectiveScriptingFunctions() {

    private val overloads = ScriptOverloads()

    override fun getContext(): PerspectiveContext {
        return context.perspectiveContext
    }

    private fun pyStore(
        name: String,
        scope: String,
        sessionId: String?,
        pageId: String?,
    ): PyStore {

        if (scope.equals("view", ignoreCase = true)) {
            require(sessionId == null && pageId == null) {
                "View-scoped StateStores cannot be targeted using sessionId or pageId"
            }

            return context.pyStores.getCurrent(name, scope)
        }

        return onPage(pageId, sessionId) { page ->
            context.pyStores.get(page, name, scope)
        }
    }

    private fun getPyStores(
        sessionId: String?,
        pageId: String?,
    ): PyObject {

        val result =
            when {
                sessionId == null && pageId == null -> context.pyStores.entries()
                pageId != null -> onPage(pageId, sessionId, context.pyStores::entries)
                else -> onSession(sessionId) { context.pyStores.entries(it) }
            }

        return result.map { mapOf("name" to it.name, "scope" to it.scope) }.toPyValue()
    }

    inner class ScriptOverloads {
        val pyStore =
            PyArgOverloadBuilder<PyStore>()
                .setName("pyStore")
                .addOverload(
                    {
                        val name = it["name"] as String
                        val scope = it["scope"] as? String ?: "page"
                        val sessionId = it["sessionId"] as? String
                        val pageId = it["pageId"] as? String
                        pyStore(name, scope, sessionId, pageId)
                    },
                    "name" to typeOf<String>(),
                    "scope" to typeOf<String?>(),
                    "sessionId" to typeOf<String?>(),
                    "pageId" to typeOf<String?>(),
                )
                .build()

        val getPyStores =
            PyArgOverloadBuilder<PyObject>()
                .setName("getPyStores")
                .addOverload(
                    {
                        val sessionId = it["sessionId"] as? String
                        val pageId = it["pageId"] as? String
                        getPyStores(sessionId, pageId)
                    },
                    "sessionId" to typeOf<String?>(),
                    "pageId" to typeOf<String?>(),
                )
                .build()
    }

    @ScriptFunction(docBundlePrefix = "${Meta.BUNDLE_PREFIX}.script")
    @KeywordArgs(
        names = ["name", "scope", "sessionId", "pageId"],
        types = [String::class, String::class, String::class, String::class],
    )
    @Suppress("unused")
    fun pyStore(args: Array<PyObject>, keywords: Array<String>) =
        overloads.pyStore.call(args, keywords)

    @ScriptFunction(docBundlePrefix = "${Meta.BUNDLE_PREFIX}.script")
    @KeywordArgs(
        names = ["sessionId", "pageId"],
        types = [String::class, String::class],
    )
    @Suppress("unused")
    fun getPyStores(args: Array<PyObject>, keywords: Array<String>) =
        overloads.getPyStores.call(args, keywords)
}
