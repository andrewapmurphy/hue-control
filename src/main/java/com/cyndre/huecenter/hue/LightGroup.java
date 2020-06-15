package com.cyndre.huecenter.hue;

import java.util.List;
import java.util.Objects;

public class LightGroup {
    private String name;
    private List<String> lights;
    private LightState action;;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getLights() {
        return lights;
    }

    public void setLights(List<String> lights) {
        this.lights = lights;
    }

    public LightState getAction() {
        return action;
    }

    public void setAction(LightState action) {
        this.action = action;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LightGroup that = (LightGroup) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(lights, that.lights) &&
                Objects.equals(action, that.action);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, lights, action);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
