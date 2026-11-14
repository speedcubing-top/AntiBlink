package top.nontage.blinkGuard;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class BlinkListener implements Listener {

    private final BlinkCounter blinkCounter;

    public BlinkListener(BlinkCounter blinkCounter) {
        this.blinkCounter = blinkCounter;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        blinkCounter.onJoin(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        blinkCounter.onQuit(e.getPlayer());
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        blinkCounter.onTeleport(e.getPlayer());
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            blinkCounter.onDamage((Player) e.getEntity());
        }
    }

}