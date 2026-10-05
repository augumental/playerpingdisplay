package com.badeye.playerpingdisplay;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.Screenshot;
import java.nio.file.Files;

public final class CustomizationSmoke implements ClientModInitializer {
    private int phase, ticks;
    private Screen main;
    private String saved;
    private static final MouseButtonInfo LEFT = new MouseButtonInfo(com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_LEFT, 0);
    @Override public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.font == null || MinecraftCompat.currentScreen(client) == null || SmokeCompat.overlay(client) != null) return;
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
                        var key = (net.minecraft.client.KeyMapping) field.get(null);
                        require(key.getDefaultKey().getValue() == com.mojang.blaze3d.platform.InputConstants.KEY_U, "U default shortcut");
                        net.minecraft.client.KeyMapping.click(key.getDefaultKey());
                    }
                    case 1 -> { require(MinecraftCompat.currentScreen(client) instanceof PlayerPingDisplayConfigScreen, "U opens settings"); main = MinecraftCompat.currentScreen(client); require(main.children().size() == 9, "Customization button added"); screenshot(client, "01-main.png"); press(main, 6); require(MinecraftCompat.currentScreen(client) instanceof CustomizationScreen, "Customization navigation"); }
                    case 2 -> { press(MinecraftCompat.currentScreen(client), 0); require(PlayerPingDisplayConfig.font == PlayerPingDisplayConfig.HudFont.UNICODE, "Unicode font"); }
                    case 3 -> { screenshot(client, "02-unicode.png"); press(MinecraftCompat.currentScreen(client), 0); require(PlayerPingDisplayConfig.font == PlayerPingDisplayConfig.HudFont.ENCHANTING, "Enchanting font"); }
                    case 4 -> {
                        screenshot(client, "03-enchanting.png"); var screen = MinecraftCompat.currentScreen(client);
                        press(screen, 0); press(screen, 2);
                        slide(screen, 3, 8.0 / 12); slide(screen, 4, (7.5 - 1) / 29); slide(screen, 5, (1.25 - 0.5) / 2.5);
                        slide(screen, 8, 8.0 / 20); slide(screen, 9, 5.0 / 16);
                        require(!PlayerPingDisplayConfig.textShadow && PlayerPingDisplayConfig.cornerRadius == 8, "shadow and rounding controls");
                        require(PlayerPingDisplayConfig.displayTicks() == 150 && PlayerPingDisplayConfig.hudScale == 1.25, "duration and scale controls");
                        ((EditBox) screen.children().get(10)).setValue("%player% - Ping: %ping%");
                        require(PlayerPingHud.formattedText("Steve", "42", 0x55FF55).getString().equals("Steve - Ping: 42"), "editable format substitution");
                        press(screen, 7);
                        require(PlayerPingHud.formattedText("Steve", "42", 0x55FF55).getString().equals("Steve - 42 ms"), "format presets");
                        press(screen, 1);
                    }
                    case 5 -> {
                        var screen = MinecraftCompat.currentScreen(client); press(screen, 8);
                        require(!PlayerPingDisplayConfig.automaticPingColor && PlayerPingDisplayConfig.pingColor == 0xFF5555, "visual red swatch");
                        slide(screen, 1, 17.0 / 255); slide(screen, 2, 34.0 / 255); slide(screen, 3, 51.0 / 255);
                        require(PlayerPingDisplayConfig.pingColor == 0x112233, "RGB sliders");
                        var text = PlayerPingHud.formattedText("Steve", "42", 0x55FF55);
                        require(text.getSiblings().stream().anyMatch(t -> t.getString().equals("42") && t.getStyle().getColor().getValue() == 0x112233), "ping token uses selected color");
                    }
                    case 6 -> { screenshot(client, "04-ping-color.png"); MinecraftCompat.currentScreen(client).onClose(); press(MinecraftCompat.currentScreen(client), 6); }
                    case 7 -> {
                        var screen = MinecraftCompat.currentScreen(client);
                        slide(screen, 1, 34.0 / 255); slide(screen, 2, 68.0 / 255); slide(screen, 3, 102.0 / 255); slide(screen, 4, 0.35);
                        require(PlayerPingDisplayConfig.backgroundColor == 0x224466 && PlayerPingDisplayConfig.backgroundOpacity == 35, "background RGB and opacity");
                        press(screen, 0); require(!PlayerPingDisplayConfig.backgroundEnabled, "background off"); press(screen, 0);
                    }
                    case 8 -> { screenshot(client, "05-background.png"); MinecraftCompat.currentScreen(client).onClose(); }
                    case 9 -> {
                        var screen = MinecraftCompat.currentScreen(client); var before = PlayerPingHud.previewBounds(screen.width, screen.height);
                        var click = new MouseButtonEvent(before.x() + 3, before.y() + 3, LEFT);
                        require(screen.mouseClicked(click, false), "HUD hit test");
                        screen.mouseDragged(new MouseButtonEvent(43, 45, LEFT), 43 - click.x(), 45 - click.y());
                        screen.mouseReleased(new MouseButtonEvent(43, 45, LEFT));
                        var after = PlayerPingHud.previewBounds(screen.width, screen.height);
                        require(Math.abs(after.x() - 40) <= 1 && Math.abs(after.y() - 42) <= 1, "drag keeps grab offset");
                        require(PlayerPingDisplayConfig.hudX != null && PlayerPingDisplayConfig.hudY != null, "dragged position saved");
                        var resized = PlayerPingHud.previewBounds(screen.width + 200, screen.height + 100);
                        require(Math.abs(resized.x() - PlayerPingDisplayConfig.hudX * (screen.width + 200 - resized.width())) <= 1, "position survives GUI resize");
                        require(screen.mouseClicked(new MouseButtonEvent(after.x() + 2, after.y() + 2, LEFT), false), "second drag");
                        screen.mouseDragged(new MouseButtonEvent(-999, -999, LEFT), -999, -999); screen.mouseReleased(new MouseButtonEvent(-999, -999, LEFT));
                        require(PlayerPingHud.previewBounds(screen.width, screen.height).x() == 0 && PlayerPingHud.previewBounds(screen.width, screen.height).y() == 0, "drag clamps to viewport");
                        PlayerPingDisplayConfig.hudX = 0.15; PlayerPingDisplayConfig.hudY = 0.18; PlayerPingDisplayConfig.save();
                        saved = Files.readString(path);
                        PlayerPingDisplayConfig.load();
                        require(PlayerPingDisplayConfig.displayTicks() == 150 && !PlayerPingDisplayConfig.textShadow && PlayerPingDisplayConfig.pingColor == 0x112233 && PlayerPingDisplayConfig.backgroundOpacity == 35, "customization round trip");
                        require(PlayerPingDisplayConfig.hudX == 0.15 && PlayerPingDisplayConfig.displayFormat.equals("%player% - %ping% ms"), "position and format round trip");
                    }
                    case 10 -> { screenshot(client, "06-customized.png"); MinecraftCompat.currentScreen(client).onClose(); require(MinecraftCompat.currentScreen(client) == main, "Done returns to settings"); press(main, 7); }
                    case 11 -> {
                        require(PlayerPingDisplayConfig.displayDuration == 5 && PlayerPingDisplayConfig.hudScale == 1 && PlayerPingDisplayConfig.cornerRadius == 0 && PlayerPingDisplayConfig.hudX == null, "reset customization and position");
                        Files.writeString(path, saved); PlayerPingDisplayConfig.load();
                        require(new PlayerPingDisplayModMenu().getModConfigScreenFactory().create(null) instanceof PlayerPingDisplayConfigScreen, "Mod Menu configuration factory");
                        System.out.println("CUSTOMIZATION_SMOKE_PASSED"); client.stop();
                    }
                }
            } catch (Throwable failure) { failure.printStackTrace(); System.out.println("CUSTOMIZATION_SMOKE_FAILED"); System.exit(1); }
        });
    }
    private static void press(Screen screen, int index) { ((Button) screen.children().get(index)).onPress(LEFT); }
    private static void slide(Screen screen, int index, double fraction) {
        var widget = (AbstractWidget) screen.children().get(index);
        var click = new MouseButtonEvent(widget.getX() + 4 + fraction * (widget.getWidth() - 8), widget.getY() + 10, LEFT);
        require(widget.mouseClicked(click, false), "slider click"); widget.mouseReleased(click);
    }
    private static void screenshot(Minecraft client, String name) { SmokeCompat.screenshot(client, name); }
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
