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

import SimBlock.node.Block;
import SimBlock.node.Node;
import SimBlock.node.Score;	//add
import static SimBlock.simulator.Timer.*;


public class Simulator {
	private static ArrayList<Node> simulatedNodes = new ArrayList<Node>();
	private static long targetInterval;// = 1000*60*10;//msec
	private static long averageDifficulty;

	//add
	public static long count = 0;			
	public static double average_propagation = 0;
	public static double getAveProp(){return average_propagation/count;}
	public static Map<Block,ArrayList<ArrayList<Node>>> bf = new HashMap<Block,ArrayList<ArrayList<Node>>>();
	
	public static ArrayList<Node> getSimulatedNodes(){ return simulatedNodes; }
	public static long getAverageDifficulty(){ return averageDifficulty; }
	public static void setTargetInterval(long interval){ targetInterval = interval; }
	
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
		count++; //add
		long num = 0;	//add

		for(Map.Entry<Integer, Long> timeEntry : propagation.entrySet()){
			// *
			// System.out.println(timeEntry.getKey() + "," + timeEntry.getValue());
			
			//add
			propagationTime = timeEntry.getValue();  // time block propagated to all nodes. 
			num++;
		}

		// add
		// System.out.println(num);
		// System.out.println(bf.get(block).size() + ", " + bf.get(block).get(0));

		// add
		average_propagation = average_propagation + propagationTime;		//add
		System.out.println("propagation   : " + propagationTime);			//add
		System.out.println("Average Score : " + Score.getAverageScore());	//add
		
		//add
		for(int i=0;i<600;i++){
			// simulatedNodes.get(i).getRoutingTable().checkNode();
		}
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

}
