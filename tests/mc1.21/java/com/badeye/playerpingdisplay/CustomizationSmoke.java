package com.badeye.playerpingdisplay;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.ScreenshotRecorder;
import java.nio.file.Files;

public final class CustomizationSmoke implements ClientModInitializer {
    private int phase, ticks;
    private Screen main;
    private String saved;
    @Override public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.textRenderer == null || client.currentScreen == null || client.getOverlay() != null) return;
            if (++ticks < 20) return;
            ticks = 0;
            try {
                var path = FabricLoader.getInstance().getConfigDir().resolve("playerpingdisplay.json");
                switch (phase++) {
                    case 0 -> {
                        Files.writeString(path, "{\"anchor\":\"TOP_CENTER\",\"pvphqMode\":true,\"refreshTicks\":20,\"yOffset\":48}");
                        PlayerPingDisplayConfig.load();
                        require(PlayerPingDisplayConfig.pingMode == PlayerPingDisplayConfig.PingMode.TAB_LIST_PROFILE_DISPLAY, "legacy lookup migration");
                        require(PlayerPingDisplayConfig.displayDuration == 5 && PlayerPingDisplayConfig.hudScale == 1 && PlayerPingDisplayConfig.textShadow, "old config customization defaults");
                        var field = PlayerPingDisplayClient.class.getDeclaredField("configKey"); field.setAccessible(true);
                        var key = (net.minecraft.client.option.KeyBinding) field.get(null);
                        require(key.getDefaultKey().getCode() == 85, "U default shortcut");
                        net.minecraft.client.option.KeyBinding.onKeyPressed(key.getDefaultKey());
                    }
                    case 1 -> { require(client.currentScreen instanceof PlayerPingDisplayConfigScreen, "U opens settings"); main = client.currentScreen; require(main.children().size() == 9, "Customization button added"); screenshot(client, "01-main.png"); press(main, 6); require(client.currentScreen instanceof CustomizationScreen, "Customization navigation"); }
                    case 2 -> { press(client.currentScreen, 0); require(PlayerPingDisplayConfig.font == PlayerPingDisplayConfig.HudFont.UNICODE, "Unicode font"); }
                    case 3 -> { screenshot(client, "02-unicode.png"); press(client.currentScreen, 0); require(PlayerPingDisplayConfig.font == PlayerPingDisplayConfig.HudFont.ENCHANTING, "Enchanting font"); }
                    case 4 -> {
                        screenshot(client, "03-enchanting.png"); var screen = client.currentScreen;
                        press(screen, 0); press(screen, 2);
                        slide(screen, 3, 8.0 / 12); slide(screen, 4, (7.5 - 1) / 29); slide(screen, 5, (1.25 - 0.5) / 2.5);
                        slide(screen, 8, 8.0 / 20); slide(screen, 9, 5.0 / 16);
                        require(!PlayerPingDisplayConfig.textShadow && PlayerPingDisplayConfig.cornerRadius == 8, "shadow and rounding controls");
                        require(PlayerPingDisplayConfig.displayTicks() == 150 && PlayerPingDisplayConfig.hudScale == 1.25, "duration and scale controls");
                        ((TextFieldWidget) screen.children().get(10)).setText("%player% - Ping: %ping%");
                        require(PlayerPingHud.formattedText("Steve", "42", 0x55FF55).getString().equals("Steve - Ping: 42"), "editable format substitution");
                        press(screen, 7);
                        require(PlayerPingHud.formattedText("Steve", "42", 0x55FF55).getString().equals("Steve - 42 ms"), "format presets");
                        press(screen, 1);
                    }
                    case 5 -> {
                        var screen = client.currentScreen; press(screen, 8);
                        require(!PlayerPingDisplayConfig.automaticPingColor && PlayerPingDisplayConfig.pingColor == 0xFF5555, "visual red swatch");
                        slide(screen, 1, 17.0 / 255); slide(screen, 2, 34.0 / 255); slide(screen, 3, 51.0 / 255);
                        require(PlayerPingDisplayConfig.pingColor == 0x112233, "RGB sliders");
                        var text = PlayerPingHud.formattedText("Steve", "42", 0x55FF55);
                        require(text.getSiblings().stream().anyMatch(t -> t.getString().equals("42") && t.getStyle().getColor().getRgb() == 0x112233), "ping token uses selected color");
                    }
                    case 6 -> { screenshot(client, "04-ping-color.png"); client.currentScreen.close(); press(client.currentScreen, 6); }
                    case 7 -> {
                        var screen = client.currentScreen;
                        slide(screen, 1, 34.0 / 255); slide(screen, 2, 68.0 / 255); slide(screen, 3, 102.0 / 255); slide(screen, 4, 0.35);
                        require(PlayerPingDisplayConfig.backgroundColor == 0x224466 && PlayerPingDisplayConfig.backgroundOpacity == 35, "background RGB and opacity");
                        press(screen, 0); require(!PlayerPingDisplayConfig.backgroundEnabled, "background off"); press(screen, 0);
                    }
                    case 8 -> { screenshot(client, "05-background.png"); client.currentScreen.close(); }
                    case 9 -> {
                        var screen = client.currentScreen; var before = PlayerPingHud.previewBounds(screen.width, screen.height);
                        var click = new double[] {before.x() + 3, before.y() + 3};
                        require(screen.mouseClicked(click[0], click[1], 0), "HUD hit test");
                        screen.mouseDragged(43, 45, 0, 43 - click[0], 45 - click[1]);
                        screen.mouseReleased(43, 45, 0);
                        var after = PlayerPingHud.previewBounds(screen.width, screen.height);
                        require(Math.abs(after.x() - 40) <= 1 && Math.abs(after.y() - 42) <= 1, "drag keeps grab offset");
                        require(PlayerPingDisplayConfig.hudX != null && PlayerPingDisplayConfig.hudY != null, "dragged position saved");
                        var resized = PlayerPingHud.previewBounds(screen.width + 200, screen.height + 100);
                        require(Math.abs(resized.x() - PlayerPingDisplayConfig.hudX * (screen.width + 200 - resized.width())) <= 1, "position survives GUI resize");
                        require(screen.mouseClicked(after.x() + 2, after.y() + 2, 0), "second drag");
                        screen.mouseDragged(-999, -999, 0, -999, -999); screen.mouseReleased(-999, -999, 0);
                        require(PlayerPingHud.previewBounds(screen.width, screen.height).x() == 0 && PlayerPingHud.previewBounds(screen.width, screen.height).y() == 0, "drag clamps to viewport");
                        PlayerPingDisplayConfig.hudX = 0.15; PlayerPingDisplayConfig.hudY = 0.18; PlayerPingDisplayConfig.save();
                        saved = Files.readString(path);
                        PlayerPingDisplayConfig.load();
                        require(PlayerPingDisplayConfig.displayTicks() == 150 && !PlayerPingDisplayConfig.textShadow && PlayerPingDisplayConfig.pingColor == 0x112233 && PlayerPingDisplayConfig.backgroundOpacity == 35, "customization round trip");
                        require(PlayerPingDisplayConfig.hudX == 0.15 && PlayerPingDisplayConfig.displayFormat.equals("%player% - %ping% ms"), "position and format round trip");
                    }
                    case 10 -> { screenshot(client, "06-customized.png"); client.currentScreen.close(); require(client.currentScreen == main, "Done returns to settings"); press(main, 7); }
                    case 11 -> {
                        require(PlayerPingDisplayConfig.displayDuration == 5 && PlayerPingDisplayConfig.hudScale == 1 && PlayerPingDisplayConfig.cornerRadius == 0 && PlayerPingDisplayConfig.hudX == null, "reset customization and position");
                        Files.writeString(path, saved); PlayerPingDisplayConfig.load();
                        require(new PlayerPingDisplayModMenu().getModConfigScreenFactory().create(null) instanceof PlayerPingDisplayConfigScreen, "Mod Menu configuration factory");
                        System.out.println("CUSTOMIZATION_SMOKE_PASSED"); client.scheduleStop();
                    }
                }
            } catch (Throwable failure) { failure.printStackTrace(); System.out.println("CUSTOMIZATION_SMOKE_FAILED"); System.exit(1); }
        });
    }
    private static void press(Screen screen, int index) { ((ButtonWidget) screen.children().get(index)).onPress(); }
    private static void slide(Screen screen, int index, double fraction) {
        var widget = (ClickableWidget) screen.children().get(index);
        double x = widget.getX() + 4 + fraction * (widget.getWidth() - 8), y = widget.getY() + 10;
        require(widget.mouseClicked(x, y, 0), "slider click"); widget.mouseReleased(x, y, 0);
    }
    private static void screenshot(MinecraftClient client, String name) { SmokeCompat.screenshot(client, name); }
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
