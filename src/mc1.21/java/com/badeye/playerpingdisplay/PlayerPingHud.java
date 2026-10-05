package com.badeye.playerpingdisplay;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.regex.Pattern;

final class PlayerPingHud {
    private static final Pattern TOKENS = Pattern.compile("%player%|%ping%");
    private PlayerPingHud() {}

    static void render(DrawContext context, PlayerPingDisplayClient.TrackedPlayer player) {
        if (player == null || MinecraftClient.getInstance().options.hudHidden) return;
        draw(context, player.playerName(), player.pingText(), player.pingColor(), player.opacity(),
                context.getScaledWindowWidth(), context.getScaledWindowHeight());
    }

    static Bounds renderPreview(DrawContext context, int width, int height) {
        return draw(context, "Steve", "42", 0x55FF55, 1, width, height);
    }

    static Text formattedText(String player, String ping, int automaticColor) {
        var font = Identifier.of("minecraft", PlayerPingDisplayConfig.font.resource);
        int color = PlayerPingDisplayConfig.automaticPingColor ? automaticColor : PlayerPingDisplayConfig.pingColor;
        String format = PlayerPingDisplayConfig.normalizedFormat(PlayerPingDisplayConfig.displayFormat);
        MutableText text = Text.empty();
        var matcher = TOKENS.matcher(format);
        int end = 0;
        while (matcher.find()) {
            text.append(Text.literal(format.substring(end, matcher.start())).styled(s -> s.withFont(font).withColor(0xFFFFFF)));
            boolean isPing = matcher.group().equals("%ping%");
            text.append(Text.literal(isPing ? ping : player).styled(s -> s.withFont(font).withColor(isPing ? color : 0xFFFFFF)));
            end = matcher.end();
        }
        return text.append(Text.literal(format.substring(end)).styled(s -> s.withFont(font).withColor(0xFFFFFF)));
    }

    static Bounds previewBounds(int screenWidth, int screenHeight) {
        return bounds(formattedText("Steve", "42", 0x55FF55), screenWidth, screenHeight);
    }

    private static Bounds bounds(Text text, int screenWidth, int screenHeight) {
        var renderer = MinecraftClient.getInstance().textRenderer;
        int width = (int) Math.ceil((renderer.getWidth(text) + PlayerPingDisplayConfig.paddingX * 2) * PlayerPingDisplayConfig.hudScale);
        int height = (int) Math.ceil((renderer.fontHeight + PlayerPingDisplayConfig.paddingY * 2) * PlayerPingDisplayConfig.hudScale);
        int availableX = Math.max(0, screenWidth - width), availableY = Math.max(0, screenHeight - height);
        int x = PlayerPingDisplayConfig.hudX == null
                ? PlayerPingDisplayConfig.anchor.resolveX(screenWidth, width, PlayerPingDisplayConfig.xOffset)
                : (int) Math.round(PlayerPingDisplayConfig.hudX * availableX);
        int y = PlayerPingDisplayConfig.hudY == null
                ? PlayerPingDisplayConfig.anchor.resolveY(screenHeight, height, PlayerPingDisplayConfig.yOffset)
                : (int) Math.round(PlayerPingDisplayConfig.hudY * availableY);
        return new Bounds(PlayerPingDisplayConfig.clamp(x, 0, availableX), PlayerPingDisplayConfig.clamp(y, 0, availableY), width, height);
    }

    private static Bounds draw(DrawContext context, String player, String ping, int automaticColor, float opacity, int screenWidth, int screenHeight) {
        Text text = formattedText(player, ping, automaticColor);
        Bounds bounds = bounds(text, screenWidth, screenHeight);
        var renderer = MinecraftClient.getInstance().textRenderer;
        int width = renderer.getWidth(text) + PlayerPingDisplayConfig.paddingX * 2;
        int height = renderer.fontHeight + PlayerPingDisplayConfig.paddingY * 2;
        int alpha = Math.round(255 * opacity);
        MinecraftCompat.pushHud(context, bounds.x(), bounds.y(), (float) PlayerPingDisplayConfig.hudScale);
        if (PlayerPingDisplayConfig.backgroundEnabled) {
            int backgroundAlpha = alpha * PlayerPingDisplayConfig.backgroundOpacity / 100;
            roundedFill(context, width, height, PlayerPingDisplayConfig.cornerRadius,
                    (backgroundAlpha << 24) | PlayerPingDisplayConfig.backgroundColor);
        }
        context.drawText(renderer, text, PlayerPingDisplayConfig.paddingX, PlayerPingDisplayConfig.paddingY,
                (alpha << 24) | 0xFFFFFF, PlayerPingDisplayConfig.textShadow);
        MinecraftCompat.popHud(context);
        return bounds;
    }

    private static void roundedFill(DrawContext context, int width, int height, int radius, int color) {
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
