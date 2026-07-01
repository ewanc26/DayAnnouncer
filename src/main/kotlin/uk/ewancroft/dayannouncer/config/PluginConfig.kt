package uk.ewancroft.dayannouncer.config

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.FileConfiguration

data class WorldConfig(
    val name: String,
    val message: String,
    val checkInterval: Long,
    val dawnThreshold: Long,
)

data class OutputConfig(
    val chat: Boolean,
    val actionBar: Boolean,
    val title: Boolean,
    val bossBar: Boolean,
)

class PluginConfig(private val config: FileConfiguration) {

    var enabled: Boolean
    val worlds: List<WorldConfig>
    val output: OutputConfig
    val sound: String?

    init {
        enabled = config.getBoolean("enabled", true)
        worlds = parseWorlds()
        output = parseOutput()
        sound = config.getString("sound", "")?.ifBlank { null }
    }

    private fun parseWorlds(): List<WorldConfig> {
        val section = config.getConfigurationSection("worlds") ?: return emptyList()
        return section.getKeys(false).mapNotNull { key ->
            val wc = section.getConfigurationSection(key) ?: return@mapNotNull null
            WorldConfig(
                name = key,
                message = wc.getString("message") ?: "<yellow>It's 06:00! Day {day}</yellow>",
                checkInterval = wc.getLong("check-interval", 20L).coerceAtLeast(1L),
                dawnThreshold = wc.getLong("dawn-threshold", 20L).coerceIn(1L, 23999L),
            )
        }
    }

    private fun parseOutput(): OutputConfig {
        val section = config.getConfigurationSection("output") ?: return OutputConfig(true, false, false, false)
        return OutputConfig(
            chat = section.getBoolean("chat", true),
            actionBar = section.getBoolean("action-bar", false),
            title = section.getBoolean("title", false),
            bossBar = section.getBoolean("boss-bar", false),
        )
    }

    fun worldConfig(name: String): WorldConfig? = worlds.find { it.name == name }
}
