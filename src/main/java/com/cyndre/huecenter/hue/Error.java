package com.cyndre.huecenter.hue;

import java.util.Objects;

public class Error {
    private int type;
    private String address;
    private String description;

    public Error() {
    }

    public Error(int type, String address, String description) {
        this.type = type;
        this.address = address;
        this.description = description;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Error error = (Error) o;
        return type == error.type &&
                Objects.equals(address, error.address) &&
                Objects.equals(description, error.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, address, description);
    }
}
