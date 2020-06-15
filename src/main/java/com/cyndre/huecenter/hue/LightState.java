package com.cyndre.huecenter.hue;

import java.awt.*;

public class LightState {
    private static final float MAX_HUE = 65535F;
    private static final float MAX_SAT = 254F;
    private static final float MAX_BRI = 254F;

    private static final int INDEX_HUE = 0;
    private static final int INDEX_SAT = 1;
    private static final int INDEX_BRI = 2;

    private boolean on;
    private int bri;
    private int hue;
    private int sat;

    //true if the light should be on.
    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    //brightness, in range 0 - 254. 0 is not off.
    public int getBri() {
        return bri;
    }

    //hue, in range 0 - 65535.
    public void setBri(int bri) {
        this.bri = bri;
    }

    //saturation, in range 0 - 254.
    public int getHue() {
        return hue;
    }

    public void setHue(int hue) {
        this.hue = hue;
    }

    public int getSat() {
        return sat;
    }

    public void setSat(int sat) {
        this.sat = sat;
    }

    public static Color toColor(final LightState lightState) {
        if (lightState == null || lightState.on == false) {
            return Color.BLACK;
        }

        float h = (float)lightState.hue / MAX_HUE;
        float s = (float)lightState.sat / MAX_SAT;
        float b = (float)lightState.bri / MAX_BRI;

        Color color = Color.getHSBColor(h, s, b);

        //Color color = HSVtoRGB(h, s, b);

        //color = new Color(color.getBlue(), color.getGreen(), color.getRed());

        //LightState newState = toLightState(color);

        return color;
    }

    public static LightState toLightState(final Color color) {
        final LightState lightState = new LightState();

        if (color == null || color.equals(Color.BLACK)) {
            lightState.on = false;
        } else {
            lightState.on = true;

            //final float[] hsbValues = RGBtoHSV(color);
            final float[] hsbValues = Color.RGBtoHSB(
                    color.getRed(),
                    color.getGreen(),
                    color.getBlue(),
                    null
            );

            lightState.hue = (int)(hsbValues[INDEX_HUE] * MAX_HUE);
            lightState.sat = (int)(hsbValues[INDEX_SAT] * MAX_SAT);
            lightState.bri = (int)(hsbValues[INDEX_BRI] * MAX_BRI);
        }

        return lightState;
    }

    @Override
    public String toString() {
        return "{" +
                " on: " + on +
                ", bri: " + bri +
                ", hue: " + hue +
                ", sat: " + sat +
                " }";
    }
}
