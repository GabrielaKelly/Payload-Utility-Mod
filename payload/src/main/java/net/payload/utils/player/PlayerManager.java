package net.payload.utils.player;

import net.minecraft.block.Blocks;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.payload.Payload;
import net.payload.event.events.TickEvent;
import net.payload.event.listeners.TickListener;
import net.payload.module.Module;
import net.payload.utils.block.BlockPosX;
import net.payload.utils.player.combat.EntityUtil;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static net.payload.PayloadClient.MC;

public class PlayerManager implements TickListener {

    public Map<PlayerEntity, EntityAttribute> map = new ConcurrentHashMap<>();
    public CopyOnWriteArrayList<PlayerEntity> inWebPlayers = new CopyOnWriteArrayList<>();
    public boolean insideBlock = false;

    public PlayerManager() {
        Payload.getInstance().eventManager.AddListener(TickListener.class, this);
    }

    public void onUpdate() {
        if (Module.nullCheck()) return;
        inWebPlayers.clear();
        insideBlock = EntityUtil.isInsideBlock();
        for (PlayerEntity player : new ArrayList<>(MC.world.getPlayers())) {
            map.put(player, new EntityAttribute(player.getArmor(), player.getAttributeValue(EntityAttributes.ARMOR_TOUGHNESS)));
            webUpdate(player);
        }
    }

    public boolean getInsideBlock() {
        return insideBlock;
    }

    public boolean isInWeb(PlayerEntity player) {
        return inWebPlayers.contains(player);
    }
    private void webUpdate(PlayerEntity player) {
        for (float x : new float[]{0, 0.3F, -0.3f}) {
            for (float z : new float[]{0, 0.3F, -0.3f}) {
                for (int y : new int[]{-1, 0, 1, 2}) {
                    BlockPos pos = new BlockPosX(player.getX() + x, player.getY(), player.getZ() + z).up(y);
                    if (new Box(pos).intersects(player.getBoundingBox()) && MC.world.getBlockState(pos).getBlock() == Blocks.COBWEB) {
                        inWebPlayers.add(player);
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void onTick(TickEvent.Pre event) {
        onUpdate();
    }

    @Override
    public void onTick(TickEvent.Post event) {

    }

    public record EntityAttribute(int armor, double toughness) {
    }
}
