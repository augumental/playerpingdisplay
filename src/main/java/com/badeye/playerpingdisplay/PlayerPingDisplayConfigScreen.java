package com.badeye.playerpingdisplay;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class PlayerPingDisplayConfigScreen extends Screen {
	private final Screen parent;

	public PlayerPingDisplayConfigScreen(Screen parent) {
		super(Component.translatable("screen.playerpingdisplay.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int centerX = width / 2;
		int y = height / 2 - 88;

		addRenderableWidget(Button.builder(anchorText(), button -> {
			PlayerPingDisplayConfig.anchor = PlayerPingDisplayConfig.anchor.next();
			button.setMessage(anchorText());
			PlayerPingDisplayConfig.save();
		}).bounds(centerX - 100, y, 200, 20).build());

		addRenderableWidget(new OffsetSlider(centerX - 100, y + 26, true));
		addRenderableWidget(new OffsetSlider(centerX - 100, y + 52, false));

		addRenderableWidget(Button.builder(pingModeText(), button -> {
			PlayerPingDisplayConfig.pingMode = PlayerPingDisplayConfig.pingMode.next();
			button.setMessage(pingModeText());
			PlayerPingDisplayConfig.save();
		}).bounds(centerX - 100, y + 78, 200, 20).build());

		addRenderableWidget(Button.builder(dynamicResolverText(), button -> {
			PlayerPingDisplayConfig.dynamicResolverEnabled = !PlayerPingDisplayConfig.dynamicResolverEnabled;
			button.setMessage(dynamicResolverText());
			PlayerPingDisplayConfig.save();
		}).bounds(centerX - 100, y + 104, 200, 20).build());

		addRenderableWidget(new RefreshSlider(centerX - 100, y + 130));

		addRenderableWidget(Button.builder(Component.translatable("screen.playerpingdisplay.reset"), button -> {
			PlayerPingDisplayConfig.reset();
			rebuildWidgets();
		}).bounds(centerX - 100, y + 164, 98, 20).build());

		addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose()).bounds(centerX + 2, y + 164, 98, 20).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		super.extractRenderState(context, mouseX, mouseY, deltaTicks);
		context.centeredText(font, title, width / 2, 20, 0xFFFFFFFF);
		PlayerPingHud.renderPreview(context, Component.translatable("screen.playerpingdisplay.preview").getString(), width, height);
	}

	@Override
	public void onClose() {
		PlayerPingDisplayConfig.save();
		MinecraftCompat.setScreen(minecraft, parent);
	}

	private static Component anchorText() {
		return Component.translatable("screen.playerpingdisplay.anchor", PlayerPingDisplayConfig.anchor.label());
	}

	private static Component offsetText(boolean xAxis, int value) {
		return Component.translatable(xAxis ? "screen.playerpingdisplay.x_offset" : "screen.playerpingdisplay.y_offset", value);
	}

	private static Component pingModeText() {
		return Component.translatable("screen.playerpingdisplay.ping_mode", PlayerPingDisplayConfig.pingMode.label());
	}

	private static Component dynamicResolverText() {
		return Component.translatable("screen.playerpingdisplay.dynamic_resolver", PlayerPingDisplayConfig.dynamicResolverEnabled ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
	}

	private static Component refreshText(int ticks) {
		return Component.translatable("screen.playerpingdisplay.refresh_interval", String.format("%.1fs", ticks / 20.0F));
	}

	private static final class OffsetSlider extends AbstractSliderButton {
		private static final int MIN = -240;
		private static final int MAX = 240;
		private final boolean xAxis;

		private OffsetSlider(int x, int y, boolean xAxis) {
			super(x, y, 200, 20, offsetText(xAxis, currentValue(xAxis)), toSliderValue(currentValue(xAxis)));
			this.xAxis = xAxis;
		}

		@Override
		protected void updateMessage() {
			setMessage(offsetText(xAxis, fromSliderValue(value)));
		}

		@Override
		protected void applyValue() {
			int offset = fromSliderValue(value);
			if (xAxis) {
				PlayerPingDisplayConfig.xOffset = offset;
			} else {
				PlayerPingDisplayConfig.yOffset = offset;
			}
			PlayerPingDisplayConfig.save();
		}

		private static int currentValue(boolean xAxis) {
			return xAxis ? PlayerPingDisplayConfig.xOffset : PlayerPingDisplayConfig.yOffset;
		}

		private static double toSliderValue(int value) {
			return (double) (PlayerPingDisplayConfig.clamp(value, MIN, MAX) - MIN) / (MAX - MIN);
		}

		private static int fromSliderValue(double value) {
			return MIN + (int) Math.round(value * (MAX - MIN));
		}
	}

	private static final class RefreshSlider extends AbstractSliderButton {
		private static final int MIN = 10;
		private static final int MAX = 100;

		private RefreshSlider(int x, int y) {
			super(x, y, 200, 20, refreshText(PlayerPingDisplayConfig.refreshTicks), toSliderValue(PlayerPingDisplayConfig.refreshTicks));
		}

		@Override
		protected void updateMessage() {
			setMessage(refreshText(fromSliderValue(value)));
		}

		@Override
		protected void applyValue() {
			PlayerPingDisplayConfig.refreshTicks = fromSliderValue(value);
			PlayerPingDisplayConfig.save();
		}

		private static double toSliderValue(int ticks) {
			return (double) (PlayerPingDisplayConfig.clamp(ticks, MIN, MAX) - MIN) / (MAX - MIN);
		}

		private static int fromSliderValue(double value) {
			int ticks = MIN + (int) Math.round(value * (MAX - MIN));
			return Math.round(ticks / 10.0F) * 10;
		}
	}
}
