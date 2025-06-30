/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.payload.event.events;

import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import net.payload.api.event.Cancelable;
import net.payload.event.listeners.AbstractListener;
import net.payload.event.listeners.PlayerMoveEventListener;

import java.util.ArrayList;
import java.util.List;

@Cancelable
public class PlayerMoveEvent extends AbstractEvent{
    private static final PlayerMoveEvent INSTANCE = new PlayerMoveEvent();

    public MovementType type;
    public Vec3d movement;

    private double x;
    private double y;
    private double z;

    public PlayerMoveEvent(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public PlayerMoveEvent() {

    }

    @Override
    public void Fire(ArrayList<? extends AbstractListener> listeners) {
        for (AbstractListener listener : List.copyOf(listeners)) {
            PlayerMoveEventListener playerMoveEventListener = (PlayerMoveEventListener) listener;
            playerMoveEventListener.onMove(this);
        }
    }


    @Override
    public Class<PlayerMoveEventListener> GetListenerClassType() {
        return PlayerMoveEventListener.class;
    }

    public static PlayerMoveEvent get(MovementType type, Vec3d movement) {
        INSTANCE.type = type;
        INSTANCE.movement = movement;
        return INSTANCE;
    }

    public double getX() {
        return this.x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return this.y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return this.z;
    }

    public void setZ(double z) {
        this.z = z;
    }
}


