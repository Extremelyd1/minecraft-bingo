package com.extremelyd1.gameboard.boardEntry;

/**
 * Represents an dynamic entry on the scoreboard that can change its value
 * @param <T> The type of the dynamic value
 */
public class DynamicBoardEntry<T> extends BoardEntry {

    private T value;

    public DynamicBoardEntry(String format, T value) {
        super(format);

        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public String getString() {
        return String.format(this.string, this.value);
    }

}
