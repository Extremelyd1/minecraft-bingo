package com.extremelyd1.gameboard.boardEntry;

import net.kyori.adventure.text.Component;
import org.intellij.lang.annotations.RegExp;

/**
 * Represents a dynamic entry on the scoreboard that can change its value.
 * If used with a Component as value, will append the value component to the end of the given base component.
 * @param <T> The type of the dynamic value.
 */
public class DynamicBoardEntry<T> extends BoardEntry {
    /**
     * The replacement placeholder string that will be replaced with the value of this entry.
     */
    @RegExp
    public static final String replacePlaceholder = "%s";

    /**
     * The dynamic value of this entry.
     */
    private T value;

    public DynamicBoardEntry(Component component, T value) {
        super(component);

        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public Component getComponent() {
        if (value instanceof Component valueComponent) {
            return this.component.append(valueComponent);
        }

        return this.component.replaceText(
                builder -> builder.match(replacePlaceholder).replacement(String.valueOf(value)).once()
        );
    }

}
