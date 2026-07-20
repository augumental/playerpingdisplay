package com.badeye.playerpingdisplay;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public final class PlayerPingDisplayConfigScreen extends Screen {
	private final Screen parent;

	public PlayerPingDisplayConfigScreen(Screen parent) {
		super(Text.translatable("screen.playerpingdisplay.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int centerX = width / 2;
		int y = height / 2 - 88;

		addDrawableChild(ButtonWidget.builder(anchorText(), button -> {
			PlayerPingDisplayConfig.anchor = PlayerPingDisplayConfig.anchor.next();
			button.setMessage(anchorText());
			PlayerPingDisplayConfig.save();
		}).dimensions(centerX - 100, y, 200, 20).build());

		addDrawableChild(new OffsetSlider(centerX - 100, y + 26, true));
		addDrawableChild(new OffsetSlider(centerX - 100, y + 52, false));

		addDrawableChild(ButtonWidget.builder(pingModeText(), button -> {
			PlayerPingDisplayConfig.pingMode = PlayerPingDisplayConfig.pingMode.next();
			button.setMessage(pingModeText());
			PlayerPingDisplayConfig.save();
		}).dimensions(centerX - 100, y + 78, 200, 20).build());

		addDrawableChild(ButtonWidget.builder(dynamicResolverText(), button -> {
			PlayerPingDisplayConfig.dynamicResolverEnabled = !PlayerPingDisplayConfig.dynamicResolverEnabled;
			button.setMessage(dynamicResolverText());
			PlayerPingDisplayConfig.save();
		}).dimensions(centerX - 100, y + 104, 200, 20).build());

		addDrawableChild(new RefreshSlider(centerX - 100, y + 130));

		addDrawableChild(ButtonWidget.builder(Text.translatable("screen.playerpingdisplay.reset"), button -> {
			PlayerPingDisplayConfig.reset();
			clearAndInit();
		}).dimensions(centerX - 100, y + 164, 98, 20).build());

		addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> close()).dimensions(centerX + 2, y + 164, 98, 20).build());
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);
		context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 20, 0xFFFFFF);
		PlayerPingHud.renderPreview(context, Text.translatable("screen.playerpingdisplay.preview").getString(), width, height);
	}

	@Override
	public void close() {
		PlayerPingDisplayConfig.save();
		client.setScreen(parent);
	}

	private static Text anchorText() {
		return Text.translatable("screen.playerpingdisplay.anchor", PlayerPingDisplayConfig.anchor.label());
	}

	private static Text offsetText(boolean xAxis, int value) {
		return Text.translatable(xAxis ? "screen.playerpingdisplay.x_offset" : "screen.playerpingdisplay.y_offset", value);
	}

	private static Text pingModeText() {
		return Text.translatable("screen.playerpingdisplay.ping_mode", PlayerPingDisplayConfig.pingMode.label());
	}

	private static Text dynamicResolverText() {
		return Text.translatable("screen.playerpingdisplay.dynamic_resolver", PlayerPingDisplayConfig.dynamicResolverEnabled ? ScreenTexts.ON : ScreenTexts.OFF);
	}

	private static Text refreshText(int ticks) {
		return Text.translatable("screen.playerpingdisplay.refresh_interval", String.format("%.1fs", ticks / 20.0F));
	}

	private static final class OffsetSlider extends SliderWidget {
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

	private static final class RefreshSlider extends SliderWidget {
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
