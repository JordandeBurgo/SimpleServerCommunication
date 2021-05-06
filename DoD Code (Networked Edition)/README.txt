Dungeons of Doom is a 1 player game (against a bot) in which a player must collect
a specific amount of gold before they can leave the game on an exit tile. The player
must not get caught by the villain (computer controlled player) before collecting 
enough gold to escape. If the player is caught by the villain then the player loses.

Run the game by launching GameLogic.java

To select a map all that is required is that you input the entire file path of where
the map is stored when the program asks "Where is the map?". For example, 
"C:\Users\jorda\OneDrive\Documents\DoD Code\simple_example_map.txt". On linux you can use the file location
/u/d/jefdb20/CW1-jefdb20/DoD Code (Networked Edition)/out/production/DoD Code/sem.txt (editted for you user).


Board representation:
	Empty Floor - '.'
	Player - 'P'
	Bot - 'B'
	Gold - 'G' (will become '.' after picking the gold up)
	Exit - 'E'
	Wall - '#' (player's cannot walk through walls)

Controls:
	HELLO - Outputs how much gold is required to escape the dungeon
	GOLD - Outputs how much gold the player has collected so far
	MOVE <direction> - Moves the player in direction indicated
	PICKUP - Picks up gold on current location
	LOOK - Outputs a 5x5 grid around the player

All commands take up a player's turn, regardless of whether they were successful or not.
Once the command has been entered a response is printed and the turn is ended.
	
The code uses a 2D array of chars to represent the map. There are 5 classes, Node,
HumanPlayer, BotPlayer, Map and GameLogic. GameLogic controls most of what is going on.
The bot uses dijkstras to find the shortest path to the player and to random points when
it can't see a player to reduce time spent using the look command.