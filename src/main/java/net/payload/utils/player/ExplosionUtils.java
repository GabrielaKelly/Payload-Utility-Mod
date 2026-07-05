package net.payload.utils.player;

import net.minecraft.entity.Entity;

import net.minecraft.util.math.Vec3d;

import static net.payload.PayloadClient.MC;


public class ExplosionUtils {

        public static double getDamageTo(Entity entity, Vec3d explosion) {
            return getDamageTo(entity, explosion, false);
        }

        public static double getDamageTo(Entity entity, Vec3d explosion, boolean ignoreTerrain) {
            return getDamageTo(entity, explosion, ignoreTerrain, 12.0F);
        }

        public static double getDamageTo(Entity entity, Vec3d explosion, boolean ignoreTerrain, float power) {
            // Calculate distance from explosion
            double distance = Math.sqrt(entity.squaredDistanceTo(explosion));


           /* // Calculate exposure factor
            double exposure = getExposure(explosion, entity, ignoreTerrain);

            */

            // Calculate damage falloff
            double falloff = distance / power;
            double impact = (1.0 - falloff);// * exposure;

            // Calculate base damage
            double damage = ((int)((impact * impact + impact) / 2.0 * 7.0 * 12.0 + 1.0));

            /*// Apply damage reduction
            damage = getReduction(
                    entity,
                    MC.world.getDifficulty().getId(null),
                    damage
            );

             */

            return Math.max(0.0, damage);
        }
    }
