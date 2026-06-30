package uk.ewancroft.dayannouncer.task

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.scheduler.BukkitRunnable
import uk.ewancroft.dayannouncer.message.MessageFormatter

/**
 * Repeating task that monitors world time and fires an announcement
 * when a new day begins (world time wraps past 23999 back to 0).
 *
 * The [dawnThreshold] defines the tick window after midnight (time 0)
 * during which the announcement can trigger. This should be at least
 * as large as the task's run period to guarantee the window is hit.
 *
 * An [announced] flag prevents duplicate announcements within the same
 * day cycle.
 */
class DayCheckTask(
    private val worldSupplier: () -> World?,
    private val dawnThreshold: Long,
    private val formatter: MessageFormatter,
    private val announce: (Component) -> Unit,
) : BukkitRunnable() {

    private var announced = false
    private var lastDay = -1L

    override fun run() {
        val world = worldSupplier() ?: return
        val timeOfDay = world.time
        val currentDay = world.fullTime / 24000

        // Detect day rollover via fullTime — more reliable than time window alone.
        // If the day counter advanced, fire regardless of the time window.
        if (lastDay >= 0 && currentDay > lastDay) {
            announced = false
        }
        lastDay = currentDay

        if (timeOfDay < dawnThreshold) {
            if (!announced) {
                announced = true
                val component = formatter.format(world)
                announce(component)
            }
        } else {
            announced = false
        }
    }

    /**
     * Resets state. Called when the plugin is disabled or the task is cancelled.
     */
    fun reset() {
        announced = false
        lastDay = -1L
    }
}
