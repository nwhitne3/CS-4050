import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

import static java.nio.file.Files.exists;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

class vertex {
    String name;
    int connectedCount;
    double weight;
    boolean directed;

    vertex() {
        name = null;
        //measures recursion depth when checking connectivity
        connectedCount = 0;
        weight = 0;
        directed = false;

    }

    vertex(String vertexName) {
        name = vertexName;
        //measures recursion depth when checking connectivity
        connectedCount = 0;
        weight = 0;
        directed = false;
    }
}

public class graphsL {

    /**
     * Adjacency Lists:
     * Example: vertex A has directly connecting edges to vertices B and E
     * A -> B -> E
     * These are sub-lists.
     * There is one of these lists per vertex. Each list starts with one vertex, and each
     * additional vertex in the list indicates an edge between the first vertex and that
     * vertex.
     *
     * This is not a list of sequential edges, such as A to B to E. Rather, it is all the
     * edges that connect to A.
     * A     B    C
     * ._____.    .
     * |
     * .     .
     * E     D
     */
    private LinkedList<vertex> adjacencyList;

    /**
     * Index of the Adjacency Lists:
     * This is the master list of all adjacency lists.
     * An individual adjacency list is a linked list of a particular vertex and all its
     * direct edges.
     * |A| -> B -> E
     * |B| -> A
     * |C| -> null
     * |D| -> null
     * |E| -> A
     */
    private LinkedList<LinkedList <vertex>> listOfAdjacencyLists;

    /**
     *
     * @param vertexName
     * @param directed
     * @return
     */
    private LinkedList<vertex> addVertex(String vertexName, boolean directed) {
        vertex beingCreated = new vertex(vertexName);
        beingCreated.directed = directed;

        // handle null cases
        if (listOfAdjacencyLists != null)
        {
            if (findAdjacencyList(beingCreated) != null)
            {
                return null;
            }
        }

        // create adjacency list of lists
        else {
            listOfAdjacencyLists = new LinkedList<LinkedList <vertex>>();
        }

        LinkedList<vertex> newAdjacencyList = new LinkedList<>();
        newAdjacencyList.add(beingCreated);
        listOfAdjacencyLists.add(newAdjacencyList);
        return newAdjacencyList;
    }

    /**
     *
     * @param starting
     * @param connection
     * @param weight
     * @param directed
     * @return
     */
    private boolean addEdge(vertex starting, vertex connection, double weight, boolean directed) {

        System.out.println("debug: addEdge: starting = " + starting.name
                + ", connection = " + connection.name + ", weight = " + weight + ", directed = " + directed);
        boolean rc = false;

        LinkedList<vertex> startingAdjacencyList = findAdjacencyList(starting);
        if (startingAdjacencyList == null) {
            return rc;
        }

        System.out.println("------- debug: addEdge: calling hasEdge does edge exist for " +
                starting.name + " -> " + connection.name + " -------");
        if (hasEdge(starting, connection) == false)
        {
            System.out.println("debug: addEdge: hasEdge was false");
            LinkedList<vertex> connectionAdjacencyList = findAdjacencyList(connection);
            if (connectionAdjacencyList == null) {
                return rc;
            }

            connection.weight = weight;
            connection.directed = directed;
            startingAdjacencyList.add(connection);
            rc = true;

            if (directed == false)
            {
                System.out.println("debug: addEdge: found connection list " + connection.name);
                starting.weight = weight;
                starting.directed = directed;
                connectionAdjacencyList.add(starting);
            }
        }
        else {
            //edge already exists, see if weight has changed
            if (weight != 0) {
                if (startingAdjacencyList != null) {
                    if (connection.weight != weight) {
                        connection.weight = weight;
                        rc = true;
                    }
                }
            }
        }

        System.out.println("debug: addEdge: returns " + rc + ", weight = " + connection.weight);
        return rc;
    }

    /**
     *
     * @param root
     * @return
     */
    private LinkedList<vertex> findAdjacencyList(vertex root) {
        int indexOfRoot;

        //System.out.println("debug: findAdjacencyList: Looking for vertex " + root.name);
        for (indexOfRoot = 0; indexOfRoot < listOfAdjacencyLists.size(); indexOfRoot++) {
            String connectionName = listOfAdjacencyLists.get(indexOfRoot).getFirst().name;
            // search for name of vertex matching the name in the adjacency list
            //System.out.println("debug: findAdjacencyList: checking for a vertex list for " + connectionName);
            if (connectionName.equals(root.name)) {
               // System.out.println("debug: findAdjacencyList: found vertex list for " + connectionName);
                return listOfAdjacencyLists.get(indexOfRoot);
            }
        }

        return null;
    }

    /**
     *
     * @param starting
     * @param connecting
     * @return
     */
    private boolean deleteEdge(vertex starting, vertex connecting) {
        boolean rc = false;
        LinkedList<vertex> adjacencyList = findAdjacencyList(starting);

        // if the edge does not exist
        if (adjacencyList == null) {
            rc = false;
            System.out.println("debug: deleteEdge: adjacencyList is null.");
        }

        else {
            int indexOfConnecting = 0;
            int indexOfStarting = 0;
            for (indexOfConnecting = 0; indexOfConnecting < adjacencyList.size(); indexOfConnecting++) {

                if (adjacencyList.get(indexOfConnecting).name.equals(connecting.name)) {
                    System.out.println("debug: deleteEdge: found connection in starting list " + starting.name
                            + " -> " + connecting.name);
                    /*
                     * Have found the target connection.
                     * If it's not a directed graph, have to delete the connection both ways.
                     * delete A -> B && delete B -> A
                     */
                    adjacencyList.remove(indexOfConnecting);
                    rc = true;
                    //debug:
                    printGraph();

                    LinkedList<vertex> adjacencyList2 = findAdjacencyList(connecting);

                    // if the vertex that starts the list indicates that it's undirected
                    if (adjacencyList.get(0).directed == false) {
                        for (indexOfStarting = 0; indexOfStarting < adjacencyList2.size(); indexOfStarting++) {

                            if ( adjacencyList2.get(indexOfStarting).name.equals(starting.name) ) {
                                System.out.println("debug: deleteEdge: unidirected graph, so found second edge to delete: "
                                        + connecting.name + " -> " + starting.name);
                                System.out.println("debug: deleteEdge: indexOfStarting = " + indexOfStarting
                                        + ", indexOfConnecting = " + indexOfConnecting);
                                // remove A from B -> A
                                adjacencyList2.remove(indexOfStarting);
                                //debug:
                                printGraph();

                                rc = true;
                                break;
                            }
                        }
                    }
                    //found it
                    break;
                }
            }
        }

        return rc;
    }

    /**
     * Example:
     * undirected:          directed:
     * A     B    C         A     B    C
     * ._____.    .         .---->.    .
     * |                    ^
     * .     .              .     .
     * E     D              E     D
     *
     * |A| -> B -> E        |A| -> B
     * |B| -> A             |B| -> null
     * |C| -> null          |C| -> null
     * |D| -> null          |D| -> null
     * |E| -> A             |E| -> A
     *
     * Deleting Vertex A should result in:
     *       B    C
     *       .    .
     *
     * .     .
     * E     D
     * |B| -> null
     * |C| -> null
     * |D| -> null
     * |E| -> null
     *
     * @param deleting
     */
    private boolean deleteVertex(vertex deleting) {
        boolean rc = false;
        LinkedList<vertex> adjacencyList = findAdjacencyList(deleting);

        if (adjacencyList == null) {
            rc = false;
        }

        final int indexOfFirstEdge = 1;
        /* have to delete all connections (edges) before remove the vertex
         * find all of the direct connections to the target vertex and
         * delete them
         */
        if (adjacencyList != null) {
            while (adjacencyList.size() > indexOfFirstEdge) {

                if (adjacencyList.get(indexOfFirstEdge).directed == false) {
                    deleteEdge(adjacencyList.get(indexOfFirstEdge), deleting);
                }
                /*
                 *Note: deleteEdge will delete the second item in the adjacency list when finished
                 *Example: delete B -> A and then A -> B
                 */
                else {
                    adjacencyList.remove(indexOfFirstEdge);
                }
            }
            // Now delete the vertex. (Example: Delete |A| -> B -> E has become |A| -> null)
            listOfAdjacencyLists.remove(adjacencyList);
            rc = true;
        }
        return rc;
    }

    /**
     * For directed, only search the starting list.
     * For undirected, search both the starting and the connected list.
     *
     * @param starting
     * @param connecting
     * @return
     */
    private boolean hasEdge(vertex starting, vertex connecting) {
        LinkedList<vertex> adjacencyList = findAdjacencyList(starting);

        if (adjacencyList == null) {
            return false;
        }

        int indexOfRoot;
        for (indexOfRoot = 0; indexOfRoot < adjacencyList.size(); indexOfRoot++) {

            if ( connecting.name.equals(adjacencyList.get(indexOfRoot).name) ) {
                return true;
            }
        }

        return false;
    }

    /**
     * If 15% of the vertices (or lower) are connected.
     * If 15% or fewer of the graph has edges.
     *
     *
     * # of vertices  undirected max # of connections  directed "  connected
     * 1              0                                0           0%
     * 2              1                                2           100%
     * 3              3                                6           100%
     * 4              6                                12          100%
     *
     * ratio of numConnections
     *
     * @return
     */
    private boolean isSparse(boolean direct)
    {
        // if 0.15 * numVertices > numEdges then graph is sparse
        final double densityProportion = 0.15;
        double numEdges = countEdges();
        double numVertices = countVertices();

        if (direct == false) {
            numEdges = numEdges / 2;
        }

        System.out.println("debug: isSparse: numEdges = " + numEdges + ", numVertices = " + numVertices
                + ", densityProportion * numVertices = " + densityProportion*numVertices);
        if (densityProportion * numVertices > numEdges) {
            return true;
        }

        return false;
    }

    /**
     * If 85% of the vertices (or higher) are connected.
     *
     * @return boolean
     */
    private boolean isDense(boolean direct) {
        //if 0.85 * numVertices < numEdges then graph is dense
        final double densityProportion = 0.85;
        double numEdges = countEdges();
        double numVertices = countVertices();

        if (direct == false) {
            numEdges = numEdges / 2;
        }

        System.out.println("debug: isDense: numEdges = " + numEdges + ", numVertices = " + numVertices
                + ", densityProportion * numVertices = " + densityProportion*numVertices);
        if (densityProportion * numVertices < numEdges) {
            return true;
        }

        return false;
    }

    /**
     * The number of vertices is the same as the number of lists.
     * @return
     */
    private int countVertices() {
        return listOfAdjacencyLists.size();
    }

    /**
     *
     * @return
     */
    private int countEdges() {
        int count = 0;
        int indexOfAdjacencyList;
        // Subtracts 1 to avoid double counting the vertex

        final int countOffset = 1;
        for (indexOfAdjacencyList = 0; indexOfAdjacencyList < listOfAdjacencyLists.size(); indexOfAdjacencyList++) {
            LinkedList<vertex> adjacencyList = listOfAdjacencyLists.get(indexOfAdjacencyList);
            System.out.println("debug: countEdges: adjacencyList.size() = " + adjacencyList.size()
                    + ", indexOfAdjacencyList " + indexOfAdjacencyList);
            count += (adjacencyList.size() - countOffset);
            System.out.println("debug: countEdges: count = " + count);
        }
        return count;
    }

    /**
     * Build map of all connections
     * Then verify that there is a path to all vertices.
     *
     * Fully connected is also connected:
     * A -> B -> C -> D -> E
     *
     * Connected: If can travel through the whole graph via edges. (There is at least one path through the whole
     * graph.)
     *
     * Since vertices are copies, have to also update all the copies.
     *
     * @return boolean
     */

    private boolean isConnected() {
        boolean returnCount = true;
        int numOfVertices = countVertices();
        int indexOfAdjacencyList;
        final int unvisited = 0;
        LinkedList<vertex> adjacencyList;

        // mark all vertices as unvisited
        for (indexOfAdjacencyList = 0; indexOfAdjacencyList < listOfAdjacencyLists.size(); indexOfAdjacencyList++) {
            adjacencyList = listOfAdjacencyLists.get(indexOfAdjacencyList);
            adjacencyList.get(0).connectedCount = unvisited;
        }

        // search a "random" vertex and see if can reach all of the other vertices from it
        //TODO make truly pseudorandom?
        adjacencyList = listOfAdjacencyLists.get(0);
        dfs(adjacencyList);

        // check if all the adjacency lists were visited
        for (indexOfAdjacencyList = 0; indexOfAdjacencyList < listOfAdjacencyLists.size(); indexOfAdjacencyList++) {
            adjacencyList = listOfAdjacencyLists.get(indexOfAdjacencyList);

            // if connection count for this vertex is 0, then all the vertices were not visited
            if (adjacencyList.get(0).connectedCount == unvisited) {
                returnCount = false;
                break;
            }
        }

        return returnCount;
    }

    /**
     * DFS: Depth First Search
     * @param adjacencyList
     */

    // only done at load time
    static int count = 1;
    private void dfs(LinkedList<vertex> adjacencyList) {
        // mark this vertex as visited
        adjacencyList.get(0).connectedCount = count;
        // look at each vertex connected to the first vertex in this list
        for (int indexOfVertex = 1; indexOfVertex < adjacencyList.size(); indexOfVertex++) {
            // create a connected adjacency list and find if each element has been visited
            LinkedList<vertex> connectedAdjacencyList = findAdjacencyList(adjacencyList.get(indexOfVertex));

            // if current element is unvisited, then increment count and also search the element's adjacency list
            if (connectedAdjacencyList.get(0).connectedCount == 0) {
                count = count + 1;
                dfs(connectedAdjacencyList);
            }

        }
    }

    /**
     * There is an edge from every vertex to every other vertex.
     * If the graph is fully connected, the length of each sublist will match the length of the
     * list of lists, which is the number of vertices.
     * rc = return code
     * @return
     */
    private boolean isFullyConnected() {
        boolean rc = true;
        int numOfVertices = countVertices();
        int indexOfAdjacencyList;

        for (indexOfAdjacencyList = 0; indexOfAdjacencyList < listOfAdjacencyLists.size(); indexOfAdjacencyList++) {
            LinkedList<vertex> adjacencyList = listOfAdjacencyLists.get(indexOfAdjacencyList);

            if (adjacencyList.size() != numOfVertices) {
                rc = false;
                break;
            }

        }

        return rc;
    }

    //TODO (1) Tries to append two extra ".txt"s if the user already specified ".txt". Fix that.
    //TODO (2) Put all the file parsing inside readGraph? (Move from main.)
    //TODO (3) Try to more gracefully handle user either not typing file extension (assumes ".txt") Vs. user typing file w/ extension (just uses that if so)
    private BufferedReader readGraph(String inputFile) throws IOException
    {
        String inputFileExtension = ".txt";
        String inputFilename = System.getProperty("user.dir") + "\\" + inputFile + inputFileExtension;

        FileReader fileBeingRead = new FileReader(inputFilename);
        BufferedReader graphFileLines = new BufferedReader(fileBeingRead);

        return graphFileLines;
    }

    /**
     * Building blocks of the following format:
     * A              -       w             -     >                          B
     * vertex name 1, dash, weight or dash, dash, dash or arrow if directed, vertex name 2
     *
     * Prints graph in the following format:
     * (1) undirected:             A-----B-----C
     * (2) undirected with weight: A--w--B--w--C
     * (3) directed:               A---->B---->C
     * (4) directed with weight:   A--w->B--w->C
     */

    private void printGraph() {
        int indexOfAdjacencyList;
        int indexOfVertex;
        vertex connectedVertex;
        String weight = "-.-";
        String directed = "-";

        // go through all the adjacency lists
        for (indexOfAdjacencyList = 0; indexOfAdjacencyList < listOfAdjacencyLists.size(); indexOfAdjacencyList++) {
            LinkedList<vertex> adjacencyList = listOfAdjacencyLists.get(indexOfAdjacencyList);

            // go through all the vertices in each adjacency list
            for (indexOfVertex = 0; indexOfVertex < adjacencyList.size(); indexOfVertex++) {
                connectedVertex = adjacencyList.get(indexOfVertex);

                // At the first element just print out the starting vertex
                if (indexOfVertex == 0) {
                    System.out.print(connectedVertex.name + "-");

                    // and if there are no further vertices
                    if (indexOfVertex == adjacencyList.size()-1) {
                        System.out.println();
                    }
                    continue;
                }

                // when weighted graph, weight will be a value
                if (connectedVertex.weight != 0) {
                    weight = String.valueOf(connectedVertex.weight);
                }

                // when directed graph, directed will show the direction (assumes first to second)
                if (connectedVertex.directed == true) {
                    directed = ">";
                }

                System.out.print(weight + directed + adjacencyList.get(indexOfVertex).name);
                if (indexOfVertex == adjacencyList.size()-1) {
                    System.out.println();
                }

                else {
                    System.out.print("-");
                }

            }
        }
    }

    /**
     *
     * @param currentLine
     * @param userFile
     * @param directed
     */
    private static void parseVertices(String currentLine, graphsL userFile, boolean directed) {
        String[] vertexNames = userFile.tokenizer(currentLine);

        // parse the vertices list
        int j = 0;

        while (vertexNames[j] != null) {
            System.out.println("debug: adding vertex " + vertexNames[j]);
            userFile.addVertex(vertexNames[j], directed);
            j = j+1;
        }

    }

    /**
     *
     * @param currentLine
     * @param userFile
     * @param weight
     * @param directed
     */
    private static void parseEdges(String currentLine, graphsL userFile, boolean weight, boolean directed) {
        try {
            double weightNumber = 0;
            String[] edgeDataToAdd = userFile.tokenizer(currentLine);

            //parse the edge data
            System.out.println("debug: parsing vertex " + 1 + " for edge: " + edgeDataToAdd[0]);
            String startingVertexName = edgeDataToAdd[0];

            System.out.println("debug: parsing vertex " + 2 + " for edge: " + edgeDataToAdd[1]);
            String connectingVertexName = edgeDataToAdd[1];

            if ((weight == true) && (edgeDataToAdd[2] != null)) {
                System.out.println("debug: parsing weight value: " + edgeDataToAdd[2]);
                weightNumber = Double.parseDouble(edgeDataToAdd[2]);

            }

            // create the edge
            vertex vertexStarting = new vertex(startingVertexName);
            vertex vertexConnecting = new vertex(connectingVertexName);
            userFile.addEdge(vertexStarting, vertexConnecting, weightNumber, directed);
        }
        catch (Exception e) {
            System.out.println("Error creating edge: " + e.getMessage());
        }

    }

    final String theNextSpace = " ";
    final int maxTokens = 27;
    public String[] output = new String[maxTokens];

    /**
     *
     * @param input
     * @return
     */
    public String [] tokenizer(String input) {
        String temp = input;
        int j = 0;

        while (temp != null) {
            //find next token
            int endOfTokenIndex = temp.indexOf(theNextSpace);

            if (endOfTokenIndex > 0) {
                output[j] = temp.substring(0, endOfTokenIndex);
                //strip off that token and the current space
                temp = temp.substring(endOfTokenIndex + 1);
            }
            else {
                // the last token
                output[j] = temp;
                temp = null;
            }

            j = j+1;
        }
        // alert caller there are only j tokens
        output[j] = null;
        return output;
    }

    public static void main(String[] args) throws IOException
    {
        graphsL userFile = new graphsL();
        String currentLine;
        boolean weighted = false;
        boolean direct = false;

        // Takes user input as an argument.
        // Bare bones user input error handling for no arguments.
        if (args.length == 0)
        {
            //user input error, no arguments
            System.out.println("Input Error: Filename required as argument.\n"
                    + "Expects a .txt file input of the form:\n"
                    + "'java graph filename' (Do not include '.txt'.)\n"
                    + "---------------------------------------------\n");

            /** (Requires Java 8)
             * Attempted listing of .txt fires in user directory.
             * Initially based on code examples found here:
             * https://stackoverflow.com/questions/2102952/listing-files-in-a-directory-matching-a-pattern-in-java
             * (Since this feature is not part of the project requirements and is just for fun, I figured it was okay.)
             */

            System.out.println(".txt files found in the directory: '" + System.getProperty("user.dir") + "':");
            File dir = new File(".");
            File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));
            for (File graphTestFile : files)
            {
                System.out.println(graphTestFile);
            }
        }
        else {
            //parse user file as commandline argument
            String inputFileExtension = ".txt";
            String fileName = args[0];
            String inputFilename = System.getProperty("user.dir") + "\\" + fileName + inputFileExtension;

            Path file = Paths.get(System.getProperty("user.dir"), fileName + inputFileExtension);
            if (exists(file, NOFOLLOW_LINKS)) {
                BufferedReader graphFileLines = userFile.readGraph(fileName);

                while ((currentLine = graphFileLines.readLine()) != null) {
                    String tempLine = currentLine;
                    tempLine = tempLine.trim();
                    //when trimmed line is blank or is a comment
                    if ((tempLine.length() == 0) || (tempLine.startsWith("*")))
                    {
                        //ignore and read the next line
                        continue;
                    }
                    //check whether graph is undirected/directed
                    if (tempLine.equalsIgnoreCase("undirected")) {
                        direct = false;
                        continue;
                    }
                    if (tempLine.equalsIgnoreCase("directed")) {
                        direct = true;
                        continue;
                    }
                    //check whether graph is unweighted/weighted
                    if (tempLine.equalsIgnoreCase("unweighted")) {
                        weighted = false;
                        continue;
                    }
                    if (tempLine.equalsIgnoreCase("weighted")) {
                        weighted = true;
                        continue;
                    }
                    // check for vertices and weights
                    if (tempLine.equalsIgnoreCase("begin")) {
                        if ( (currentLine = graphFileLines.readLine()) != null )
                        {
                            //there must be at least one vertex
                            parseVertices(currentLine, userFile, direct);
                        }

                        //parsing edges
                        //TODO combine if condition with while?
                        while ((currentLine = graphFileLines.readLine()) != null) {
                            //there might not be any edges
                            if (currentLine.equalsIgnoreCase("end")) {
                                break;
                            }

                            tempLine = currentLine;
                            tempLine = tempLine.trim();
                            parseEdges(currentLine, userFile, weighted, direct);
                        }
                        continue;
                    } //end of parsing begin/end pair

                    final String theNextSpace = " ";
                    /** parsing tests
                     * Tokenize the next input line.
                     */
                    String[] tokenArray = userFile.tokenizer(tempLine);
                    System.out.println("debug: " + tempLine);

                    /**
                     * hasEdge (vertex A, vertex B)
                     * Outputs true if the edge exists, otherwise false
                     */

                    if (tokenArray[0].equalsIgnoreCase("hasEdge")) {
                        vertex starting = new vertex(tokenArray[1]);
                        vertex connecting = new vertex(tokenArray[2]);

                        //get expected result
                        String expectedResult = graphFileLines.readLine();
                        expectedResult = expectedResult.replace("\t", " ");
                        expectedResult = expectedResult.trim();

                        //set default return value
                        String result = "false";
                        //get test result
                        if (userFile.hasEdge(starting, connecting) == true) {
                            result = "true";
                        }

                        System.out.println("Parsing hasEdge " + starting.name + " " + connecting.name + ":");
                        System.out.println("Test results. Expected: " + expectedResult + ". Received: " + result + ".");
                        System.out.println();
                    }

                    /**
                     * addEdge (vertex A, vertex B, weight)
                     *   (weight is optional)
                     * Outputs false if nothing changed, true otherwise.
                     */
                    if (tokenArray[0].equalsIgnoreCase("addEdge")) {
                        double weight = 0;
                        vertex starting = new vertex(tokenArray[1]);
                        vertex connecting = new vertex(tokenArray[2]);
                        if(tokenArray[3] != null) {
                            weight = Double.parseDouble(tokenArray[3]);
                        }

                        //get expected result
                        String expectedResult = graphFileLines.readLine();
                        expectedResult = expectedResult.replace("\t", " ");
                        expectedResult = expectedResult.trim();

                        //get test result
                        String result = "false";
                        if (userFile.addEdge(starting, connecting, weight, direct) == true) {
                            result = "true";
                        }

                        System.out.println("Parsing addEdge " + starting.name + " " + connecting.name + " " + weight + ":");
                        System.out.println("Test results. Expected: " + expectedResult + ". Received: " + result + ".");
                        System.out.println();
                    }

                    /**
                     * deleteEdge (vertex A, vertex B)
                     * Outputs true if edge deleted, otherwise false.
                     */
                    if (tokenArray[0].equalsIgnoreCase("deleteEdge")) {
                        vertex starting = new vertex(tokenArray[1]);
                        vertex connecting = new vertex(tokenArray[2]);

                        //get expected result
                        String expectedResult = graphFileLines.readLine();
                        expectedResult = expectedResult.replace("\t", " ");
                        expectedResult = expectedResult.trim();

                        //set default return
                        String result = "false";
                        //get test result
                        if (userFile.deleteEdge(starting, connecting) == true) {
                            result = "true";
                        }

                        System.out.println("Parsing deleteEdge: "  + starting.name + " " + connecting.name + ":");
                        System.out.println("Test results. Expected: " + expectedResult + ". Received: " + result + ".");
                        System.out.println();
                    }
                    /**
                     * addVertex (vertex A)
                     * Outputs true if the vertex is added, otherwise false.
                     */
                    if (tokenArray[0].equalsIgnoreCase("addVertex")) {

                        //get expected result
                        String expectedResult = graphFileLines.readLine();
                        expectedResult = expectedResult.replace("\t", " ");
                        expectedResult = expectedResult.trim();

                        //set default return
                        String result = "false";

                        //get test result
                        if (userFile.addVertex(tokenArray[1], direct) != null) {
                            result = "true";
                        }

                        System.out.println("Parsing addVertex " + tokenArray[1] + ":");
                        System.out.println("Test results. Expected: " + expectedResult + ". Received: " + result + ".");
                        System.out.println();
                    }

                    /**
                     * deleteVertex (vertex A)
                     * Outputs true if the vertex was deleted, otherwise false.
                     */
                    if (tokenArray[0].equalsIgnoreCase("deleteVertex")) {

                        vertex toDelete = new vertex(tokenArray[1]);
                        //get expected result
                        String expectedResult = graphFileLines.readLine();
                        expectedResult = expectedResult.replace("\t", " ");
                        expectedResult = expectedResult.trim();

                        //set default return
                        String result = "false";

                        //get test result
                        if (userFile.deleteVertex(toDelete) == true) {
                            result = "true";
                        }

                        System.out.println("Parsing deleteVertex " + toDelete.name + ":");
                        System.out.println("Test results. Expected: " + expectedResult + ". Received: " + result + ".");
                        System.out.println();
                    }
                    /**
                     * isSparse()
                     * Outputs true if the graph is sparse, otherwise false.
                     */
                    if (tokenArray[0].equalsIgnoreCase("isSparse")) {

                        //get expected result
                        String expectedResult = graphFileLines.readLine();
                        expectedResult = expectedResult.replace("\t", " ");
                        expectedResult = expectedResult.trim();

                        //set default return
                        String result = "false";

                        //get test result
                        if (userFile.isSparse(direct) == true) {
                            result = "true";
                        }

                        System.out.println("Parsing isSparse:");
                        System.out.println("Test results. Expected: " + expectedResult + ". Received: " + result + ".");
                        System.out.println();
                    }

                    /**
                     * isDense()
                     * Outputs true if the graph is dense, otherwise false.
                     */
                    if (tokenArray[0].equalsIgnoreCase("isDense")) {

                        //get expected result
                        String expectedResult = graphFileLines.readLine();
                        expectedResult = expectedResult.replace("\t", " ");
                        expectedResult = expectedResult.trim();

                        //set default return
                        String result = "false";

                        //get test result
                        if (userFile.isDense(direct) == true) {
                            result = "true";
                        }

                        System.out.println("Parsing isDense:");
                        System.out.println("Test results. Expected: " + expectedResult + ". Received: " + result + ".");
                        System.out.println();
                    }
                    /**
                     * countVertices()
                     * Outputs the count of vertices.
                     */
                    if (tokenArray[0].equalsIgnoreCase("countVertices")) {

                        //get expected result
                        String expectedResult = graphFileLines.readLine();
                        expectedResult = expectedResult.replace("\t", " ");
                        expectedResult = expectedResult.trim();

                        //get test result
                        int result = userFile.countVertices();

                        System.out.println("Parsing countVertices:");
                        System.out.println("Test results. Expected: " + expectedResult + ". Received: " + result + ".");
                        System.out.println();
                    }
                    /**
                     * countEdges()
                     * Outputs the count of edges.
                     */
                    if (tokenArray[0].equalsIgnoreCase("countEdges")) {
                        //get expected result
                        String expectedResult = graphFileLines.readLine();
                        expectedResult = expectedResult.replace("\t", " ");
                        expectedResult = expectedResult.trim();

                        //get test result
                        int result = userFile.countEdges();

                        System.out.println("Parsing countEdges:");
                        System.out.println("Test results. Expected: " + expectedResult + ". Received: " + result + ".");
                        System.out.println();
                    }
                    /**
                     * isConnected()
                     * Outputs true if the graph is connected, otherwise false.
                     */
                    if (tokenArray[0].equalsIgnoreCase("isConnected")) {


                        //get expected result
                        String expectedResult = graphFileLines.readLine();
                        expectedResult = expectedResult.replace("\t", " ");
                        expectedResult = expectedResult.trim();

                        //set default return
                        String result = "false";

                        //get test result
                        if (userFile.isConnected() == true) {
                            result = "true";
                        }

                        System.out.println("Parsing isConnected:");
                        System.out.println("Test results. Expected: " + expectedResult + ". Received: " + result + ".");
                        System.out.println();
                    }

                    /**
                     * isFullyConnected()
                     * Outputs true if the graph is fully connected, otherwise false.
                     */
                    if (tokenArray[0].equalsIgnoreCase("isFullyConnected")) {

                        //get expected result
                        String expectedResult = graphFileLines.readLine();
                        expectedResult = expectedResult.replace("\t", " ");
                        expectedResult = expectedResult.trim();

                        //set default return
                        String result = "false";

                        //get test result
                        if (userFile.isFullyConnected() == true) {
                            result = "true";
                        }

                        System.out.println("Parsing isFullyConnected:");
                        System.out.println("Test results. Expected: " + expectedResult + ". Received: " + result + ".");
                        System.out.println();
                    }
                    /*
                    In current form, this would be a cycle.
                    if (tempLine.startsWith("readGraph")) {

                    }
                    */
                    /**
                     * printGraph()
                     * Outputs the current graph.
                     */
                    if (tempLine.startsWith("printGraph")) {
                        userFile.printGraph();
                        System.out.println();
                    }
                }
            }
            else {
                System.out.println("File not found. Tried to load:");
                System.out.println(file + inputFileExtension);
            }
        }
    }
}
