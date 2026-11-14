package top.speedcubing.antiblink;

import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import net.minecraft.server.v1_8_R3.PacketPlayOutPosition;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BlinkCounter {

    public static class PosData {
        public long lastPacketTime = 0L;
        public double lastLegalX, lastLegalY, lastLegalZ;
        public boolean initialized = false;
        public boolean firstJoin = true;
        public long lastPunishTime = 0L;
        public long blockActionsUntil = 0L;
        public long immuneUntil = 0L;
        public long damageImmuneUntil = 0L;
        public long lastAlertTime = 0L;
        public int fastPacketCount = 0;
    }

    private static final Map<UUID, PosData> dataMap = new ConcurrentHashMap<>();
    public final ConfigManager cfg;

    public BlinkCounter(ConfigManager cfg) {
        this.cfg = cfg;
    }

    public void onQuit(Player player) {
        if (player != null) dataMap.remove(player.getUniqueId());
    }

    public void onTeleport(Player player) {
        if (player == null) return;
        PosData d = dataMap.computeIfAbsent(player.getUniqueId(), k -> new PosData());
        d.immuneUntil = System.currentTimeMillis() + 750;
        d.initialized = false;
        d.lastPacketTime = 0L;
        d.fastPacketCount = 0;
    }

    public void onJoin(Player player) {
        if (player == null) return;
        PosData d = dataMap.computeIfAbsent(player.getUniqueId(), k -> new PosData());
        d.firstJoin = false;
    }

    public void onDamage(Player player) {
        if (player == null) return;
        PosData d = dataMap.computeIfAbsent(player.getUniqueId(), k -> new PosData());
        d.damageImmuneUntil = System.currentTimeMillis() + 1000;
        d.blockActionsUntil = 0L;
        d.initialized = false;
        d.lastPacketTime = 0L;
        d.fastPacketCount = 0;
    }

    private double getTps() {
        try {
            MinecraftServer server = MinecraftServer.getServer();
            return Math.min(20.0, server.recentTps[0]);
        } catch (Throwable e) {
            return 20.0;
        }
    }

    public boolean onPacket(Player player, PacketPlayInFlying p) {
        if (!cfg.enabled) return false;
        if (cfg.opBypass && player.isOp()) return false;

        double currentTps = getTps();
        if (currentTps < 15.0) return false;

        PosData d = dataMap.computeIfAbsent(player.getUniqueId(), k -> new PosData());
        long now = System.currentTimeMillis();

        if (now < d.damageImmuneUntil) {
            if(p.g()) {
                d.lastLegalX = p.a();
                d.lastLegalY = p.b();
                d.lastLegalZ = p.c();
                d.lastPacketTime = now;
                d.initialized = true;
                d.fastPacketCount = 0;
            }
            return false;
        }

        if (!p.g()) return false;

        long gap = now - d.lastPacketTime;

        double multiplier = (currentTps < 18.0) ? 0.5 : 1.0;
        long currentGapThreshold = Math.max(1L, (long) (cfg.burstGapMs * multiplier));

        if (now < d.immuneUntil) {
            if (gap < currentGapThreshold && d.lastPacketTime != 0L) {
                if (cfg.debug)
                    System.out.println("[BlinkGuard] Cancelled burst during teleport immunity for " + player.getName());
                return true;
            }
            d.lastPacketTime = now;
            d.lastLegalX = p.a();
            d.lastLegalY = p.b();
            d.lastLegalZ = p.c();
            d.initialized = true;
            d.fastPacketCount = 0;
            return false;
        }

        if (!d.initialized) {
            d.lastLegalX = p.a();
            d.lastLegalY = p.b();
            d.lastLegalZ = p.c();
            d.lastPacketTime = now;
            d.initialized = true;
            d.fastPacketCount = 0;
            return false;
        }

        d.lastPacketTime = now;

        if (gap < currentGapThreshold) {
            d.fastPacketCount++;
        } else {
            d.fastPacketCount = 0;
        }

        if (d.fastPacketCount >= cfg.burstCount) {
            if (now - d.lastPunishTime > 500) {
                d.lastPunishTime = now;
                d.blockActionsUntil = now + cfg.blockActionMs;

                PacketPlayOutPosition s08 = new PacketPlayOutPosition(
                        d.lastLegalX, d.lastLegalY, d.lastLegalZ,
                        player.getLocation().getYaw(),
                        player.getLocation().getPitch(),
                        Collections.emptySet()          // flags = 0,全部是絕對座標
                );
                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(s08);

                if (cfg.debug) {
                    System.out.println("[BlinkGuard] CAUGHT BLINK BURST for " + player.getName()
                            + " (gap=" + gap + "ms, Count=" + d.fastPacketCount
                            + ", TPS=" + String.format("%.1f", currentTps) + ")");
                }

                if (cfg.staffAlert && now - d.lastAlertTime > 2000) {
                    d.lastAlertTime = now;
                    String alertMsg = "§8[§cBlinkGuard§8] §e" + player.getName() + " §7failed §cBlink §7(gap: " + gap + "ms)";
                    for (Player staff : Bukkit.getOnlinePlayers()) {
                        if (staff.hasPermission("blinkguard.alerts")) {
                            staff.sendMessage(alertMsg);
                        }
                    }
                }
            }
            return true;
        }

        if (d.fastPacketCount > 0) {
            return true;
        }

        d.lastLegalX = p.a();
        d.lastLegalY = p.b();
        d.lastLegalZ = p.c();
        return false;
    }

    public boolean shouldBlockAction(Player player) {
        if (!cfg.enabled) return false;
        if (cfg.opBypass && player.isOp()) return false;
        PosData d = dataMap.get(player.getUniqueId());
        if (d == null) return false;
        if (d.firstJoin) return false;
        return System.currentTimeMillis() < d.blockActionsUntil;
    }
}