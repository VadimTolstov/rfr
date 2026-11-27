package io.student.rangiffler.data;

/**
 * Describes the photos supplied by the mock travel feed.
 */
public enum MockPhoto {
    AMSTERDAM("Netherlands", "Амстердам!", true),
    PARIS("France", "Париж!", true),
    SAINT_PETERSBURG("Russian Federation", "Петербург!", true),
    NEW_YORK("United States", "Нью-Йорк!", true),
    PRAGUE("Czech Republic", "Старая Прага завораживает!", false),
    MUNICH("Germany", "Мюнхен - мой любимый город!", false),
    THAILAND("Thailand", "Обожаю Таиланд!", false);

    private final String country;
    private final String description;
    private final boolean own;

    MockPhoto(String country, String description, boolean own) {
        this.country = country;
        this.description = description;
        this.own = own;
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
}
