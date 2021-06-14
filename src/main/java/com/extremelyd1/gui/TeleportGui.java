package com.extremelyd1.gui;

import com.extremelyd1.game.Game;
import com.extremelyd1.util.ArrayUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;

public class TeleportGui implements IGui{
    private final Inventory inv;
    private final Player[] players;
    private final Game game;
    private Player player;

    public TeleportGui(Game game, Player[] players){
        this.game = game;
        this.players = players;
        inv = Bukkit.createInventory(this, 27, ChatColor.BOLD + "Team Teleport");
    }

    public void openInventory(Player player) {
        this.player = player;
        initializeItems();
        player.openInventory(inv);
    }

    public void initializeItems() {
        Material playerHead = Material.PLAYER_HEAD;
        for (Player p : players) {
            if(!p.equals(player)){
                ItemStack head = createItem(playerHead, p.getPlayer());
                inv.addItem(head);
            }
        }
        for (int i = players.length; i < 27; i++)
            inv.addItem(new ItemStack(Material.AIR));
    }

    public ItemStack createItem(Material mat, OfflinePlayer player) {
        ItemStack stack = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) stack.getItemMeta();
        meta.setDisplayName(Bukkit.getPlayer(player.getName()).getName());
        meta.setLore(Arrays.asList("Teleport to this player"));
        meta.setOwningPlayer(player);
        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public void onGUIClick(Player whoClicked, int slot, ItemStack clickedItem) {
        if(clickedItem.getType().equals(Material.PLAYER_HEAD)){
            String playerName = clickedItem.getItemMeta().getDisplayName();
            Player player = Arrays.stream(players).filter(player1 -> player1.getName().equals(playerName)).collect(ArrayUtil.toSingleton());
            whoClicked.closeInventory();
            whoClicked.sendMessage(ChatColor.RED + "Teleport will commence in 5 seconds...");
            Bukkit
                    .getScheduler()
                    .scheduleSyncDelayedTask(
                            game.getPlugin(),
                            () -> whoClicked.teleport(player.getLocation()), 100L);
        }
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }
}