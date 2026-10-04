package com.badeye.playerpingdisplay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

final class MinecraftCompat {
	private MinecraftCompat() {
	}

	static Screen currentScreen(Minecraft client) {
		return client.screen;
	}

	static void setScreen(Minecraft client, Screen screen) {
		client.setScreen(screen);
	}

	static boolean isHudHidden(Minecraft client) {
		return client.options.hideGui;
	}
}
