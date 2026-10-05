package com.badeye.playerpingdisplay;
import net.minecraft.client.gui.DrawContext;
final class MinecraftCompat {
    private MinecraftCompat() {}
    static void pushHud(DrawContext context, int x, int y, float scale) {
        var matrices = context.getMatrices();
        matrices.push(); matrices.translate(x, y, 0); matrices.scale(scale, scale, 1);
    }
    static void popHud(DrawContext context) { context.getMatrices().pop(); }
}
