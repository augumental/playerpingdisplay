package com.badeye.playerpingdisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.Overlay;
final class SmokeCompat {
    static Overlay overlay(Minecraft client) { return client.gui.overlay(); }
    static void screenshot(Minecraft client, String name) { Screenshot.grab(client.gameDirectory, name, client.gameRenderer.mainRenderTarget(), 1, text -> {}); }
}
