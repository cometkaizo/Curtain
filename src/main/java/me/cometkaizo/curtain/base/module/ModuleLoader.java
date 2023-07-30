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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class ModuleLoader {
    public static final Logger LOGGER = LogUtils.getLogger();
    public final DeferredRegister<Module> modulesRegister = DeferredRegister.create(
            ResourceKey.createRegistryKey(new ResourceLocation(Main.MOD_ID, "modules")),
            Main.MOD_ID);
    public final Supplier<IForgeRegistry<Module>> modulesSup = modulesRegister.makeRegistry(RegistryBuilder::new);
    protected List<Diagnostic> problems;
    protected final Supplier<ModuleCreator> creatorSup;

    public ModuleLoader(Supplier<ModuleCreator> creatorSup) {
        this.creatorSup = creatorSup;
    }

    public void register(IEventBus eventBus) {
        modulesRegister.register(eventBus);
    }

    public List<Diagnostic> registerModules() {
        problems = new ArrayList<>(1);
        ModuleCreator creator = creatorSup.get();
        Collection<Module> modules = creator.createModules(this);
        for (Module m : modules) {
            if (m.isEnabled()) modulesRegister.register(m.getNamespace(), () -> m);
        }

        int modulesInitialized = 0;
        for (Module module : modules) {
            boolean success = initModule(module);
            if (success) modulesInitialized ++;
        }

        LOGGER.info("Successfully initialized {} modules out of {}", modulesInitialized, modules.size());
        return problems;
    }

    private boolean initModule(Module module) {
        try {
            module.init();
            LOGGER.info("Initialized module '{}'", module.getName());
            return true;
        } catch (Exception e) {
            module.disable();
            LOGGER.error("Could not initialize module '{}'; disabled", module.getName());
            addDiagnostic(new Error(module, "An exception occurred while initializing the module; module has been disabled", e));
            return false;
        }
    }

    public IForgeRegistry<Module> registry() {
        var registry = modulesSup.get();
        if (registry == null) throw new IllegalStateException("Modules registry has not been created yet");
        return registry;
    }

    public interface Diagnostic {
        void log(Logger logger);
    }
    public record Error(String moduleTypeName, String message, Throwable cause) implements Diagnostic {
        public Error(Class<? extends Module> moduleType, String message, Throwable e) {
            this(moduleType.getName(), message, e);
        }
        public Error(Module module, String message, Throwable e) {
            this(module.getClass(), message, e);
        }
        @Override
        public void log(Logger logger) {
            if (cause != null) LOGGER.error("Failed to load module type '" + moduleTypeName + "': " + message, cause);
            else LOGGER.error("Failed to load module type '{}': {}", moduleTypeName, message);
        }
    }
    public record Warning(String moduleTypeName, String message, Throwable cause) implements Diagnostic {
        public Warning(Class<? extends Module> moduleType, String message, Throwable e) {
            this(moduleType.getName(), message, e);
        }
        public Warning(Module module, String message, Throwable e) {
            this(module.getClass(), message, e);
        }
        @Override
        public void log(Logger logger) {
            if (cause != null) LOGGER.warn("Warning while loading module type '" + moduleTypeName + "': " + message, cause);
            else LOGGER.warn("Warning while loading module type '{}': {}", moduleTypeName, message);
        }
    }

    public void addDiagnostic(Diagnostic diagnostic) {
        if (problems != null) problems.add(diagnostic);
    }
}
