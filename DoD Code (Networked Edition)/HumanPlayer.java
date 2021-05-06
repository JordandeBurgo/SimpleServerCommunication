import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.Scanner;

/**
 * Runs the game with a human player and contains code needed to read inputs.
 *
 */
public class HumanPlayer {

    private DODClient client;

    private String inputFromNetwork;

    /**
     * Stores the amount of gold gathered by the player.
     */
    private int goldGathered;

    /**
     * Stores the current x coordinate of the player.
     */
    private int currentX = -1;

    /**
     * Stores the current y coordinate of the player.
     */
    private int currentY = -1;

    /**
     * Stores the map as an object.
     */
    private Map map;

    /**
     * Stores the instance of the game that is running.
     */
    private GameLogic logic;

    /**
     * Stores the upper bound for the x coordinate of the random starting position.
     */
    private int upperX;

    /**
     * Stores the upper bound for the y coordinate of the random starting position.
     */
    private int upperY;

    /**
     * Stores the keywords which the user can enter that will cause something to happen.
     */
    private String[] commands = {"hello", "gold", "move", "pickup", "look", "quit"};

    /**
     * Stores the directions in which the player can move.
     */
    private char[] directions = {'n', 'e', 's', 'w'};

    /**
     * Stores whether the position changed successfully or not.
     */
    private boolean changeSuccess;

    /**
     * Constructor that accepts an instance of GameLogic to play.
     *
     * @param logical : The instance of GameLogic to play.
     */
    public HumanPlayer(GameLogic logical, DODClient dodClient){
        client = dodClient;
        logic = logical;
        map = logic.getMap();
        goldGathered = 0;
        upperX = map.getMap()[0].length;
        upperY = map.getMap().length;

        /**
         * Creates a random object.
         */
        Random rand = new Random();

        //While the position of the bot has not been set
        while(currentX == -1 && currentY == -1){

            /**
             * Stores a random number for the x coordinate between 0 and upperX.
             */
            int randX = rand.nextInt(upperX);

            /**
             * Stores a random number for the y coordinate between 0 and upperY.
             */
            int randY = rand.nextInt(upperY);

            //Checks that the player can actually start at this position.
            if(map.collisionDetected(randX, randY) == 'F' || map.collisionDetected(randX, randY) == 'E'){
                //If it can then it sets the players position to this.
                setCurrentX(randX);
                setCurrentY(randY);
            }
        }

    }

    /**
     * Increments the player's gold.
     */
    public void incrementGold(){
        goldGathered++;
    }

    /**
     * @return : The amount of gold the player has gathered.
     */
    public int getGoldGathered(){
        return goldGathered;
    }

    /**
     * @return : Whether the player's position moved.
     */
    public boolean changed(){
        return changeSuccess;
    }

    /**
     * @param newX : Value to set the current x coordinate to.
     */
    public void setCurrentX(int newX){
        if(currentY == -1){
            currentX = newX;
        }
        else if(map.collisionDetected(newX, currentY) != '#') {
            currentX = newX;
            changeSuccess = true;
        }
        else{
            changeSuccess = false;
        }
    }

    /**
     * @return : The current x value.
     */
    public int getCurrentX(){
        return currentX;
    }

    /**
     * @param newY : Value to set the current y coordinate to.
     */
    public void setCurrentY(int newY){
        if(currentX == -1){
            currentY = newY;
        }
        else if(map.collisionDetected(currentX, newY) != '#') {
            currentY = newY;
            changeSuccess = true;
        }
        else{
            changeSuccess = false;
        }
    }

    /**
     * @return : The current y value.
     */
    public int getCurrentY(){
        return currentY;
    }

    /**
     * Reads player's input from the console.
     *
     * @return : A string containing the input the player entered.
     */
    protected String getInputFromNetwork() {
        return inputFromNetwork;
    }

    protected void setInputFromNetwork(String msg){
        inputFromNetwork = msg.toLowerCase();
    }

    /**
     * Processes the command. It should return a reply in form of a String, as the protocol dictates.
     * Otherwise it should return the string "Invalid".
     *
     * @return : Processed output or Invalid if the @param command is wrong.
     */
    protected String getNextAction() {

        String input = getInputFromNetwork();

        /**
         * Stores the direction in which we will move if the user wants to move after validation.
         */
        char direction = 'X';

        /**
         * The position in the array of commands of the command the user inputted.
         */
        int commandID = -1;

        for(int i = 0; i < commands.length; i++){
            if(input.contains(commands[i])){
                //Checks to see what command the user inputted
                commandID = i;
                break;
            }
        }

        if(commandID == 2){
            //If the commandID is 2 the user wants to move, but if the user wants to move we need to know in what direction.
            //Removing the "move" as well as any spaces will leave us with the character of the direction they want to go
            input = input.replace("move", "");
            input = input.replace(" ", "");

            /**
             * The direction in which the user showed they wanted to move.
             */
            char compass = input.charAt(0);

            //Validate that the direction they inputted is a direction you can move in.
            for(int i = 0; i < directions.length; i++){
                if(compass == directions[i]){
                    direction = directions[i];
                }
            }
        }
        switch(commandID){
            case 0:
                //CommandID 0 is "HELLO"
                return logic.hello();
            case 1:
                //CommandID 1 is "GOLD"
                return logic.gold();
            case 2:
                //CommandID 2 is "MOVE <direction>"
                return logic.move(direction, 'P');
            case 3:
                //CommandID 3 is "PICKUP"
                return logic.pickup();
            case 4:
                //CommandID 4 is "LOOK"
                return logic.look('P');
            case 5:
                //CommandID 5 is "QUIT"
                logic.quitGame(client);
                return "Exiting dungeon.";
        }
        //If it hasn't returned anything yet then the input was not a valid command in some way.
        return "Invalid";
    }


}

class DODClient {

    HumanPlayer humanPlayer;

    /**
     * Stores the socket of the server
     */
    private Socket server;

    /**
     * Stores whether or not the server has been shutdown.
     */
    boolean serverShutdown = false;

    BufferedReader serverIn;

    PrintWriter serverOut;

    /**
     * Instance method for DODClient
     *
     * @param args : parameters of client.
     */
    public DODClient(String[] args) {

        /**
         * Stores the address that the client will bind to.
         */
        String address = "localhost";

        /**
         * Stores the port the client will use to connect to the server.
         */
        int port = 14001;

        /**
         * Stores whether or not the address needs to be changed.
         */
        boolean changeAddress = false;

        /**
         * Stores whether or not the port needs to be changed.
         */
        boolean changePort = false;

        for (String arg: args) {
            //Goes through parameters passed in by the user running the client and attempts to apply given parameters.
            if(changeAddress){
                //If we are changing the address we set it to the argument after a -cca and inform the user.
                address = arg;
                System.out.println("Setting address: " + address);
            }
            if(changePort){
                try{
                    /* If changing the port we attempt to convert the parameter to an int. If it throws an error there
                     * was an error with the parameter so we set to default and inform the user what has happened.
                     */
                    port = Integer.parseInt(arg);
                    System.out.println("Setting port: " + port);
                }
                catch(NumberFormatException e){
                    System.out.println("Invalid port: setting port to 14001 (default).");
                }
            }

            /* If the parameter is -cca we will be changing the address in the next arg, if it is -ccp we will be
             * changing the port is the next arg.
             */
            changeAddress = arg.equals("-cca");
            changePort = arg.equals("-ccp");
        }
        try {
            //Attempts to create a new socket with the server on the address and port given.
            server = new Socket(address,port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (ConnectException e){
            /* However if there was a timeout there was likely a problem with the address given so we set the address to
             * default and see if that will work.
             */

            System.out.println("Connection timed out: setting address to localhost (default).");
            try {
                server = new Socket("localhost", port);
            } catch(UnknownHostException e1){
                e.printStackTrace();
            } catch(IOException e1){
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates thread for listening to the server.
     */
    public String waitForUserInput(HumanPlayer humanPlayer) {

        try {
            /**
             * Stores the buffer reader used to read the input stream reader for the input steam of the server.
             */
            serverIn = new BufferedReader(new InputStreamReader(server.getInputStream()));
            serverOut = new PrintWriter(server.getOutputStream(), true);


            /**
             * Stores the incoming message from the server.
             */
            String serverMsg = "";

            /**
             * Stores the response the bot will give.
             */
            String response = "";

            //While someone has not exited the server, this will read what is being written to the server.
            serverMsg = serverIn.readLine();

            /**
             * Where to start the substring of the serverMsg to remove the username
             */
             int splitMsg = 0;

             for (int i = 0; i < serverMsg.length(); i++) {
                 if(serverMsg.charAt(i) == ']' ){
                     //Goes through the serverMsg to see where the first ] is indicating the end of the username
                     splitMsg = i+2; //And adds 2 as it is 0 based and there are spaces.

                     break;
                 }
             }

             String username = serverMsg.substring(0, splitMsg);

             serverMsg = serverMsg.substring(splitMsg); //Substring to remove the username


            if(serverMsg.equals("JOIN")){

             }
             else if(serverMsg.equals("EXIT")){
                 //It will shutdown the client when the server has been shutdown by someone.
                 serverShutdown = true;
             }
             //If we are not setting the username or exiting, the bot needs a response.
             while(username.equals("[DODClient] ") || serverMsg.equals("Username: ")){
                 serverMsg = serverIn.readLine();
                 int splitMsg2 = 0;

                 for (int i = 0; i < serverMsg.length(); i++) {
                     if(serverMsg.charAt(i) == ']' ){
                         //Goes through the serverMsg to see where the first ] is indicating the end of the username
                         splitMsg2 = i+2; //And adds 2 as it is 0 based and there are spaces.

                         break;
                     }
                 }

                 username = serverMsg.substring(0, splitMsg2);
                 serverMsg = serverMsg.substring(splitMsg2);

             }

             humanPlayer.setInputFromNetwork(serverMsg);
             response = humanPlayer.getNextAction();
             return response;

        }
        catch(IOException e){
            e.printStackTrace();
        }

        return "response";
    }

    /**
     * @return : the path for the map
     */
    public String getMap(){
        try {

            serverIn = new BufferedReader(new InputStreamReader(server.getInputStream()));
            serverOut = new PrintWriter(server.getOutputStream(), true);

            /**
             * Stores the most recent message from the server.
             */
            String serverMsg = serverIn.readLine();

            /**
             * Stores the point at which to do a substring to remove the username from the message.
             */
            int splitMsg = 0;

            for (int i = 0; i < serverMsg.length(); i++) {
                if (serverMsg.charAt(i) == ']') {
                    //Goes through the serverMsg to see where the first ] is indicating the end of the username
                    splitMsg = i + 2; //And adds 2 as it is 0 based and there are spaces.

                    break;
                }
            }

            /**
             * Stores username of client.
             */
            String username = serverMsg.substring(0, splitMsg);

            serverMsg = serverMsg.substring(splitMsg);


            while (username.equals("[DODClient] ") || serverMsg.equals("Username: ")) {
                serverMsg = serverIn.readLine();
                int splitMsg2 = 0;

                for (int i = 0; i < serverMsg.length(); i++) {
                    if (serverMsg.charAt(i) == ']') {
                        //Goes through the serverMsg to see where the first ] is indicating the end of the username
                        splitMsg2 = i + 2; //And adds 2 as it is 0 based and there are spaces.

                        break;
                    }
                }

                username = serverMsg.substring(0, splitMsg2);
                serverMsg = serverMsg.substring(splitMsg2);

            }
            return serverMsg;
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return "";
    }

    public void outputToClient(String msg){
        try {
            serverOut = new PrintWriter(server.getOutputStream(), true);
            serverOut.println(msg);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}

