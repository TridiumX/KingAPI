package org.nobledev.kingapi.configuration

import org.bukkit.Material
import kotlin.reflect.KProperty

class Attribute <T : Any> (
    val name : String,
    val description : String,
    val icon : Material,
    val type : Class<T>,
    default : T
) {

    private var _value = default

    val currentValue : T
        get() {
            return _value
        }

    private var _hasBeenLoaded = false

    fun loadValue(value : Any?) {
        _value = value as? T ?: _value.also {
            println("$this was unable to cast $value and will use default value instead!")
        }

        _hasBeenLoaded = true
    }

    fun forceSetLoad() {
        _hasBeenLoaded = true
    }

    operator fun getValue(
        ref : Any?,
        property : KProperty<*>
    ) : T {

        if(_hasBeenLoaded) {
            println("$ref has called to retrieve attribute[$name, $description] but it has not been loaded yet, will provide default value!")
        }
        return _value

    }

    operator fun setValue(
        ref : Any?,
        property: KProperty<*>,
        value : T
    ) {
        _value = value
    }
}