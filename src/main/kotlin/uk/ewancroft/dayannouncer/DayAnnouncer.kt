package uk.ewancroft.dayannouncer

import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.plugin.java.JavaPlugin
import uk.ewancroft.dayannouncer.command.DayAnnouncerCommand
import uk.ewancroft.dayannouncer.config.PluginConfig
import uk.ewancroft.dayannouncer.config.WorldConfig
import uk.ewancroft.dayannouncer.listener.TimeSkipListener
import uk.ewancroft.dayannouncer.message.AnnounceDispatcher
import uk.ewancroft.dayannouncer.message.MessageFormatter
import uk.ewancroft.dayannouncer.task.DayCheckTask

class DayAnnouncer : JavaPlugin() {

    private var command: DayAnnouncerCommand? = null
    private var _pluginState: PluginState? = null
    val pluginState: PluginState? get() = _pluginState

    data class PluginState(
        val config: PluginConfig,
        val dispatcher: AnnounceDispatcher,
        val worlds: Map<String, WorldState>,
        val listener: TimeSkipListener,
    )

    data class WorldState(
        val worldConfig: WorldConfig,
        val worldSupplier: () -> World?,
        val task: DayCheckTask?,
        var enabled: Boolean,
    )

    override fun onEnable() {
        saveDefaultConfig()
        command = DayAnnouncerCommand(this)
        val cmd = getCommand("dayannouncer") ?: return
        cmd.setExecutor(command)
        cmd.setTabCompleter(command)
        _pluginState = buildPluginState()
        logger.info("DayAnnouncer enabled. ${_pluginState?.config?.worlds?.size ?: 0} world(s) configured.")
    }

    override fun onDisable() {
        _pluginState?.worlds?.values?.forEach { it.task?.cancel(); it.task?.reset() }
        _pluginState?.dispatcher?.cancelAllBossBars()
        _pluginState = null
        command = null
        logger.info("DayAnnouncer disabled.")
    }

    private fun buildPluginState(): PluginState {
        val config = PluginConfig(config)

        val dispatcher = AnnounceDispatcher(this, config.output, config.sound)

        val worldStates = mutableMapOf<String, WorldState>()

        for (wc in config.worlds) {
            val worldSupplier: () -> World? = { Bukkit.getWorld(wc.name) }
            val task = DayCheckTask(wc, worldSupplier, dispatcher)
            task.runTaskTimer(this, 0L, wc.checkInterval)
            worldStates[wc.name] = WorldState(
                worldConfig = wc,
                worldSupplier = worldSupplier,
                task = task,
                enabled = true,
            )
        }

        val listenerWorldStates = worldStates.mapValues { (_, ws) ->
            TimeSkipListener.WorldState(ws.worldSupplier, ws.worldConfig)
        }
        val listener = TimeSkipListener(listenerWorldStates, dispatcher)
        server.pluginManager.registerEvents(listener, this)

        return PluginState(config, dispatcher, worldStates, listener)
    }

    fun reloadPluginConfig() {
        reloadConfig()
        restartTasks()
    }

    fun restartTasks() {
        _pluginState?.worlds?.values?.forEach { it.task?.cancel(); it.task?.reset() }
        _pluginState?.listener?.let { server.pluginManager.registerEvents(it, this) }
        _pluginState = buildPluginState()
    }

    fun announceForWorld(world: World) {
        val state = _pluginState ?: return
        val wc = state.config.worldConfig(world.name) ?: return
        val formatter = MessageFormatter(wc.message)
        state.dispatcher.announce(formatter.format(world), world)
    }

    fun toggleWorld(name: String) {
        val state = _pluginState ?: return
        val ws = state.worlds[name] ?: return
        ws.enabled = !ws.enabled
        if (ws.enabled) {
            ws.task?.runTaskTimer(this, 0L, ws.worldConfig.checkInterval)
        } else {
            ws.task?.cancel()
        }
    }

    fun isWorldEnabled(name: String): Boolean {
        return _pluginState?.worlds?.get(name)?.enabled ?: false
    }

    fun togglePlugin() {
        val state = _pluginState ?: return
        val wasEnabled = state.config.enabled
        state.config.enabled = !wasEnabled
        if (state.config.enabled) {
            restartTasks()
        } else {
            state.worlds.values.forEach { it.task?.cancel(); it.task?.reset() }
            state.dispatcher.cancelAllBossBars()
        }
    }
}
