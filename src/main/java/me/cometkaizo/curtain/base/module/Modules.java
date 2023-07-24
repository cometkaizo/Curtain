package me.cometkaizo.curtain.base.module;

import com.mojang.logging.LogUtils;
import me.cometkaizo.curtain.Main;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.function.Supplier;

public final class Modules {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<Module> MODULES_REGISTER = DeferredRegister.create(
            ResourceKey.createRegistryKey(new ResourceLocation(Main.MOD_ID, "modules")),
            Main.MOD_ID);
    public static final Supplier<IForgeRegistry<Module>> MODULES_SUP = MODULES_REGISTER.makeRegistry(RegistryBuilder::new);

    public static IForgeRegistry<Module> registry() {
        var registry = MODULES_SUP.get();
        if (registry == null) throw new IllegalStateException("Modules registry has not been created yet");
        return registry;
    }

    public static void register(IEventBus eventBus) {
        MODULES_REGISTER.register(eventBus);
    }

    public static void registerModules() {
        ModuleLoader loader = Main.newModuleLoader();
        Collection<Module> modules = loader.loadModules();
        for (Module m : modules) {
            if (m.isEnabled()) MODULES_REGISTER.register(m.getNamespace(), () -> m);
        }

        modules.forEach(Modules::initModule);
    }

    private static void initModule(Module module) {
        try {
            module.init();
            LOGGER.info("Loaded module '{}'", module.getName());
        } catch (Exception e) {
            logModuleLoadingError(module, "An exception occurred while initializing the module", e);
        }
    }

    private Modules() {
        throw new AssertionError();
    }

    public static void logModuleLoadingError(Class<? extends Module> moduleType, String message, Throwable e) {
        logModuleLoadingError(moduleType.getName(), message, e);
    }

    public static void logModuleLoadingError(String moduleTypeName, String message, Throwable e) {
        LOGGER.error("Failed to load module type '" + moduleTypeName + "': " + message, e);
    }

    public static void logModuleLoadingError(Module module, String message, Throwable e) {
        LOGGER.error("Failed to load module '" + module.getName() + "': " + message, e);
    }

    public static void logModuleLoadingWarning(Class<? extends Module> moduleType, String message) {
        LOGGER.warn("Loading module type '{}': {}", moduleType.getName(), message);
    }
}
