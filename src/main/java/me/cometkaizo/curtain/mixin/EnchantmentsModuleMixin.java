package me.cometkaizo.curtain.mixin;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import me.cometkaizo.curtain.module.enchant.EnchantmentsModule;
import me.cometkaizo.curtain.module.enchant.JetstreamEnchantment;
import me.cometkaizo.curtain.util.Resettable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.world.item.enchantment.EnchantmentHelper.getTagEnchantmentLevel;

public final class EnchantmentsModuleMixin {

    @Mixin(Player.class)
    public static abstract class AttackInfusion extends LivingEntity {
        protected AttackInfusion(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
            super(pEntityType, pLevel);
        }

        @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getAttributeValue(Lnet/minecraft/world/entity/ai/attributes/Attribute;)D", ordinal = 1))
        protected void tryApplyInfusion(Entity target, CallbackInfo ci) {
            if (EnchantmentsModule.isNotEnabled()) return;
            if (!(target instanceof LivingEntity livingTarget)) return;

            var infusionEnchantment = EnchantmentsModule.INSTANCE.infusionEnchantment.get();
            boolean hasInfusion = getItemInHand(InteractionHand.MAIN_HAND).getEnchantmentLevel(infusionEnchantment) > 0;

            if (hasInfusion) {
                infusionEnchantment.transferEffects(this, livingTarget);
            }
        }

    }

    @Mixin(ThrownTrident.class)
    public static abstract class TridentJetstream extends AbstractArrow {
        @Shadow private ItemStack tridentItem;

        @Shadow private boolean dealtDamage;

        protected TridentJetstream(EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
            super(pEntityType, pLevel);
        }

        @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;tick()V"))
        protected void tickJetstream(CallbackInfo ci) {
            JetstreamEnchantment jetstreamEnchantment = EnchantmentsModule.INSTANCE.jetstreamEnchantment.get();
            int jetstreamLevel = tridentItem.getEnchantmentLevel(jetstreamEnchantment);
            if (jetstreamLevel > 0 && !dealtDamage) jetstreamEnchantment.tick((ThrownTrident)(Object)this, jetstreamLevel);
        }
    }

    @Mixin(AbstractArrow.class)
    public static abstract class TridentShockwave extends Projectile {

        @Shadow protected abstract ItemStack getPickupItem();

        protected TridentShockwave(EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
            super(pEntityType, pLevel);
        }

        @SuppressWarnings("ConstantConditions")
        @Inject(method = "onHitBlock", at = @At("TAIL"))
        protected void applyShockwave(BlockHitResult hitResult, CallbackInfo ci) {
            if (EnchantmentsModule.isNotEnabled()) return;
            if (!((Object)this instanceof ThrownTrident trident)) return;

            var shockwaveEnchantment = EnchantmentsModule.INSTANCE.shockwaveEnchantment.get();
            int shockwaveLevel = getTagEnchantmentLevel(shockwaveEnchantment, getPickupItem());
            if (shockwaveLevel <= 0) return;

            EnchantmentsModule.INSTANCE.shockwaveEnchantment.get()
                    .onTridentHit(trident, hitResult.getLocation(), shockwaveLevel);
        }
    }
    @Mixin(ThrownTrident.class)
    public static abstract class TridentShockwave2 extends AbstractArrow {
        protected TridentShockwave2(EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
            super(pEntityType, pLevel);
        }

        @Inject(method = "onHitEntity", at = @At("TAIL"))
        protected void applyShockwave(EntityHitResult hitResult, CallbackInfo ci) {
            if (EnchantmentsModule.isNotEnabled()) return;

            var shockwaveEnchantment = EnchantmentsModule.INSTANCE.shockwaveEnchantment.get();
            int shockwaveLevel = getTagEnchantmentLevel(shockwaveEnchantment, getPickupItem());
            if (shockwaveLevel <= 0) return;

            EnchantmentsModule.INSTANCE.shockwaveEnchantment.get()
                    .onTridentHit((ThrownTrident) (Object)this, hitResult.getLocation(), shockwaveLevel);
        }

    }

    @Mixin(Enchantment.class)
    public static abstract class EnchantmentCategoryMod {

        @SuppressWarnings("ConstantConditions")
        @Inject(method = "canApplyAtEnchantingTable", at = @At("TAIL"), cancellable = true, remap = false)
        protected void accountForModdedCategories(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            if (EnchantmentsModule.isNotEnabled()) return;
            cir.setReturnValue(cir.getReturnValueZ() || (Enchantment)(Object)this instanceof ArrowPiercingEnchantment &&
                    EnchantmentCategory.TRIDENT.canEnchant(stack.getItem()));
        }
    }

    @Mixin(ThrownTrident.class)
    public static abstract class TridentPiercing extends AbstractArrow implements Resettable {
        @Shadow private ItemStack tridentItem;
        @Shadow private boolean dealtDamage;
        @Unique @Nullable private IntOpenHashSet curtain$piercingIgnoreEntityIds;

        protected TridentPiercing(EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
            super(pEntityType, pLevel);
        }

        @Redirect(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ThrownTrident;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"))
        protected void tryPierce(ThrownTrident instance, Vec3 motion) {
            if (EnchantmentsModule.isNotEnabled() ||
                    curtain$getPiercingLevel() <= 0 ||
                    curtain$piercingIgnoreEntityIds != null &&
                            curtain$piercingIgnoreEntityIds.size() >= curtain$getPiercingLevel() + 1)
                instance.setDeltaMovement(motion);
        }

        @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ThrownTrident;getOwner()Lnet/minecraft/world/entity/Entity;"))
        protected void cancelDealtDamageIfPiercingEntity(CallbackInfo ci) {
            if (EnchantmentsModule.isNotEnabled()) return;
            if (inGroundTime > 4) return;

            var entitiesPierced = curtain$piercingIgnoreEntityIds;
            if (entitiesPierced == null || curtain$getPiercingLevel() + 1 > entitiesPierced.size()) {
                dealtDamage = false;
            }
        }

        @Inject(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ThrownTrident;getOwner()Lnet/minecraft/world/entity/Entity;"), cancellable = true)
        protected void updatePiercedEntities(EntityHitResult hitResult, CallbackInfo ci) {
            if (EnchantmentsModule.isNotEnabled()) return;

            if (curtain$getPiercingLevel() > 0) {
                if (this.curtain$piercingIgnoreEntityIds == null) {
                    this.curtain$piercingIgnoreEntityIds = new IntOpenHashSet(5);
                }

                if (this.curtain$piercingIgnoreEntityIds.size() >= curtain$getPiercingLevel() + 1) {
                    ci.cancel();
                    return;
                }

                this.curtain$piercingIgnoreEntityIds.add(hitResult.getEntity().getId());
            }
        }

        @Unique
        @Override
        public void reset() {
            if (this.curtain$piercingIgnoreEntityIds != null) {
                this.curtain$piercingIgnoreEntityIds.clear();
            }
        }

        @Unique
        private int curtain$getPiercingLevel() {
            return EnchantmentHelper.getTagEnchantmentLevel(Enchantments.PIERCING, tridentItem);
        }
    }

    @Mixin(AbstractArrow.class)
    public static abstract class Resetting {
        @Inject(method = "onHitBlock", at = @At("TAIL"))
        protected void reset(BlockHitResult pResult, CallbackInfo ci) {
            if (this instanceof Resettable resettable) {
                resettable.reset();
            }
        }
    }
}
