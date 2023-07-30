package me.cometkaizo.curtain.base.module;

import me.cometkaizo.curtain.Main;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class DefaultModuleCreator implements ModuleCreator {
    protected final Set<String> ignoredFilePrefixes;
    protected final boolean setInstanceOnLoad;
    protected ModuleLoader loader;

    public DefaultModuleCreator(Set<String> ignoredFilePrefixes, boolean setInstanceOnLoad) {
        this.ignoredFilePrefixes = ignoredFilePrefixes;
        this.setInstanceOnLoad = setInstanceOnLoad;
    }

    @Override
    public @NotNull Set<@NotNull Module> createModules(ModuleLoader loader) {
        this.loader = loader;
        var moduleTypes = getModuleTypes();
        Set<Module> modules = new HashSet<>(moduleTypes.size());

        for (var moduleType : moduleTypes) {
            var constructor = getConstructor(moduleType);
            if (constructor == null) continue;

            Module module = createInstance(constructor);
            if (module != null) {
                modules.add(module);
                tryAddInstanceToClass(module);
            }
        }

        this.loader = null;
        return modules;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    private Collection<? extends Class<? extends Module>> getModuleTypes() {
        ModFileScanData scanData = ModList.get().getModFileById(Main.MOD_ID).getFile().getScanResult();
        return scanData.getClasses().stream()
                .map(ModFileScanData.ClassData::clazz)
                .filter(this::isNotIgnored)
                .map(this::toClass)
                .filter(Objects::nonNull)
                .filter(Module.class::isAssignableFrom)
                .map(c -> (Class<? extends Module>) c)
                .toList();
    }

    private boolean isNotIgnored(Type type) {
        String className = type.getClassName();
        if (className == null) return false;
        for (String ignoredFilePrefix : ignoredFilePrefixes) {
            if (className.startsWith(ignoredFilePrefix)) return false;
        }
        return true;
    }

    private Class<?> toClass(Type type) {
        try {
            return type == null ? null : Class.forName(type.getClassName());
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private void tryAddInstanceToClass(Module module) {
        if (!setInstanceOnLoad) return;
        Class<? extends Module> moduleType = module.getClass();

        Field instanceField = tryGetInstanceField(moduleType);
        if (instanceField != null) {
            trySetInstanceField(module, instanceField);
        }
    }

    private Field tryGetInstanceField(Class<? extends Module> moduleType) {
        Field[] fields = moduleType.getDeclaredFields();

        for (Field field : fields) {
            Annotation[] annotations = field.getDeclaredAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation.annotationType() != InstanceHolder.class) continue;
                if (Modifier.isFinal(field.getModifiers())) {
                    loader.addDiagnostic(new ModuleLoader.Warning(moduleType, "Field '" + field.getName() + "' is annotated with " + InstanceHolder.class.getSimpleName() + "' but is final", null));
                } else if (Modifier.isPrivate(field.getModifiers())) {
                    loader.addDiagnostic(new ModuleLoader.Warning(moduleType, "Field '" + field.getName() + "' is annotated with " + InstanceHolder.class.getSimpleName() + "' but is private", null));
                } else if (!Modifier.isStatic(field.getModifiers())) {
                    loader.addDiagnostic(new ModuleLoader.Warning(moduleType, "Field '" + field.getName() + "' is annotated with " + InstanceHolder.class.getSimpleName() + "' but is not static", null));
                } else if (!field.getType().isAssignableFrom(moduleType)) {
                    loader.addDiagnostic(new ModuleLoader.Warning(moduleType, "Field '" + field.getName() + "' is annotated with " + InstanceHolder.class.getSimpleName() + "' but of the incorrect type", null));
                } else return field;
            }
        }

        return null;
    }

    private void trySetInstanceField(Module module, Field field) {
        try {
            FieldUtils.writeField(field, (Object) null, module, false);
        } catch (IllegalAccessException e) {
            loader.addDiagnostic(new ModuleLoader.Warning(module.getClass(), "Field '" + field.getName() + "' is annotated with " + InstanceHolder.class.getSimpleName() + "' but is not accessible", null));
        }
    }

    private Constructor<? extends Module> getConstructor(Class<? extends Module> moduleType) {
        if (moduleType.isInterface() || Modifier.isAbstract(moduleType.getModifiers())) return null;
        try {
            return moduleType.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            loader.addDiagnostic(new ModuleLoader.Error(moduleType, "Could not find no-arg constructor", e));
            return null;
        }
    }

    private Module createInstance(Constructor<? extends Module> constructor) {
        try {
            return constructor.newInstance();
        } catch (InstantiationException e) {
            loader.addDiagnostic(new ModuleLoader.Error(constructor.getDeclaringClass(), "Could not instantiate the object", e));
        } catch (IllegalAccessException e) {
            loader.addDiagnostic(new ModuleLoader.Error(constructor.getDeclaringClass(), "Could not access the constructor", e));
        } catch (InvocationTargetException e) {
            loader.addDiagnostic(new ModuleLoader.Error(constructor.getDeclaringClass(), "An exception occurred while instantiating the object", e));
        }
        return null;
    }

    public static class Builder {
        protected final Set<String> ignoredFilePrefixes = new HashSet<>(1);
        protected boolean setInstancesOnLoad = true;
        public Builder setInstancesOnLoad(boolean setInstancesOnLoad) {
            this.setInstancesOnLoad = setInstancesOnLoad;
            return this;
        }

        public Builder ignoreFilesStartingWith(String fileName) {
            ignoredFilePrefixes.add(fileName);
            return this;
        }

        public DefaultModuleCreator build() {
            return new DefaultModuleCreator(ignoredFilePrefixes, setInstancesOnLoad);
        }
    }

}
