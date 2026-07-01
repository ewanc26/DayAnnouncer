package uk.ewancroft.dayannouncer.util

import org.bukkit.plugin.java.JavaPlugin
import java.net.URI

class UpdateChecker(
    private val plugin: JavaPlugin,
    private val repoOwner: String,
    private val repoName: String,
) {

    fun checkAsync() {
        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
            try {
                val url = URI("https://api.github.com/repos/$repoOwner/$repoName/releases/latest").toURL()
                val connection = url.openConnection()
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                connection.setRequestProperty("User-Agent", "DayAnnouncer/$repoOwner")
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val json = connection.inputStream.bufferedReader().readText()
                val tagName = extractTagName(json) ?: return@Runnable
                val currentVersion = plugin.pluginMeta.version
                val latestVersion = tagName.removePrefix("v")

                if (currentVersion != latestVersion) {
                    val msg = "Update available: v$latestVersion (current: v$currentVersion) — $url"
                    plugin.server.scheduler.runTask(plugin, Runnable {
                        plugin.logger.info(msg)
                    })
                }
            } catch (_: Exception) {
                // update check is best-effort
            }
        })
    }

    private fun extractTagName(json: String): String? {
        val key = "\"tag_name\":\""
        val start = json.indexOf(key)
        if (start == -1) return null
        val valueStart = start + key.length
        val valueEnd = json.indexOf('"', valueStart)
        return if (valueEnd == -1) null else json.substring(valueStart, valueEnd)
    }
}
