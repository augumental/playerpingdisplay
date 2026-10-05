package com.badeye.playerpingdisplay;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;

final class ConfigValueSlider extends SliderWidget {
    private final double min, max, step;
    private final DoubleConsumer change;
    private final DoubleFunction<String> label;
    ConfigValueSlider(int x, int y, int width, double min, double max, double step, double initial,
                      DoubleConsumer change, DoubleFunction<String> label) {
        super(x, y, width, 20, Text.literal(label.apply(initial)), (initial - min) / (max - min));
        this.min = min; this.max = max; this.step = step; this.change = change; this.label = label;
    }
    private double selected() { return PlayerPingDisplayConfig.bounded(Math.round((min + value * (max - min)) / step) * step, min, max, min); }
    @Override protected void updateMessage() { setMessage(Text.literal(label.apply(selected()))); }
    @Override protected void applyValue() { change.accept(selected()); PlayerPingDisplayConfig.save(); }
}
