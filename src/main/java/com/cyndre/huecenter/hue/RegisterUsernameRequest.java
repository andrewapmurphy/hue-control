package com.cyndre.huecenter.hue;

import java.util.Objects;

public class RegisterUsernameRequest {
    private String devicetype = "";

    public RegisterUsernameRequest() {
    }

    public RegisterUsernameRequest(String devicetype) {
        this.devicetype = devicetype;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegisterUsernameRequest that = (RegisterUsernameRequest) o;
        return devicetype.equals(that.devicetype);
    }

    @Override
    public int hashCode() {
        return Objects.hash(devicetype);
    }

    public String getDevicetype() {
        return devicetype;
    }

    public void setDevicetype(String devicetype) {
        this.devicetype = devicetype;
    }


}
