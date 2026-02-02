package com.FlashBid_Main.FlashBid_Main.Item.domain;

public enum Category {
    ELECTRONICS("가전"),
    FASHION("의류"),
    SHOES("신발"),
    BAGS("가방"),
    ACCESSORIES("악세사리"),
    BEAUTY("뷰티"),
    SPORTS("스포츠"),
    BOOKS_MUSIC("도서/음반"),
    FURNITURE("가구/인테리어"),
    DIGITAL("디지털"),
    HOBBY("게임/취미"),
    OTHERS("기타");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
