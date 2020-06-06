package com.extremelyd1.potion;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionEffects {

    /**
     * The resistance 4 potion effect lasting 150 ticks = 7.5 seconds
     * Does not display particles
     */
    public static final PotionEffect RESISTANCE = new PotionEffect(
            PotionEffectType.DAMAGE_RESISTANCE,
            150,
            4,
            false,
            false
    );

}
