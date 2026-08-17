package corp.prime.chat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.ArrayList;
import java.util.List;

public class ChatChannelTabCompleter implements Listener {
    private final PrimeChat plugin;

    public ChatChannelTabCompleter(PrimeChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTabComplete(TabCompleteEvent event) {
        String buffer = event.getBuffer();
        if (buffer == null || !buffer.startsWith("/")) {
            return;
        }

        String input = buffer.substring(1);
        int space = input.indexOf(' ');

        if (space < 0) {
            String prefix = input.toLowerCase();
            List<String> completions = new ArrayList<>();

            for (ChatChannel channel : plugin.getChatChannelManager().getChannels()) {
                if (!channel.isEnabled() || !channel.isCommand()) {
                    continue;
                }

                String command = channel.getCommand();
                if (command != null && !command.isEmpty() && command.toLowerCase().startsWith(prefix)) {
                    completions.add(command);
                }

                for (String alias : channel.getAliases()) {
                    if (alias != null && !alias.isEmpty() && alias.toLowerCase().startsWith(prefix)) {
                        completions.add(alias);
                    }
                }
            }

            if (!completions.isEmpty()) {
                event.setCompletions(completions);
            }
            return;
        }

        String command = input.substring(0, space);
        ChatChannel channel = plugin.getChatChannelManager().getChannelByCommand(command);
        if (channel == null || !channel.isEnabled() || !channel.isCommand()) {
            return;
        }

        if (input.substring(space + 1).trim().isEmpty()) {
            event.setCompletions(new ArrayList<>());
        }
    }
}
