package com.extremelyd1.gameboard.boardEntry;

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
