package uk.ewancroft.dayannouncer.message

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.World
import org.bukkit.plugin.java.JavaPlugin
import uk.ewancroft.dayannouncer.config.OutputConfig
import java.time.Duration

class AnnounceDispatcher(
    private val plugin: JavaPlugin,
    private val defaultOutput: OutputConfig,
    private val defaultSound: String?,
) {

    private val activeBossBars = mutableListOf<BossBar>()

    fun announce(
        component: Component,
        world: World,
        worldOutput: OutputConfig? = null,
        worldSound: String? = null,
    ) {
        val output = worldOutput ?: defaultOutput
        val sound = worldSound ?: defaultSound

        if (output.chat) broadcastChat(component, world)
        if (output.actionBar) broadcastActionBar(component, world)
        if (output.title) broadcastTitle(component, world)
        if (output.bossBar) broadcastBossBar(component, world)
        if (sound != null) playSound(world, sound)
    }

    private fun broadcastChat(component: Component, world: World) {
        val players = world.players
        if (players.isEmpty()) {
            Bukkit.broadcast(component)
        } else {
            players.forEach { it.sendMessage(component) }
            Bukkit.getConsoleSender().sendMessage(component)
        }
    }

    private fun broadcastActionBar(component: Component, world: World) {
        world.players.forEach { it.sendActionBar(component) }
    }

    private fun broadcastTitle(component: Component, world: World) {
        val title = Title.title(
            component,
            Component.empty(),
            Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500)),
        )
        world.players.forEach { it.showTitle(title) }
    }

    private fun broadcastBossBar(component: Component, world: World) {
        val bossBar = BossBar.bossBar(component, 1f, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS)
        world.players.forEach { it.showBossBar(bossBar) }
        activeBossBars.add(bossBar)

        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            world.players.forEach { it.hideBossBar(bossBar) }
            activeBossBars.remove(bossBar)
        }, 100L)
    }

    private fun playSound(world: World, soundName: String) {
        val key = if (soundName.contains(':')) soundName else "minecraft:$soundName"
        val nsKey = NamespacedKey.fromString(key) ?: return
        val bukkitSound = Registry.SOUND_EVENT.get(nsKey) ?: return
        world.players.forEach { it.playSound(it.location, bukkitSound, 1f, 1f) }
    }

    fun cancelAllBossBars() {
        val copy = activeBossBars.toList()
        copy.forEach { bossBar ->
            Bukkit.getOnlinePlayers().forEach { it.hideBossBar(bossBar) }
            activeBossBars.remove(bossBar)
        }
    }
}
