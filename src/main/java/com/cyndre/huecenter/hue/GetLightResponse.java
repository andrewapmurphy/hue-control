package com.cyndre.huecenter.hue;

import java.util.Objects;

public class GetLightResponse {
    private LightState state;

    public LightState getState() {
        return state;
    }

    public void setState(LightState state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetLightResponse that = (GetLightResponse) o;
        return Objects.equals(state, that.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state);
    }

    @Override
    public String toString() {
        return "GetLightResponse{" +
                "state=" + state +
                '}';
    }
}
