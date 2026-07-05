

/**
 * A class to represent a fake player.
 */
package net.payload.utils.entity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;

public class FakePlayerEntity extends AbstractClientPlayerEntity {

    public FakePlayerEntity() {
        super(MinecraftClient.getInstance().world, MinecraftClient.getInstance().player.getGameProfile());
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false);
        this.setPos(player.getPos().x, player.getPos().y, player.getPos().z);
        this.setRotation(player.getYaw(tickDelta), player.getPitch(tickDelta));
        //this.inventory = player.getInventory();
    }

    public void despawn() {
        this.remove(RemovalReason.DISCARDED);
    }
}
