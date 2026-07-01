package uk.ewancroft.dayannouncer.task

import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.scheduler.BukkitRunnable
import uk.ewancroft.dayannouncer.config.WorldConfig
import uk.ewancroft.dayannouncer.message.AnnounceDispatcher
import uk.ewancroft.dayannouncer.message.MessageFormatter

class DayCheckTask(
    private val config: WorldConfig,
    private val worldSupplier: () -> World?,
    private val dispatcher: AnnounceDispatcher,
) : BukkitRunnable() {

    private val formatter = MessageFormatter(config.message)
    private var announced = false
    private var lastDay = -1L

    override fun run() {
        val world = worldSupplier() ?: return
        val timeOfDay = world.time
        val currentDay = world.fullTime / 24000

        if (lastDay >= 0 && currentDay > lastDay) {
            announced = false
        }
        lastDay = currentDay

        if (timeOfDay < config.dawnThreshold) {
            if (!announced) {
                announced = true
                dispatcher.announce(formatter.format(world), world)
            }
        } else {
            announced = false
        }
    }

    fun reset() {
        announced = false
        lastDay = -1L
    }
}
