import java.util.*;
import java.util.stream.Collectors;
import java.io.*;


class Node{

    // node ID;
    private int nodeID;

    // Data Structure to hold the neighbor of the incoming node;
    private Set<Integer> incomingNeighborsOfNode;

    // Liet of updated neighbors
    private Set<Integer> updatedNeighborList;

    // Point from which we have to start looking at input file;
    private int inputFileReadInstant;

    // simlarly, the output point;
    private int outputFileInstant;

    // String to store current InTree
    private String currInTree;

    // List to store the links in the tree of the incoming links;
    private List<String> listOfIntreeLinks;
    
    // The input file path;
    private String InputFilePath;

    // The output file path;
    private String outputFilePath;

    // List containing transmitted message;
    private List<String> messageSent;

    // Map to store the neighbor nodes with their In-Trees;
    private HashMap<Character, List<String>> neighborTreesOfNode;

    // Calling in the instance of Controller;
    private Controller controller;

    // Constructor of Node Class;
    Node(int nodeID){
        this.nodeID = nodeID;
        this.incomingNeighborsOfNode = new TreeSet<>();
        this.updatedNeighborList = new HashSet<>();
        this.inputFileReadInstant = 0;
        this.outputFileInstant = 0;
        this.InputFilePath = "InputFolder/input_"+nodeID;
        this.outputFilePath =  "OutputFolder/output_"+this.getNodeID();
        this.currInTree = this.getNodeID()+" ";
        this.listOfIntreeLinks = new LinkedList<>();
        this.messageSent = new ArrayList<>();
        this.neighborTreesOfNode = new HashMap<>();  
    }

    // Function to init the controller transfer;
    public void setController(Controller controller) {
        this.controller = controller;
    }

    // funtion to get the ID of the node;
    public int getNodeID() {
        return this.nodeID;
    }

    // func to set the instance where we read the input file;
    public void setInputFileInstant(int instance) {
        this.inputFileReadInstant = instance;
    }


    // func to get the instant from which input file has to be read;
    public int getInputFileInstant() {
        return this.inputFileReadInstant;
    }

    // Function to get input file path
    public String getInputFileLocation() {
        return this.InputFilePath;
    }

    // func to get the output file path;
    public String getOutputFileLocation() {
        return this.outputFilePath;
    }

    // Function to set the instant from where the output file has to be read;
    public void setOutputFileInstant(int instant) {
        this.outputFileInstant = instant;
    }

    // Function to get the instant from where we have to read the outpiut file
    public int getOutputFileInstant() {
        return this.outputFileInstant;
    }

    // function to link the current path link
    public void setCurrentPathLinks(List<String> pathLinks) {
        this.listOfIntreeLinks = new LinkedList<>(pathLinks);
    }

    // function to return the list with the current linkage/paths
    public List<String> getCurrentPathLinks() {
        return this.listOfIntreeLinks;
    }

    // return the map with neighbor of the trees
    public HashMap<Character,List<String>> getNeighborTree() {
        return this.neighborTreesOfNode;
    }

    // function to add incoming 
    
    public void addIncomingMessagesToInputFile() {
        // Fetch the file;
        File file = new File(InputFilePath);

        // If you have a file with already stored input path;
        if(file.exists()){

            // Read the content of the file;
            ArrayList<String> fileContent = Helper.fileReader(InputFilePath);
            List<String> currentContentInFile = fileContent.subList(getInputFileInstant(), fileContent.size());

            // Update the lists;
            List<String> helloMessageList = currentContentInFile.stream().filter(s->s.contains("Hello")&(!s.contains("data"))).collect(Collectors.toList());
            List<String> inTreeMessageList = currentContentInFile.stream().filter(s->s.contains("intree")).collect(Collectors.toList());
            List<String> messageSent = currentContentInFile.stream().filter(s->s.contains("data")).collect(Collectors.toList());    

            
            setInputFileInstant(fileContent.size());

            // if hello message isn't received, add it to the incoming neighbor list;
            if (!helloMessageList.isEmpty()) {
                addIncomingNeighbor(helloMessageList);
            }

            // if it's not in the intree; update it;
            if (!inTreeMessageList.isEmpty()) {
                updateTree(inTreeMessageList);        
            }

            // if messageSent is not empty;
            if (!messageSent.isEmpty()) {

            }
        }
    }

    // Function to add incoming neighbor;
    public void addIncomingNeighbor(List<String> helloMessageList){
        // Add the neighbor at the 6th index in the incoming neighbor
        helloMessageList.forEach(s -> incomingNeighborsOfNode.add(s.charAt(6)-'0'));

        // Update the neighbor at the 6th index in the incoming neighbor
        helloMessageList.forEach(s -> updatedNeighborList.add(s.charAt(6)-'0'));
    }

    // If neighbors aren't connected, they shall be removed;
    // Function to remove the not connected components;
    public void removeNotConnectedNeighbors(){
        // if updated neighbor list is different than incoming neighbor nodes, update it:
        if(!updatedNeighborList.equals(incomingNeighborsOfNode)){
            incomingNeighborsOfNode = updatedNeighborList;
        }

        // set updated neighbor list to zero;
        updatedNeighborList = new HashSet<>();
        return;
    }

    // Function to send hello broadcast to every node in the network;
    public void sendHelloBroadcast(){
        // hello message 
        String message = "Hello "+this.getNodeID();

        // write it in the output file of the nodes;
        Helper.fileWriter(message, outputFilePath);
    }
    public void constructInTree(){
        //System.out.println(incomingNeighborsOfNode.size());
        incomingNeighborsOfNode.stream().forEach(node->this.currInTree+="("+node+" "+this.getNodeID()+")");
        incomingNeighborsOfNode.stream().forEach(node->this.listOfIntreeLinks.add(node+" "+this.getNodeID()));
        sendRouting();
    }

    public void updateTree(List<String> inTreeMessageList){
        //System.out.println(this.getNodeID());
        for(String message: inTreeMessageList){
            char treeRootNode = message.charAt(7);
            String[] paths = message.split("[()]");
            List<String> temporaryPaths = new LinkedList<>();
            for(String path: paths){
                if(path.length() == 0 || path.contains("intree")){
                    continue;
                }           
                if(!this.currInTree.contains(path.charAt(0)+"")){
                    this.currInTree+="("+path+")";
                    this.listOfIntreeLinks.add(path);    
                }
                else{
                    temporaryPaths.add(path);        
                }
            }
            if(temporaryPaths.size()!=0){
                List<String> finalLinks = treesMergeBFS(treeRootNode,temporaryPaths);
                if(Helper.compareLists(finalLinks, getCurrentPathLinks())) {
                    continue;
                }
                setCurrentPathLinks(finalLinks);
                
            }    
            
            sendRouting();
            controller.sendMessageToNeighbor(this);     
        }
        
    }

    private void changeCollections(int pointer, LinkedList<String> workingPaths, List<String> otherListPaths, List<String> result, ArrayList<Character> level) {

        result.add(workingPaths.get(pointer));
        updateList(workingPaths.get(pointer).charAt(0),otherListPaths);

        if(!level.contains(workingPaths.get(pointer).charAt(0))){
            level.add(workingPaths.get(pointer).charAt(0));
        }
    }

    // Function to update the list by removing node or a path;
    private void updateList(char removeNode, List<String> listWithNodesToRemove) {
        // List to store paths to remove;
        List<String> pathsToRemove = new ArrayList<>();

        // Iterate over target list;
        for(String path : listWithNodesToRemove){
            // if path has target node;
            if(path.contains(removeNode+"")) {
                // remove it;
                pathsToRemove.add(path);
            }
        }

        // remove paths;
        for(String path : pathsToRemove){
            listWithNodesToRemove.remove(path);
        }

        return;
    }

    // Function to implement sendRouting as per the instructions;
    public void sendRouting(){

        // output string;
        String out = "intree " + nodeID + " ";

        for(String data : this.listOfIntreeLinks){
            out += "(" + data + ")";
        }

        // write the output into the file;
        Helper.fileWriter(out, outputFilePath);
    }

    // Function to merge tress using SPT (BFS)
    
    public List<String> treesMergeBFS(char rootNode, List<String> temporaryPaths) {
        // List to store the level order nodes;
        ArrayList<ArrayList<Character>> levelOrderNodes = new ArrayList<>();
        LinkedList<String> currentPaths = new LinkedList<>(this.listOfIntreeLinks);
        ArrayList<String> result = new ArrayList<>();

        int count = 0;
        char source = (getNodeID() + "").charAt(0);

        updateList(source, temporaryPaths);
        updateList(rootNode, currentPaths);
        temporaryPaths.add(rootNode + " " + source);
        levelOrderNodes.add(new ArrayList<>());

        levelOrderNodes.get(0).add(source);
        while (count++ < levelOrderNodes.size()) {
            LinkedList<String> workingCurrPaths = new LinkedList<>();
            LinkedList<String> workingTempPaths = new LinkedList<>();
            ArrayList<Character> level = new ArrayList<>();
            int countC = 0, countT = 0;

            for (String path : currentPaths) {
                if (levelOrderNodes.get(count - 1).contains(path.charAt(path.length() - 1))) {
                    workingCurrPaths.add(path);
                }
            }
            Collections.sort(workingCurrPaths);
            for (String path : temporaryPaths) {
                if (levelOrderNodes.get(count - 1).contains(path.charAt(path.length() - 1))) {
                    workingTempPaths.add(path);
                }
            }
            Collections.sort(workingTempPaths);

            while (countC < workingCurrPaths.size() && countT < workingTempPaths.size()) {
                if (workingCurrPaths.get(countC).charAt(0) < workingTempPaths.get(countT).charAt(0)) {
                    if (!result.contains(workingCurrPaths.get(countC))) {
                        changeCollections(countC, workingCurrPaths, temporaryPaths, result, level);
                    }
                    countC++;
                } else if (workingCurrPaths.get(countC).charAt(0) > workingTempPaths.get(countT).charAt(0)) {
                    if (!result.contains(workingTempPaths.get(countT))) {
                        changeCollections(countT, workingTempPaths, currentPaths, result, level);
                    }
                    countT++;
                } else {
                    if (workingCurrPaths.get(countC).charAt(0) < workingTempPaths.get(countT).charAt(0)) {
                        if (!result.contains(workingCurrPaths.get(countC))) {
                            changeCollections(countC, workingCurrPaths, temporaryPaths, result, level);
                        }
                        countC++;
                    } else {
                        if (!result.contains(workingTempPaths.get(countT))) {
                            changeCollections(countT, workingTempPaths, currentPaths, result, level);
                        }
                        countT++;
                    }
                }
            }
            if (countC < workingCurrPaths.size()) {
                for (int i = countC; i < workingCurrPaths.size(); i++) {
                    char pathNode = workingCurrPaths.get(i).charAt(0);
                    if (result.stream().filter(path -> path.charAt(0) == pathNode).count() == 0) {
                        changeCollections(i, workingCurrPaths, temporaryPaths, result, level);
                    }
                }
            }

            if (countT < workingTempPaths.size()) {
                for (int i = countT; i < workingTempPaths.size(); i++) {
                    char pathNode = workingTempPaths.get(i).charAt(0);
                    if (result.stream().filter(path -> path.charAt(0) == pathNode).count() == 0) {
                        changeCollections(i, workingTempPaths, currentPaths, result, level);
                    }
                }
            }
            if (!level.isEmpty()) {
                levelOrderNodes.add(level);
            }
        }

        return result;
    }
    
    public static void main(String[] args) {
        // Driver function
    }
    
}