package uk.ewancroft.dayannouncer.listener

import net.kyori.adventure.text.Component
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.TimeSkipEvent
import uk.ewancroft.dayannouncer.message.MessageFormatter

/**
 * Listens for [TimeSkipEvent] — fires when players skip the night
 * via beds or when time is changed via commands.
 *
 * If the skip lands in the dawn window, immediately announces the new day
 * rather than waiting for the next polling cycle.
 */
class TimeSkipListener(
    private val worldSupplier: () -> World?,
    private val formatter: MessageFormatter,
    private val announce: (Component) -> Unit,
) : Listener {

    @EventHandler
    fun onTimeSkip(event: TimeSkipEvent) {
        val world = worldSupplier() ?: return
        if (event.world != world) return

        val newTime = world.time
        // Only announce if the skip brought us into the dawn window
        if (newTime < 20L) {
            announce(formatter.format(world))
        }
    }
}
