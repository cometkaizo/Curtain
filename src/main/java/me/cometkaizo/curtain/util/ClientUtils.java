package me.cometkaizo.curtain.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@SuppressWarnings("unused")
public class ClientUtils {
    public static Level getClientLevel() {
        return Minecraft.getInstance().level;
    }
    public static Screen getClientScreen() {
        return Minecraft.getInstance().screen;
    }
    public static Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    public static Minecraft getMinecraft() {
        return Minecraft.getInstance();
    }
}
