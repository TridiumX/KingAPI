package org.nobledev.kingapi.events

import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class EventAdapter : Listener {

    @EventHandler
    fun onEvent(event : Event) {
        GlobalBus.post(event)
    }
}