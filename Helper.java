import java.io.*;
import java.util.*;


public class Helper {


    public static void fileWriter(String message, String fileLocation){
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        PrintWriter printWriter = null;

        try {
            fileWriter = new FileWriter(fileLocation, true);
            bufferedWriter = new BufferedWriter(fileWriter);
            printWriter = new PrintWriter(bufferedWriter);

            printWriter.println(message);

            //System.out.println("Data Successfully appended into file");
            printWriter.flush();

        } catch (IOException io) {
            io.printStackTrace();
        } 
        
        finally {
            try {
                printWriter.close();
                bufferedWriter.close();
                fileWriter.close();
            } catch (IOException io) {
                io.printStackTrace();
            }
        }
    }
    
    public static ArrayList<String> fileReader(String fileName){
        ArrayList<String> topology = new ArrayList<>();
        BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(fileName));
			String line = reader.readLine();
			while (line != null) {
                topology.add(line);
				line = reader.readLine(); 
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return topology;
    }
    
    public static boolean compareLists(List<String> l1, List<String> l2){
        for(String value : l1){
            if(!l2.contains(value)){
                return false;
            }
        }
        if(l1.size()<l2.size()){
            return false;
        }
        return true;
    }
}
