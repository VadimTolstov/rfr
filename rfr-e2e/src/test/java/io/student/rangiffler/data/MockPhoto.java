package io.student.rangiffler.data;

/**
 * Describes the photos supplied by the mock travel feed.
 */
public enum MockPhoto {
    AMSTERDAM("Netherlands", "Амстердам!", true, "DpjQbKSv0gGPQBn9vaOtPg8E"),
    PARIS("France", "Париж!", true, "dyyxk3tTNjV+piXnBwk6JRqs"),
    SAINT_PETERSBURG("Russian Federation", "Петербург!", true, "7d0pe2mLsqcimwRkutewRtMW"),
    NEW_YORK("United States", "Нью-Йорк!", true, "yxQeVbbZiY28e69Ur+7GvPdi"),
    PRAGUE("Czech Republic", "Старая Прага завораживает!", false, "UKyuIJ4wVH+SLEZ+A8CjgCHw"),
    MUNICH("Germany", "Мюнхен - мой любимый город!", false, "J+M35tjxPIafG719/Z3kJm0V"),
    THAILAND("Thailand", "Обожаю Таиланд!", false, "ttq156TFJ0tl4b2WxmsSEG+V");

    private final String country;
    private final String description;
    private final boolean own;
    private final String image;

    MockPhoto(String country, String description, boolean own, String image) {
        this.country = country;
        this.description = description;
        this.own = own;
        this.image = image;
    }

    public String country() {
        return country;
    }

    public String description() {
        return description;
    }

    public boolean own() {
        return own;
    }

    public String image() {
        return image;
    }
}
