package com.malaclord.clientcommands.client.command.argument;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public enum PotionTypes {
    NORMAL ("normal"),
    SPLASH ("splash"),
    LINGER ("linger")
    ;

    private final String type;

    PotionTypes(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }

    public static PotionTypes getType(String type) {
        for (PotionTypes value : values()) {
            if (Objects.equals(value.toString(), type)) return value;
        }

        return null;
    }

    public static List<String> getTypeNames() {
        List<String> names = new ArrayList<>();

        for (PotionTypes value : values()) {
            names.add(value.toString());
        }

        return names;
    }
}
