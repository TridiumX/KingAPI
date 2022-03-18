package org.nobledev.kingapi.events

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import org.nobledev.kingapi.architecture.monitor.Monitorable
import org.nobledev.kingapi.architecture.monitor.ShutdownEvent
import org.nobledev.kingapi.architecture.plugin.KingPlugin


open class EventsReciever(
    private val bus : EventBus = GlobalBus
) {

    private val jobs = mutableMapOf<Class<*>, MutableList<Job>>()

    private var returnDispatcher: CoroutineDispatcher = DispatcherProvider.get()

    fun returnOn(dispatcher: CoroutineDispatcher) : EventsReciever {
        returnDispatcher = dispatcher
        return this
    }

    fun <T : Any> subscribe(
        clazz: Class<T>,
        skipRetained : Boolean = false,
        callback : suspend (event : T) -> Unit
    ) : EventsReciever {

        val exceptionHandler = CoroutineExceptionHandler {_, throwable ->
            throw throwable
        }

        val job = CoroutineScope(Job() + Dispatchers.Default + exceptionHandler).launch {
            bus.forEvent(clazz)
                .drop(if (skipRetained) 1 else 0)
                .filterNotNull()
                .collect { withContext(returnDispatcher) {callback.invoke(it)} }
        }

        jobs.getOrPut(clazz) { mutableListOf() }.add(job)

        return this
    }

    fun <T : Any> unsubscribe(clazz : Class<T>) {
        jobs.remove(clazz)?.forEach {
            it.cancel()
        }
    }

    fun unsubscribe() {
        jobs.values.forEach {
            it.forEach { job ->
                job.cancel()
            }
        }
        jobs.clear()
    }
}


inline fun <reified T : Any> EventsReciever.subscribe(skipRetained: Boolean = false, noinline callback : suspend (event : T) -> Unit) : EventsReciever {
    return subscribe(T::class.java, skipRetained, callback)
}

fun EventsReciever.bindLifecycle(owner : Monitorable) : EventsReciever {
    owner.monitor {
        if(it is ShutdownEvent) {
            unsubscribe()
        }
    }

    return this
}