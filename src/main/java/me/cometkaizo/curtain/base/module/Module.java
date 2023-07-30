package me.cometkaizo.curtain.base.module;

public interface Module {

    String getName();
    String getNamespace();
    void init();
    void enable();
    void disable();
    boolean isEnabled();

}
