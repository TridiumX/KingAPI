package org.nobledev.kingapi.architecture.feature

import kotlinx.coroutines.flow.MutableStateFlow
import org.bukkit.Material
import org.nobledev.kingapi.architecture.monitor.*
import org.nobledev.kingapi.architecture.plugin.KingPlugin
import org.nobledev.kingapi.configuration.Attribute
import org.nobledev.kingapi.configuration.Configurable
import org.nobledev.kingapi.events.EventsReciever
import org.nobledev.kingapi.events.Listenable
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class Feature(
    val featureName : String,
    val description : String,
    val icon : Material
) : Configurable, Monitorable, Listenable {

    override val headerKey: String = featureName

    override val attributes: MutableList<Attribute<*>> = mutableListOf()

    override val monitorChannel: MutableStateFlow<MonitorableEvent> = MutableStateFlow(NillEvent)

    open val logMessages : Boolean = true
    open val logLevel : MessageEvent.MessageLevel = MessageEvent.MessageLevel.ALERT

    private val logger : Logger by lazy {
        LoggerFactory.getLogger("Feature[$featureName]")
    }

    override val events: EventsReciever = EventsReciever()

    fun onInstall(plugin : KingPlugin) {
        loadConfiguration(plugin.dataFolder.resolve("config.conf").toPath())
        plugin.monitor {
            if (it is PluginDisableEvent || it is ShutdownEvent) {
                events.unsubscribe()
            }
        }
        setup(plugin)
        postEvent(FeatureInstallEvent)
    }

    override fun sendMessage(level: MessageEvent.MessageLevel, message: String) {
        if(logMessages) {
            if(level.ordinal < logLevel.ordinal) {
                when(level) {
                    MessageEvent.MessageLevel.DEBUG -> logger.debug(message)
                    MessageEvent.MessageLevel.INFO -> logger.info(message)
                    MessageEvent.MessageLevel.WARN -> logger.warn(message)
                    MessageEvent.MessageLevel.ALERT -> logger.warn(message)
                    MessageEvent.MessageLevel.ERROR -> logger.error(message)
                }
            }
        }
        super.sendMessage(level, message)
    }

    abstract fun setup(plugin : KingPlugin)
}