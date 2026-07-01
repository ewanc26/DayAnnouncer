package uk.ewancroft.dayannouncer.listener

import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.TimeSkipEvent
import uk.ewancroft.dayannouncer.config.WorldConfig
import uk.ewancroft.dayannouncer.message.AnnounceDispatcher
import uk.ewancroft.dayannouncer.message.MessageFormatter

class TimeSkipListener(
    private val perWorldState: Map<String, WorldState>,
    private val dispatcher: AnnounceDispatcher,
) : Listener {

    data class WorldState(
        val worldSupplier: () -> World?,
        val config: WorldConfig,
    )

    @EventHandler
    fun onTimeSkip(event: TimeSkipEvent) {
        val world = event.world
        val state = perWorldState[world.name] ?: return
        val actualWorld = state.worldSupplier() ?: return
        if (actualWorld !== world) return

        if (world.time < state.config.dawnThreshold) {
            val formatter = MessageFormatter(state.config.randomMessage())
            dispatcher.announce(formatter.format(world), world, state.config.output, state.config.sound)
        }
    }
}
