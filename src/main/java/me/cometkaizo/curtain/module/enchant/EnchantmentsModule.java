package me.cometkaizo.curtain.module.enchant;

import me.cometkaizo.curtain.base.module.AbstractModule;
import me.cometkaizo.curtain.base.module.InstanceHolder;
import me.cometkaizo.curtain.network.Packets;
import me.cometkaizo.curtain.registries.CurtainEnchantments;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.registries.RegistryObject;

public class EnchantmentsModule extends AbstractModule {
    @InstanceHolder
    public static EnchantmentsModule INSTANCE;
    public EnchantmentCategory meleeWeaponCategory;
    public RegistryObject<ShockwaveEnchantment> shockwaveEnchantment;
    public RegistryObject<InfusionEnchantment> infusionEnchantment;
    public RegistryObject<JetstreamEnchantment> jetstreamEnchantment;

    @Override
    public void init() {
        super.init();
        Packets.register(S2CApplyShockwave.class, S2CApplyShockwave::new, NetworkDirection.PLAY_TO_CLIENT);

        meleeWeaponCategory = EnchantmentCategory.create("MELEE_WEAPON", item ->
                item instanceof SwordItem || item instanceof AxeItem);

        shockwaveEnchantment = CurtainEnchantments.register("shockwave", ShockwaveEnchantment::new);
        infusionEnchantment = CurtainEnchantments.register("infusion", InfusionEnchantment::new);
        jetstreamEnchantment = CurtainEnchantments.register("jetstream", JetstreamEnchantment::new);
    }

    public static boolean isNotEnabled() {
        return INSTANCE == null || !INSTANCE.isEnabled();
    }
}
