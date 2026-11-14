package top.speedcubing.antiblink;

import org.bukkit.plugin.java.JavaPlugin;
import top.speedcubing.lib.eventbus.CubingEventManager;

public class AntiBlinkPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        ConfigManager configManager = new ConfigManager(this);
        BlinkCounter blinkCounter = new BlinkCounter(configManager);
        CubingEventManager.registerListeners(new PacketListener(blinkCounter));
        getServer().getPluginManager().registerEvents(new BlinkListener(blinkCounter), this);

        BlinkCommand blinkCommand = new BlinkCommand(configManager);
        getCommand("acblink").setExecutor(blinkCommand);
        getCommand("acblink").setTabCompleter(blinkCommand);
    }

    @Override
    public void onDisable() {
        getLogger().info("BlinkGuard disabled.");
    }
}