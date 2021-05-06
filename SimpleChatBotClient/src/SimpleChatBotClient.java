import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * The main class for the client. Creates a connection with the server, reads an processes parameters, and instantiates
 * itself and then creates threads of LookingForInput and ListeningToServer.
 */
public class SimpleChatBotClient {

    /**
     * Stores the socket of the server
     */
    private Socket server;

    /**
     * Stores whether or not the server has been shutdown.
     */
    boolean serverShutdown = false;

    /**
     * Instance method for SimpleChatBotClient
     *
     * @param address : address that the client binds to.
     * @param port : port the client tries to connect to a server through.
     */
    public SimpleChatBotClient(String address, int port) {
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
    public void go() {
        /**
         * Creates an instance of ListeningToServer.
         */
        ListeningToServer l1 = new ListeningToServer(server, this);

        //And begins a thread of it.
        l1.start();
    }

    /**
     * The main method of the SimpleChatBotClient. Creates an instance of SimpleChatBotClient with processed parameters.
     *
     * @param args : the parameters given by the user of the client.
     */
    public static void main(String[] args) {

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

        //Creates an instance of SimpleChatBotClient with the address and port guided by parameters.
        new SimpleChatBotClient(address, port).go();
    }
}

/**
 * Listens out on the port for action of the server the client is connected to.
 */
class ListeningToServer extends Thread{

    /**
     * Stores the socket of the server.
     */
    private Socket server;

    /**
     * Stores an instance of SimpleChatBotClient passed in on instantiation.
     */
    private SimpleChatBotClient scbc;

    /**
     * Instance method for Listening to Server
     *
     * @param s : the socket of the server.
     * @param SimpleChatBotClient : the instance of SimpleChatBotClient being used.
     */
    public ListeningToServer(Socket s, SimpleChatBotClient SimpleChatBotClient){
        server = s;
        scbc = SimpleChatBotClient;
    }

    /**
     * The function run when the thread is started. Handles listening to the server for incoming messages and will
     * respond accordingly.
     */
    public void run(){
        try {
            /**
             * Stores the buffer reader used to read the input stream reader for the input steam of the server.
             */
            BufferedReader serverIn = new BufferedReader(new InputStreamReader(server.getInputStream()));
            PrintWriter serverOut = new PrintWriter(server.getOutputStream(), true);


            /**
             * Stores the incoming message from the server.
             */
            String serverMsg = "";

            /**
             * Stores the response the bot will give.
             */
            String response = "";

            while (!serverMsg.equals("EXIT")) {
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

                serverMsg = serverMsg.substring(splitMsg); //Substring to remove the username

                if(serverMsg.equals("Username: ")){
                    //Sets the username to bot.
                    serverOut.println("BOT");
                }
                else if(serverMsg.equals("EXIT")){
                    //It will shutdown the client when the server has been shutdown by someone.
                    scbc.serverShutdown = true;
                }
                else{
                    if(!serverMsg.equals(response)){
                        //If we are not setting the username or exiting, the bot needs a response.
                        response = getResponse(serverMsg);
                        //And will write this response to the server.
                        serverOut.println(response);
                    }

                }
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Works out a reply for the bot to give to the server
     *
     * @param msg : the message the bot must work out a reply for.
     * @return : the response the bot will give.
     */
    public String getResponse(String msg){
        /**
         * Stores an array of greeting that could be used.
         */
        String[] greetings = {"hello", "hi", "howdy", "greetings", "hiya", "hey"};

        /**
         * Stores an array of positive words that could be used.
         */
        String[] positiveWords = {"yes", "good"};

        /**
         * Stores an array of negative words that could be used.
         */
        String[] negativeWords = {"no", "bad"};

        /**
         * Stores an array of ways of saying thanks that could be used.
         */
        String[] thanks = {"thanks", "thank you"};

        msg = msg.toLowerCase();

        /**
         * Stores the response the chatbot will give
         */
        String response = "";

        /* Mostly pretty self-explanatory. Looks through the message for keywords that have a generic and easy response
         * and gives the generic and easy response to it.
         */

        for (String posMsg: greetings) {
            if(msg.contains(posMsg)) {
                response += "Greetings. ";
                break;
            }
        }

        if(msg.contains("how are you")){
            response += "I am fine. How are you? ";
        }
        if(msg.contains("you okay")){
            response += "Yes. Are you? ";
        }
        for (String posMsg: positiveWords) {
            if(msg.contains(posMsg)) {
                response += "Good. ";
                break;
            }
        }
        for (String posMsg: negativeWords) {
            if(msg.contains(posMsg)) {
                response += "Oh. ";
                break;
            }
        }
        for (String posMsg: thanks) {
            if(msg.contains(posMsg)) {
                response += "You're welcome. ";
            }
        }
        if(response.equals("")) {
            response += "I am not yet capable of responding to that.";
        }

        return response;
    }

}
