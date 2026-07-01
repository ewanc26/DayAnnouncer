package uk.ewancroft.dayannouncer.config

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.FileConfiguration

data class WorldConfig(
    val name: String,
    val enabled: Boolean,
    val messages: List<String>,
    val checkInterval: Long,
    val dawnThreshold: Long,
    val output: OutputConfig?,
    val sound: String?,
) {
    fun randomMessage(): String = if (messages.size == 1) messages[0] else messages.random()
}

data class OutputConfig(
    val chat: Boolean,
    val actionBar: Boolean,
    val title: Boolean,
    val bossBar: Boolean,
)

class PluginConfig(private val config: FileConfiguration) {

    var enabled: Boolean
    val worlds: List<WorldConfig>
    val defaultOutput: OutputConfig
    val defaultSound: String?
    val configVersion: Int

    init {
        migrateConfig()
        enabled = config.getBoolean("enabled", true)
        worlds = parseWorlds()
        defaultOutput = parseOutput("output")
        defaultSound = config.getString("sound", "")?.ifBlank { null }
        configVersion = config.getInt("config-version", 1)
    }

    private fun migrateConfig() {
        val version = config.getInt("config-version", 0)
        if (version >= 1) return

        val worlds = config.getConfigurationSection("worlds")
        if (worlds != null) {
            for (key in worlds.getKeys(false)) {
                val wc = worlds.getConfigurationSection(key)
                if (wc != null && !wc.contains("enabled")) {
                    wc.set("enabled", true)
                }
            }
        }
        config.set("config-version", 1)
    }

    private fun parseWorlds(): List<WorldConfig> {
        val section = config.getConfigurationSection("worlds") ?: return emptyList()
        return section.getKeys(false).mapNotNull { key ->
            val wc = section.getConfigurationSection(key) ?: return@mapNotNull null
            val messages = parseMessages(wc)
            WorldConfig(
                name = key,
                enabled = wc.getBoolean("enabled", true),
                messages = messages,
                checkInterval = wc.getLong("check-interval", 20L).coerceAtLeast(1L),
                dawnThreshold = wc.getLong("dawn-threshold", 20L).coerceIn(1L, 23999L),
                output = parseOutputOrNull(wc.getConfigurationSection("output")),
                sound = wc.getString("sound", "")?.ifBlank { null },
            )
        }
    }

    private fun parseMessages(wc: ConfigurationSection): List<String> {
        val single = wc.getString("message")
        if (single != null) return listOf(single)
        val list = wc.getStringList("messages")
        if (list.isNotEmpty()) return list
        return listOf("<yellow>It's {time}! Day {day}</yellow>")
    }

    private fun parseOutput(sectionPath: String): OutputConfig {
        val section = config.getConfigurationSection(sectionPath) ?: return OutputConfig(true, false, false, false)
        return OutputConfig(
            chat = section.getBoolean("chat", true),
            actionBar = section.getBoolean("action-bar", false),
            title = section.getBoolean("title", false),
            bossBar = section.getBoolean("boss-bar", false),
        )
    }

    private fun parseOutputOrNull(section: ConfigurationSection?): OutputConfig? {
        if (section == null) return null
        return OutputConfig(
            chat = section.getBoolean("chat", true),
            actionBar = section.getBoolean("action-bar", false),
            title = section.getBoolean("title", false),
            bossBar = section.getBoolean("boss-bar", false),
        )
    }

    fun worldConfig(name: String): WorldConfig? = worlds.find { it.name == name }
}
