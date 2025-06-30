/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.payload.event.events;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.payload.event.listeners.AbstractListener;
import net.payload.event.listeners.LivingEntityMoveEventListener;

import java.util.ArrayList;
import java.util.List;

public class LivingEntityMoveEvent extends AbstractEvent{
    private static final LivingEntityMoveEvent INSTANCE = new LivingEntityMoveEvent();

    public LivingEntity entity;
    public Vec3d movement;

    @Override
    public void Fire(ArrayList<? extends AbstractListener> listeners) {
        for (AbstractListener listener : List.copyOf(listeners)) {
            LivingEntityMoveEventListener livingEntityMoveEventListener = (LivingEntityMoveEventListener) listener;
            livingEntityMoveEventListener.onEntityMove(this);
        }
    }

    @Override
    public Class<LivingEntityMoveEventListener> GetListenerClassType() {
        return LivingEntityMoveEventListener.class;
    }

    public static LivingEntityMoveEvent get(LivingEntity entity, Vec3d movement) {
        INSTANCE.entity = entity;
        INSTANCE.movement = movement;
        return INSTANCE;
    }
}


