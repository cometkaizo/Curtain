package me.cometkaizo.curtain;

import com.mojang.logging.LogUtils;
import me.cometkaizo.curtain.base.module.DefaultModuleLoader;
import me.cometkaizo.curtain.base.module.ModuleLoader;
import me.cometkaizo.curtain.base.module.Modules;
import me.cometkaizo.curtain.item.CurtainItems;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Main.MOD_ID)
public class Main {
    public static final String MOD_ID = "ck_curtain";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Main() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        Modules.register(eventBus);
        CurtainItems.register(eventBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        Modules.registerModules();
    }

    public static ModuleLoader newModuleLoader() {
        return new DefaultModuleLoader("me.cometkaizo.curtain", true);
    }
}
