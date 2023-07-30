package me.cometkaizo.curtain.module.enchant;

import me.cometkaizo.curtain.util.CollectionUtils;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class JetstreamEnchantment extends Enchantment {
    //public static final EntityDataAccessor<Byte> ID_JETSTREAM = SynchedEntityData.defineId(ThrownTrident.class, EntityDataSerializers.BYTE);
    protected double curveTargetRangeRadius = 4;

    public JetstreamEnchantment() {
        this(Rarity.RARE, EnchantmentCategory.TRIDENT, EquipmentSlot.MAINHAND);
    }
    public JetstreamEnchantment(Rarity pRarity, EnchantmentCategory pCategory, EquipmentSlot... pApplicableSlots) {
        super(pRarity, pCategory, pApplicableSlots);
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

    public void tick(ThrownTrident trident, int level) {
        if (trident.isNoPhysics()) return;
        Vec3 travelDirection = trident.getDeltaMovement().normalize();
        AABB curveTargetRange = trident.getBoundingBox()
                .inflate(curveTargetRangeRadius)
                .move(travelDirection.scale(curveTargetRangeRadius / 2));

        // TODO: 2023-07-30 fix this

        var curveTargets = trident.level().getEntities(trident, curveTargetRange, e -> e instanceof LivingEntity && e != trident.getOwner());
        var closestTarget = CollectionUtils.findMin(curveTargets, trident::distanceToSqr);
        if (closestTarget == null) return;

        Vec3 targetDirection = closestTarget.position().subtract(trident.position()).normalize();
        trident.addDeltaMovement(targetDirection.scale(getBoostScale(level)));
    }

    public double getBoostScale(int level) {
        return switch (level) {
            case 1 -> 0.3;
            case 2 -> 0.5;
            default -> 0;
        };
    }
}
