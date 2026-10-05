package com.badeye.playerpingdisplay;

import net.minecraft.client.gui.Click;
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
            context.drawStrokedRectangle(bounds.x() - 1, bounds.y() - 1, bounds.width() + 2, bounds.height() + 2, 0xFF55FFFF);
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        var bounds = PlayerPingHud.previewBounds(width, height);
        if (click.button() == 0 && bounds.contains(click.x(), click.y())) {
            draggingHud = true;
            grabX = click.x() - bounds.x(); grabY = click.y() - bounds.y();
            setFocused(null);
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double dx, double dy) {
        if (draggingHud && click.button() == 0) {
            var bounds = PlayerPingHud.previewBounds(width, height);
            double availableX = Math.max(0, width - bounds.width()), availableY = Math.max(0, height - bounds.height());
            PlayerPingDisplayConfig.hudX = availableX == 0 ? 0 : PlayerPingDisplayConfig.bounded((click.x() - grabX) / availableX, 0, 1, 0);
            PlayerPingDisplayConfig.hudY = availableY == 0 ? 0 : PlayerPingDisplayConfig.bounded((click.y() - grabY) / availableY, 0, 1, 0);
            return true;
        }
        return super.mouseDragged(click, dx, dy);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (draggingHud && click.button() == 0) {
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
