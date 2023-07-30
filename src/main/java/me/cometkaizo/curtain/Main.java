package me.cometkaizo.curtain;

import com.mojang.logging.LogUtils;
import me.cometkaizo.curtain.base.module.DefaultModuleCreator;
import me.cometkaizo.curtain.base.module.ModuleCreator;
import me.cometkaizo.curtain.base.module.ModuleLoader;
import me.cometkaizo.curtain.network.Packets;
import me.cometkaizo.curtain.registries.CurtainEnchantments;
import me.cometkaizo.curtain.registries.CurtainItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.List;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Main.MOD_ID)
public class Main {
    public static final String MOD_ID = "ck_curtain";
    public static final String MIXIN_PACKAGE = "me.cometkaizo.curtain.mixin";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static ModuleLoader moduleLoader;
    private final List<ModuleLoader.Diagnostic> moduleProblems;

    public Main() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        moduleLoader = newModuleLoader();
        moduleLoader.register(eventBus);
        Packets.init();
        CurtainItems.register(eventBus);
        CurtainEnchantments.register(eventBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        moduleProblems = moduleLoader.registerModules();
        logModuleProblems();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStart(ServerStartedEvent event) {
        logModuleProblems();
    }

    private void logModuleProblems() {
        if (moduleProblems != null) {
            if (!moduleProblems.isEmpty()) {
                LOGGER.info("Server started with the following module problems: ");
                moduleProblems.forEach(diagnostic -> diagnostic.log(LOGGER));
            }
        }
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (moduleProblems == null || moduleProblems.isEmpty()) return;
        sendDebugMessage(event.getEntity(), Component.literal("Curtain: not all modules loaded successfully").withStyle(ChatFormatting.RED));
    }

    public static void sendDebugMessage(Player player, Component message) {
        String playerName = player.getName().getString();
        if ("CometKaizo".equals(playerName) || "Dev".equals(playerName)) {
            player.sendSystemMessage(message);
        }
    }

    public ModuleLoader newModuleLoader() {
        return new ModuleLoader(this::newModuleCreator);
    }

    public ModuleCreator newModuleCreator() {
        return new DefaultModuleCreator.Builder().ignoreFilesStartingWith(MIXIN_PACKAGE).build();
    }
}
