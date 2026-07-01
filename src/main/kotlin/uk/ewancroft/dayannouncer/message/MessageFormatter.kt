package uk.ewancroft.dayannouncer.message

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.World

class MessageFormatter(private val template: String) {

    private val miniMessage = MiniMessage.miniMessage()

    fun format(world: World): Component {
        val raw = template
            .replace("{day}", (world.fullTime / 24000).toString())
            .replace("{time}", formatTimeString(world.time))
            .replace("{time-name}", formatTimeName(world.time))
            .replace("{world}", world.name)
            .replace("{players}", world.players.size.toString())
            .replace("{max-players}", Bukkit.getMaxPlayers().toString())

        return miniMessage.deserialize(raw)
    }

    fun formatPlain(world: World): String {
        return PlainTextComponentSerializer.plainText().serialize(format(world))
    }

    companion object {
        fun formatTimeString(ticks: Long): String {
            val totalMinutes = (ticks / 1000.0 * 60).toInt()
            val hours = ((totalMinutes / 60 + 6) % 24)
            val minutes = totalMinutes % 60
            return "%02d:%02d".format(hours, minutes)
        }

        fun formatTimeName(ticks: Long): String {
            return when {
                ticks < 1000 -> "dawn"
                ticks < 6000 -> "morning"
                ticks < 12000 -> "afternoon"
                ticks < 13000 -> "dusk"
                ticks < 18000 -> "night"
                else -> "midnight"
            }
        }
    }
}
