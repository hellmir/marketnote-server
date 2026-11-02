package com.personal.marketnote.community.domain.review;

import lombok.Getter;

@Getter
public enum RatingPoint {
    FIVE("5점", 5),
    FOUR("4점", 4),
    THREE("3점", 3),
    TWO("2점", 2),
    ONE("1점", 1);

    private final String description;
    private final int value;

    RatingPoint(String description, int value) {
        this.description = description;
        this.value = value;
    }

    public static boolean isFive(int point) {
        return point == FIVE.getValue();
    }

    public static boolean isFour(int point) {
        return point == FOUR.getValue();
    }

    public static boolean isThree(int point) {
        return point == THREE.getValue();
    }

    public static boolean isTwo(int point) {
        return point == TWO.getValue();
    }

    public static boolean isOne(int point) {
        return point == ONE.getValue();
    }
}
