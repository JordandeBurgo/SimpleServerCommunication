import java.util.LinkedList;
import java.util.List;
import java.util.Random;


/**
 * Runs the game with a computer player and contains code needed to decide upon moves.
 *
 */
public class BotPlayer {

    /**
     * Stores the current x coordinate of the bot.
     */
    private int currentX = -1;

    /**
     * Stores the current y coordinate of the bot.
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
     * Stores whether the position changed successfully or not.
     */
    private boolean changeSuccess;

    /**
     * Creates a queue of nodes for dijkstras.
     */
    private LinkedList<Node> queue;

    /**
     * Stores the route as a string.
     */
    private String routing;

    /**
     * Stores the route as an array of characters.
     */
    private char[] route = new char[0];

    /**
     * Stores how many moves the bot has had since the last time it changed route.
     */
    private int routeMove = 0;

    /**
     * Constructor that accepts the game instance to play.
     *
     * @param logical : The game instance.
     */
    public BotPlayer(GameLogic logical){
        logic = logical;

        map = logic.getMap();
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
            if(map.collisionDetected(randX, randY) == 'F' || map.collisionDetected(randX, randY) == 'E') {
                //If it can then it sets the players position to this.
                setCurrentX(randX);
                setCurrentY(randY);
            }
        }
    }

    /**
     * @return : Whether the bot's position moved.
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
     *
     * @return : The current y value.
     */
    public int getCurrentY(){
        return currentY;
    }

    /**
     * Process to decide what to do (move or look) and where to move given the bot has just looked.
     *
     * @return : What the bot did as a string.
     */
    public String decideNextAction(){
        /**
         * Creates a random object.
         */
        Random rand = new Random();

        //If the length of route is the same as routeMove or it is 0 we need a new route to go on.
        if(route.length == routeMove || route.length == 0) {

            routeMove = -1;

            /**
             * Stores the visible part of the map when looking as a string.
             */
            String miniMapRep = logic.look('B');

            /**
             * Stores the rows of the visible map as an array of strings.
             */
            String[] rows = miniMapRep.split("\n");

            /**
             * Stores the visible map as a 2D array fo characters.
             */
            char[][] miniMap = new char[5][];

            /**
             * Stores the x coordinate to move to.
             */
            int x = -1;

            /**
             * Stores the y coordinate to move to.
             */
            int y = -1;

            //Convert the string array of rows into a 2D array of chars to represent the part of the visible map

            for (int i = 0; i < rows.length; i++) {
                char[] columns = rows[i].toCharArray();
                for (int j = 0; j < columns.length; j++) {
                    if (columns[j] == 'P') {
                        //If we can see a player then go to it.
                        x = j;
                        y = i;
                    }
                }
                miniMap[i] = columns;
            }

            //If we havent seen a player
            while (x == -1 && y == -1) {
                //Pick a random destination
                int randX = rand.nextInt(5);
                int randY = rand.nextInt(5);
                //Verify it
                if (miniMap[randY][randX] != '#' && miniMap[randY][randX] != 'B') {
                    x = randX;
                    y = randY;
                }
            }

            //Do dijkstras to find shortest path to destination
            route = dijkstras(x, y, miniMap);

            routeMove++;

            //Return what the bot did
            return("Bot looked.");

        }

        else{
            //We have a route to move along so just move in the next direction.
            logic.move(route[routeMove], 'B');
            routeMove++;
            return("Bot moved.");
        }
    }

    /**
     * Does dijkstras to find the shortest path from the bot to where it decides to move.
     *
     * @param targetX : The x coordinate of the destination.
     * @param targetY : The y coordinate of the destination.
     * @param miniMap : The visible part of the map.
     * @return : List of characters that are direction for the bot to move in.
     */
    public char[] dijkstras(int targetX, int targetY, char[][] miniMap){

        queue = new LinkedList<>();
        routing = "";

        /**
         * Makes the bots position as a node and stores as source.
         */
        Node source = new Node(2, 2, 0, null, 'X');

        //Add the source node to the queue
        queue.add(source);

        //Whilst there are unvisited nodes in the queue
        while(!queue.isEmpty()){
            /**
             * Stores the node as the front of the queue as it is removed.
             */
            Node popped = queue.poll(); //Get the first item in the queue and remove it.

            //If we are at the destination node then we can just output the path we took to get there.
            if(isEnd(popped.x, popped.y, targetX, targetY) ){
                int shortestLength = popped.distanceFromSource;
                for(int i = 0; i < shortestLength; i++){
                    routing += popped.compassDirection;
                    popped = popped.previousNode;
                }
                routing = new StringBuilder(routing).reverse().toString();

                return routing.toCharArray();
            }
            else{
                //Otherwise we need to keep testing the neighbours of the popped node.
                //So set the popped node to tested
                miniMap[popped.y][popped.x]='0';
                //Run addNeighbours to get all the valid neighbours of the popped node
                List<Node> neighbourList = addNeighbours(popped, miniMap);
                //Add all the valid neighbours to the queue.
                queue.addAll(neighbourList);
            }
        }
        return routing.toCharArray();
    }

    /**
     * Tests which nodes next to the current node are valid so they can be added to the queue.
     *
     * @param popped : The node we are currently looking at.
     * @param miniMap : The visible map.
     * @return : A list of nodes to add to the queue.
     */
    private List<Node> addNeighbours(Node popped, char[][] miniMap) {

        /**
         * The list of nodes next to  popped that are valid.
         */
        List<Node> list = new LinkedList<>();

        if(isValid(popped.x-1, popped.y, miniMap)) {
            /*
              If the one west of the popped node is valid add it.
              Create the node with popped as it's previous node and an x position of the popped node's x subtract 1
              and y of the same as the popped node.
              And with the direction as west.
            */
            list.add(new Node(popped.x-1, popped.y, popped.distanceFromSource+1, popped, 'w'));
        }
        if(isValid(popped.x+1, popped.y, miniMap)) {
            //Same but for east
            list.add(new Node(popped.x+1, popped.y, popped.distanceFromSource+1, popped, 'e'));
        }
        if(isValid(popped.x, popped.y-1, miniMap)) {
            //north
            list.add(new Node(popped.x, popped.y-1, popped.distanceFromSource+1, popped, 'n'));
        }
        if(isValid(popped.x, popped.y+1, miniMap)) {
            //south
            list.add(new Node(popped.x, popped.y+1, popped.distanceFromSource+1, popped, 's'));
        }
        return list;
    }

    /**
     * Tests if the tile (node) can be moved to.
     *
     * @param x : The x coordinate of the node.
     * @param y : The y coordinate of the node.
     * @param miniMap : The visible part of the map.
     * @return : Whether the node can be moved to
     */
    private boolean isOpen(int x, int y, char[][] miniMap) {
        if(inRange(x, y, miniMap)){
            //If it isn't a '#' then the node is open.
            return miniMap[y][x]!='#';
        }
        return false;
    }

    /**
     * Test if the node has been tried yet.
     *
     * @param x : The x coordinate of the node.
     * @param y : The y coordinate of the node.
     * @param miniMap : The visible part of the map.
     * @return : Whether or not the node has been tried.
     */
    private boolean isTried(int x, int y, char[][] miniMap) {
        if(inRange(x, y, miniMap)){
            //If it is a zero the node has been tried already.
            return miniMap[y][x] == '0';
        }
        return false;
    }

    /**
     * Tests if the node is the destination.
     *
     * @param x : The x coordinate of the node.
     * @param y : The y coordinate of the node.
     * @param targetX : The x coordinate of the destination.
     * @param targetY : The y coordinate of the destination.
     * @return : Whether or not the node is the destination.
     */
    private boolean isEnd(int x, int y, int targetX, int targetY) {
        return x == targetX && y == targetY;
    }

    /**
     * Test to see if the node is valid.
     *
     * @param x : The x coordinate of the node.
     * @param y : The y coordinate of the node.
     * @param miniMap : The visible part of the map.
     * @return : Whether or not the node is valid.
     */
    private boolean isValid(int x, int y, char[][] miniMap) {
        if (inRange(x, y, miniMap) && isOpen(x, y, miniMap) && !isTried(x, y, miniMap)) {
            return true;
        }

        return false;
    }

    /**
     * Test if the node is in the range of the visible map.
     *
     * @param x : The x coordinate of the node.
     * @param y : The y coordinate of the node.
     * @param miniMap : The visible part of the map.
     * @return : Whether or not the node is in range.
     */
    private boolean inRange(int x, int y, char[][] miniMap) {
        return x >=0 && x < miniMap.length && y >= 0 && y < miniMap[0].length - 1;
    }

}

/**
 * Creates an easy way of storing information about points visited in dijkstras.
 *
 */
class Node {
    /**
     * Stores the x coordinate of the node.
     */
    int x;

    /**
     * Stores the y coordinate of the node.
     */
    int y;

    /**
     * Stores the node's distance from the node it is originating from.
     */
    int distanceFromSource;

    /**
     * Stores the node that lead to this node.
     */
    Node previousNode;

    /**
     * Stores the direction to follow to get from the source to the destination
     */
    char compassDirection;

    /**
     * Constructor that accepts an x and y coordinate, a distance, a previous node and character.
     *
     * @param x : The x coordinate of the node
     * @param y : The y coordinate of the node
     * @param dis : The node's distance from the node it is originating from.
     * @param pred : The node that lead to this node.
     * @param direction : The direction to follow to get from the source to the destination.
     */
    Node(int x, int y, int dis, Node pred, char direction) {
        this.x = x;
        this.y = y;
        this.distanceFromSource = dis;
        this.previousNode = pred;
        this.compassDirection = direction;
    }
}
