package com.cyndre.huecenter.hue;

import java.util.Objects;

public class RegisterUsernameRequest {
    private String username = "";
    private String devicetype = "";

    public RegisterUsernameRequest() {
    }

    public RegisterUsernameRequest(String username, String devicetype) {
        this.username = username;
        this.devicetype = devicetype;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegisterUsernameRequest that = (RegisterUsernameRequest) o;
        return username.equals(that.username) &&
                devicetype.equals(that.devicetype);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, devicetype);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDevicetype() {
        return devicetype;
    }

    public void setDevicetype(String devicetype) {
        this.devicetype = devicetype;
    }


}
