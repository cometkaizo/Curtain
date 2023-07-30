package me.cometkaizo.curtain.util;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("unused")
public final class PhysicsUtils {

    // Returns a Vec3 based on start and end coordinates, and speed
    // normalize the vector between start and end and then multiply by speed
    public static Vec3 getVelocityTowards(Vec3 start, Vec3 end, double speed) {
        return end.subtract(start)
                .normalize()
                .scale(speed);
    }

    public static Vec3 getClosestVector(Vec3 a, Vec3 b, Vec3 target) {
        return b == null || (a != null && a.distanceToSqr(target) < b.distanceToSqr(target)) ? a : b;
    }

    public static BlockState getBlockUnder(Entity entity) {
        return entity.level().getBlockState(new BlockPos(Mth.floor(entity.getX()), Mth.floor(entity.getY() - 0.5000001D), Mth.floor(entity.getZ())));
    }

    public static boolean isInRain(Player player) {
        BlockPos blockpos = player.blockPosition();
        return player.level().isRainingAt(blockpos) ||
                player.level().isRainingAt(new BlockPos(blockpos.getX(), (int) player.getBoundingBox().maxY, blockpos.getZ()));
    }

    private PhysicsUtils() {
        throw new AssertionError("No PhysicsUtil instances for you!");
    }

}
