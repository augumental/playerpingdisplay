package com.badeye.playerpingdisplay;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PlayerPingDisplayConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("playerpingdisplay.json");

	public static Anchor anchor = Anchor.TOP_CENTER;
	public static int xOffset = 0;
	public static int yOffset = 48;
	public static PingMode pingMode = PingMode.UUID;
	public static boolean dynamicResolverEnabled = true;
	public static int refreshTicks = 20;

	private PlayerPingDisplayConfig() {
	}

	public static void load() {
		if (!Files.exists(CONFIG_PATH)) {
			save();
			return;
		}

		try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
			Data data = GSON.fromJson(reader, Data.class);
			if (data != null) {
				anchor = data.anchor == null ? Anchor.TOP_CENTER : data.anchor;
				xOffset = clamp(data.xOffset, -400, 400);
				yOffset = clamp(data.yOffset, -240, 240);
				pingMode = data.pingMode == null ? migratedPingMode(data.pvphqMode) : data.pingMode;
				dynamicResolverEnabled = data.dynamicResolverEnabled == null || data.dynamicResolverEnabled;
				refreshTicks = clamp(data.refreshTicks <= 0 ? 20 : data.refreshTicks, 10, 100);
			}
		} catch (IOException | RuntimeException ignored) {
			reset();
		}
	}

	public static void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
				GSON.toJson(new Data(anchor, xOffset, yOffset, pingMode, null, dynamicResolverEnabled, refreshTicks), writer);
			}
		} catch (IOException ignored) {
		}
	}

	public static void reset() {
		anchor = Anchor.TOP_CENTER;
		xOffset = 0;
		yOffset = 48;
		pingMode = PingMode.UUID;
		dynamicResolverEnabled = true;
		refreshTicks = 20;
		save();
	}

	static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	private static PingMode migratedPingMode(Boolean pvphqMode) {
		return Boolean.TRUE.equals(pvphqMode) ? PingMode.TAB_LIST_PROFILE_DISPLAY : PingMode.UUID;
	}

	private record Data(Anchor anchor, int xOffset, int yOffset, PingMode pingMode, Boolean pvphqMode, Boolean dynamicResolverEnabled, int refreshTicks) {
	}

	public enum PingMode {
		UUID("UUID"),
		TAB_LIST_PROFILE_DISPLAY("Tab List/Profile/Display");

		private final String label;

		PingMode(String label) {
			this.label = label;
		}

		public String label() {
			return label;
		}

		public PingMode next() {
			PingMode[] values = values();
			return values[(ordinal() + 1) % values.length];
		}
	}

	public enum Anchor {
		TOP_LEFT("Top Left"),
		TOP_CENTER("Top Center"),
		TOP_RIGHT("Top Right"),
		CENTER_LEFT("Center Left"),
		CENTER("Center"),
		CENTER_RIGHT("Center Right"),
		BOTTOM_LEFT("Bottom Left"),
		BOTTOM_CENTER("Bottom Center"),
		BOTTOM_RIGHT("Bottom Right");

		private final String label;

		Anchor(String label) {
			this.label = label;
		}

		public String label() {
			return label;
		}

		public Anchor next() {
			Anchor[] values = values();
			return values[(ordinal() + 1) % values.length];
		}

		int resolveX(int screenWidth, int elementWidth, int offset) {
			return switch (this) {
				case TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT -> offset;
				case TOP_CENTER, CENTER, BOTTOM_CENTER -> (screenWidth - elementWidth) / 2 + offset;
				case TOP_RIGHT, CENTER_RIGHT, BOTTOM_RIGHT -> screenWidth - elementWidth - offset;
			};
		}

		int resolveY(int screenHeight, int elementHeight, int offset) {
			return switch (this) {
				case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> offset;
				case CENTER_LEFT, CENTER, CENTER_RIGHT -> (screenHeight - elementHeight) / 2 + offset;
				case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> screenHeight - elementHeight - offset;
			};
		}
	}
}
