package uk.ewancroft.dayannouncer

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.plugin.java.JavaPlugin
import uk.ewancroft.dayannouncer.config.PluginConfig
import uk.ewancroft.dayannouncer.listener.TimeSkipListener
import uk.ewancroft.dayannouncer.message.MessageFormatter
import uk.ewancroft.dayannouncer.task.DayCheckTask

/**
 * Broadcasts a message at dawn each in-game day.
 *
 * Uses a repeating task to poll world time and a [TimeSkipListener]
 * to catch night skips from beds. Messages support MiniMessage formatting
 * with {day} and {time} placeholders.
 */
class DayAnnouncer : JavaPlugin() {

    private lateinit var pluginConfig: PluginConfig
    private lateinit var formatter: MessageFormatter
    private var checkTask: DayCheckTask? = null
    private var timeSkipListener: TimeSkipListener? = null
    private var lastAnnouncedDay = -1L

    override fun onEnable() {
        saveDefaultConfig()
        loadConfigValues()

        val worldSupplier: () -> World? = {
            if (pluginConfig.worldName.isNotEmpty()) {
                Bukkit.getWorld(pluginConfig.worldName)
            } else {
                Bukkit.getWorlds().firstOrNull()
            }
        }

        val announce: (Component) -> Unit = announce@{ component ->
            val world = worldSupplier() ?: return@announce
            val currentDay = world.fullTime / 24000
            if (currentDay == lastAnnouncedDay) return@announce
            lastAnnouncedDay = currentDay
            Bukkit.broadcast(component)
            logger.info("Day announcement: ${formatter.formatPlain(world)}")
        }

        formatter = MessageFormatter(pluginConfig.message)

        checkTask = DayCheckTask(
            worldSupplier = worldSupplier,
            dawnThreshold = pluginConfig.dawnThreshold,
            formatter = formatter,
            announce = announce,
        ).also { it.runTaskTimer(this, 0L, pluginConfig.checkInterval) }

        timeSkipListener = TimeSkipListener(
            worldSupplier = worldSupplier,
            dawnThreshold = pluginConfig.dawnThreshold,
            formatter = formatter,
            announce = announce,
        ).also { server.pluginManager.registerEvents(it, this) }

        logger.info("DayAnnouncer enabled. Monitoring world: ${pluginConfig.worldName.ifEmpty { "(first loaded)" }}")
    }

    override fun onDisable() {
        checkTask?.cancel()
        checkTask?.reset()
        checkTask = null
        timeSkipListener = null
        logger.info("DayAnnouncer disabled.")
    }

    private fun loadConfigValues() {
        pluginConfig = PluginConfig(config)
    }
}
