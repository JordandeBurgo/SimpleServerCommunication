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
public class ChatClient {

    /**
     * Stores the socket of the server
     */
    private Socket server;

    /**
     * Stores whether or not the server has been shutdown.
     */
    boolean serverShutdown = false;

    /**
     * Instance method for ChatClient
     *
     * @param address : address that the client binds to.
     * @param port : port the client tries to connect to a server through.
     */
    public ChatClient(String address, int port) {
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
     * Creates threads for IO and listening to the server so they can happen simultaneously.
     */
    public void go() {
        /**
         * Creates an instance of ListeningToServer.
         */
        ListeningToServer l1 = new ListeningToServer(server, this);

        //And begins a thread of it.
        l1.start();

        /**
         * Creates an instance of LookingForInput.
         */
        LookingForInput l2 = new LookingForInput(server, this);

        //And begins a thread of it.
        l2.start();
    }

    /**
     * The main method of the ChatClient. Creates an instance of ChatClient with processed parameters.
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

        //Creates an instance of ChatClient with the address and port guided by parameters.
        new ChatClient(address, port).go();
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
     * Stores an instance of ChatClient passed in on instantiation.
     */
    private ChatClient cm;

    /**
     * Instance method for Listening to Server
     *
     * @param s : the socket of the server.
     * @param chatClient : the instance of ChatClient being used.
     */
    public ListeningToServer(Socket s, ChatClient chatClient){
        server = s;
        cm = chatClient;
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

            /**
             * Stores the incoming message from the server
             */
            String serverMsg = "";

            while (!serverMsg.equals("EXIT")) {
                //While someone has not exited the server, this will read what is being written to the server.
                serverMsg = serverIn.readLine();
                //And print out what has been written.
                System.out.println(serverMsg);
                if(serverMsg.equals("EXIT")){
                    //It will shutdown the client when the server has been shutdown by someone.
                    cm.serverShutdown = true;
                }
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

}

/**
 * Handles I/O of the user on this client-side of program.
 */
class LookingForInput extends Thread{

    /**
     * Stores the socket of the server.
     */
    private Socket server;

    /**
     * Stores an instance of ChatClient.
     */
    private ChatClient cm;

    /**
     * Instance method of LookingForInput.
     *
     * @param s : socket of the server.
     * @param chatClient : instance of ChatClient being used.
     */
    public LookingForInput(Socket s, ChatClient chatClient){
        cm = chatClient;
        server = s;
    }

    /**
     * The method that is run when the thread is started. Handles user input and getting those inputs to the server.
     */
    public void run(){
        try {

            /**
             * Stores the user input.
             */
            String userInput = "";

            /**
             * Stores the bugger reader of the input stream reader of the system system input steam.
             */
            BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in));

            /**
             * Stores the print writer for the server so we can write things to the servers output stream.
             */
            PrintWriter serverOut = new PrintWriter(server.getOutputStream(), true);

            while(!userInput.equals("EXIT") && !cm.serverShutdown) {
                //Whilst the server has not been shutdown, this looks for user inputs and writes it to the server.
                userInput = userIn.readLine();
                serverOut.println(userInput);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
