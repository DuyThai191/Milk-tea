package com.milktea.models;

import java.io.Serializable;

public enum Size implements Serializable {
    S("Size S (Vừa)", 0.0),
    M("Size M (Lớn)", 5000.0),
    L("Size L (Khổng lồ)", 10000.0);

    private final String displayName;
    private final double extraPrice;

    Size(String displayName, double extraPrice) {
        this.displayName = displayName;
        this.extraPrice = extraPrice;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getExtraPrice() {
        return extraPrice;
    }

    @Override
    public String toString() {
        if (extraPrice > 0) {
            return String.format("%s (+%,.0f đ)", displayName, extraPrice);
        }
        return displayName;
    }
}
