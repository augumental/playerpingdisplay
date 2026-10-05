package com.badeye.playerpingdisplay;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

final class ColorPickerScreen extends HudEditorScreen {
    private static final int[] PALETTE = {0xFFFFFF, 0x55FF55, 0xFFFF55, 0xFF5555, 0x55FFFF, 0x5555FF, 0xFF55FF, 0x000000};
    private static final String[] NAMES = {"White", "Green", "Yellow", "Red", "Cyan", "Blue", "Pink", "Black"};
    private final Screen parent;
    private final boolean background;
    ColorPickerScreen(Screen parent, boolean background) { super(Component.literal(background ? "Background Appearance" : "Ping Color")); this.parent = parent; this.background = background; }
    @Override protected void init() {
        int total = Math.min(360, width - 24), left = (width - total) / 2, y = Math.max(48, (height - 240) / 2 + 70);
        addRenderableWidget(Button.builder(modeText(), b -> {
            if (background) PlayerPingDisplayConfig.backgroundEnabled = !PlayerPingDisplayConfig.backgroundEnabled;
            else PlayerPingDisplayConfig.automaticPingColor = !PlayerPingDisplayConfig.automaticPingColor;
            b.setMessage(modeText()); PlayerPingDisplayConfig.save();
        }).bounds(left, y, total, 20).build());
        String[] names = {"Red", "Green", "Blue"};
        for (int i = 0; i < 3; i++) {
            int shift = 16 - i * 8; String label = names[i];
            addRenderableWidget(new ConfigValueSlider(left, y + 24 + i * 24, total, 0, 255, 1, (color() >> shift) & 255,
                    v -> { setColor((color() & ~(255 << shift)) | ((int) v << shift)); }, v -> label + ": " + (int) v));
        }
        if (background) addRenderableWidget(new ConfigValueSlider(left, y + 96, total, 0, 100, 1, PlayerPingDisplayConfig.backgroundOpacity,
                v -> PlayerPingDisplayConfig.backgroundOpacity = (int) v, v -> "Background Opacity: " + (int) v + "%"));
        else addRenderableWidget(Button.builder(Component.literal("Automatic uses the original ping thresholds"), b -> {})
                .bounds(left, y + 96, total, 20).build()).active = false;
        int swatchWidth = (total - 7 * 4) / 8;
        for (int i = 0; i < PALETTE.length; i++) {
            int color = PALETTE[i];
            var button = Button.builder(Component.literal("■").withStyle(s -> s.withColor(color)), b -> { setColor(color); PlayerPingDisplayConfig.save(); rebuildWidgets(); })
                    .bounds(left + i * (swatchWidth + 4), y + 120, swatchWidth, 20).build();
            button.setTooltip(Tooltip.create(Component.literal(NAMES[i]))); addRenderableWidget(button);
        }
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> onClose()).bounds(left, y + 146, total, 20).build());
    }
    private int color() { return background ? PlayerPingDisplayConfig.backgroundColor : PlayerPingDisplayConfig.pingColor; }
    private void setColor(int value) {
        if (background) { PlayerPingDisplayConfig.backgroundColor = value; PlayerPingDisplayConfig.backgroundEnabled = true; }
        else { PlayerPingDisplayConfig.pingColor = value; PlayerPingDisplayConfig.automaticPingColor = false; }
        // Slider changes also update the mode label without rebuilding the active slider.
        ((Button) children().getFirst()).setMessage(modeText());
    }
    private Component modeText() { return Component.literal(background ? "Background: " + (PlayerPingDisplayConfig.backgroundEnabled ? "On" : "Off") : "Ping Color: " + (PlayerPingDisplayConfig.automaticPingColor ? "Automatic" : "Custom")); }
    @Override public void onClose() { PlayerPingDisplayConfig.save(); MinecraftCompat.setScreen(minecraft, parent); }
}
