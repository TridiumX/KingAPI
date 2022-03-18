package org.nobledev.kingapi.architecture.monitor

sealed interface MonitorableEvent

object NillEvent : MonitorableEvent

object PluginLoadEvent : MonitorableEvent
object PluginEnableEvent : MonitorableEvent
object PluginDisableEvent : MonitorableEvent
object PluginReloadEvent : MonitorableEvent
object FeatureInstallEvent : MonitorableEvent

object ShutdownEvent : MonitorableEvent
class MessageEvent(
    val level : MessageLevel,
    val message : String
) : MonitorableEvent {
    enum class MessageLevel {
        DEBUG,
        INFO,
        WARN,
        ALERT,
        ERROR
    }
}
