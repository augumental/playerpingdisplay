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
	public static final String DEFAULT_FORMAT = "%player% %ping% ms";
	public static HudFont font = HudFont.DEFAULT;
	public static boolean automaticPingColor = true;
	public static int pingColor = 0x55FF55;
	public static boolean textShadow = true;
	public static int cornerRadius = 0;
	public static double displayDuration = 5;
	public static double hudScale = 1;
	public static boolean backgroundEnabled = true;
	public static int backgroundColor = 0;
	public static int backgroundOpacity = 60;
	public static int paddingX = 6;
	public static int paddingY = 4;
	public static String displayFormat = DEFAULT_FORMAT;
	public static Double hudX;
	public static Double hudY;

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
				font = data.font == null ? HudFont.DEFAULT : data.font;
				automaticPingColor = data.automaticPingColor;
				pingColor = data.pingColor & 0xFFFFFF;
				textShadow = data.textShadow;
				cornerRadius = clamp(data.cornerRadius, 0, 12);
				displayDuration = bounded(data.displayDuration, 1, 30, 5);
				hudScale = bounded(data.hudScale, 0.5, 3, 1);
				backgroundEnabled = data.backgroundEnabled;
				backgroundColor = data.backgroundColor & 0xFFFFFF;
				backgroundOpacity = clamp(data.backgroundOpacity, 0, 100);
				paddingX = clamp(data.paddingX, 0, 20);
				paddingY = clamp(data.paddingY, 0, 16);
				displayFormat = normalizedFormat(data.displayFormat);
				hudX = data.hudX == null || !Double.isFinite(data.hudX) ? null : bounded(data.hudX, 0, 1, 0.5);
				hudY = data.hudY == null || !Double.isFinite(data.hudY) ? null : bounded(data.hudY, 0, 1, 0.5);
			}
		} catch (IOException | RuntimeException ignored) {
			reset();
		}
	}

	public static void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
				Data data = new Data();
				data.anchor = anchor; data.xOffset = xOffset; data.yOffset = yOffset;
				data.pingMode = pingMode; data.dynamicResolverEnabled = dynamicResolverEnabled; data.refreshTicks = refreshTicks;
				data.font = font; data.automaticPingColor = automaticPingColor; data.pingColor = pingColor;
				data.textShadow = textShadow; data.cornerRadius = cornerRadius; data.displayDuration = displayDuration;
				data.hudScale = hudScale; data.backgroundEnabled = backgroundEnabled; data.backgroundColor = backgroundColor;
				data.backgroundOpacity = backgroundOpacity; data.paddingX = paddingX; data.paddingY = paddingY;
				data.displayFormat = normalizedFormat(displayFormat); data.hudX = hudX; data.hudY = hudY;
				GSON.toJson(data, writer);
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
		font = HudFont.DEFAULT; automaticPingColor = true; pingColor = 0x55FF55; textShadow = true;
		cornerRadius = 0; displayDuration = 5; hudScale = 1; backgroundEnabled = true;
		backgroundColor = 0; backgroundOpacity = 60; paddingX = 6; paddingY = 4;
		displayFormat = DEFAULT_FORMAT; clearDraggedPosition();
		save();
	}

	static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	private static PingMode migratedPingMode(Boolean pvphqMode) {
		return Boolean.TRUE.equals(pvphqMode) ? PingMode.TAB_LIST_PROFILE_DISPLAY : PingMode.UUID;
	}

	static double bounded(double value, double min, double max, double fallback) {
		return Double.isFinite(value) ? Math.max(min, Math.min(max, value)) : fallback;
	}

	static String normalizedFormat(String value) {
		if (value == null || value.isBlank()) return DEFAULT_FORMAT;
		return value.substring(0, Math.min(120, value.length())).replace('\n', ' ').replace('\r', ' ');
	}

	static int displayTicks() { return (int) Math.round(displayDuration * 20); }
	static void clearDraggedPosition() { hudX = null; hudY = null; }

	private static final class Data {
		Anchor anchor = Anchor.TOP_CENTER;
		int xOffset; int yOffset = 48;
		PingMode pingMode; Boolean pvphqMode; Boolean dynamicResolverEnabled = true; int refreshTicks = 20;
		HudFont font = HudFont.DEFAULT;
		boolean automaticPingColor = true; int pingColor = 0x55FF55; boolean textShadow = true;
		int cornerRadius; double displayDuration = 5; double hudScale = 1;
		boolean backgroundEnabled = true; int backgroundColor; int backgroundOpacity = 60;
		int paddingX = 6; int paddingY = 4;
		String displayFormat = DEFAULT_FORMAT; Double hudX; Double hudY;
	}

	public enum HudFont {
		DEFAULT("Minecraft", "default"), UNICODE("Unicode", "uniform"), ENCHANTING("Enchanting", "alt");
		public final String label;
		public final String resource;
		HudFont(String label, String resource) { this.label = label; this.resource = resource; }
		public HudFont next() { return values()[(ordinal() + 1) % values().length]; }
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
