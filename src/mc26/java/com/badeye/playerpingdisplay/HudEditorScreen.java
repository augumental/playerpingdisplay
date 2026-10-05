package com.badeye.playerpingdisplay;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

abstract class HudEditorScreen extends Screen {
    private boolean draggingHud;
    private double grabX, grabY;

    HudEditorScreen(Component title) { super(title); }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        if (!draggingHud) {
            super.extractRenderState(context, mouseX, mouseY, delta);
            context.centeredText(font, title, width / 2, 12, 0xFFFFFFFF);
        }
        context.centeredText(font, Component.literal("Drag the ping preview to move it"), width / 2, 28, 0xFFB8B8B8);
        var bounds = PlayerPingHud.renderPreview(context, width, height);
        if (draggingHud || bounds.contains(mouseX, mouseY)) {
            context.outline(bounds.x() - 1, bounds.y() - 1, bounds.width() + 2, bounds.height() + 2, 0xFF55FFFF);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        var bounds = PlayerPingHud.previewBounds(width, height);
        if (click.button() == com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_LEFT && bounds.contains(click.x(), click.y())) {
            draggingHud = true;
            grabX = click.x() - bounds.x(); grabY = click.y() - bounds.y();
            setFocused(null);
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double dx, double dy) {
        if (draggingHud && click.button() == com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_LEFT) {
            var bounds = PlayerPingHud.previewBounds(width, height);
            double availableX = Math.max(0, width - bounds.width()), availableY = Math.max(0, height - bounds.height());
            PlayerPingDisplayConfig.hudX = availableX == 0 ? 0 : PlayerPingDisplayConfig.bounded((click.x() - grabX) / availableX, 0, 1, 0);
            PlayerPingDisplayConfig.hudY = availableY == 0 ? 0 : PlayerPingDisplayConfig.bounded((click.y() - grabY) / availableY, 0, 1, 0);
            return true;
        }
        return super.mouseDragged(click, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        if (draggingHud && click.button() == com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_LEFT) {
            draggingHud = false;
            PlayerPingDisplayConfig.save();
            onHudPositionChanged();
            return true;
        }
        return super.mouseReleased(click);
    }

    protected void onHudPositionChanged() {}

    @Override
    public void removed() {
        draggingHud = false;
        PlayerPingDisplayConfig.save();
        super.removed();
    }
}
