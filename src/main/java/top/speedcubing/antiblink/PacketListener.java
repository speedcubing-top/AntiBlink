package top.speedcubing.antiblink;

import net.minecraft.server.v1_8_R3.PacketPlayInBlockDig;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockPlace;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import top.speedcubing.lib.bukkit.events.packet.PlayInEvent;
import top.speedcubing.lib.eventbus.CubingEventHandler;

import java.util.EnumSet;
import java.util.Set;

public class PacketListener {

    private final BlinkCounter counter;

    public PacketListener(BlinkCounter counter) {
        this.counter = counter;
    }

    private static final Set<Material> SWORDS = EnumSet.of(
            Material.WOOD_SWORD, Material.STONE_SWORD, Material.IRON_SWORD,
            Material.GOLD_SWORD, Material.DIAMOND_SWORD);

    @CubingEventHandler
    public void a(PlayInEvent event) {
        final Player player = event.getPlayer();
        if (!player.isOnline()) return;

        final Object packet = event.getPacket();

        if (packet instanceof PacketPlayInFlying flying) {
            try {
                if (counter.onPacket(player, flying)) event.setCancelled(true);
            } catch (Exception ignored) {
            }
            return;
        }

        if (!(packet instanceof PacketPlayInBlockPlace|| packet instanceof PacketPlayInBlockDig || packet instanceof PacketPlayInUseEntity)) {
            return;
        }

        try {
            if (!counter.shouldBlockAction(player)) return;

            event.setCancelled(true);

            if (SWORDS.contains(player.getItemInHand().getType())) {
                unblockServerSide(player);
            }

            if (counter.cfg != null && counter.cfg.debug) {
                String name = packet instanceof PacketPlayInBlockPlace ? "block-place" : packet instanceof PacketPlayInBlockDig ? "block-dig" : "use-entity";
                System.out.println("[AntiBlink] Blocked action (" + name + ") from punished player " + player.getName());
            }
        } catch (Exception ignored) {
        }
    }

    public void unblockServerSide(Player player) {
        try {
            int currentSlot = player.getInventory().getHeldItemSlot();
            int newSlot = (currentSlot + 1) % 9;

            player.getInventory().setHeldItemSlot(newSlot);
            player.getInventory().setHeldItemSlot(currentSlot);

            player.updateInventory();
        } catch (Exception ignored) {
        }
    }

}