package com.cyndre.huecenter.hue;

public class LightState {
    private boolean on;
    private byte bri;
    private int hue;
    private byte sat;
    private int ct;

    //true if the light should be on.
    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    //brightness, in range 0 - 254. 0 is not off.
    public byte getBri() {
        return bri;
    }

    //hue, in range 0 - 65535.
    public void setBri(byte bri) {
        this.bri = bri;
    }

    //saturation, in range 0 - 254.
    public int getHue() {
        return hue;
    }

    public void setHue(int hue) {
        this.hue = hue;
    }

    public byte getSat() {
        return sat;
    }

    public void setSat(byte sat) {
        this.sat = sat;
    }

    //white color temperature, 154 (cold) - 500 (warm).
    public int getCt() {
        return ct;
    }

    public void setCt(int ct) {
        this.ct = ct;
    }
}
