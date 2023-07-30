package me.cometkaizo.curtain.base.module;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface ModuleCreator {

    @NotNull Set<@NotNull Module> createModules(ModuleLoader loader);

}
