package org.nobledev.kingapi.events

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

open class EventBus {
    private val flows = mutableMapOf<Class<*>, MutableStateFlow<*>>()

    internal fun <T : Any> forEvent(clazz: Class<T>) : MutableStateFlow<T?> {
        return flows.getOrPut(clazz) {MutableStateFlow<T?>(null) } as MutableStateFlow<T?>
    }

    fun <T : Any> getFlow(clazz : Class<T>) : Flow<T> {
        return forEvent(clazz).asStateFlow().filterNotNull()
    }

    fun <T : Any> post(event : T, retain : Boolean = true) {
        val flow = forEvent(event.javaClass)
        flow.tryEmit(event).also {
            if (!it) throw IllegalStateException("Stateflow was unable to post event, this should never happen!")
        }

        if(!retain) {
            CoroutineScope(Job() + Dispatchers.Unconfined).launch {
                dropEvent(event.javaClass)
            }
        }
    }

    fun <T : Any> getLastEvent(clazz : Class<T>) :T? {
        return flows.getOrElse(clazz) {null}?.value as T?
    }

    fun <T> dropEvent(clazz : Class<T>) {
        if(!flows.contains(clazz)) return

        val channel = flows[clazz] as MutableStateFlow<T?>
        channel.tryEmit(null)
    }

    fun dropAll() {
        flows.values.forEach {
            (it as MutableStateFlow<Any?>).tryEmit(null)
        }
    }
}

inline fun <reified T : Any> EventBus.dropEvent() = dropEvent(T::class.java)

inline fun <reified T : Any> EventBus.getLastEvent() : T? = getLastEvent(T::class.java)

object GlobalBus : EventBus() {
    var isAdapted : Boolean = false
}