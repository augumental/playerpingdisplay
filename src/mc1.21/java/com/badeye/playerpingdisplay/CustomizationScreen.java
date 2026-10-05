package com.badeye.playerpingdisplay;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import java.util.Locale;

final class CustomizationScreen extends HudEditorScreen {
    private final Screen parent;
    private TextFieldWidget formatField;
    private static final String[] FORMATS = { PlayerPingDisplayConfig.DEFAULT_FORMAT, "%player% - Ping: %ping%", "%player% - %ping% ms", "Ping: %ping% ms" };
    CustomizationScreen(Screen parent) { super(Text.literal("Customization")); this.parent = parent; }
    @Override protected void init() {
        int total = Math.min(360, width - 24), column = (total - 8) / 2;
        int left = (width - total) / 2, right = left + column + 8, y = Math.max(48, (height - 240) / 2 + 70);
        addDrawableChild(ButtonWidget.builder(fontText(), b -> {
            PlayerPingDisplayConfig.font = PlayerPingDisplayConfig.font.next(); b.setMessage(fontText()); PlayerPingDisplayConfig.save();
        }).dimensions(left, y, column, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Ping Color..."), b -> client.setScreen(new ColorPickerScreen(this, false)))
                .dimensions(right, y, column, 20).build());
        addDrawableChild(ButtonWidget.builder(shadowText(), b -> {
            PlayerPingDisplayConfig.textShadow = !PlayerPingDisplayConfig.textShadow; b.setMessage(shadowText()); PlayerPingDisplayConfig.save();
        }).dimensions(left, y + 24, column, 20).build());
        addDrawableChild(new ConfigValueSlider(right, y + 24, column, 0, 12, 1, PlayerPingDisplayConfig.cornerRadius,
                v -> PlayerPingDisplayConfig.cornerRadius = (int) v, v -> "Rounded Edges: " + (int) v + " px"));
        addDrawableChild(new ConfigValueSlider(left, y + 48, column, 1, 30, 0.5, PlayerPingDisplayConfig.displayDuration,
                v -> PlayerPingDisplayConfig.displayDuration = v, v -> String.format(Locale.ROOT, "Duration: %.1fs", v)));
        addDrawableChild(new ConfigValueSlider(right, y + 48, column, 0.5, 3, 0.05, PlayerPingDisplayConfig.hudScale,
                v -> PlayerPingDisplayConfig.hudScale = v, v -> "HUD Scale: " + Math.round(v * 100) + "%"));
        addDrawableChild(ButtonWidget.builder(Text.literal("Background..."), b -> client.setScreen(new ColorPickerScreen(this, true)))
                .dimensions(left, y + 72, column, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Next Format Preset"), b -> {
            int index = -1;
            for (int i = 0; i < FORMATS.length; i++) if (FORMATS[i].equals(formatField.getText())) index = i;
            formatField.setText(FORMATS[(index + 1) % FORMATS.length]);
        }).dimensions(right, y + 72, column, 20).build());
        addDrawableChild(new ConfigValueSlider(left, y + 96, column, 0, 20, 1, PlayerPingDisplayConfig.paddingX,
                v -> PlayerPingDisplayConfig.paddingX = (int) v, v -> "Horizontal Padding: " + (int) v));
        addDrawableChild(new ConfigValueSlider(right, y + 96, column, 0, 16, 1, PlayerPingDisplayConfig.paddingY,
                v -> PlayerPingDisplayConfig.paddingY = (int) v, v -> "Vertical Padding: " + (int) v));
        formatField = new TextFieldWidget(textRenderer, left, y + 120, total, 20, Text.literal("Display Format"));
        formatField.setMaxLength(120); formatField.setText(PlayerPingDisplayConfig.displayFormat);
        formatField.setTooltip(Tooltip.of(Text.literal("Display format: %player% is the player name, %ping% is their ping number. Add ms where you want it.")));
        formatField.setChangedListener(value -> { PlayerPingDisplayConfig.displayFormat = value; PlayerPingDisplayConfig.save(); });
        addDrawableChild(formatField);
        addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, b -> close()).dimensions(left, y + 146, total, 20).build());
    }
    private static Text fontText() { return Text.literal("Font: " + PlayerPingDisplayConfig.font.label); }
    private static Text shadowText() { return Text.literal("Text Shadow: " + (PlayerPingDisplayConfig.textShadow ? "On" : "Off")); }
    @Override public void close() {
        PlayerPingDisplayConfig.displayFormat = PlayerPingDisplayConfig.normalizedFormat(PlayerPingDisplayConfig.displayFormat);
        PlayerPingDisplayConfig.save(); client.setScreen(parent);
    }
}
