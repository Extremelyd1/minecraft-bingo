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
     * The base component that contains the replaceable or acts as the prefix for a value.
     */
    private final Component baseComponent;

    public DynamicBoardEntry(Component component, T value) {
        super(null);

        baseComponent = component;

        setValue(value);
    }

    /**
     * Set the value for this dynamic entry. This will immediately update the entry on the scoreboard.
     * @param value The value to set.
     */
    public void setValue(T value) {
        if (value instanceof Component valueComponent) {
            this.score.customName(baseComponent.append(valueComponent));
            return;
        }

        this.score.customName(baseComponent.replaceText(
                builder -> builder.match(replacePlaceholder).replacement(String.valueOf(value)).once()
        ));
    }
}
