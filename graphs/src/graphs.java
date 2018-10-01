import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

public class graphs {

    private class vertex {
        String name;
        int connectedCount;
        int weight;
        boolean directed;

        vertex() {
            name = null;
            connectedCount = 0;
            weight = 0;
            directed = false;

        }

        vertex(String vertexName) {
            name = vertexName;
            connectedCount = 0;
            weight = 0;
            directed = false;
        }
    }

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

    private void addVertex(vertex beingCreated) {
        LinkedList<vertex> newAdjacencyList = new LinkedList<vertex>();
        newAdjacencyList.add(beingCreated);
        listOfAdjacencyLists.add(newAdjacencyList);
    }

    // TODO Error checking for if this fails
    // TODO reevaluate now that have added connectivity attribute
    private void addEdge(vertex starting, vertex connection, int weight, boolean directed) {
        int indexOfStarting;

        LinkedList<vertex> adjacencyList = findAdjacencyList(starting);
        if (adjacencyList != null) {
            connection.weight = weight;
            connection.directed = directed;
            adjacencyList.add(connection);
            if (directed == false)
            {
                adjacencyList = findAdjacencyList(connection);
                if (adjacencyList != null) {
                    starting.weight = weight;
                    starting.directed = directed;
                    adjacencyList.add(starting);
                }
            }
        }
    }

    private LinkedList<vertex> findAdjacencyList(vertex root) {
        int indexOfRoot;
        for (indexOfRoot = 0; indexOfRoot < listOfAdjacencyLists.size(); indexOfRoot++) {
            // search for name of vertex matching the name in the adjacency list
            if (listOfAdjacencyLists.get(indexOfRoot).getFirst().name == root.name) {
                return listOfAdjacencyLists.get(indexOfRoot);
            }
        }
        return null;
    }

    private void deleteEdge(vertex starting, vertex connecting) {
        LinkedList<vertex> adjacencyList = findAdjacencyList(starting);
        if (adjacencyList == null)         {
            return;
        }
        int indexOfRoot;
        int indexOfRoot2 = 0;
        for (indexOfRoot = 0; indexOfRoot < adjacencyList.size(); indexOfRoot++) {
            if (adjacencyList.get(indexOfRoot).name == connecting.name) {
                // Have found the target connection.
                // If it's not a directed graph, have to delete the connection both ways.
                // delete A -> B && delete B -> A
                if (adjacencyList.get(indexOfRoot).directed == false) {
                    LinkedList<vertex> adjacencyList2 = findAdjacencyList(starting);
                    for (indexOfRoot2 = 0; indexOfRoot2 < adjacencyList2.size(); indexOfRoot2++) {
                        if (adjacencyList.get(indexOfRoot2).name == starting.name) {
                            // delete B -> A
                            adjacencyList2.remove(indexOfRoot2);
                            break;
                        }
                    }
                }
                // delete directed graph connection
                // delete A-> B
                adjacencyList.remove(indexOfRoot);
                break;
            }
        }
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
    private void deleteVertex(vertex deleting) {
        LinkedList<vertex> adjacencyList = findAdjacencyList(deleting);
        if (adjacencyList == null)         {
            return;
        }
        final int indexOfFirstEdge = 1;
        // have to delete all connections (edges) before remove the vertex
        // find all of the direct connections to the target vertex and
        // delete them
        if (adjacencyList != null) {
            // TODO: Verify this does not have problems because it is changing the Linked List every time it deletes
            while (adjacencyList.size() > indexOfFirstEdge) {

                if (adjacencyList.get(indexOfFirstEdge).directed == false) {
                    deleteEdge(adjacencyList.get(indexOfFirstEdge), deleting);
                }
                // Note: deleteEdge will delete the second item in the adjacency list when finished
                // Example: delete B -> A and then A -> B
                else {
                    adjacencyList.remove(indexOfFirstEdge);
                }
            }
            // Now delete the vertex. (Example: Delete |A| -> B -> E has become |A| -> null)
            listOfAdjacencyLists.remove(adjacencyList);
        }
    }

    private boolean hasEdge(vertex starting, vertex connecting) {
        LinkedList<vertex> adjacencyList = findAdjacencyList(starting);
        if (adjacencyList == null) {
            return false;
        }
        int indexOfRoot;
        for (indexOfRoot = 0; indexOfRoot < adjacencyList.size(); indexOfRoot++) {
            if (connecting.name == adjacencyList.get(indexOfRoot).name) {
                return true;
            }
        }
        adjacencyList = findAdjacencyList(connecting);
        for (indexOfRoot = 0; indexOfRoot < adjacencyList.size(); indexOfRoot++) {
            if (starting.name == adjacencyList.get(indexOfRoot).name) {
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
    private boolean isSparse()
    {
        //if numVertices <= .15 * numEdges then graph is sparse
        final double densityProportion = 0.15;
        double numEdges = countEdges();
        double numVertices = countVertices();
        if (numVertices <= densityProportion*numEdges) {
            return true;
        }
        return false;
    }

    /**
     * If 85% of the vertices (or higher) are connected.
     * @return
     */
    private boolean isDense()
    {
        // if numEdges >= 0.85 * numVertices then think the graph is dense
        final double densityProportion = 0.85;
        double numEdges = countEdges();
        double numVertices = countVertices();
        if (numEdges >= densityProportion*numVertices) {
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

    private int countEdges() {

        int count = 0;
        int indexOfAdjacencyList;
        // -1 to avoid double counting the vertex
        final int countOffset = -1;
        for (indexOfAdjacencyList = 0; indexOfAdjacencyList < listOfAdjacencyLists.size(); indexOfAdjacencyList++) {
            LinkedList<vertex> adjacencyList = listOfAdjacencyLists.get(indexOfAdjacencyList);
            count += adjacencyList.size() - countOffset;
        }
        return count;
    }

    /**
     * Build map of all connections (a matrix?)
     * Then verify that there is a path to all vertices.
     *
     * Fully connected is also connected:
     * A -> B -> C -> D -> E
     *
     * Connected: If can travel through the whole graph via edges.
     *
     * Since vertices are copies, have to also update all the copies.
     *
     * @param starting
     * @param ending
     * @return
     */
    //TODO Unfinished. Currently Causes compile error.
    private boolean isConnected(vertex starting, vertex ending) {
        boolean rc = true;
        int numOfVertices = countVertices();
        int indexOfAdjacencyList;
        for (indexOfAdjacencyList = 0; indexOfAdjacencyList < listOfAdjacencyLists.size(); indexOfAdjacencyList++) {
            LinkedList<vertex> adjacencyList = listOfAdjacencyLists.get(indexOfAdjacencyList);
            //if connection count for this vertex has not been updated yet, call depth first search
            if (adjacencyList.get(indexOfAdjacencyList).connectedCount == 0) {
                dfs(adjacencyList.get(indexOfAdjacencyList));
                break;
            }
        }
        return rc;
    }

    /**
     * DFS: Depth First Search
     * @param adjacencyList
     */
    //TODO Unfinished. Currently Causes compile error.
    private void dfs(LinkedList<vertex> adjacencyList) {

    }

    /**
     * There is an edge from every vertex to every other vertex.
     * If the graph is fully connected, the length of each sublist will match the length of the
     * list of lists, which is the number of vertices.
     * rc = return code
     * @param starting
     * @return
     */
    private boolean isFullyConnected(vertex starting) {
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

    private void readGraph(String inputFile) throws IOException
    {
        String inputFileExtension = ".txt";
        String inputFilename = System.getProperty("user.dir") + "\\" + inputFile + inputFileExtension;
    }

    private void printGraph() {

    }

    public static void main(String[] args) throws IOException
    {
        graphs userFile = new graphs();

        // Takes user input as an argument.
        // Bare bones user input error handling for no arguments.
        if (args.length == 0)
        {
            //user input error, no arguments
            System.out.println("Input Error: Filename required as argument. Expects input of the form:\n"
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
        else
        {
            //parse user file as commandline argument
            String fileName = args[0];
            userFile.readGraph(fileName);
        }

        
    }
}
