package com.badeye.playerpingdisplay;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class PlayerPingDisplayConfigScreen extends HudEditorScreen {
    private final Screen parent;
    public PlayerPingDisplayConfigScreen(Screen parent) {
        super(Component.translatable("screen.playerpingdisplay.title"));
        this.parent = parent;
    }
    @Override protected void init() {
        int total = Math.min(360, width - 24), column = (total - 8) / 2;
        int left = (width - total) / 2, right = left + column + 8, y = Math.max(62, (height - 240) / 2 + 78);
        addRenderableWidget(Button.builder(anchorText(), b -> {
            PlayerPingDisplayConfig.anchor = PlayerPingDisplayConfig.anchor.next();
            PlayerPingDisplayConfig.clearDraggedPosition(); b.setMessage(anchorText()); PlayerPingDisplayConfig.save();
        }).bounds(left, y, column, 20).build());
        addRenderableWidget(new ConfigValueSlider(right, y, column, -400, 400, 1, PlayerPingDisplayConfig.xOffset,
                v -> { PlayerPingDisplayConfig.xOffset = (int) v; PlayerPingDisplayConfig.clearDraggedPosition(); onHudPositionChanged(); }, v -> "X Offset: " + (int) v));
        addRenderableWidget(new ConfigValueSlider(left, y + 24, column, -240, 240, 1, PlayerPingDisplayConfig.yOffset,
                v -> { PlayerPingDisplayConfig.yOffset = (int) v; PlayerPingDisplayConfig.clearDraggedPosition(); onHudPositionChanged(); }, v -> "Y Offset: " + (int) v));
        addRenderableWidget(Button.builder(pingModeText(), b -> {
            PlayerPingDisplayConfig.pingMode = PlayerPingDisplayConfig.pingMode.next(); b.setMessage(pingModeText()); PlayerPingDisplayConfig.save();
        }).bounds(right, y + 24, column, 20).build());
        addRenderableWidget(Button.builder(dynamicText(), b -> {
            PlayerPingDisplayConfig.dynamicResolverEnabled = !PlayerPingDisplayConfig.dynamicResolverEnabled;
            b.setMessage(dynamicText()); PlayerPingDisplayConfig.save();
        }).bounds(left, y + 48, column, 20).build());
        addRenderableWidget(new ConfigValueSlider(right, y + 48, column, 10, 100, 10, PlayerPingDisplayConfig.refreshTicks,
                v -> PlayerPingDisplayConfig.refreshTicks = (int) v, v -> String.format(java.util.Locale.ROOT, "Refresh: %.1fs", v / 20)));
        addRenderableWidget(Button.builder(Component.literal("Customization..."), b -> MinecraftCompat.setScreen(minecraft, new CustomizationScreen(this)))
                .bounds(left, y + 78, total, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.playerpingdisplay.reset"), b -> {
            PlayerPingDisplayConfig.reset(); rebuildWidgets();
        }).bounds(left, y + 106, column, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> onClose()).bounds(right, y + 106, column, 20).build());
    }
    @Override protected void onHudPositionChanged() { ((Button) children().getFirst()).setMessage(anchorText()); }
    @Override public void onClose() { PlayerPingDisplayConfig.save(); MinecraftCompat.setScreen(minecraft, parent); }
    private static Component anchorText() {
        return Component.literal(PlayerPingDisplayConfig.hudX != null ? "Position: Dragged" : "Anchor: " + PlayerPingDisplayConfig.anchor.label());
    }
    private static Component pingModeText() { return Component.literal("Lookup: " + PlayerPingDisplayConfig.pingMode.label()); }
    private static Component dynamicText() { return Component.literal("Live Refresh: " + (PlayerPingDisplayConfig.dynamicResolverEnabled ? "On" : "Off")); }
}
