package corp.prime.chat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.entity.Player;

public class ChatChannelCommandListener implements Listener {

    private final PrimeChat plugin;

    public ChatChannelCommandListener(PrimeChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {

        String message = event.getMessage();

        if (message == null || message.length() <= 1) {
            return;
        }

        if (!message.startsWith("/")) {
            return;
        }

        String commandLine = message.substring(1).trim();

        if (commandLine.isEmpty()) {
            return;
        }

        String[] parts = commandLine.split("\\s+");

        if (parts.length == 0) {
            return;
        }

        String commandName = parts[0];

        /*
         * Ищем динамический канал.
         */
        ChatChannel channel =
                plugin.getChatChannelManager()
                        .getChannelByCommand(commandName);

        if (channel == null) {
            return;
        }

        /*
         * Это наша команда.
         * Bukkit больше не должен передавать её дальше.
         */
        event.setCancelled(true);

        Player player = event.getPlayer();

        /*
         * Проверяем permission.
         */
        String permission = channel.getPermission();

        if (permission != null
                && !permission.isEmpty()
                && !player.hasPermission(permission)) {

            player.sendMessage(
                    "§cУ вас нет прав для использования этого чата."
            );

            return;
        }

        /*
         * Команда без сообщения.
         */
        if (parts.length == 1) {

            player.sendMessage(
                    "§7Использование: §f/"
                            + commandName
                            + " <сообщение>"
            );

            return;
        }

        /*
         * Собираем сообщение обратно.
         */
        StringBuilder messageBuilder = new StringBuilder();

        for (int i = 1; i < parts.length; i++) {

            if (i > 1) {
                messageBuilder.append(" ");
            }

            messageBuilder.append(parts[i]);
        }

        String chatMessage = messageBuilder.toString();

        /*
         * Отправляем сообщение через
         * общую систему каналов.
         */
        plugin.getChatChannelMessageSender().send(
                player,
                channel,
                chatMessage
        );
    }
}