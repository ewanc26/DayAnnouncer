package uk.ewancroft.dayannouncer.config

import org.bukkit.configuration.file.FileConfiguration

/**
 * Typed wrapper around the plugin's [FileConfiguration].
 *
 * Reads all values once at load time and exposes them as immutable properties.
 */
class PluginConfig(private val config: FileConfiguration) {

    val message: String
    val worldName: String
    val checkInterval: Long
    val dawnThreshold: Long

    init {
        message = config.getString("message", DEFAULT_MESSAGE) ?: DEFAULT_MESSAGE
        worldName = config.getString("world", "") ?: ""
        checkInterval = config.getLong("check-interval", 20L).coerceAtLeast(1L)
        dawnThreshold = config.getLong("dawn-threshold", 20L).coerceIn(1L, 23999L)
    }

    companion object {
        const val DEFAULT_MESSAGE = "<yellow>It's 06:00! Day {day}</yellow>"
    }
}
