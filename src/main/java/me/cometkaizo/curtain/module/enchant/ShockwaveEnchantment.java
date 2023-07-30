package me.cometkaizo.curtain.module.enchant;

import me.cometkaizo.curtain.network.Packets;
import me.cometkaizo.curtain.util.PhysicsUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

public class ShockwaveEnchantment extends Enchantment {
    public ShockwaveEnchantment() {
        this(Rarity.VERY_RARE, EnchantmentCategory.TRIDENT, EquipmentSlot.MAINHAND);
    }
    public ShockwaveEnchantment(Rarity pRarity, EnchantmentCategory pCategory, EquipmentSlot... pApplicableSlots) {
        super(pRarity, pCategory, pApplicableSlots);
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    public double getShockwaveRadius(int shockwaveLevel) {
        return switch (shockwaveLevel) {
            case 0 -> 0;
            case 1 -> 3;
            case 2 -> 4.5;
            default -> 6;
        };
    }

    /**
     * Gets the shockwave push speed when the entity is at the same position as the trident
     * @param shockwaveLevel the shockwave enchantment level
     * @return the max shockwave push speed for the given enchantment level
     */
    public double getMaxShockwaveSpeed(int shockwaveLevel) {
        return switch (shockwaveLevel) {
            case 0 -> 0;
            case 1 -> 0.65;
            case 2 -> 1;
            default -> 1.5;
        };
    }

    @Override
    public boolean isTradeable() {
        return false;
    }

    public void onTridentHit(ThrownTrident trident, Vec3 hitPos, int shockwaveLevel) {
        if (trident.level() instanceof ServerLevel) {
            Packets.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CApplyShockwave(trident, hitPos, shockwaveLevel));
        }

        double shockwaveRadius = getShockwaveRadius(shockwaveLevel);
        double shockwaveRadiusSqr = shockwaveRadius * shockwaveRadius;
        double maxShockwaveSpeed = getMaxShockwaveSpeed(shockwaveLevel);
        AABB shockwaveArea = new AABB(BlockPos.containing(hitPos)).inflate(shockwaveRadius);
        var nearbyEntities = trident.level().getEntities(trident, shockwaveArea);

        for (var entity : nearbyEntities) {
            Vec3 entityPos = entity.position();
            Vec3 tridentPosition = trident.position();

            double speed = (1 - entityPos.distanceToSqr(tridentPosition) / shockwaveRadiusSqr) * maxShockwaveSpeed;
            Vec3 motion = PhysicsUtils.getVelocityTowards(tridentPosition, entityPos, speed);
            Vec3 lift = new Vec3(0, maxShockwaveSpeed * 0.5, 0);
            entity.addDeltaMovement(motion.add(lift));
        }
    }
}
