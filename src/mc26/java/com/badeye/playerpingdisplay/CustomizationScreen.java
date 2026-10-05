package com.badeye.playerpingdisplay;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import java.util.Locale;

final class CustomizationScreen extends HudEditorScreen {
    private final Screen parent;
    private EditBox formatField;
    private static final String[] FORMATS = { PlayerPingDisplayConfig.DEFAULT_FORMAT, "%player% - Ping: %ping%", "%player% - %ping% ms", "Ping: %ping% ms" };
    CustomizationScreen(Screen parent) { super(Component.literal("Customization")); this.parent = parent; }
    @Override protected void init() {
        int total = Math.min(360, width - 24), column = (total - 8) / 2;
        int left = (width - total) / 2, right = left + column + 8, y = Math.max(48, (height - 240) / 2 + 70);
        addRenderableWidget(Button.builder(fontText(), b -> {
            PlayerPingDisplayConfig.font = PlayerPingDisplayConfig.font.next(); b.setMessage(fontText()); PlayerPingDisplayConfig.save();
        }).bounds(left, y, column, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Ping Color..."), b -> MinecraftCompat.setScreen(minecraft, new ColorPickerScreen(this, false)))
                .bounds(right, y, column, 20).build());
        addRenderableWidget(Button.builder(shadowText(), b -> {
            PlayerPingDisplayConfig.textShadow = !PlayerPingDisplayConfig.textShadow; b.setMessage(shadowText()); PlayerPingDisplayConfig.save();
        }).bounds(left, y + 24, column, 20).build());
        addRenderableWidget(new ConfigValueSlider(right, y + 24, column, 0, 12, 1, PlayerPingDisplayConfig.cornerRadius,
                v -> PlayerPingDisplayConfig.cornerRadius = (int) v, v -> "Rounded Edges: " + (int) v + " px"));
        addRenderableWidget(new ConfigValueSlider(left, y + 48, column, 1, 30, 0.5, PlayerPingDisplayConfig.displayDuration,
                v -> PlayerPingDisplayConfig.displayDuration = v, v -> String.format(Locale.ROOT, "Duration: %.1fs", v)));
        addRenderableWidget(new ConfigValueSlider(right, y + 48, column, 0.5, 3, 0.05, PlayerPingDisplayConfig.hudScale,
                v -> PlayerPingDisplayConfig.hudScale = v, v -> "HUD Scale: " + Math.round(v * 100) + "%"));
        addRenderableWidget(Button.builder(Component.literal("Background..."), b -> MinecraftCompat.setScreen(minecraft, new ColorPickerScreen(this, true)))
                .bounds(left, y + 72, column, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Next Format Preset"), b -> {
            int index = -1;
            for (int i = 0; i < FORMATS.length; i++) if (FORMATS[i].equals(formatField.getValue())) index = i;
            formatField.setValue(FORMATS[(index + 1) % FORMATS.length]);
        }).bounds(right, y + 72, column, 20).build());
        addRenderableWidget(new ConfigValueSlider(left, y + 96, column, 0, 20, 1, PlayerPingDisplayConfig.paddingX,
                v -> PlayerPingDisplayConfig.paddingX = (int) v, v -> "Horizontal Padding: " + (int) v));
        addRenderableWidget(new ConfigValueSlider(right, y + 96, column, 0, 16, 1, PlayerPingDisplayConfig.paddingY,
                v -> PlayerPingDisplayConfig.paddingY = (int) v, v -> "Vertical Padding: " + (int) v));
        formatField = new EditBox(font, left, y + 120, total, 20, Component.literal("Display Format"));
        formatField.setMaxLength(120); formatField.setValue(PlayerPingDisplayConfig.displayFormat);
        formatField.setTooltip(Tooltip.create(Component.literal("Display format: %player% is the player name, %ping% is their ping number. Add ms where you want it.")));
        formatField.setResponder(value -> { PlayerPingDisplayConfig.displayFormat = value; PlayerPingDisplayConfig.save(); });
        addRenderableWidget(formatField);
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> onClose()).bounds(left, y + 146, total, 20).build());
    }
    private static Component fontText() { return Component.literal("Font: " + PlayerPingDisplayConfig.font.label); }
    private static Component shadowText() { return Component.literal("Text Shadow: " + (PlayerPingDisplayConfig.textShadow ? "On" : "Off")); }
    @Override public void onClose() {
        PlayerPingDisplayConfig.displayFormat = PlayerPingDisplayConfig.normalizedFormat(PlayerPingDisplayConfig.displayFormat);
        PlayerPingDisplayConfig.save(); MinecraftCompat.setScreen(minecraft, parent);
    }
}
