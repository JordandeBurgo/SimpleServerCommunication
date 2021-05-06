import java.util.Scanner;

/**
 * Contains the main logic part of the game, as it processes.
 *
 */
public class GameLogic {

    /**
     * Stores the map as an object.
     */
	private Map map;

    /**
     * Stores the human player as an object.
     */
	private HumanPlayer hero;

    /**
     * Stores the bot as an object.
     */
	private BotPlayer villain;

    /**
     * Stores whether the game has ended.
     */
    private boolean gameOn = true;

    /**
     * Stores the character of the tile the bot is currently on.
     */
    private char botCurrentlyOn;

    /**
     * Stores the character of the tile the player is currently on.
     */
    private char playerCurrentlyOn;

	/**
	 * Constructor that accepts a fileLocation for the map.
     *
     * @param fileLocation : The filename of the map.
	 */
	public GameLogic(String fileLocation) {
		map = new Map(fileLocation);
	}

    /**
	 * Checks if the game is running
	 *
     * @return : If the game is running.
     */
    protected boolean gameRunning() {
        return gameOn;
    }

    /**
	 * Returns the gold required to win.
	 *
     * @return : Gold required to win.
     */
    protected String hello() {
        return "Gold to win: " + map.getGoldRequired();
    }
	
	/**
	 * Returns the gold currently owned by the player.
	 *
     * @return : Gold currently owned.
     */
    protected String gold() {
        return "Gold owned: " + hero.getGoldGathered();
    }

    /**
     * Checks if movement is legal and updates player's location on the map.
     *
     * @param direction : The direction of the movement.
     * @param player : The player which is moving
     * @return : Protocol if success or not.
     */
    protected String move(char direction, char player) {

        /**
         * Stores the x coordinate of where the player was.
         */
        int previousTileX;

        /**
         * Stores the y coordinate of where the player was.
         */
        int previousTileY;

        switch(direction){
            case 'n':
                //If the player wanted to move north
                if(player == 'P') {
                    //Set the previous x and y to what it is currently on.
                    previousTileX = hero.getCurrentX();
                    previousTileY = hero.getCurrentY();
                    //The set the current y to the current y -1 to move north
                    hero.setCurrentY(hero.getCurrentY() - 1);
                }
                else{
                    //Here is the same but for the bot.
                    previousTileX = villain.getCurrentX();
                    previousTileY = villain.getCurrentY();
                    villain.setCurrentY(villain.getCurrentY() - 1);
                }
                break;
            case 'e':
                //If the player wanted to move east.
                if(player == 'P') {
                    previousTileX = hero.getCurrentX();
                    previousTileY = hero.getCurrentY();
                    //Then we just add one to the current x value.
                    hero.setCurrentX(hero.getCurrentX() + 1);
                }
                else{
                    previousTileX = villain.getCurrentX();
                    previousTileY = villain.getCurrentY();
                    villain.setCurrentX(villain.getCurrentX() + 1);
                }
                break;
            case 's':
                //If the player wanted to move south.
                if(player == 'P') {
                    previousTileX = hero.getCurrentX();
                    previousTileY = hero.getCurrentY();
                    //We add one to the current y value.
                    hero.setCurrentY(hero.getCurrentY() + 1);
                }
                else{
                    previousTileX = villain.getCurrentX();
                    previousTileY = villain.getCurrentY();
                    villain.setCurrentY(villain.getCurrentY() + 1);
                }
                break;
            case 'w':
                //And if the player wanted to move west.
                if(player == 'P') {
                    previousTileX = hero.getCurrentX();
                    previousTileY = hero.getCurrentY();
                    //Take 1 away from the current x value.
                    hero.setCurrentX(hero.getCurrentX() - 1);
                }
                else{
                    previousTileX = villain.getCurrentX();
                    previousTileY = villain.getCurrentY();
                    villain.setCurrentX(villain.getCurrentX() - 1);
                }
                break;
            default:
                //If the direction was anything other than n, e, s, w then it fails.
                return"FAIL";
        }

        //Here we update the map
        if(player == 'P'){
            if(hero.changed()){
                //The tile we were on gets changed back to whatever it was before we moved onto it.
                map.getMap()[previousTileY][previousTileX] = playerCurrentlyOn;
                //The tile we are on gets updated to whatever the player is standing on.
                playerCurrentlyOn = map.getMap()[hero.getCurrentY()][hero.getCurrentX()];
                //The tile we are on gets overwritten as 'P'.
                map.getMap()[hero.getCurrentY()][hero.getCurrentX()] = 'P';
            }
            else{
                //If the player didn't move it failed.
                return "FAIL";
            }
        }
        else{
            //This is just the same for the bot.
            if(villain.changed()) {
                map.getMap()[previousTileY][previousTileX] = botCurrentlyOn;
                botCurrentlyOn = map.getMap()[villain.getCurrentY()][villain.getCurrentX()];
                map.getMap()[villain.getCurrentY()][villain.getCurrentX()] = 'B';
            }
            else{
                return "FAIL";
            }
        }

        //If we haven't returned fail yet then it was a success.
        return "SUCCESS";
    }

    /**
     * Converts the map from a 2D char array to a single string.
     *
     * @param player : The player which is looking
     * @return : A String representation of the game map.
     */
    protected String look(char player) {

        /**
         * Stores the current x coordinate of the player.
         */
        int currentX = (player == 'P') ? hero.getCurrentX() : villain.getCurrentX();

        /**
         * Stores the current y coordinate of the player.
         */
        int currentY = (player == 'P') ? hero.getCurrentY() : villain.getCurrentY();

        /**
         * Stores the subsection of the map that the player is able to see as a string.
         */
        String stringMiniMap = "";

        //Loops through from y-2 to y+2.
        for(int i = currentY-2; i<=currentY+2; i++){
            //And then from x-2 to x+2 to make it a 5 by 5 grid around the player.
            for(int j = currentX-2; j<=currentX+2; j++){
                //Tries adding the character at each location to the map
                try {
                    stringMiniMap = stringMiniMap.concat(Character.toString(map.getMap()[i][j]));
                }
                catch(Exception e){
                    //But it might be out of the index of the map so if it is we just need a '#' there.
                    stringMiniMap = stringMiniMap.concat("#");
                }
            }
            //Then we get to the end of the row so add a new line.
            stringMiniMap = stringMiniMap.concat("\n");
        }

        return stringMiniMap;
    }

    /**
     * Processes the player's pickup command, updating the map and the player's gold amount.
     *
     * @return If the player successfully picked-up gold or not.
     */
    protected String pickup() {
        //If the tile we are on is a gold tile
        if(playerCurrentlyOn == 'G'){
            //Increment gold and output how much gold the player has.
            hero.incrementGold();
            playerCurrentlyOn = '.';
            return ("SUCCESS. Gold owned: " + hero.getGoldGathered());
        }
        //If it didn't return anything yet it failed.
        return "FAIL";
    }

    /**
     * Quits the game, shutting down the application.
     */
    protected void quitGame(DODClient client) {
        //Check if the win conditions have been met
        if(playerCurrentlyOn == 'E' && hero.getGoldGathered() >= map.getGoldRequired()){
            //If they have the player has won.
            client.outputToClient("WIN");
        }
        else{
            //Otherwise it is a loss.
            client.outputToClient("LOSE");
        }
        //Set gameOn to false to end the while loop in main.
        gameOn = false;
    }


    /**
     * @return : The map.
     */
    protected Map getMap(){
        return map;
    }

    /**
     * @param args : parameters of the client.
     */
	public static void main(String[] args) {

        DODClient client = new DODClient(args);

        client.outputToClient("DODClient");
        client.outputToClient("Where is the map?");
        String map = client.getMap();

        /**
         * Stores an instance of GameLogic running the game.
         */
        GameLogic logic = new GameLogic(map);


        /**
         * Stores whether it is the human's turn.
         */
        boolean humanTurn = true;

        //Creates instances for the hero and villain.
        logic.hero = new HumanPlayer(logic, client);
        logic.villain = new BotPlayer(logic);



        client.outputToClient(" ________  ___  ___  ________   ________  _______   ________  ________   ________           ________  ________      ________  ________  ________  _____ ______      ");
        client.outputToClient("|\\   ___ \\|\\  \\|\\  \\|\\   ___  \\|\\   ____\\|\\  ___ \\ |\\   __  \\|\\   ___  \\|\\   ____\\         |\\   __  \\|\\  _____\\    |\\   ___ \\|\\   __  \\|\\   __  \\|\\   _ \\  _   \\    ");
        client.outputToClient(" \\ \\  \\ \\\\ \\ \\  \\\\\\  \\ \\  \\\\ \\  \\ \\  \\  __\\ \\  \\_|/_\\ \\  \\\\\\  \\ \\  \\\\ \\  \\ \\_____  \\        \\ \\  \\\\\\  \\ \\   __\\     \\ \\  \\ \\\\ \\ \\  \\\\\\  \\ \\  \\\\\\  \\ \\  \\\\|__| \\  \\");
        client.outputToClient("  \\ \\  \\_\\\\ \\ \\  \\\\\\  \\ \\  \\\\ \\  \\ \\  \\|\\  \\ \\  \\_|\\ \\ \\  \\\\\\  \\ \\  \\\\ \\  \\|____|\\  \\        \\ \\  \\\\\\  \\ \\  \\_|      \\ \\  \\_\\\\ \\ \\  \\\\\\  \\ \\  \\\\\\  \\ \\  \\    \\ \\  \\");
        client.outputToClient("   \\ \\_______\\ \\_______\\ \\__\\\\ \\__\\ \\_______\\ \\_______\\ \\_______\\ \\__\\\\ \\__\\____\\_\\  \\        \\ \\_______\\ \\__\\        \\ \\_______\\ \\_______\\ \\_______\\ \\__\\    \\ \\__\\");
        client.outputToClient("    \\|_______|\\|_______|\\|__| \\|__|\\|_______|\\|_______|\\|_______|\\|__| \\|__|\\_________\\        \\|_______|\\|__|         \\|_______|\\|_______|\\|_______|\\|__|     \\|__|");
        client.outputToClient("                                                                           \\|_________|\"");


        //Set the position for the hero and villain.
        logic.botCurrentlyOn = logic.map.getMap()[logic.villain.getCurrentY()][logic.villain.getCurrentX()];
        logic.map.getMap()[logic.villain.getCurrentY()][logic.villain.getCurrentX()] = 'B';
        logic.playerCurrentlyOn = logic.map.getMap()[logic.hero.getCurrentY()][logic.hero.getCurrentX()];
        logic.map.getMap()[logic.hero.getCurrentY()][logic.hero.getCurrentX()] = 'P';

        while(logic.gameRunning()){
            //If the bot has moved onto the player or vice versa
            if(logic.playerCurrentlyOn == 'B' || logic.botCurrentlyOn == 'P'){
                //End the game.
                client.outputToClient("The villain has caught you.");
                logic.quitGame(client);
                break;
            }
            if(humanTurn){
                //If it is the humans turn call the function that allows the user to make a move and output result.
                client.outputToClient(client.waitForUserInput(logic.hero));
            }
            else{
                //Otherwise get  bot to make a move and output result.
                client.outputToClient(logic.villain.decideNextAction());
            }
            //Toggle humanTurn.
            humanTurn = !humanTurn;
        }

    }
}