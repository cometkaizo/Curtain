package me.cometkaizo.curtain.base.module;

public interface Module {

    String getName();
    String getNamespace();
    void init();
    boolean isEnabled();

}
