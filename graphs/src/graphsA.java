import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

import static java.nio.file.Files.exists;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

public class graphsA {

    /**
     * Adjacency Matrix:
     * Example: (undirected) vertex A has directly connecting edges to vertices B and E
     *   A B C D E
     * A|\|1|0|0|1|
     * ------------
     * B|1|\|0|0|0|
     * ------------
     * C|0|0|\|0|0|
     * ------------
     * D|0|0|0|\|0|
     * ------------
     * E|1|0|0|0|\|
     * ------------
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

    static final int MAX_GRAPH_VERTICES = 26;
    double adjMatrix[][] = new double [MAX_GRAPH_VERTICES][MAX_GRAPH_VERTICES];
    // maps a vertex name to its index in adjMatrix
    static final String vertexIndex[] = new String[MAX_GRAPH_VERTICES];
    /*
     * Counts the number of times vertex was visited during a connection search.
     *  Nonzero means the graph is connected.
     *  Nonzero in all vertices means graph is fully connected.
     */
    static final int connectedCount[] = new int[MAX_GRAPH_VERTICES];
    private int numAddedVertices = 0;

    /**
     *
     * @param vertexName
     * @param directed
     * @return indexOfVertex
     */
    private int addVertex(String vertexName, boolean directed) {
        int indexOfVertex;

        // handle error cases
        // if outside the graph bounds
        if (numAddedVertices >= MAX_GRAPH_VERTICES)
        {
            return -1;
        }
        // if it is already there
        if (findVertexIndex(vertexName) > 0)
        {
            return -1;
        }

        // add vertexName to vertexIndex
        vertexIndex[numAddedVertices] = vertexName;
        indexOfVertex = numAddedVertices;
        numAddedVertices++;
        return indexOfVertex;
    }

    /**
     *
     * @param starting
     * @param connection
     * @param weight
     * @param directed
     * @return true if edge added, false if edge could not be added
     */
    private boolean addEdge(String starting, String connection, double weight, boolean directed) {

        System.out.println("debug: addEdge: starting = " + starting
                + ", connection = " + connection + ", weight = " + weight + ", directed = " + directed);
        boolean rc = false;

        int indexOfStarting = findVertexIndex(starting);
        int indexOfConnection = findVertexIndex(connection);

        System.out.println("------- debug: addEdge: calling hasEdge does edge exist for " +
                starting + " -> " + connection + " -------");
        if (hasEdge(starting, connection) == false)
        {
            System.out.println("debug: addEdge: hasEdge was false");
            if (indexOfConnection < 0) {
                return rc;
            }

            // if graph is unweighted
            if (weight == 0) {
                weight = 1;
            }
            adjMatrix[indexOfStarting][indexOfConnection] = weight;
            rc = true;

            if (directed == false)
            {
                System.out.println("debug: addEdge: found connection " + connection);
                adjMatrix[indexOfConnection][indexOfStarting] = weight;
            }
        }
        else {
            //edge already exists, see if weight has changed
            if (weight != 0) {
                if (adjMatrix[indexOfStarting][indexOfConnection] != weight) {
                    adjMatrix[indexOfStarting][indexOfConnection] = weight;
                    rc = true;
                }
            }
        }

        System.out.println("debug: addEdge: returns " + rc + ", weight = " + weight);
        return rc;
    }

    /**
     *
     * @param vertexName
     * @return int 
     */
    private int findVertexIndex(String vertexName) {
        int indexOfRoot;

        //System.out.println("debug: findVertexIndex: Looking for vertex " + vertexName);
        for (indexOfRoot = 0; indexOfRoot < MAX_GRAPH_VERTICES; indexOfRoot++) {
            String connectionName = vertexIndex[indexOfRoot];
            // search for name of vertex matching the name caller passed in
            // System.out.println("debug: findVertexIndex: checking for vertex = " + vertexName + ", connection = " + connectionName);
            // ignore null entries
            if (connectionName == null) {
                continue;
            }
            if (connectionName.equals(vertexName)) {
               //  System.out.println("debug: findVertexIndex: found vertex index for " + vertexName);
                return indexOfRoot;
            }
        }

        return -1;
    }

    /**
     *
     * @param starting
     * @param connecting
     * @return
     */
    private boolean deleteEdge(String starting, String connecting, boolean directed) {
        boolean rc = false;
        final int errorFromFindVertexIndex = -1;
        int indexOfStarting = findVertexIndex(starting);
        int indexOfConnecting = findVertexIndex(connecting);
        System.out.println("debug: deleteEdge: starting = " + indexOfStarting + ", connecting = " + indexOfConnecting);
        if ((indexOfStarting == errorFromFindVertexIndex) || (indexOfConnecting == errorFromFindVertexIndex)) {
            return rc;
        }
        System.out.println("debug: deleteEdge: starting = " + vertexIndex[indexOfStarting]
                + ", connecting = " + vertexIndex[indexOfConnecting]);
        System.out.println("debug: deleteEdge: adjMatrix = " + adjMatrix[indexOfStarting][indexOfConnecting]);
        if (adjMatrix[indexOfStarting][indexOfConnecting] != 0) {
            adjMatrix[indexOfStarting][indexOfConnecting] = 0;
            if (directed == true) {
                adjMatrix[indexOfConnecting][indexOfStarting] = 0;
            }
            rc = true;
        } else {
            // edge does not exist
            rc = false;
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
    private boolean deleteVertex(String deleting, boolean directed) {
        boolean rc = false;
        int indexOfDeleting = findVertexIndex(deleting);
        // does vertex exist?
        if (indexOfDeleting == -1) {
            return rc;
        }
        // find and delete connections to the vertex
        for (int indexOfConnection = 0; indexOfConnection < MAX_GRAPH_VERTICES; indexOfConnection++) {
            // should handle all directed graph A -> B cases:
            adjMatrix[indexOfDeleting][indexOfConnection] = 0;
            // undirected graph B -> A cases:
            if (directed == false) {
                adjMatrix[indexOfConnection][indexOfDeleting] = 0;
            }
        }

        // delete the vertex
        vertexIndex[indexOfDeleting] = null;
        rc = true;
        return rc;
    }

    /**
     *
     * @param starting
     * @param connecting
     * @return true if has an edge, false otherwise
     */
    private boolean hasEdge(String starting, String connecting) {
        int indexOfStarting = findVertexIndex(starting);
        int indexOfConnecting = findVertexIndex(connecting);
        final int errorFindVertexIndex = -1;
        if ((indexOfStarting == errorFindVertexIndex) || (indexOfConnecting == errorFindVertexIndex)) {
            return false;
        }

        // invalid cases for an edge
        if ((indexOfStarting == indexOfConnecting)) {
            return false;
        }

        int indexOfRoot;
        for (indexOfRoot = 0; indexOfRoot < MAX_GRAPH_VERTICES; indexOfRoot++) {

           // System.out.println("debug: hasEdge: starting = " + vertexIndex[indexOfStarting]
             //       + ", connecting = " + vertexIndex[indexOfConnecting]
               //     + ", weight = " + adjMatrix[indexOfStarting][indexOfStarting]);
            if (adjMatrix[indexOfStarting][indexOfConnecting] != 0) {
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
        double numEdges = countEdges(direct);
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
        double numEdges = countEdges(direct);
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
     * @return numVertices
     */
    private int countVertices() {
        int numVertices = 0;
        for (int indexOfVertices = 0; indexOfVertices < MAX_GRAPH_VERTICES; indexOfVertices++) {
            if (vertexIndex[indexOfVertices] != null) {
                numVertices++;
            }
        }
        return numVertices;
    }

    /**
     *
     * @return numEdges
     */
    private int countEdges(boolean directed) {
        int indexOfStarting = 0;
        int indexOfConnecting = 0;
        int numEdges = 0;
        for (indexOfStarting = 0; indexOfStarting < MAX_GRAPH_VERTICES; indexOfStarting++) {
            // check the first triangle, regardless of directed or undirected
            if (vertexIndex[indexOfStarting] != null) {
                for (indexOfConnecting = 0; indexOfConnecting < indexOfStarting; indexOfConnecting++) {
                    if ((vertexIndex[indexOfConnecting] != null)
                            && (adjMatrix[indexOfStarting][indexOfConnecting] > 0)) {
                        numEdges++;
                    }
                }
            }
        }
        // undirected: check the second triangle
        if (directed == false) {
            for (indexOfConnecting = 0; indexOfConnecting < MAX_GRAPH_VERTICES; indexOfConnecting++) {
                if (vertexIndex[indexOfConnecting] != null) {
                    for (indexOfStarting = 0; indexOfStarting > indexOfConnecting; indexOfStarting++) {
                        if ((vertexIndex[indexOfStarting] != null)
                                && (adjMatrix[indexOfConnecting][indexOfStarting] > 0)) {
                            numEdges++;
                        }
                    }
                }
            }
        }

        return numEdges;
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
     * @return boolean
     */

    private boolean isConnected() {
        boolean returnCount = true;
        int numOfVertices = countVertices();
        int indexOfVertices;
        final int unvisited = 0;


        // mark all vertices as unvisited
        for (indexOfVertices = 0; indexOfVertices < MAX_GRAPH_VERTICES; indexOfVertices++) {
            connectedCount[indexOfVertices] = 0;
        }

        // search the first vertex and see if can reach all of the other vertices from it
        indexOfVertices = 0;

        // ensure that the first element given dfs is a valid vertex
        for (indexOfVertices = 0; indexOfVertices < MAX_GRAPH_VERTICES; indexOfVertices++) {
            if (vertexIndex[indexOfVertices] != null) {
                break;
            }
        }

        dfs(indexOfVertices);

        // check if all the vertices were visited
        for (indexOfVertices = 0; indexOfVertices < MAX_GRAPH_VERTICES; indexOfVertices++) {
            if (vertexIndex[indexOfVertices] == null) {
                continue;
            }
            // if the connection count for any vertex is 0, then all the vertices were not visited
            if (connectedCount[indexOfVertices] == 0) {
                returnCount = false;
                break;
            }
        }
        for (indexOfVertices = 0; indexOfVertices < MAX_GRAPH_VERTICES; indexOfVertices++) {
            System.out.println("debug: isConnected: connectedCount = " + connectedCount[indexOfVertices]);
        }
        return returnCount;
    }

    /**
     * DFS: Depth First Search
     * @param adjacencyList
     */

    // only done at load time
    static int count = 1;
    private void dfs(int indexOfVertex) {
        System.out.println("debug: dfs: called with indexOfVertex = " + indexOfVertex);
        // mark this vertex as visited
        connectedCount[indexOfVertex] = count;

        // look at each vertex connected to the first vertex in this list
        for (int indexOfConnection = 1; indexOfConnection < MAX_GRAPH_VERTICES; indexOfConnection++) {
            // ignore diagonal
            if (indexOfVertex == indexOfConnection) {
                continue;
            }
            if (vertexIndex[indexOfConnection] == null) {
                continue;
            }
            // undirected: check for connections
            System.out.println("debug: dfs: check for connections: adjMatrix = " + adjMatrix[indexOfVertex][indexOfConnection]
                    + ", connectedCount[indexOfConnection] = " + connectedCount[indexOfConnection]);
            System.out.println("debug: dfs: indexOfVertex = " + indexOfVertex + ", indexOfConnection = " + indexOfConnection);
            if (adjMatrix[indexOfVertex][indexOfConnection] != 0) {
                // if current element is unvisited, then increment count and also search the element's adjacency matrix
                if (connectedCount[indexOfConnection] == 0) {
                    count = count + 1;
                    dfs(indexOfConnection);
                }
            }
        }
    }

    private void printAdjMatrix() {
        System.out.println("name 000 001 002 003 004 005 006 007 008 009 010 011 012 013 014 015 016 017 018 019 020 021 022 023 024 025 026" );
        for (int indexOfRow = 0; indexOfRow < MAX_GRAPH_VERTICES; indexOfRow++) {
            System.out.print(vertexIndex[indexOfRow] + " ");
            for (int indexOfCol = 0; indexOfCol < MAX_GRAPH_VERTICES; indexOfCol++) {
                System.out.print(adjMatrix[indexOfRow][indexOfCol] + " ");
            }
            System.out.println();
        }
    }

    /**
     * There is an edge from every vertex to every other vertex.
     * If the graph is fully connected, a
     *
     * rc = return code
     * @return
     */
    private boolean isFullyConnected() {
        //debug:
        printAdjMatrix();

        boolean rc = true;
        int indexOfStarting = 0;
        int indexOfConnecting = 0;
        for (indexOfStarting = 0; indexOfStarting < MAX_GRAPH_VERTICES; indexOfStarting++) {
            // check the first triangle, regardless of directed or undirected
            if (vertexIndex[indexOfStarting] != null) {
                for (indexOfConnecting = 0; indexOfConnecting < MAX_GRAPH_VERTICES; indexOfConnecting++) {
                    // ignore the diagonal
                    if (indexOfStarting == indexOfConnecting) {
                        continue;
                    }
                    if ((vertexIndex[indexOfConnecting] != null)
                            && (adjMatrix[indexOfStarting][indexOfConnecting] == 0)) {
                        rc = false;
                        return rc;
                    }
                }
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

    private void printGraph(boolean direct) {
        int indexOfStarting;
        int indexOfConnecting;
        String weight = "-.-";
        String directed = "-";

        // go through all the matrix
        for (indexOfStarting = 0; indexOfStarting < MAX_GRAPH_VERTICES; indexOfStarting++) {

            // ignoring non-populated elements in the adjacency matrix
            if (vertexIndex[indexOfStarting] == null) {
                continue;
            }
            System.out.print(vertexIndex[indexOfStarting]);

            // go through all the vertices connected to the starting vertex
            for (indexOfConnecting = 0; indexOfConnecting < MAX_GRAPH_VERTICES; indexOfConnecting++) {
                // ignore diagonal
                if (indexOfStarting == indexOfConnecting) {
                    continue;
                }
                double weightValue = adjMatrix[indexOfStarting][indexOfConnecting];
                // ignoring non-populated and unconnected elements in the adjacency matrix
                if ((vertexIndex[indexOfConnecting] == null) || (weightValue == 0)) {
                    // determining if need a new line
                    if (indexOfConnecting == MAX_GRAPH_VERTICES-1) {
                        System.out.println();
                    }

                    continue;
                }

                if (weightValue > 1) {
                    weight = String.valueOf(adjMatrix[indexOfStarting][indexOfConnecting]);
                }

                // when directed graph, directed will show the direction (assumes first to second)
                if (direct == true) {
                    directed = ">";
                }

                System.out.print("-" + weight + directed + vertexIndex[indexOfConnecting]);
            }
        }
    }

    /**
     *
     * @param currentLine
     * @param userFile
     * @param directed
     */
    private static void parseVertices(String currentLine, graphsA userFile, boolean directed) {
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
    private static void parseEdges(String currentLine, graphsA userFile, boolean weight, boolean directed) {
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
            userFile.addEdge(startingVertexName, connectingVertexName, weightNumber, directed);
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
        graphsA userFile = new graphsA();
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
                        //get expected result
                        String expectedResult = graphFileLines.readLine();
                        expectedResult = expectedResult.replace("\t", " ");
                        expectedResult = expectedResult.trim();

                        //set default return value
                        String result = "false";
                        //get test result
                        if (userFile.hasEdge(tokenArray[1], tokenArray[2]) == true) {
                            result = "true";
                        }

                        System.out.println("Parsing hasEdge " + tokenArray[1] + " " + tokenArray[2] + ":");
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
                        if(tokenArray[3] != null) {
                            weight = Double.parseDouble(tokenArray[3]);
                        }

                        //get expected result
                        String expectedResult = graphFileLines.readLine();
                        expectedResult = expectedResult.replace("\t", " ");
                        expectedResult = expectedResult.trim();

                        //get test result
                        String result = "false";
                        if (userFile.addEdge(tokenArray[1], tokenArray[2], weight, direct) == true) {
                            result = "true";
                        }

                        System.out.println("Parsing addEdge " + tokenArray[1] + " " + tokenArray[2] + " " + weight + ":");
                        System.out.println("Test results. Expected: " + expectedResult + ". Received: " + result + ".");
                        System.out.println();
                    }

                    /**
                     * deleteEdge (vertex A, vertex B)
                     * Outputs true if edge deleted, otherwise false.
                     */
                    if (tokenArray[0].equalsIgnoreCase("deleteEdge")) {

                        //get expected result
                        String expectedResult = graphFileLines.readLine();
                        expectedResult = expectedResult.replace("\t", " ");
                        expectedResult = expectedResult.trim();

                        //set default return
                        String result = "false";
                        //get test result
                        if (userFile.deleteEdge(tokenArray[1], tokenArray[2], direct) == true) {
                            result = "true";
                        }

                        System.out.println("Parsing deleteEdge: "  + tokenArray[1] + " " + tokenArray[2] + ":");
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
                        if (userFile.addVertex(tokenArray[1], direct) > -1) {
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
                        //get expected result
                        String expectedResult = graphFileLines.readLine();
                        expectedResult = expectedResult.replace("\t", " ");
                        expectedResult = expectedResult.trim();

                        //set default return
                        String result = "false";

                        //get test result
                        if (userFile.deleteVertex(tokenArray[1], direct) == true) {
                            result = "true";
                        }

                        System.out.println("Parsing deleteVertex " + tokenArray[1] + ":");
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
                        int result = userFile.countEdges(direct);

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
                        userFile.printGraph(direct);
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
