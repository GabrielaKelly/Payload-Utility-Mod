package net.payload.utils.block;


import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.payload.Payload;

import static net.payload.PayloadClient.MC;

public class MiningData {
    private final BlockPos position;
    private final Direction direction;
    private float blockDamage;
    private boolean instantRemine;
    private boolean started;

    public MiningData(BlockPos position, Direction direction) {
        this.position = position;
        this.direction = direction;
    }

    public boolean isInstantRemine() {
        return this.instantRemine;
    }

    public void setInstantRemine() {
        this.instantRemine = true;
    }

    public float damage(float damage) {
        this.blockDamage += damage;
        return this.blockDamage;
    }

    public void setDamage(float blockDamage) {
        this.blockDamage = blockDamage;
    }

    public void resetDamage() {
        this.instantRemine = false;
        this.blockDamage = 0.0F;
    }

    public BlockPos getPos() {
        return this.position;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public int getSlot() {
        return Payload.getInstance().moduleManager.autoTool.getOutsideTool(this.getPos());
       // return Modules.AUTO_TOOL.getBestToolNoFallback(this.getState());
    }

    public BlockState getState() {
        return MC.world.getBlockState(this.position);
        //return Globals.mc.world.getBlockState(this.position);
    }

    public boolean isStarted() {
        return this.started;
    }

    public void setStarted() {
        this.started = true;
    }

    public float getBlockDamage() {
        return this.blockDamage;
    }
}