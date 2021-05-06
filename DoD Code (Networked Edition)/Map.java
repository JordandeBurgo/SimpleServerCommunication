import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * Reads and contains in memory the map of the game.
 *
 */


public class Map {

	/**
	 *  Representation of the map
	 */
	private char[][] map;

	/**
	 *  Key for tiles that are special in some way
	 */
	private char[] collisionTiles = new char[]{'P', 'G', 'E', 'B', '#'};

	/**
	 *  Map name
	 */
	private String mapName;
	
	/**
	 *  Gold required for the human player to win
	 */
	private int goldRequired;
	
	/**
	 * Constructor that accepts a map to read in from.
	 *
	 * @param fileName : The filename of the map file.
	 */
	public Map(String fileName) {
		readMap(fileName);
	}

    /**
     * @return : Gold required to exit the current map.
     */
    protected int getGoldRequired() {
        return goldRequired;
    }
    protected void setGoldRequired(int gold){
    	goldRequired = gold;
	}

    /**
     * @return : The map as stored in memory.
     */
    protected char[][] getMap() {
        return map;
    }


    /**
     * @return : The name of the current map.
     */
    protected String getMapName() {
        return mapName;
    }

	/**
	 * @param name : The name of the map.
	 */
	protected void setMapName(String name){
    	mapName = name;
	}


    /**
     * Reads the map from file.
     *
     * @param fileName : Name of the map's file.
     */
    protected void readMap(String fileName) {

		/**
		 * Stores the file at the location of the given file path.
		 */
		File maptxt = new File(fileName);

		try {

			/**
			 * Creates a scanner to scan the file.
			 */
			Scanner sc = new Scanner(maptxt);

			/**
			 * Counts the number of lines.
			 */
			int noOfLines = 0;

			while(sc.hasNextLine()) {
				sc.nextLine();
				noOfLines++;
			}

			//Restarts the scanner now we have the number of lines
			sc = new Scanner(maptxt);

			/**
			 * Stores the map found in the file.
			 */
			char[][] mapStore = new char[noOfLines-2][]; //Now we have the number of lines we know how big the map is.

			/**
			 * Stores each line as it gets scanned.
			 */
			char[] lineStore;

			/**
			 * Stores the number of the current line.
			 */
			int lineNumber = 0;


			while(sc.hasNextLine()){
				String line = sc.nextLine(); //Reads in the next line
				if(lineNumber == 0){
				    //If the line number is 0 then that line will just be the name.
                    //Removing the "name " part will leave us with just the actual name.
					line = line.replace("name ", "");
					setMapName(line);
				}
				else if(lineNumber == 1){
				    //If line number is 1 then that line will be the amount of gold needed to win.
                    //Removing "win " will leave us with just the string of the number we need.
					line = line.replace("win ", "");
					//So convert it to an int and set that to gold required.
					setGoldRequired(Integer.parseInt(line));
				}
				else{
				    //Otherwise the line is a row in the map.
					lineStore = line.toCharArray();
					mapStore[lineNumber - 2] = lineStore;
				}
				lineNumber++;
			}

			map = mapStore;
		}
		catch(IOException e){
		    //If the file doesn't exist this will happen.
			e.printStackTrace();
		}

    }


	/**
	 * Checks if the tile at (x, y) is special or empty.
	 *
	 * @param x : The x co-ordinate of the tile.
	 * @param y : The y co-ordinate of the tile.
	 * @return : The character of the special tile or 'F' if not a special tile.
	 */
    protected char collisionDetected(int x, int y){
        //For every character in the collisionTiles array
		for (char colTile: collisionTiles) {
		    //If the tile we're testing is equal to the character.
			if(map[y][x] == colTile) {
                //We return the special character.
				return map[y][x];
			}
		}
        //Returning 'F' if it is not special.
    	return 'F';
	}
}
