package uk.ewancroft.dayannouncer.message

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.World

/**
 * Formats announcement messages with placeholder replacement and MiniMessage parsing.
 */
class MessageFormatter(private val template: String) {

    private val miniMessage = MiniMessage.miniMessage()

    /**
     * Builds the announcement component for the given world.
     *
     * @param world the world to build the message for
     * @return the formatted [Component] ready for broadcasting
     */
    fun format(world: World): Component {
        val day = world.fullTime / 24000
        val timeString = formatTimeString(world.time)

        val raw = template
            .replace("{day}", day.toString())
            .replace("{time}", timeString)

        return miniMessage.deserialize(raw)
    }

    /**
     * Returns the plain-text version of the message (no formatting)
     * for logging purposes.
     */
    fun formatPlain(world: World): String {
        return PlainTextComponentSerializer.plainText().serialize(format(world))
    }

    companion object {
        /**
         * Converts a Minecraft tick time (0-23999) to a human-readable
         * HH:MM string. Time 0 = 06:00.
         */
        fun formatTimeString(ticks: Long): String {
            val totalMinutes = (ticks / 1000.0 * 60).toInt()
            val hours = ((totalMinutes / 60 + 6) % 24)
            val minutes = totalMinutes % 60
            return "%02d:%02d".format(hours, minutes)
        }
    }
}
