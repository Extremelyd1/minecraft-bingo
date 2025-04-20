# Minecraft Bingo
Item bingo in Minecraft

![Item Bingo](https://i.imgur.com/7qXBAQK.png)

## What is minecraft bingo?
Minecraft Bingo is a gamemode in Minecraft in which teams battle in order to collect items on their bingo card.
The first team to reach a completed row, column or diagonal wins the game.
Collecting items is simply done through the vanilla game experience of Minecraft.

## Usage
Players that have OP on the server have access to all commands. 
Before being able to start the game, a player needs to create teams with the `/team` command. 
Other settings can be configured as explained in the commands section below. 
As soon as the game is started, teams are scattered across the map (players within teams are grouped together). 
The teams work together to gather the items on their bingo card. 
If a team successfully completed the card, the game ends. 
When, however, the time limit is reached, a winner is decided based on the number of items collected by each of the teams. 
The team with the highest number of items wins, or the game ends in a tie.
Note that the bingo card can be right-clicked in order to view which items need to be gathered.

## Install
Download the [latest release](https://github.com/Extremelyd1/minecraft-bingo/releases/latest) or compile it yourself using Gradle.
The plugin requires a [Paper](https://papermc.io/) server to run.
Place the `MinecraftBingo-[version].jar` file in the plugins directory of your server.
The first time you run the plugin a config file will be generated in `<server>/plugins/MinecraftBingo`, in which you can edit some configuration settings.

## Commands
#### Team manage command
- `/team [random|add|remove]`
  - `/team random <number of teams> [-e] [players...]` Create a set number of teams dividing the players randomly over them. If given a list of player names, it will only create teams with those players (or exclude those player if the flag `-e` is given).
  - `/team add <player name> <team name>` Add a player to a given team  
  Possible team names are: Red, Blue, Green, Yellow, Pink, Aqua, Orange, Gray
  - `/team remove <player name>` Remove a player from a given team

#### Game start/end commands
- `/start` Start the game
- `/end` End the game

#### Configuration commands  
- `/pvp` Enable/disable PvP
- `/maintenance` Enable maintenance mode (this will disallow all non-OP players from joining)
- `/wincondition <full|lines|lockout> [number]` Change the win condition to either a full card, a number of lines (rows, columns or diagonals) to complete in order to win or lockout. 
  In case of 'lines' or 'lockout' you can specify a number to indicate how many lines needed to be completed, or after how many collections an item locks.
  (alias: `/wincon`)
- `/itemdistribution <S> <A> <B> <C> <D>` Change the item distribution scales, the number of S, A, B, C, and D tier items that appear on the bingo card. 
  These numbers must add up to 25. (aliases: `/itemdist`, `/distribution`, `/dist`)
- `/timer <enable|disable|length>` Enable/disable the timer or set the length of the timer (the length can be specified in hours/minutes/seconds, such as `/timer 10m` or `/timer 1h20m30s`)

#### Miscellaneous commands
- `/bingo` Check the items on the card
- `/card` Receive a new bingo card (if somehow lost)
- `/reroll` Re-roll the items on the bingo card
- `/coordinates [message]` Sends your current coordinates to your team, optionally with a message (aliases: `/coord`, `/coords`)
- `/all [message]` Allows players to talk to all players in the game (aliases: `/a`, `/g`, `/global`)
- `/teamchat [message]` Allows players to talk to their team members (alias: `/tc`)
- `/channel <team|global>` Allows players to switch the default chat channel that chat messages will be sent to (alias `/c`)
- `/join [team name]` Allows players to join a team by the given name

## World generation
The plugin offers the ability to pre-generate worlds to reduce chunk generation lag during gameplay.
The following command can be used to manage this:
- `/generate <start world number> <number of worlds>` Start pre-generating the given number of worlds from the given index and storing them in zip format
- `/generate stop` Stop pre-generating worlds  

This command only works if the config value `pregeneration-mode.enabled` is set to `True`.
Pre-generation also requires you to set a world border for the worlds you want to have generated.
The sizes for these borders can be set in the config file: `border.overworld-size` and `border.nether-size`.
All chunks within the world border, including a buffer of 2 chunks outside the world border will be generated in this process.
When finished, the directories for the overworld and nether will be zipped as `world[number].zip` and placed in `<server>/plugins/MinecrftBingo/worlds/` directory.