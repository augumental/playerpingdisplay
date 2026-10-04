package com.badeye.playerpingdisplay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

final class PlayerPingHud {
	private static final int TEXT_COLOR = 0xFFFFFF;
	private static final int BACKGROUND_COLOR = 0x99000000;
	private static final int PADDING_X = 6;
	private static final int PADDING_Y = 4;

	private PlayerPingHud() {
	}

	static void render(GuiGraphicsExtractor context, PlayerPingDisplayClient.TrackedPlayer trackedPlayer) {
		if (trackedPlayer == null) {
			return;
		}

		Minecraft client = Minecraft.getInstance();
		if (client.font == null || MinecraftCompat.isHudHidden(client)) {
			return;
		}

		String nameText = trackedPlayer.playerName() + " ";
		String pingText = trackedPlayer.pingText();
		int nameWidth = client.font.width(nameText);
		int textWidth = nameWidth + client.font.width(pingText);
		int width = textWidth + PADDING_X * 2;
		int height = client.font.lineHeight + PADDING_Y * 2;
		int x = PlayerPingDisplayConfig.anchor.resolveX(context.guiWidth(), width, PlayerPingDisplayConfig.xOffset);
		int y = PlayerPingDisplayConfig.anchor.resolveY(context.guiHeight(), height, PlayerPingDisplayConfig.yOffset);
		int alpha = Math.round(255.0F * trackedPlayer.opacity());

		context.fill(x, y, x + width, y + height, withAlpha(BACKGROUND_COLOR, alpha * 0x99 / 0xFF));
		context.text(client.font, nameText, x + PADDING_X, y + PADDING_Y, withAlpha(TEXT_COLOR, alpha));
		context.text(client.font, pingText, x + PADDING_X + nameWidth, y + PADDING_Y, withAlpha(trackedPlayer.pingColor(), alpha));
	}

	static void renderPreview(GuiGraphicsExtractor context, String text, int screenWidth, int screenHeight) {
		Minecraft client = Minecraft.getInstance();
		int textWidth = client.font.width(text);
		int width = textWidth + PADDING_X * 2;
		int height = client.font.lineHeight + PADDING_Y * 2;
		int x = PlayerPingDisplayConfig.anchor.resolveX(screenWidth, width, PlayerPingDisplayConfig.xOffset);
		int y = PlayerPingDisplayConfig.anchor.resolveY(screenHeight, height, PlayerPingDisplayConfig.yOffset);

		context.fill(x, y, x + width, y + height, BACKGROUND_COLOR);
		context.text(client.font, text, x + PADDING_X, y + PADDING_Y, withAlpha(TEXT_COLOR, 255));
	}

	private static int withAlpha(int color, int alpha) {
		return (Math.max(0, Math.min(255, alpha)) << 24) | (color & 0x00FFFFFF);
	}
}
