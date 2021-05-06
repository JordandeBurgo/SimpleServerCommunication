import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Main class for the server. Creates connections with clients, reads and processes parameters, and instantiates the
 * thread handler.
 */

public class ChatServer {

    /**
     * main method for ChatServer, listens for clients on the port given or 14001 which is default.
     * @param args : contains the parameters for the server (i.e. whether to set the port to a specified value).
     */

    public static void main(String[] args) {

        /**
         * Stores the socket connection between the server and client.
         */
        Socket s;

        /**
         * Stores the socket of the server.
         */
        ServerSocket ss = null;

        /**
         * Stores the port number
         */
        int port = 14001;

        /**
         * Stores whether or not the port needs to be changed based on the parameters.
         */
        boolean changePort = false;

        /**
         * Stores an instance of ThreadHandler.
         */
        ThreadHandler th;

        //Loops through the strings in args to see if port needs to be changed and changes the port if needed.
        for (String arg: args) {
            if(changePort){
                try {
                    //Attempts to change the port to the port given within the parameters.
                    port = Integer.parseInt(arg);
                    System.out.println("Setting port: " + arg);
                }
                catch(NumberFormatException e){
                    /* If there is some error within the port (i.e. something that is not a number) then it detects it
                     * and continues with the default port, informing the user.
                     */
                    System.out.println("Invalid error: setting port to 14001 (default).");
                }
            }

            //If the argument is equal to -csp then the next parameter should be what to change the port to.
            changePort = arg.equals("-csp");
        }

        try {
            //Attempts to create a serversocket on the port
            ss = new ServerSocket(port);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        //Creates a new ThreadHandler on the serversocket.
        th = new ThreadHandler(ss);

        while (!th.getServerShutdown()) {
            try {
                //Whilst the server is not shutdown it will listen out for other clients trying to connect
                System.out.println("Server listening");
                assert ss != null;
                //And attempt connections when it hears a client trying to connect.
                s = ss.accept();
                System.out.println("Server accepted connection on " + ss.getLocalPort() + " ; " + s.getPort());
                th.newConnection(s);
            }
            catch (Exception e) {
                System.out.println("Server shutting down...");
            }
        }
    }
}

/**
 * Creates and handles the threads that links to clients, ensuring messages are sent to each client.
 */
class ThreadHandler{

    /**
     * Stores the socket of the server.
     */
    ServerSocket serverSocket;

    /**
     * Stores a list of all the threads created by connecting clients.
     */
    private List<ClientHandler> elClientes = new ArrayList<>() {};

    /**
     * Stores whether or not the server has been shutdown by someone.
     */
    private boolean serverShutdown = false;

    /**
     * Instance method for ThreadHandler
     *
     * @param ss : the socket of the server that the ThreadHandler is handling the clients of.
     */
    public ThreadHandler(ServerSocket ss){
        serverSocket = ss;
    }

    /**
     * Creates a new thread for a new connection.
     *
     * @param s : the socket the new connection is on.
     */
    public void newConnection(Socket s){

        /**
         * Stores the instance of ClientHandler to be used by the new connection.
         */
        ClientHandler ch = new ClientHandler(s, this);

        //Adds the new client to the list of clients and starts the thread.
        elClientes.add(ch);
        ch.start();
    }

    /**
     * Sends the message passed in to every client connected to the server.
     *
     * @param msg : the message to output to clients.
     */
    public synchronized void sendAll(String msg){
        if(msg.equals("EXIT")){
            //shuts down the server
            serverShutdown = true;
            try {
                serverSocket.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        for (ClientHandler ch: elClientes) {
            //outputs the message on the client's thread.
            ch.output(msg);
        }
    }

    /**
     * @return : whether or not the server has been shutdown.
     */
    public boolean getServerShutdown(){
        return serverShutdown;
    }
}

/**
 * This class contains multiple threads - one for each client. It handles everything that needs to happen within each
 * of the clients separately (e.g. I/O).
 */
class ClientHandler extends Thread{

    /**
     * Stores the socket of the client on this thread.
     */
    private Socket s;

    /**
     * Stores the buffer reader for the input stream reader of this client.
     */
    private BufferedReader clientIn;

    /**
     * Stores the print writer for the socket of this client so we can write things to the clients output stream.
     */
    private PrintWriter clientOut;

    /**
     * Stores the instance of ThreadHandler handling the threads for this server.
     */
    private ThreadHandler th;

    /**
     * Instance method for ClientHandler
     *
     * @param inSocket : the socket of the client.
     * @param threadHandler : the instance of ThreadHandler handling the threads for this server.
     */
    public ClientHandler(Socket inSocket, ThreadHandler threadHandler) {
        this.s = inSocket;
        this.th = threadHandler;
    }

    /**
     * The method which is run when the thread starts.
     */
    public void run() {
        try {
            /**
             * Stores what the user has inputted.
             */
            String userInput;

            /**
             * Stores the input stream reader for the input stream of the socket of this client
             */
            InputStreamReader r = new InputStreamReader(s.getInputStream());

            clientIn = new BufferedReader(r);
            clientOut = new PrintWriter(s.getOutputStream(), true);

            //Gets a username for the client
            clientOut.println("Username: ");
            userInput = clientIn.readLine();

            /**
             * Stores the username of the client.
             */
            String username = "[" + userInput + "] ";

            while(!userInput.equals("EXIT") && !th.getServerShutdown()) {
                /* Whilst the server is not shutdown it will take in the clients inputs and broadcast them to the rest
                 * of the server with the username of the client.
                 */
                userInput = clientIn.readLine();
                if(userInput.equals("EXIT")){
                    th.sendAll(userInput);
                }
                else {
                    th.sendAll(username + userInput);
                }
                System.out.println(userInput);
            }

        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                //After the client exits it will close the connection.
                clientIn.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Outputs a given message.
     *
     * @param msg : the message to output.
     */
    public void output(String msg){
        clientOut.println(msg);
    }

}

