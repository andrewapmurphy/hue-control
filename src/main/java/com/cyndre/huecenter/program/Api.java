package com.cyndre.huecenter.program;

import com.cyndre.huecenter.hue.LightState;

import java.awt.*;

public class Api {
    public Color toColor(final double r, double g, double b ) {
        return toColor((float)r, (float)g, (float)b);
    }
    public Color toColor(final float r, float g, float b ) {
        return new Color(r, g, b);
    }

    public Color toColor(final LightState light) {
        return LightState.toColor(light);
    }

    public LightState toLightState(final Color color) {
        return LightState.toLightState(color);
    }

    public LightState toLightState(final int r, int g, int b) {
        return toLightState(new Color(r, g, b));
    }

    public double random() {
        return Math.random();
    }
}
