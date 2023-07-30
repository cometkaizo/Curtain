package me.cometkaizo.curtain.base.module;

public abstract class AbstractModule implements Module {
    protected boolean enabled;
    private boolean initialized;

    public AbstractModule() {
        this(true);
    }

    public AbstractModule(boolean enabled) {
        this.enabled = enabled;
    }

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
    public void enable() {
        enabled = true;
    }

    @Override
    public void disable() {
        enabled = false;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
