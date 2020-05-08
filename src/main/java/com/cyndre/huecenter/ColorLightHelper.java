package com.cyndre.huecenter;

import com.cyndre.huecenter.hue.LightState;

import java.awt.Color;

public final class ColorLightHelper {
    private static final boolean ON = true;
    private static final float MAX_RATIO = 254; //saturation and brightness can be on the range of 0-254
    private static final int MAX_HUE = 65535;

    private static final int INDEX_HUE = 0;
    private static final int INDEX_SAT = 1;
    private static final int INDEX_BRI = 2;

    public static LightState fromColor(final Color color) {
        return fromColor(color, ON);
    }

    public static LightState fromColor(final Color color, boolean on) {
        final LightState out = new LightState();

        float[] hsv = new float[3];

        hsv = Color.RGBtoHSB(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                hsv
        );

        int h = (int)(hsv[INDEX_HUE] * MAX_HUE);
        byte s = (byte)(hsv[INDEX_SAT] * MAX_RATIO);
        byte b = (byte)(hsv[INDEX_BRI] * MAX_RATIO);

        out.setOn(on);
        out.setHue(h);
        out.setSat(s);
        out.setBri(b);

        return out;
    }

    public static Color fromLightState(final LightState light) {
        if (!light.isOn()) {
            return Color.BLACK;
        }

        float h = (float)light.getHue();
        float s = (float)light.getSat() / MAX_RATIO;
        float b = (float)light.getBri() / MAX_RATIO;

        return Color.getHSBColor(h, s, b);
    }
}
