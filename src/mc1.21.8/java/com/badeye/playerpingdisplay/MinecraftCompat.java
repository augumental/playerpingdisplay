package com.badeye.playerpingdisplay;
import net.minecraft.client.gui.DrawContext;
final class MinecraftCompat {
    private MinecraftCompat() {}
    static void pushHud(DrawContext context, int x, int y, float scale) {
        var matrices = context.getMatrices();
        matrices.pushMatrix(); matrices.translate(x, y); matrices.scale(scale, scale);
    }
    static void popHud(DrawContext context) { context.getMatrices().popMatrix(); }
}
