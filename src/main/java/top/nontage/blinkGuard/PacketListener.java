package top.nontage.blinkGuard;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import org.bukkit.entity.Player;

public class PacketListener extends PacketListenerAbstract {

    private final BlinkCounter counter;

    public PacketListener(BlinkCounter counter) {
        this.counter = counter;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!(event.getPacketType() instanceof PacketType.Play.Client)) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.isOnline()) {
            return;
        }

        PacketType.Play.Client type = (PacketType.Play.Client) event.getPacketType();

        if (type == PacketType.Play.Client.PLAYER_FLYING
                || type == PacketType.Play.Client.PLAYER_POSITION
                || type == PacketType.Play.Client.PLAYER_ROTATION
                || type == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {

            try {
                WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);
                if (counter.onPacket(player, wrapper)) {
                    event.setCancelled(true);
                }
            } catch (Exception ignored) {
            }
            return;
        }

        if (type == PacketType.Play.Client.INTERACT_ENTITY
                || type == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT
                || type == PacketType.Play.Client.PLAYER_DIGGING) {

            try {
                if (counter.shouldBlockAction(player)) {
                    if (player.getItemInHand().getType().name().contains("SWORD")) {
                        unblockServerSide(player);
                    } else {
                        System.out.println(player.getItemInHand().getType().name());
                    }
                    event.setCancelled(true);
                    if (counter.cfg != null && counter.cfg.debug) {
                        System.out.println("[BlinkGuard] Blocked action (" + type.name() + ") from punished player " + player.getName());
                    }
                }
            } catch (Exception ignored) {
            }
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