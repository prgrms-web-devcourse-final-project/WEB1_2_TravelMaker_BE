package edu.example.wayfarer.entity.enums;

import lombok.Getter;

@Getter
public enum Color {
    RED("#F72216"),
    ORANGE("#FF9500"),
    YELLOW("#FFCC00"),
    GREEN("#34c759"),
    BABYBLUE("#00C7BE"),
    BLUE("#007AFF"),
    PURPLE("#AF52DE"),
    HOTPINK("#FF2D55"),
    BROWN("#A2845E"),
    WHITE("#FFFFFF"),
    GREY("#8E8E93");

    private final String hexCode;

    Color(String hexCode) {
        this.hexCode = hexCode;
    }


}
