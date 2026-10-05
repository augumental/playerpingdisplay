package com.badeye.playerpingdisplay;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

abstract class HudEditorScreen extends Screen {
    private boolean draggingHud;
    private double grabX, grabY;

    HudEditorScreen(Text title) { super(title); }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!draggingHud) {
            super.render(context, mouseX, mouseY, delta);
            context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xFFFFFFFF);
        }
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Drag the ping preview to move it"), width / 2, 28, 0xFFB8B8B8);
        var bounds = PlayerPingHud.renderPreview(context, width, height);
        if (draggingHud || bounds.contains(mouseX, mouseY)) {
            context.drawBorder(bounds.x() - 1, bounds.y() - 1, bounds.width() + 2, bounds.height() + 2, 0xFF55FFFF);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        var bounds = PlayerPingHud.previewBounds(width, height);
        if (button == 0 && bounds.contains(mouseX, mouseY)) {
            draggingHud = true;
            grabX = mouseX - bounds.x(); grabY = mouseY - bounds.y();
            setFocused(null);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (draggingHud && button == 0) {
            var bounds = PlayerPingHud.previewBounds(width, height);
            double availableX = Math.max(0, width - bounds.width()), availableY = Math.max(0, height - bounds.height());
            PlayerPingDisplayConfig.hudX = availableX == 0 ? 0 : PlayerPingDisplayConfig.bounded((mouseX - grabX) / availableX, 0, 1, 0);
            PlayerPingDisplayConfig.hudY = availableY == 0 ? 0 : PlayerPingDisplayConfig.bounded((mouseY - grabY) / availableY, 0, 1, 0);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingHud && button == 0) {
            draggingHud = false;
            PlayerPingDisplayConfig.save();
            onHudPositionChanged();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    protected void onHudPositionChanged() {}

    @Override
    public void removed() {
        draggingHud = false;
        PlayerPingDisplayConfig.save();
        super.removed();
    }
}
