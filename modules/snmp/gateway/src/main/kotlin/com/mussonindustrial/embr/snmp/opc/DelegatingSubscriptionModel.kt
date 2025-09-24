package com.mussonindustrial.embr.snmp.opc

import java.util.concurrent.*
import java.util.function.Function
import java.util.stream.Collectors
import org.eclipse.milo.opcua.sdk.core.util.GroupMapCollate
import org.eclipse.milo.opcua.sdk.server.AbstractLifecycle
import org.eclipse.milo.opcua.sdk.server.AddressSpace
import org.eclipse.milo.opcua.sdk.server.OpcUaServer
import org.eclipse.milo.opcua.sdk.server.Session
import org.eclipse.milo.opcua.sdk.server.items.DataItem
import org.eclipse.milo.opcua.sdk.server.items.MonitoredItem
import org.eclipse.milo.opcua.sdk.server.util.PendingRead
import org.eclipse.milo.opcua.stack.core.AttributeId
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId
import org.eclipse.milo.opcua.stack.core.util.ExecutionQueue

class DelegatingSubscriptionModel(
    private val server: OpcUaServer,
    private val scheduledUpdate: ScheduledUpdateContext.() -> List<DataValue>,
) : AbstractLifecycle() {

    private val itemSet = ConcurrentHashMap.newKeySet<DataItem>()
    private val schedule = CopyOnWriteArrayList<ScheduledFuture<*>>()

    private val executor = server.executorService
    private val scheduler = server.scheduledExecutorService
    private val executionQueue = ExecutionQueue(executor)

    override fun onStartup() {}

    override fun onShutdown() {
        executionQueue.submit {
            schedule.forEach { it.cancel(true) }
            schedule.clear()
            itemSet.clear()
        }
    }

    fun onDataItemsCreated(items: List<DataItem>) {
        require(!isNotRunning) { "not running" }

        executionQueue.submit {
            itemSet.addAll(items)
            reschedule()
        }
    }

    fun onDataItemsModified(items: List<DataItem>) {
        require(!isNotRunning) { "not running" }

        executionQueue.submit { this.reschedule() }
    }

    fun onDataItemsDeleted(items: List<DataItem>) {
        require(!isNotRunning) { "not running" }

        executionQueue.submit {
            items.forEach { itemSet.remove(it) }
            reschedule()
        }
    }

    fun onMonitoringModeChanged(items: List<MonitoredItem>) {
        require(!isNotRunning) { "not running" }

        executionQueue.submit { this.reschedule() }
    }

    val dataItems: MutableList<DataItem>
        get() = itemSet.toMutableList()

    inner class ScheduledUpdateContext(
        val session: Session,
        val samplingInterval: Double,
        val maxAge: Double,
        val timestamps: TimestampsToReturn,
        val readValueIds: List<ReadValueId>,
    ) {
        val server = this@DelegatingSubscriptionModel.server
        val context = AddressSpace.ReadContext(server, session)
    }

    inner class ScheduledUpdater(val samplingInterval: Double, val items: List<DataItem>) :
        Runnable {
        override fun run() {
            val values =
                GroupMapCollate.groupMapCollate(
                    items,
                    { it.session },
                    { session ->
                        Function { sessionItems ->
                            val pending =
                                sessionItems.stream().map { PendingRead(it.readValueId) }.toList()
                            val ids = pending.stream().map { it.input }.collect(Collectors.toList())
                            val context =
                                ScheduledUpdateContext(
                                    session,
                                    samplingInterval,
                                    0.0,
                                    TimestampsToReturn.Both,
                                    ids,
                                )
                            scheduledUpdate(context)
                        }
                    },
                )

            val ii = items.iterator()
            val vi = values.iterator()
            while (ii.hasNext() && vi.hasNext()) {
                val item = ii.next()
                var value = vi.next()

                val timestamps = item.timestampsToReturn

                if (timestamps != null) {
                    val attributeId = item.readValueId.attributeId

                    value =
                        if (AttributeId.Value.isEqual(attributeId))
                            DataValue.derivedValue(value, timestamps)
                        else DataValue.derivedNonValue(value, timestamps)
                }

                item.setValue(value)
            }
        }
    }

    private fun reschedule() {
        val bySamplingInterval =
            itemSet
                .stream()
                .filter { it.isSamplingEnabled }
                .collect(Collectors.groupingBy { it.samplingInterval })

        val updates =
            bySamplingInterval.entries
                .stream()
                .map { (samplingInterval, items) ->
                    val task = ScheduledUpdater(samplingInterval, items)
                    scheduler.scheduleAtFixedRate(
                        task,
                        0L,
                        samplingInterval.toLong(),
                        TimeUnit.MILLISECONDS,
                    )
                }
                .toList()

        schedule.forEach { it.cancel(false) }
        schedule.clear()
        schedule.addAll(updates)
    }
}
