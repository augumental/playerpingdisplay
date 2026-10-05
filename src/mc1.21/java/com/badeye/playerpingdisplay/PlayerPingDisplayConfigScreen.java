package com.badeye.playerpingdisplay;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public final class PlayerPingDisplayConfigScreen extends HudEditorScreen {
    private final Screen parent;
    public PlayerPingDisplayConfigScreen(Screen parent) {
        super(Text.translatable("screen.playerpingdisplay.title"));
        this.parent = parent;
    }
    @Override protected void init() {
        int total = Math.min(360, width - 24), column = (total - 8) / 2;
        int left = (width - total) / 2, right = left + column + 8, y = Math.max(62, (height - 240) / 2 + 78);
        addDrawableChild(ButtonWidget.builder(anchorText(), b -> {
            PlayerPingDisplayConfig.anchor = PlayerPingDisplayConfig.anchor.next();
            PlayerPingDisplayConfig.clearDraggedPosition(); b.setMessage(anchorText()); PlayerPingDisplayConfig.save();
        }).dimensions(left, y, column, 20).build());
        addDrawableChild(new ConfigValueSlider(right, y, column, -400, 400, 1, PlayerPingDisplayConfig.xOffset,
                v -> { PlayerPingDisplayConfig.xOffset = (int) v; PlayerPingDisplayConfig.clearDraggedPosition(); onHudPositionChanged(); }, v -> "X Offset: " + (int) v));
        addDrawableChild(new ConfigValueSlider(left, y + 24, column, -240, 240, 1, PlayerPingDisplayConfig.yOffset,
                v -> { PlayerPingDisplayConfig.yOffset = (int) v; PlayerPingDisplayConfig.clearDraggedPosition(); onHudPositionChanged(); }, v -> "Y Offset: " + (int) v));
        addDrawableChild(ButtonWidget.builder(pingModeText(), b -> {
            PlayerPingDisplayConfig.pingMode = PlayerPingDisplayConfig.pingMode.next(); b.setMessage(pingModeText()); PlayerPingDisplayConfig.save();
        }).dimensions(right, y + 24, column, 20).build());
        addDrawableChild(ButtonWidget.builder(dynamicText(), b -> {
            PlayerPingDisplayConfig.dynamicResolverEnabled = !PlayerPingDisplayConfig.dynamicResolverEnabled;
            b.setMessage(dynamicText()); PlayerPingDisplayConfig.save();
        }).dimensions(left, y + 48, column, 20).build());
        addDrawableChild(new ConfigValueSlider(right, y + 48, column, 10, 100, 10, PlayerPingDisplayConfig.refreshTicks,
                v -> PlayerPingDisplayConfig.refreshTicks = (int) v, v -> String.format(java.util.Locale.ROOT, "Refresh: %.1fs", v / 20)));
        addDrawableChild(ButtonWidget.builder(Text.literal("Customization..."), b -> client.setScreen(new CustomizationScreen(this)))
                .dimensions(left, y + 78, total, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.playerpingdisplay.reset"), b -> {
            PlayerPingDisplayConfig.reset(); clearAndInit();
        }).dimensions(left, y + 106, column, 20).build());
        addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, b -> close()).dimensions(right, y + 106, column, 20).build());
    }
    @Override protected void onHudPositionChanged() { ((ButtonWidget) children().getFirst()).setMessage(anchorText()); }
    @Override public void close() { PlayerPingDisplayConfig.save(); client.setScreen(parent); }
    private static Text anchorText() {
        return Text.literal(PlayerPingDisplayConfig.hudX != null ? "Position: Dragged" : "Anchor: " + PlayerPingDisplayConfig.anchor.label());
    }
    private static Text pingModeText() { return Text.literal("Lookup: " + PlayerPingDisplayConfig.pingMode.label()); }
    private static Text dynamicText() { return Text.literal("Live Refresh: " + (PlayerPingDisplayConfig.dynamicResolverEnabled ? "On" : "Off")); }
}
