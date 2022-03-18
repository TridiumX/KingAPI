package org.nobledev.kingapi.configuration

import org.bukkit.Material
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.hocon.HoconConfigurationLoader
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.io.path.exists

interface Configurable {


    val headerKey: String

    val attributes: MutableList<Attribute<*>>

    private fun getConfLoader(path : Path) : HoconConfigurationLoader {
        return HoconConfigurationLoader
            .builder()
            .prettyPrinting(true)
            .path(path)
            .build()
    }

    fun loadConfiguration(path : Path) {
        if (path.exists().not()) {
            loadDefaults()
            createConfigurationFromDefaults(path)
            return
        }

        val conf = getConfLoader(path).load()

        attributes.forEach {
            it.loadValue(conf.node(headerKey, it.name).get(it.type))
        }

    }

    private fun loadDefaults() {
        attributes.forEach { it.forceSetLoad() }
    }

    fun getNode(confPath : Path, vararg paths : String) : ConfigurationNode {
        return getConfLoader(confPath).load().node(*paths)
    }


    fun saveConfig(path : Path) {
        val loader = getConfLoader(path)
        val conf = loader.load()
        attributes.forEach {
            conf.node(headerKey, it.name).set(it.currentValue).comment(it.description)
        }

        loader.save(conf)
    }

    private fun createConfigurationFromDefaults(path: Path) {
        path.createFile()
        saveConfig(path)
    }
}

inline fun <reified T : Any> Configurable.attribute(
    name : String,
    description : String,
    icon : Material,
    default : T
) : Attribute<T> {
    val attrib = Attribute(name, description, icon, T::class.java, default)
    attributes.add(attrib)
    return attrib
}