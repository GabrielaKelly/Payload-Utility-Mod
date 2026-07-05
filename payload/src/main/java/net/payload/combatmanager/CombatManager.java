package net.payload.combatmanager;

import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.payload.Payload;
import net.payload.event.events.DeathEvent;
import net.payload.event.events.ReceivePacketEvent;
import net.payload.event.events.TickEvent;
import net.payload.event.events.TotemPopEvent;
import net.payload.event.listeners.DeathListener;
import net.payload.event.listeners.ReceivePacketListener;
import net.payload.event.listeners.TickListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

import static net.payload.PayloadClient.MC;

public class CombatManager implements ReceivePacketListener {

    public CombatManager() {
        Payload.getInstance().eventManager.AddListener(ReceivePacketListener.class, this);
    }

    public final HashMap<String, Integer> popContainer = new HashMap<>();

    public Integer getPop(String s) {
        return popContainer.getOrDefault(s, 0);
    }

    public void resetPops() {
        popContainer.clear();
    }

    public static boolean nullCheck() {
        return MC.player == null || MC.world == null;
    }

    @Override
    public void onReceivePacket(ReceivePacketEvent event) {
        if (nullCheck()) return;
        if (event.getPacket() instanceof EntityStatusS2CPacket packet) {
            if (packet.getStatus() == EntityStatuses.USE_TOTEM_OF_UNDYING) {
                Entity entity = packet.getEntity(MC.world);
                if(entity instanceof PlayerEntity player) {
                    onTotemPop(player);
                }
            }
        }

        if (event.getPacket() instanceof DeathMessageS2CPacket packet) {

            Entity entity = MC.world.getEntityById(packet.playerId());

            if (entity instanceof PlayerEntity) {
                Payload.getInstance().combatManager.onDeath((PlayerEntity) entity);
                DeathEvent deathEvent = new DeathEvent((PlayerEntity) entity);
                Payload.getInstance().eventManager.Fire(deathEvent);
            }
        }
    }

    public void onDeath(PlayerEntity player) {
        popContainer.remove(player.getName().getString());
    }

    public void onTotemPop(PlayerEntity player) {
        int l_Count = 1;
        if (popContainer.containsKey(player.getName().getString())) {
            l_Count = popContainer.get(player.getName().getString());
            popContainer.put(player.getName().getString(), ++l_Count);
        } else {
            popContainer.put(player.getName().getString(), l_Count);
        }
        TotemPopEvent totemPopEvent = new TotemPopEvent(player);
        Payload.getInstance().eventManager.Fire(totemPopEvent);
    }
}