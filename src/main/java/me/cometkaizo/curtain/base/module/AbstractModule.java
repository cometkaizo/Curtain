package me.cometkaizo.curtain.base.module;

public abstract class AbstractModule implements Module {
    private boolean initialized;

    @Override
    public String getName() {
        return getClass().getSimpleName().replaceAll("Module$", "").replaceAll("(?<=.)([A-Z])", " $1");
    }

    @Override
    public String getNamespace() {
        return getName().toLowerCase().replaceAll(" ", "");
    }

    @Override
    public void init() {
        initialized = true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
