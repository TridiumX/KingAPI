package org.nobledev.kingapi.architecture.plugin

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.bukkit.plugin.java.JavaPlugin
import org.nobledev.kingapi.architecture.feature.Feature
import org.nobledev.kingapi.architecture.monitor.*
import org.nobledev.kingapi.configuration.Attribute
import org.nobledev.kingapi.configuration.Configurable
import org.nobledev.kingapi.events.EventAdapter
import org.nobledev.kingapi.events.EventsReciever
import org.nobledev.kingapi.events.GlobalBus
import org.nobledev.kingapi.events.Listenable
import org.spongepowered.configurate.hocon.HoconConfigurationLoader
import java.io.File

abstract class KingPlugin : Configurable, Monitorable, Listenable, JavaPlugin() {

    override val headerKey: String = this.description.name

    override val attributes: MutableList<Attribute<*>> = mutableListOf()

    open val features : List<Feature> = emptyList()

    override val monitorChannel: MutableStateFlow<MonitorableEvent> = MutableStateFlow(NillEvent)

    override val events: EventsReciever = EventsReciever()

    override fun onLoad() {

        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() = runBlocking {
                postEvent(ShutdownEvent)
            }
        })

        attemptAdaptEvents()
        loadConfiguration(dataFolder.resolve("config.conf").toPath())
        postEvent(PluginLoadEvent)


    }

    override fun onEnable() {

        val featureNode = getNode(dataFolder.resolve("features.conf").toPath())
        features.forEach {
            val thisNode = featureNode.node(it.featureName)
            if(thisNode.empty()) {
                featureNode.node(it.featureName).set(true)
            }
            if(thisNode.getBoolean(true)) {
                it.onInstall(this)
            }
        }

        postEvent(PluginEnableEvent)
    }

    override fun onDisable() {
        postEvent(PluginDisableEvent)
    }

    private fun checkFiles() {
        val confPath = dataFolder.resolve("config.conf")

        if(confPath.exists().not()) {
            confPath.mkdirs()
        }

        val featurePath = dataFolder.resolve("features.conf")

        if(featurePath.exists().not()) {
            featurePath.mkdirs()
            createFeatureConfig(featurePath)
        }
    }

    private fun createFeatureConfig(file : File) {
        val loader = HoconConfigurationLoader.builder().prettyPrinting(true).path(file.toPath()).build()

        val conf = loader.createNode()

        features.forEach {
            conf.node(it.featureName).set(true).comment(it.description)
        }
        loader.save(conf)
    }

    private fun attemptAdaptEvents() {
        if(GlobalBus.isAdapted.not()) {
            server.pluginManager.registerEvents(EventAdapter(), this)
            GlobalBus.isAdapted = true
            this.monitor {
                if(it is ShutdownEvent || it is PluginDisableEvent) {
                    GlobalBus.isAdapted = false
                }
            }
        }
    }
}