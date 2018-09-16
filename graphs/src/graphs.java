import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

public class graphs {

    private class vertex {
        String name;
        int weight;
        boolean directed;

        vertex() {
            name = null;
            weight = 0;
            directed = false;
        }

        vertex(String vertexName) {
            name = vertexName;
            weight = 0;
            directed = false;
        }
    }

    /**
     * These are sub-lists.
     * There is one of these lists per vertex. Each list starts with one vertex, and each
     * additional vertex in the list indicates an edge between the first vertex and that
     * vertex.
     *\
     * This is not a list of sequential edges, such as A to B to C. Rather, it is all the
     * edges that connect to A.
     */
    private LinkedList<vertex> vertexEdges;

    /**
     * This is the master list of all vertex edge lists.
     * An individual vertex list is a linked list of a particular vertex and all its edges.
     */
    private LinkedList<LinkedList <vertex>> vertexEdgeMap;

    private void addVertex(vertex inserting) {
        LinkedList<vertex> newEdge = new LinkedList<vertex>();
        newEdge.add(inserting);
        vertexEdgeMap.add(newEdge);
    }

    // TODO Error checking for if this fails
    private void addEdge(vertex starting, vertex connection, int weight, boolean directed) {
        int indexOfStarting;

        LinkedList<vertex> edgeList = findEdgeList(starting);
        if (edgeList != null) {
            connection.weight = weight;
            connection.directed = directed;
            edgeList.add(connection);
            if (directed == false)
            {
                edgeList = findEdgeList(connection);
                if (edgeList != null) {
                    starting.weight = weight;
                    starting.directed = directed;
                    edgeList.add(starting);
                }
            }
        }
    }

    private LinkedList<vertex> findEdgeList(vertex root) {
        int indexOfRoot;
        for (indexOfRoot = 0; indexOfRoot < vertexEdgeMap.size(); indexOfRoot++) {
            if (vertexEdgeMap.get(indexOfRoot).getFirst().name == root.name) {
                return vertexEdgeMap.get(indexOfRoot);
            }
        }
        return null;
    }

    private void deleteEdge(vertex starting, vertex connecting) {
        LinkedList<vertex> edgeList = findEdgeList(starting);
        int indexOfRoot;
        int indexOfRoot2 = 0;
        for (indexOfRoot = 0; indexOfRoot < edgeList.size(); indexOfRoot++) {
            if (edgeList.get(indexOfRoot).name == connecting.name) {
                if (edgeList.get(indexOfRoot2).directed == false) {
                    LinkedList<vertex> edgeList2 = findEdgeList(connecting);
                    for (indexOfRoot2 = 0; indexOfRoot2 < edgeList2.size(); indexOfRoot2++) {
                        edgeList2.remove(indexOfRoot2);
                        break;
                    }
                }
                edgeList.remove(indexOfRoot);
                break;
            }
        }
    }

    private void deleteVertex(vertex deleting) {
        LinkedList<vertex> edgeList = findEdgeList(deleting);
        if (edgeList != null) {
            vertexEdgeMap.remove(edgeList);
        }
    }

    private boolean hasEdge(vertex starting, vertex connecting) {
        return false;
    }

    private boolean isSparse() {
        return false;
    }

    private boolean isDense() {
        return false;
    }

    private int countVertices() {
        int count = 0;
        return count;
    }

    private int countEdges() {
        int count = 0;
        return count;
    }

    private boolean isConnected(vertex starting, vertex ending) {
        return false;
    }

    /**
     * Edge from every vertex.
     * @param starting
     * @return
     */
    private boolean isFullyConnected(vertex starting) {
        return false;
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
