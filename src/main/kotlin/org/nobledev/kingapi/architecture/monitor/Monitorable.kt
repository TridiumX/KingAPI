package org.nobledev.kingapi.architecture.monitor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

interface Monitorable {

    val monitorChannel : MutableStateFlow<MonitorableEvent>

    fun postEvent(event: MonitorableEvent) {
        monitorChannel.tryEmit(event)
    }

    fun monitor(callback : suspend (MonitorableEvent) -> Unit) {
        CoroutineScope(Job()).launch {
            monitorChannel.filterNotNull().collect {
                callback.invoke(it)
            }
        }
    }

    fun sendMessage(level : MessageEvent.MessageLevel, message : String) {
        postEvent(MessageEvent(level, message))
    }

}