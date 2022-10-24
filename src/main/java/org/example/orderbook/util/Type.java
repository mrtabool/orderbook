package org.example.orderbook.util;

import java.util.Locale;

public enum Type {

    BID(""),
    ASK(""),
    SPREAD("");

    private String type;

    Type(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static Type valueOfOrNull(String value) {
        Type type;
        try {
            type = Type.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            type = null;
        }
        return type;
    }
}
