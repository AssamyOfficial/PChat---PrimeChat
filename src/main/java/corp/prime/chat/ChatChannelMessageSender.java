package corp.prime.chat;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ChatChannelMessageSender {

    private final PrimeChat plugin;

    public ChatChannelMessageSender(
            PrimeChat plugin
    ) {
        this.plugin = plugin;
    }

    /**
     * Отправляет сообщение командного канала.
     */
    public void send(
            Player sender,
            ChatChannel channel,
            String message
    ) {

        if (channel == null
                || !channel.isEnabled()) {

            return;
        }

        if (message == null
                || message.trim().isEmpty()) {

            return;
        }

        /*
         * Проверяем permission отправителя.
         */

        String permission =
                channel.getPermission();

        if (permission != null
                && !permission.isEmpty()
                && !sender.hasPermission(permission)) {

            sender.sendMessage(
                    "§cУ вас нет прав для использования этого чата."
            );

            return;
        }

        /*
         * Создаём форматированное сообщение.
         */

        Component formatted =
                plugin.getChatChannelFormatter()
                        .format(
                                sender,
                                channel,
                                message
                        );

        /*
         * Отправляем только игрокам,
         * которые имеют право видеть канал.
         */

        for (Player viewer :
                Bukkit.getOnlinePlayers()) {

            if (!plugin.getChatChannelRouter()
                    .canReceiveMessage(
                            sender,
                            viewer,
                            channel
                    )) {

                continue;
            }

            viewer.sendMessage(formatted);
        }
    }
}