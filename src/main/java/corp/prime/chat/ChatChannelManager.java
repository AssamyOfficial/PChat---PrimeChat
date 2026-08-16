package corp.prime.chat;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatChannelManager {
    private final PrimeChat plugin;
    private final List<ChatChannel> channels = new ArrayList<>();

    public ChatChannelManager(PrimeChat plugin) {
        this.plugin = plugin;
        loadChannels();
    }

    public void loadChannels() {
        channels.clear();

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("channels");
        if (section == null) {
            plugin.getLogger().warning("Раздел 'channels' не найден в config.yml!");
            return;
        }

        for (String id : section.getKeys(false)) {
            ConfigurationSection channelSection = section.getConfigurationSection(id);
            if (channelSection == null) {
                continue;
            }

            boolean enabled = channelSection.getBoolean("enabled", true);
            String mode = channelSection.getString("mode", "local");
            String format = channelSection.getString(
                    "format",
                    "<gray>[" + id + "]</gray> <aqua>%player%</aqua> <gray>»</gray> <white>%message%</white>"
            );
            List<String> aliases = channelSection.getStringList("aliases");
            String trigger = channelSection.getString("trigger", "");
            String command = channelSection.getString("command", "");
            String permission = channelSection.getString("permission", "");
            int radius = channelSection.getInt("radius", 100);

            Boolean unheardMessageEnabled = null;
            if (channelSection.contains("unheard-message.enabled")) {
                unheardMessageEnabled = channelSection.getBoolean("unheard-message.enabled");
            }

            String unheardMessage = null;
            if (channelSection.contains("unheard-message.message")) {
                unheardMessage = channelSection.getString("unheard-message.message");
            }

            channels.add(new ChatChannel(
                    id,
                    enabled,
                    mode,
                    format,
                    aliases,
                    trigger,
                    command,
                    permission,
                    radius,
                    unheardMessageEnabled,
                    unheardMessage
            ));

            plugin.getLogger().info("Загружен чат-канал: " + id + " (" + mode + ")");
        }

        plugin.getLogger().info("Всего загружено каналов: " + channels.size());
    }

    public ChatChannel getChannel(String id) {
        for (ChatChannel channel : channels) {
            if (channel.getId().equalsIgnoreCase(id)) {
                return channel;
            }
        }
        return null;
    }

    public ChatChannel getChannelByCommand(String command) {
        if (command == null || command.isEmpty()) {
            return null;
        }

        String cleanCommand = command.startsWith("/") ? command.substring(1) : command;

        for (ChatChannel channel : channels) {
            if (!channel.isEnabled() || !channel.isCommand()) {
                continue;
            }

            if (channel.getCommand() != null
                    && !channel.getCommand().isEmpty()
                    && channel.getCommand().equalsIgnoreCase(cleanCommand)) {
                return channel;
            }

            for (String alias : channel.getAliases()) {
                if (alias != null && alias.equalsIgnoreCase(cleanCommand)) {
                    return channel;
                }
            }
        }

        return null;
    }

    public List<ChatChannel> getChannels() {
        return Collections.unmodifiableList(channels);
    }

    public void sendCommandChannelMessage(Player sender, ChatChannel channel, String message) {
        if (channel == null || !channel.isEnabled()) {
            return;
        }

        String permission = channel.getPermission();
        if (permission != null && !permission.isEmpty() && !sender.hasPermission(permission)) {
            sender.sendMessage("§cУ вас нет прав для использования этого чата.");
            return;
        }

        plugin.getChatChannelMessageSender().send(sender, channel, message);
    }
}
