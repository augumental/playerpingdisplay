package com.badeye.playerpingdisplay;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

final class PlayerPingHud {
	private static final int TEXT_COLOR = 0xFFFFFFFF;
	private static final int BACKGROUND_COLOR = 0x99000000;
	private static final int PADDING_X = 6;
	private static final int PADDING_Y = 4;

	private PlayerPingHud() {
	}

	static void render(DrawContext context, PlayerPingDisplayClient.TrackedPlayer trackedPlayer) {
		if (trackedPlayer == null) {
			return;
		}

		MinecraftClient client = MinecraftClient.getInstance();
		if (client.textRenderer == null || client.options.hudHidden) {
			return;
		}

		String nameText = trackedPlayer.playerName() + " ";
		String pingText = trackedPlayer.pingText();
		int nameWidth = client.textRenderer.getWidth(nameText);
		int textWidth = nameWidth + client.textRenderer.getWidth(pingText);
		int width = textWidth + PADDING_X * 2;
		int height = client.textRenderer.fontHeight + PADDING_Y * 2;
		int x = PlayerPingDisplayConfig.anchor.resolveX(context.getScaledWindowWidth(), width, PlayerPingDisplayConfig.xOffset);
		int y = PlayerPingDisplayConfig.anchor.resolveY(context.getScaledWindowHeight(), height, PlayerPingDisplayConfig.yOffset);
		int alpha = Math.round(255.0F * trackedPlayer.opacity());

		context.fill(x, y, x + width, y + height, withAlpha(BACKGROUND_COLOR, alpha * 0x99 / 0xFF));
		context.drawTextWithShadow(client.textRenderer, nameText, x + PADDING_X, y + PADDING_Y, withAlpha(TEXT_COLOR, alpha));
		context.drawTextWithShadow(client.textRenderer, pingText, x + PADDING_X + nameWidth, y + PADDING_Y, withAlpha(trackedPlayer.pingColor(), alpha));
	}

	static void renderPreview(DrawContext context, String text, int screenWidth, int screenHeight) {
		MinecraftClient client = MinecraftClient.getInstance();
		int textWidth = client.textRenderer.getWidth(text);
		int width = textWidth + PADDING_X * 2;
		int height = client.textRenderer.fontHeight + PADDING_Y * 2;
		int x = PlayerPingDisplayConfig.anchor.resolveX(screenWidth, width, PlayerPingDisplayConfig.xOffset);
		int y = PlayerPingDisplayConfig.anchor.resolveY(screenHeight, height, PlayerPingDisplayConfig.yOffset);

		context.fill(x, y, x + width, y + height, BACKGROUND_COLOR);
		context.drawTextWithShadow(client.textRenderer, text, x + PADDING_X, y + PADDING_Y, TEXT_COLOR);
	}

	private static int withAlpha(int color, int alpha) {
		return (Math.max(0, Math.min(255, alpha)) << 24) | (color & 0x00FFFFFF);
	}
}
