import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Controller {
    // List to Store the nodes;
    private ArrayList<Node> nodes;

    // Graph of the entire network;
    private HashMap<Node, ArrayList<Node>> graphOfNetwork=null;

    public Controller() {
        this.nodes = new ArrayList<Node>();
        this.graphOfNetwork = new HashMap<>();

        // Add new node to the network;
        for(int i = 0; i < 10; i++){
            this.nodes.add(new Node(i));
        }
    }

    // Function to retrieve node when we need it;
    public Node getNode(int id){
        return this.nodes.get(id);
    }

    // Function that returns the graph of the network;
    public HashMap<Node, ArrayList<Node>> getNetworkGraph(){
        return this.graphOfNetwork;
    }

    // function to get the topology of the map from the topology.txt
    public void getTopology(String fileName){

        // map to store the 
        HashMap<Node, ArrayList<Node>> topology = new HashMap<>();
        ArrayList<String> map = Helper.fileReader(fileName);

        // iterate over the nodes and update them in the map;

        for (int i = 0; i < map.size(); i++) {
            int source = map.get(i).charAt(0) - '0';
            int destination = map.get(i).charAt(2) - '0';

            // If the node is not already in the map; update it with a list for storing neighbors;
            topology.putIfAbsent(getNode(source), new ArrayList<Node>());

            // If node is already, just update their neighbor in the list;
            topology.get(getNode(source)).add(getNode(destination));
        }

        // if map is not null, we have the topology map;
        if (topology != null) {
            this.graphOfNetwork = topology;
        }
    }
    

    // Function to send the message to the neigbor;
    public void sendMessageToNeighbor() {

        // Iterate through the nodes in the element
        for(Map.Entry<Node, ArrayList<Node>> e: this.getNetworkGraph().entrySet()) {

            // get the output file for this particular element;
            String outputFile = e.getKey().getOutputFileLocation();

            // Get the list of messages for this corresponding node;
            ArrayList<String> messages = Helper.fileReader(outputFile);

            // List of current Messages
            List<String> currentMessages = messages.subList(e.getKey().getOutputFileInstant(), messages.size());

            // Update the neighbor's message input file with message;
            for (Node node: e.getValue()) {
                String inputFile = node.getInputFileLocation();
                currentMessages.stream().forEach(message -> Helper.fileWriter(message, inputFile));
            }
            e.getKey().setOutputFileInstant(messages.size());
        }

        for (Map.Entry<Node, ArrayList<Node>> element: this.getNetworkGraph().entrySet()) { 
            element.getKey().addIncomingMessagesToInputFile();   
        }
    }

    // Function to send message to the neighbor;

    public void sendMessageToNeighbor(Node node){
        // fetch the path of the output file;
        String outputFile = node.getOutputFileLocation();

        // list to store messsages;
        ArrayList<String> messages = Helper.fileReader(outputFile);
        List<String> currentMessages = messages.subList(node.getOutputFileInstant(), messages.size());
        
        // List to update the neighbor about our message;
        for(Node n : this.getNetworkGraph().get(node)){
            String inputFile = n.getInputFileLocation();
            currentMessages.stream().forEach(message->Helper.fileWriter(message, inputFile));
        }

        //set node's output file point;
        node.setOutputFileInstant(messages.size());

        // Get the node and set the incoming message to it;
        for(Node n: this.getNetworkGraph().get(node)){
            n.addIncomingMessagesToInputFile();
        }
    }

    public static void main(String[] args) {
        // Creating a controller instance;
        Controller controller = new Controller();

        // make map out of topology file;
        String file = "topology.txt";
        controller.getTopology(file);

        // init controller for all nodes & send initial hello broadcast;
        for (int i = 0; i < 10; i++) {
            controller.getNode(i).setController(controller);
            controller.getNode(i).sendHelloBroadcast();
        }

        // call function to send the message to neighbor;
        controller.sendMessageToNeighbor();

        // Build In-Tree;
        for (int i = 0; i < 10; i++) {
            controller.getNode(i).constructInTree();
        }

        //similarly, again call function to send the message to neighbor;
        controller.sendMessageToNeighbor();
    }
}
