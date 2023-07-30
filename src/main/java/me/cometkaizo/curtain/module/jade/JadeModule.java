package me.cometkaizo.curtain.module.jade;

import me.cometkaizo.curtain.base.module.AbstractModule;
import me.cometkaizo.curtain.base.module.InstanceHolder;
import me.cometkaizo.curtain.registries.CurtainItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;

public class JadeModule extends AbstractModule {
    @InstanceHolder
    public static JadeModule INSTANCE;

    public RegistryObject<Item> jadeItem, refinedJadeItem, jadeCoinItem;

    @Override
    public void init() {
        super.init();
        FMLJavaModLoadingContext.get().getModEventBus().register(this);

        jadeItem = CurtainItems.register("jade", JadeItem::new);
        refinedJadeItem = CurtainItems.register("refined_jade", RefinedJadeItem::new);
        jadeCoinItem = CurtainItems.register("jade_coin", JadeCoinItem::new);
    }

    @SubscribeEvent
    public void addItemsToTabs(BuildCreativeModeTabContentsEvent event) {
        if (!isInitialized()) throw new IllegalStateException();
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(jadeItem);
            event.accept(refinedJadeItem);
            event.accept(jadeCoinItem);
        }
    }

    public static boolean isNotEnabled() {
        return INSTANCE == null || !INSTANCE.isEnabled();
    }
}
