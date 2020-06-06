# Minecraft Bingo
Item bingo in Minecraft

![Item Bingo](https://i.imgur.com/7qXBAQK.png)

## What is minecraft bingo?
Minecraft Bingo is a gamemode in Minecraft in which teams battle in order to collect items on their bingo card.
The first team to reach a completed row, column or diagonal wins the game.
Collecting items is simply done through the vanilla game experience of minecraft.

## Usage
Players that have OP on the server have access to all commands. Before being able to start the game, a player needs to create teams with the /team command. Other settings can be configured as explained in the commands section below. As soon as the game is started, teams are scattered across the map (players within teams are grouped together). The teams work together to gather the items on their bingo card. If a team successfully completed the card, the game ends. When, however, the time limit is reached, a winner is decided based on the number of items collected by each of the teams. The team with the highest number of items wins, or the game ends in a tie.
Note that the bingo card can be right-clicked in order to view which items need to be gathered.

## Install
The plugin requires a Spigot server running version 1.15.2. Move the `.txt` files and the `images/images.zip` in the `item_data` folder to `<server>/plugins/MinecraftBingo/item_data`. Then unzip the `images/images.zip` archive.
The first time you run the plugin a config file will be generated in `<server>/plugins/MinecraftBingo`, in which you can edit some configuration settings.

## Commands
#### Team manage command
- `/team [random|add|remove]`
  - `/team random <number of teams>` Create a set number of teams dividing the players randomly over them
  - `/team add <player name> <team name>` Add a player to a given team  
  Possible team names are: Red, Blue, Green, Yellow, Pink, Aqua, White, Gray
  - `/team remove <player name>` Remove a player from a given team

#### Game start/end commands
- `/start` Start the game
- `/end` End the game

#### Configuration commands  
- `/pvp` Enable/disable PvP
- `/maintenance` Enable maintenance mode (this will disallow all non-op players from joining)
- `/wincondition <full|number of lines to complete>` Change the wincondition to either a full card or a number of lines (rows, columns or diagonals) to complete in order to win
- `/itemdistribution <S,A,B,C,D>` Change the item distribution scales, the number of S, A, B, C, D tier items that appear on the bingo card
- `/timer <enable|disable|length>` Enable/disable the timer or set the length of the timer

#### Miscellaneous commands
- `/bingo` Check the items on the card
- `/card` Receive a new bingo card (if somehow lost)
- `/reroll` Reroll the items on the bingo card
