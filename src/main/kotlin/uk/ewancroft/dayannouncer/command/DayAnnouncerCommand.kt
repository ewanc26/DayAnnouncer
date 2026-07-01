package uk.ewancroft.dayannouncer.command

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import uk.ewancroft.dayannouncer.DayAnnouncer
import uk.ewancroft.dayannouncer.message.MessageFormatter

class DayAnnouncerCommand(
    private val plugin: DayAnnouncer,
) : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage(usage())
            return true
        }

        when (args[0].lowercase()) {
            "reload" -> handleReload(sender)
            "test" -> handleTest(sender, args.getOrNull(1))
            "status" -> handleStatus(sender, args.getOrNull(1))
            "toggle" -> handleToggle(sender, args.getOrNull(1))
            else -> sender.sendMessage(usage())
        }
        return true
    }

    private fun handleReload(sender: CommandSender) {
        plugin.reloadPluginConfig()
        plugin.restartTasks()
        sender.sendMessage(Component.text("DayAnnouncer config reloaded.", NamedTextColor.GREEN))
    }

    private fun handleTest(sender: CommandSender, worldName: String?) {
        val world = resolveWorld(worldName)
        if (world == null) {
            sender.sendMessage(Component.text("World not found: $worldName", NamedTextColor.RED))
            return
        }
        plugin.announceForWorld(world)
        sender.sendMessage(Component.text("Test announcement sent to world: ${world.name}", NamedTextColor.GREEN))
    }

    private fun handleStatus(sender: CommandSender, worldName: String?) {
        val state = plugin.pluginState ?: run {
            sender.sendMessage(Component.text("Plugin is not initialized.", NamedTextColor.RED))
            return
        }
        val config = state.config
        sender.sendMessage(Component.text("--- DayAnnouncer Status ---", NamedTextColor.GOLD))
        sender.sendMessage(Component.text("Enabled: ${config.enabled}", NamedTextColor.WHITE))
        sender.sendMessage(Component.text("Sound: ${config.sound ?: "(none)"}", NamedTextColor.WHITE))
        sender.sendMessage(Component.text("Output - Chat: ${config.output.chat} | ActionBar: ${config.output.actionBar} | Title: ${config.output.title} | BossBar: ${config.output.bossBar}", NamedTextColor.WHITE))

        val worlds = if (worldName != null) {
            listOfNotNull(config.worldConfig(worldName))
        } else {
            config.worlds
        }

        if (worlds.isEmpty()) {
            sender.sendMessage(Component.text("No worlds configured.", NamedTextColor.YELLOW))
        } else {
            sender.sendMessage(Component.text("Worlds (${worlds.size}):", NamedTextColor.WHITE))
            worlds.forEach { wc ->
                val w = Bukkit.getWorld(wc.name)
                val online = w?.players?.size ?: 0
                sender.sendMessage(Component.text(
                    "  ${wc.name} — msg: \"${wc.message}\" check: ${wc.checkInterval}t dawn: ${wc.dawnThreshold}t players: $online",
                    NamedTextColor.GRAY,
                ))
            }
        }
    }

    private fun handleToggle(sender: CommandSender, worldName: String?) {
        val state = plugin.pluginState ?: run {
            sender.sendMessage(Component.text("Plugin is not initialized.", NamedTextColor.RED))
            return
        }

        if (worldName != null) {
            val wc = state.config.worldConfig(worldName)
            if (wc == null) {
                sender.sendMessage(Component.text("World not configured: $worldName", NamedTextColor.RED))
                return
            }
            plugin.toggleWorld(worldName)
            val enabled = plugin.isWorldEnabled(worldName)
            sender.sendMessage(Component.text("World '$worldName' announcements: ${if (enabled) "enabled" else "disabled"}", NamedTextColor.GREEN))
        } else {
            plugin.togglePlugin()
            sender.sendMessage(Component.text("Plugin globally: ${if (state.config.enabled) "enabled" else "disabled"}", NamedTextColor.GREEN))
        }
    }

    private fun resolveWorld(name: String?): World? {
        if (name != null) return Bukkit.getWorld(name)
        return plugin.pluginState?.config?.worlds?.firstOrNull()?.let { Bukkit.getWorld(it.name) }
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>,
    ): List<String> {
        if (args.size == 1) {
            return listOf("reload", "test", "status", "toggle").filter { it.startsWith(args[0].lowercase()) }
        }
        if (args.size == 2 && args[0].lowercase() in listOf("test", "status", "toggle")) {
            val worlds = plugin.pluginState?.config?.worlds?.map { it.name }.orEmpty()
            return worlds.filter { it.startsWith(args[1].lowercase()) }
        }
        return emptyList()
    }

    private fun usage(): Component {
        return Component.text("Usage: /da <reload|test [world]|status [world]|toggle [world]>", NamedTextColor.RED)
    }
}
