package com.extremelyd1.potion;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionEffects {

    /**
     * The resistance 5 potion effect lasting 150 ticks = 7.5 seconds.
     */
    public static final PotionEffect RESISTANCE = new PotionEffect(
            PotionEffectType.RESISTANCE,
            150,
            4,
            false,
            false
    );

    /**
     * Blindness potion effect applied on game start lasting 100 ticks = 5 seconds.
     */
    public static final PotionEffect BLINDNESS = new PotionEffect(
            PotionEffectType.BLINDNESS,
            100,
            0,
            false,
            false
    );

    /**
     * Darkness potion effect applied on game start lasting 100 ticks = 5 seconds.
     */
    public static final PotionEffect DARKNESS = new PotionEffect(
            PotionEffectType.DARKNESS,
            100,
            0,
            false,
            false
    );
}
