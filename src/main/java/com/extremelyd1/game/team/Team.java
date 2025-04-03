package com.extremelyd1.game.team;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Team {

    /**
     * The name of the team.
     */
    protected final String name;
    /**
     * The color of the team.
     */
    protected final NamedTextColor color;
    /**
     * The list of players in this team, stored as UUIDs.
     */
    protected final List<UUID> uuids;

    /**
     * Whether this is a spectator team.
     */
    protected final boolean isSpectatorTeam;

    public Team() {
        this("Spectators", NamedTextColor.WHITE, true);
    }

    public Team(String name, NamedTextColor color, boolean isSpectatorTeam) {
        this.name = name;
        this.color = color;

        this.uuids = new ArrayList<>();

        this.isSpectatorTeam = isSpectatorTeam;
    }

    /**
     * Add a player to this team, and whether to notify them of their new team.
     * @param player The player to add.
     * @param notify Whether to notify the player of their new team.
     */
    void addPlayer(Player player, boolean notify) {
        this.uuids.add(player.getUniqueId());

        if (notify) {
            player.sendMessage(Component
                    .text("Joined ")
                    .color(NamedTextColor.WHITE)
                    .append(Component
                            .text(name)
                            .color(color)
                    ).append(Component
                            .text(" team")
                            .color(NamedTextColor.WHITE)
                    )
            );
        }
    }

    /**
     * Remove a player from this team.
     * @param player The player to remove.
     */
    void removePlayer(Player player) {
        this.uuids.remove(player.getUniqueId());
    }

    /**
     * Check whether the given player is on this team.
     * @param player The player to check for.
     * @return Whether to player is on this team.
     */
    public boolean contains(Player player) {
        return this.uuids.contains(player.getUniqueId());
    }

    /**
     * Removes all players from this team.
     */
    public void clear() {
        uuids.clear();
    }

    public String getName() {
        return name;
    }

    public NamedTextColor getColor() {
        return color;
    }

    /**
     * Gets player objects in this team by retrieving them by UUID,
     * does not return any null objects.
     * @return An iterable of Player instances.
     */
    public Iterable<Player> getPlayers() {
        return () -> uuids.stream()
                // Filter out players that are not valid anymore
                .filter(uuid -> Bukkit.getPlayer(uuid) != null)
                // Map everything to their respective Player instance
                .map(Bukkit::getPlayer)
                // Return an iterator
                .iterator();
    }

    public Iterable<UUID> getUUIDs() {
        return uuids;
    }

    /**
     * Gets the number of players on this team.
     * @return The number of players on this team.
     */
    public int getNumPlayers() {
        return uuids.size();
    }

    public boolean isSpectatorTeam() {
        return isSpectatorTeam;
    }

}
