package corp.prime.chat;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ChatChannelManager {
    private final PrimeChat plugin;
    private final List<ChatChannel> channels = new ArrayList<>();
    private final Map<String, ChatChannel> channelsById = new LinkedHashMap<>();
    private final Map<String, ChatChannel> commandChannels = new LinkedHashMap<>();

    public ChatChannelManager(PrimeChat plugin) {
        this.plugin = plugin;
        loadChannels();
    }

    public void loadChannels() {
        channels.clear();
        channelsById.clear();
        commandChannels.clear();

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

            ChatChannel channel = new ChatChannel(
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
            );

            channels.add(channel);
            channelsById.put(id.toLowerCase(), channel);

            if (channel.isCommand()) {
                if (command != null && !command.isEmpty()) {
                    commandChannels.put(command.toLowerCase(), channel);
                }

                for (String alias : aliases) {
                    if (alias != null && !alias.isEmpty()) {
                        commandChannels.put(alias.toLowerCase(), channel);
                    }
                }
            }
        }

        plugin.getLogger().info("Загружено каналов: " + channels.size());
    }

    public ChatChannel getChannel(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        return channelsById.get(id.toLowerCase());
    }

    public ChatChannel getChannelByCommand(String command) {
        if (command == null || command.isEmpty()) {
            return null;
        }

        String cleanCommand = command.startsWith("/") ? command.substring(1) : command;
        ChatChannel channel = commandChannels.get(cleanCommand.toLowerCase());

        if (channel == null || !channel.isEnabled()) {
            return null;
        }

        return channel;
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
            plugin.getPrimeScheduler().run(sender, () ->
                    sender.sendMessage("§cУ вас нет прав для использования этого чата.")
            );
            return;
        }

        plugin.getChatChannelMessageSender().send(sender, channel, message);
    }
}
