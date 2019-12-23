/**
 * Copyright 2019 Distributed Systems Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package SimBlock.simulator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;	//add
import java.util.List;	//add

import SimBlock.node.Block;
import SimBlock.node.Node;
import SimBlock.node.Score;	//add
import static SimBlock.simulator.Timer.*;
import static SimBlock.settings.SimulationConfiguration.*; //add

//add
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;


public class Simulator {
	private static ArrayList<Node> simulatedNodes = new ArrayList<Node>();
	private static long targetInterval;// = 1000*60*10;//msec
	private static long averageDifficulty;

	//add
	public static long count = 0;			
	public static double average_propagation = 0;
	public static double average_propagation2 = 0;
	public static double getAveProp(){return average_propagation/count;}
	public static Map<Block,ArrayList<ArrayList<Node>>> bf = new HashMap<Block,ArrayList<ArrayList<Node>>>();
	public static List<Double> propList = new ArrayList<>();
	
	public static ArrayList<Node> getSimulatedNodes(){ return simulatedNodes; }
	public static long getAverageDifficulty(){ return averageDifficulty; }
	public static void setTargetInterval(long interval){ targetInterval = interval; }

	//add
	public static URI CONF_FILE_URI;
	public static URI OUT_FILE_URI;
	static {
		try {
			CONF_FILE_URI = ClassLoader.getSystemResource("simulator.conf").toURI();
			OUT_FILE_URI = CONF_FILE_URI.resolve(new URI("../output/"));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	public static PrintWriter OUT_CSV_FILE;
	static {
		try{
			OUT_CSV_FILE = new PrintWriter(new BufferedWriter(new FileWriter(new File(OUT_FILE_URI.resolve("./mpt.csv")),true)));
		} catch (IOException e){
			e.printStackTrace();
		}
	}

	public static void addNode(Node node){
		simulatedNodes.add(node);
		setAverageDifficulty();
	}
	
	public static void removeNode(Node node){
		simulatedNodes.remove(node);
		setAverageDifficulty();
	}
	
	public static void addNodeWithConnection(Node node){
		node.joinNetwork();
		addNode(node);
		for(Node existingNode: simulatedNodes){
			existingNode.addNeighbor(node);
		}
	}
	
	// calculate averageDifficulty from totalMiningPower
	private static void setAverageDifficulty(){
		long totalMiningPower = 0;
		
		for(Node node : simulatedNodes){
			totalMiningPower += node.getMiningPower();
		}
		
		if(totalMiningPower != 0){
			averageDifficulty =  totalMiningPower * targetInterval;
		}
	}
	
	//
	// Record block propagation time
	// For saving memory, Record only the latest 10 Blocks
	//
	private static ArrayList<Block> observedBlocks = new ArrayList<Block>();
	private static ArrayList<LinkedHashMap<Integer, Long>> observedPropagations = new ArrayList<LinkedHashMap<Integer, Long>>();
	
	public static void arriveBlock(Block block,Node node){
		if(observedBlocks.contains(block)){
			LinkedHashMap<Integer, Long> Propagation = observedPropagations.get(observedBlocks.indexOf(block));
			Propagation.put(node.getNodeID(), getCurrentTime() - block.getTime());
		}else{
			if(observedBlocks.size() > 10){
				printPropagation(observedBlocks.get(0),observedPropagations.get(0));
				observedBlocks.remove(0);
				observedPropagations.remove(0);
			}
			LinkedHashMap<Integer, Long> propagation = new LinkedHashMap<Integer, Long>();
			propagation.put(node.getNodeID(), getCurrentTime() - block.getTime());
			observedBlocks.add(block);
			observedPropagations.add(propagation);
		}
	}
	
	public static void printPropagation(Block block,LinkedHashMap<Integer, Long> propagation){
		System.out.println(block + ":" + block.getHeight());

		long propagationTime = 0;	//add
		// count++; //add
		long num = 0;	//add

		long median = 0;

		for(Map.Entry<Integer, Long> timeEntry : propagation.entrySet()){
			// *
			// System.out.println(timeEntry.getKey() + "," + timeEntry.getValue());

			//add
			if(num == (NUM_OF_NODES/2))median = timeEntry.getValue();
			if(timeEntry.getKey()==10){
				propagationTime += timeEntry.getValue();  // time block propagated to all nodes. 
				count++;
			}
			propList.add((double)propagationTime);	//add

			if(block.getHeight() >= ENDBLOCKHEIGHT-10){
				// System.out.println(timeEntry.getValue());
				OUT_CSV_FILE.print(timeEntry.getValue() + "\n");
			}
			num++;
		}
		// propList.add((double)propagationTime);	//add
		// add
		average_propagation2 += propagationTime/NUM_OF_NODES;
		System.out.println("average propagation 	: " + propagationTime/NUM_OF_NODES);			//add
		// Double[] prop = propList.toArray(new Double[propList.size()]);
		// median = median(bubble_sort(prop));
		System.out.println("median propagation 	: " + median +"\n");
		average_propagation = average_propagation + median;
		if(count >= ENDBLOCKHEIGHT){
			// System.out.println("\naverage median propagation : "+average_propagation/count);
			System.out.println("\naverage oneNode propagation : "+average_propagation/count);
		}
		// System.out.println("median propagation  :" + median);
		
		OUT_CSV_FILE.flush();
		
	}	
	
	public static void printAllPropagation(){
		for(int i=0;i < observedBlocks.size();i++){
			printPropagation(observedBlocks.get(i), observedPropagations.get(i));
		}
	}

	//add
	public static void addBF(Block block, Node from, Node to){
		ArrayList<Node> from_to = new ArrayList<>();
		from_to.add(from);
		from_to.add(to);
		if(!bf.containsKey(block)){
			ArrayList<ArrayList<Node>> From_to = new ArrayList<ArrayList<Node>>();
			From_to.add(from_to);
			bf.put(block,From_to);
			return;
		}
		bf.get(block).add(from_to);
		return;
	}

	//add
	public static Double[] bubble_sort(Double[] d) {
        // iはi回目の交換する回数
        for (int i = d.length-1; i > 0; i-- ) {
            // j は交換する箇所の前からの番号を示している
            for (int j = 0; j < i; j++) {
                if(d[j]>d[j+1]){
                  //降順にしたい場合は不等号を逆に
                  double box = d[j];
                  d[j] = d[j+1];
                  d[j+1] = box;
                  // System.out.println(d[j] + ":" +d[j+1]);
                } else{
                  //そのまま
                }
            }
        }
        return d;
    }

    //add
    public static double median(Double[] m) {
	    int middle = m.length/2;
	    if (m.length%2 == 1) {
	        return m[middle];
	    } else {
	        return (m[middle-1] + m[middle]) / 2.0;
	    }
	}

}
