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
import java.util.*;

import static me.cometkaizo.curtain.base.module.Modules.*;

public class DefaultModuleLoader implements ModuleLoader {
    protected final String packageName;
    protected final boolean setInstanceOnLoad;

    public DefaultModuleLoader(String packageName, boolean setInstanceOnLoad) {
        this.packageName = packageName;
        this.setInstanceOnLoad = setInstanceOnLoad;
    }

    @Override
    public Set<Module> loadModules() {
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

        return modules;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    private Collection<? extends Class<? extends Module>> getModuleTypes() {
        ModFileScanData scanData = ModList.get().getModFileById(Main.MOD_ID).getFile().getScanResult();
        return scanData.getClasses().stream()
                .map(ModFileScanData.ClassData::clazz)
                .map(this::toClass)
                .filter(Objects::nonNull)
                .filter(Module.class::isAssignableFrom)
                .map(c -> (Class<? extends Module>) c)
                .toList();
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
                    logModuleLoadingWarning(moduleType, "Field '" + field.getName() + "' is annotated with " + InstanceHolder.class.getSimpleName() + "' but is final");
                } else if (Modifier.isPrivate(field.getModifiers())) {
                    logModuleLoadingWarning(moduleType, "Field '" + field.getName() + "' is annotated with " + InstanceHolder.class.getSimpleName() + "' but is private");
                } else if (!Modifier.isStatic(field.getModifiers())) {
                    logModuleLoadingWarning(moduleType, "Field '" + field.getName() + "' is annotated with " + InstanceHolder.class.getSimpleName() + "' but is not static");
                } else if (!field.getType().isAssignableFrom(moduleType)) {
                    logModuleLoadingWarning(moduleType, "Field '" + field.getName() + "' is annotated with " + InstanceHolder.class.getSimpleName() + "' but of the incorrect type");
                } else return field;
            }
        }

        return null;
    }

    private void trySetInstanceField(Module module, Field field) {
        try {
            FieldUtils.writeField(field, (Object) null, module, false);
        } catch (IllegalAccessException e) {
            logModuleLoadingWarning(module.getClass(), "Field '" + field.getName() + "' is annotated with " + InstanceHolder.class.getSimpleName() + "' but is not accessible");
        }
    }

    private Constructor<? extends Module> getConstructor(Class<? extends Module> moduleType) {
        if (moduleType.isInterface() || Modifier.isAbstract(moduleType.getModifiers())) return null;
        try {
            return moduleType.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            logModuleLoadingError(moduleType, "Could not find no-arg constructor", e);
            return null;
        }
    }

    private Module createInstance(Constructor<? extends Module> constructor) {
        try {
            return constructor.newInstance();
        } catch (InstantiationException e) {
            logModuleLoadingError(constructor.getDeclaringClass(), "Could not instantiate the object", e);
        } catch (IllegalAccessException e) {
            logModuleLoadingError(constructor.getDeclaringClass(), "Could not access the constructor", e);
        } catch (InvocationTargetException e) {
            logModuleLoadingError(constructor.getDeclaringClass(), "An exception occurred while instantiating the object", e);
        }
        return null;
    }
}
