package com.badeye.playerpingdisplay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ScreenshotRecorder;
final class SmokeCompat {
    static void screenshot(MinecraftClient client, String name) { ScreenshotRecorder.saveScreenshot(client.runDirectory, name, client.getFramebuffer(), 1, text -> {}); }
}
