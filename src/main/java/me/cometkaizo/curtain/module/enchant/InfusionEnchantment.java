package me.cometkaizo.curtain.module.enchant;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class InfusionEnchantment extends Enchantment {
    public InfusionEnchantment() {
        this(Rarity.VERY_RARE, EnchantmentsModule.INSTANCE.meleeWeaponCategory, EquipmentSlot.MAINHAND);
    }
    public InfusionEnchantment(Rarity pRarity, EnchantmentCategory pCategory, EquipmentSlot... pApplicableSlots) {
        super(pRarity, pCategory, pApplicableSlots);
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    @Override
    public boolean isTradeable() {
        return false;
    }

    public void transferEffects(LivingEntity from, LivingEntity to) {
        var effects = from.getActiveEffects();
        var negativeEffects = effects.stream().filter(effect -> effect.getEffect().getCategory() == MobEffectCategory.HARMFUL).toList();

        for (var negativeEffect : negativeEffects) {
            from.removeEffect(negativeEffect.getEffect());
            to.addEffect(negativeEffect);
        }
    }

}
