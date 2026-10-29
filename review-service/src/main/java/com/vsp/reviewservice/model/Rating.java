package com.vsp.reviewservice.model;

public enum Rating {
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5);

    private final int value;

    Rating(int value){
        this.value = value;
    }

    public int getValue(){
        return this.value;
    }

    public static Rating fromValue(int value) {
        return switch (value) {
            case 1 -> Rating.ONE;
            case 2 -> Rating.TWO;
            case 3 -> Rating.THREE;
            case 4 -> Rating.FOUR;
            case 5 -> Rating.FIVE;
            default -> throw new IllegalArgumentException(
                    "Invalid rating: " + value
            );
        };
    }

}
