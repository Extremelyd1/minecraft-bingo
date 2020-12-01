package com.extremelyd1.util;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;

import java.util.HashSet;
import java.util.Set;

public class RecipeUtil {

    private final Set<NamespacedKey> recipeKeys;

    public RecipeUtil() {
        recipeKeys = new HashSet<>();

        Bukkit.getServer().recipeIterator().forEachRemaining(recipe -> {
            if (recipe instanceof ComplexRecipe) {
                recipeKeys.add(((ComplexRecipe) recipe).getKey());
            }
            if (recipe instanceof BlastingRecipe) {
                recipeKeys.add(((BlastingRecipe) recipe).getKey());
            }
            if (recipe instanceof CampfireRecipe) {
                recipeKeys.add(((CampfireRecipe) recipe).getKey());
            }
            if (recipe instanceof CookingRecipe) {
                recipeKeys.add(((CookingRecipe<?>) recipe).getKey());
            }
            if (recipe instanceof FurnaceRecipe) {
                recipeKeys.add(((FurnaceRecipe) recipe).getKey());
            }
            if (recipe instanceof ShapedRecipe) {
                recipeKeys.add(((ShapedRecipe) recipe).getKey());
            }
            if (recipe instanceof ShapelessRecipe) {
                recipeKeys.add(((ShapelessRecipe) recipe).getKey());
            }
            if (recipe instanceof SmithingRecipe) {
                recipeKeys.add(((SmithingRecipe) recipe).getKey());
            }
            if (recipe instanceof SmokingRecipe) {
                recipeKeys.add(((SmokingRecipe) recipe).getKey());
            }
            if (recipe instanceof StonecuttingRecipe) {
                recipeKeys.add(((StonecuttingRecipe) recipe).getKey());
            }
        });
    }

    public void discoverAllRecipes(Player player) {
        recipeKeys.forEach(player::discoverRecipe);
    }

}
