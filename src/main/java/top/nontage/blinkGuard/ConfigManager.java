package top.nontage.blinkGuard;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {

    private final JavaPlugin plugin;

    public boolean enabled;
    public long burstGapMs;
    public long burstCount;
    public long blockActionMs;
    public boolean opBypass;
    public boolean staffAlert;
    public boolean debug;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration c = plugin.getConfig();

        enabled = c.getBoolean("enabled", true);
        burstGapMs = c.getLong("burst-gap-ms", 20L);
        burstCount = c.getLong("burst-count", 3L);
        blockActionMs = c.getLong("block-action-ms", 500L);
        opBypass = c.getBoolean("op-bypass", true);
        staffAlert = c.getBoolean("staff-alert", true);
        debug = c.getBoolean("debug", false);
    }

    public boolean set(String key, String value) {
        FileConfiguration c = plugin.getConfig();
        Object parsed;
        switch (key.toLowerCase()) {
            case "enabled":
            case "op-bypass":
            case "staff-alert":
            case "debug":
                parsed = Boolean.parseBoolean(value);
                break;
            case "burst-gap-ms":
            case "burst-count":
            case "block-action-ms":
                try { parsed = Long.parseLong(value); } catch (NumberFormatException e) { return false; }
                break;
            default:
                return false;
        }
        c.set(key.toLowerCase(), parsed);
        plugin.saveConfig();
        load();
        return true;
    }
}