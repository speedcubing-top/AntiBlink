package top.nontage.blinkGuard;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.plugin.java.JavaPlugin;

public class BlinkGuardPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private BlinkCounter blinkCounter;
    private PacketListener packetListener;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings().reEncodeByDefault(false).checkForUpdates(false);
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        blinkCounter = new BlinkCounter(configManager);
        packetListener = new PacketListener(blinkCounter);

        PacketEvents.getAPI().getEventManager().registerListener(packetListener);
        PacketEvents.getAPI().init();

        getServer().getPluginManager().registerEvents(new BlinkListener(blinkCounter), this);

        getCommand("acblink").setExecutor(new BlinkCommand(configManager));
        getCommand("acblink").setTabCompleter(new BlinkCommand(configManager));
    }

    @Override
    public void onDisable() {
        if (PacketEvents.getAPI() != null) {
            PacketEvents.getAPI().terminate();
        }
        getLogger().info("BlinkGuard disabled.");
    }
}