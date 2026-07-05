package net.payload.utils.block;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.payload.Payload;
import net.payload.event.events.ReceivePacketEvent;
import net.payload.event.listeners.ReceivePacketListener;
import net.payload.utils.math.CacheTimer;

import java.util.concurrent.ConcurrentHashMap;

import static net.payload.PayloadClient.MC;

public class BreakManager implements ReceivePacketListener {

    public BreakManager() {
        Payload.getInstance().eventManager.AddListener(ReceivePacketListener.class, this);
    }

    public final ConcurrentHashMap<Integer, BreakData> breakMap = new ConcurrentHashMap<>();

    public boolean isMining(BlockPos pos) {
        return isMining(pos, true);
    }
    public boolean isMining(BlockPos pos, boolean self) {
        /*
        if (self && PacketMine.getBreakPos() != null && PacketMine.getBreakPos().equals(pos)) {
            return true;
        }

         */

        for (BreakData breakData : breakMap.values()) {
            if (breakData.getEntity() == null) {
                continue;
            }
            if (breakData.getEntity().getEyePos().distanceTo(pos.toCenterPos()) > 7) {
                continue;
            }
            if (breakData.pos.equals(pos)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onReceivePacket(ReceivePacketEvent readPacketEvent) {
        if (readPacketEvent.getPacket() instanceof BlockBreakingProgressS2CPacket packet) {
            if (packet.getPos() == null) return;
            BreakData breakData = new BreakData(packet.getPos(), packet.getEntityId());
            if (breakMap.containsKey(packet.getEntityId()) && breakMap.get(packet.getEntityId()).pos.equals(packet.getPos())) {
                return;
            }
            if (breakData.getEntity() == null) {
                return;
            }
            if (MathHelper.sqrt((float) breakData.getEntity().getEyePos().squaredDistanceTo(packet.getPos().toCenterPos())) > 8) {
                return;
            }
            breakMap.put(packet.getEntityId(), breakData);
        }
    }

    public static class BreakData {
        public final BlockPos pos;
        public final int entityId;
        //public final FadeUtils fade;
        public final CacheTimer timer;
        public BreakData(BlockPos pos, int entityId) {
            this.pos = pos;
            this.entityId = entityId;
            //this.fade = new FadeUtils((long) BreakESP.INSTANCE.animationTime.getValue());
            this.timer = new CacheTimer();
        }

        public Entity getEntity() {
            if (MC.world == null) return null;
            Entity entity = MC.world.getEntityById(entityId);
            if (entity instanceof PlayerEntity) {
                return entity;
            }
            return null;
        }
    }
}
