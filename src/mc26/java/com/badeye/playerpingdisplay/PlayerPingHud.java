package com.badeye.playerpingdisplay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.regex.Pattern;

final class PlayerPingHud {
    private static final Pattern TOKENS = Pattern.compile("%player%|%ping%");
    private PlayerPingHud() {}

    static void render(GuiGraphicsExtractor context, PlayerPingDisplayClient.TrackedPlayer player) {
        if (player == null || MinecraftCompat.isHudHidden(Minecraft.getInstance())) return;
        draw(context, player.playerName(), player.pingText(), player.pingColor(), player.opacity(),
                context.guiWidth(), context.guiHeight());
    }

    static Bounds renderPreview(GuiGraphicsExtractor context, int width, int height) {
        return draw(context, "Steve", "42", 0x55FF55, 1, width, height);
    }

    static Component formattedText(String player, String ping, int automaticColor) {
        var font = new FontDescription.Resource(Identifier.fromNamespaceAndPath("minecraft", PlayerPingDisplayConfig.font.resource));
        int color = PlayerPingDisplayConfig.automaticPingColor ? automaticColor : PlayerPingDisplayConfig.pingColor;
        String format = PlayerPingDisplayConfig.normalizedFormat(PlayerPingDisplayConfig.displayFormat);
        MutableComponent text = Component.empty();
        var matcher = TOKENS.matcher(format);
        int end = 0;
        while (matcher.find()) {
            text.append(Component.literal(format.substring(end, matcher.start())).withStyle(s -> s.withFont(font).withColor(0xFFFFFF)));
            boolean isPing = matcher.group().equals("%ping%");
            text.append(Component.literal(isPing ? ping : player).withStyle(s -> s.withFont(font).withColor(isPing ? color : 0xFFFFFF)));
            end = matcher.end();
        }
        return text.append(Component.literal(format.substring(end)).withStyle(s -> s.withFont(font).withColor(0xFFFFFF)));
    }

    static Bounds previewBounds(int screenWidth, int screenHeight) {
        return bounds(formattedText("Steve", "42", 0x55FF55), screenWidth, screenHeight);
    }

    private static Bounds bounds(Component text, int screenWidth, int screenHeight) {
        var renderer = Minecraft.getInstance().font;
        int width = (int) Math.ceil((renderer.width(text) + PlayerPingDisplayConfig.paddingX * 2) * PlayerPingDisplayConfig.hudScale);
        int height = (int) Math.ceil((renderer.lineHeight + PlayerPingDisplayConfig.paddingY * 2) * PlayerPingDisplayConfig.hudScale);
        int availableX = Math.max(0, screenWidth - width), availableY = Math.max(0, screenHeight - height);
        int x = PlayerPingDisplayConfig.hudX == null
                ? PlayerPingDisplayConfig.anchor.resolveX(screenWidth, width, PlayerPingDisplayConfig.xOffset)
                : (int) Math.round(PlayerPingDisplayConfig.hudX * availableX);
        int y = PlayerPingDisplayConfig.hudY == null
                ? PlayerPingDisplayConfig.anchor.resolveY(screenHeight, height, PlayerPingDisplayConfig.yOffset)
                : (int) Math.round(PlayerPingDisplayConfig.hudY * availableY);
        return new Bounds(PlayerPingDisplayConfig.clamp(x, 0, availableX), PlayerPingDisplayConfig.clamp(y, 0, availableY), width, height);
    }

    private static Bounds draw(GuiGraphicsExtractor context, String player, String ping, int automaticColor, float opacity, int screenWidth, int screenHeight) {
        Component text = formattedText(player, ping, automaticColor);
        Bounds bounds = bounds(text, screenWidth, screenHeight);
        var renderer = Minecraft.getInstance().font;
        int width = renderer.width(text) + PlayerPingDisplayConfig.paddingX * 2;
        int height = renderer.lineHeight + PlayerPingDisplayConfig.paddingY * 2;
        int alpha = Math.round(255 * opacity);
        var matrices = context.pose();
        matrices.pushMatrix();
        matrices.translate(bounds.x(), bounds.y());
        matrices.scale((float) PlayerPingDisplayConfig.hudScale, (float) PlayerPingDisplayConfig.hudScale);
        if (PlayerPingDisplayConfig.backgroundEnabled) {
            int backgroundAlpha = alpha * PlayerPingDisplayConfig.backgroundOpacity / 100;
            roundedFill(context, width, height, PlayerPingDisplayConfig.cornerRadius,
                    (backgroundAlpha << 24) | PlayerPingDisplayConfig.backgroundColor);
        }
        context.text(renderer, text, PlayerPingDisplayConfig.paddingX, PlayerPingDisplayConfig.paddingY,
                (alpha << 24) | 0xFFFFFF, PlayerPingDisplayConfig.textShadow);
        matrices.popMatrix();
        return bounds;
    }

    private static void roundedFill(GuiGraphicsExtractor context, int width, int height, int radius, int color) {
        int r = Math.min(radius, Math.min(width, height) / 2);
        if (r == 0) { context.fill(0, 0, width, height, color); return; }
        for (int y = 0; y < height; y++) {
            int edge = Math.min(y, height - y - 1);
            int inset = edge >= r ? 0 : (int) Math.ceil(r - Math.sqrt(r * r - Math.pow(r - edge - 0.5, 2)));
            context.fill(inset, y, width - inset, y + 1, color);
        }
    }

    record Bounds(int x, int y, int width, int height) {
        boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        }
    }
}
