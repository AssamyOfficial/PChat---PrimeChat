package corp.prime.chat;

import java.util.List;

public class ChatChannel {
    private final String id;
    private final boolean enabled;
    private final String mode;
    private final String format;
    private final List<String> aliases;
    private final String trigger;
    private final String command;
    private final String permission;
    private final int radius;
    private final Boolean unheardMessageEnabled;
    private final String unheardMessage;

    public ChatChannel(String id, boolean enabled, String mode, String format, List<String> aliases, String trigger, String command, String permission, int radius, Boolean unheardMessageEnabled, String unheardMessage) {
        this.id = id;
        this.enabled = enabled;
        this.mode = mode;
        this.format = format;
        this.aliases = aliases;
        this.trigger = trigger;
        this.command = command;
        this.permission = permission;
        this.radius = radius;
        this.unheardMessageEnabled = unheardMessageEnabled;
        this.unheardMessage = unheardMessage;
    }

    public String getId() { return id; }
    public boolean isEnabled() { return enabled; }
    public String getMode() { return mode; }
    public String getFormat() { return format; }
    public List<String> getAliases() { return aliases; }
    public String getTrigger() { return trigger; }
    public String getCommand() { return command; }
    public String getPermission() { return permission; }
    public int getRadius() { return radius; }
    public Boolean getUnheardMessageEnabled() { return unheardMessageEnabled; }
    public String getUnheardMessage() { return unheardMessage; }
    public boolean isLocal() { return mode.equalsIgnoreCase("local"); }
    public boolean isGlobal() { return mode.equalsIgnoreCase("global"); }
    public boolean isCommand() { return mode.equalsIgnoreCase("command"); }
}
