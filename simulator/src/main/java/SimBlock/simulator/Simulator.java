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
import java.util.spi.TimeZoneNameProvider;
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
	public static Map<Block,ArrayList<ArrayList<Node>>> bf = new HashMap<Block,ArrayList<ArrayList<Node>>>();
	public static List<Double> propList = new ArrayList<>();
	public static Map<Integer,Integer> node_change_num = new HashMap<Integer,Integer>();
	public static int change_count = 0;

	public static double sum_interval = 0;
	public static long interval_count = 0;

	public static int divide_count = 0;
	public static long sum_50percent = 0;

	public static ArrayList<Node> getSimulatedNodes(){ return simulatedNodes; }
	public static long getAverageDifficulty(){ return averageDifficulty; }
	public static void setTargetInterval(long interval){ targetInterval = interval; }
	public static void nodeChangeNum(int changedNum){
		if(node_change_num.containsKey(changedNum)){
			node_change_num.put(changedNum,node_change_num.get(changedNum)+1);	
		}else{
			node_change_num.put(changedNum,1);		
		}
	}
	// public static int changedSum(){}

	public static double getTotalAveProp(){
		long sum_propagation = 0;
		int count = 0;
		for(LinkedHashMap<Integer, Long> timeEntry : observedPropagations){
			int count_prop=0;
			long propagation = 0;
			for(long one_prop : timeEntry.values()){
				propagation = propagation + one_prop;
				count_prop++;
			}
			sum_propagation = sum_propagation + propagation/count_prop;
			count++;
		}
		return sum_propagation/count;
	}

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
	public static PrintWriter OUT_INDIVIDUAL_CSV_FILE = OUT_CSV_FILE;
	static {
		try{
			OUT_CSV_FILE = new PrintWriter(new BufferedWriter(new FileWriter(new File(OUT_FILE_URI.resolve("./average_bpt.csv")),true)));
		} catch (IOException e){
			e.printStackTrace();
		}
	}
	static {
		try{
			OUT_INDIVIDUAL_CSV_FILE = new PrintWriter(new BufferedWriter(new FileWriter(new File(OUT_FILE_URI.resolve("./individual/"+(int)(Score.getPara()*10)+".csv")),false)));
		} catch (IOException e){
			e.printStackTrace();
		}
	}
	// individual_bpt.csv にヘッダーを設定
	public static void setHeaderINDIVISUALCSV(){
		OUT_INDIVIDUAL_CSV_FILE.print("block height"+",");
		for(int count=1;count<=NUM_OF_NODES;count++){
			if(count == NUM_OF_NODES) OUT_INDIVIDUAL_CSV_FILE.print(count);
			else OUT_INDIVIDUAL_CSV_FILE.print(count+",");
		}
		OUT_INDIVIDUAL_CSV_FILE.print("\n");
		return ;
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
		int count = 0;

		/**
		全体のブロック伝播をファイル"individual_bpt.csv"に出力
		目的：出力ファイルのサイズ削減
		**/
		if(propagation.size()==NUM_OF_NODES){
			OUT_INDIVIDUAL_CSV_FILE.print(block.getHeight()+",");
			for(Map.Entry<Integer, Long> timeEntry : propagation.entrySet()){
				// System.out.println(timeEntry.getValue());
				count ++;
				if(count == NUM_OF_NODES/2){
					OUT_INDIVIDUAL_CSV_FILE.print(timeEntry.getValue() + ",");
					
					if(block.getHeight()>=ENDBLOCKHEIGHT/2){
						sum_50percent = sum_50percent + timeEntry.getValue();
						divide_count++;
						System.out.println(timeEntry.getValue() + ":" + timeEntry.getValue());
						System.out.println("median: " + sum_50percent/divide_count);
					}
				}
				if(count == NUM_OF_NODES)OUT_INDIVIDUAL_CSV_FILE.print(timeEntry.getValue());
				else OUT_INDIVIDUAL_CSV_FILE.print(timeEntry.getValue() + ",");
				// OUT_INDIVIDUAL_CSV_FILE.print("\n");
			}
			OUT_INDIVIDUAL_CSV_FILE.print("\n");

			OUT_INDIVIDUAL_CSV_FILE.flush();
		}
	}	

	public static void countInterval(long interval){
		sum_interval = sum_interval + interval;
		interval_count++;
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

	//add 
	public static void printPropagation(double propagation, int orphans){
		// OUT_CSV_FILE.print(propagation+","+orphans+","+Score.getPara()+","+node_change_num+"\n");
		OUT_CSV_FILE.print(propagation+","+orphans+","+Score.getPara()+",");
		for(int num :node_change_num.values()){
			OUT_CSV_FILE.print(num+",");	
		}
		OUT_CSV_FILE.print("\n");	
		System.out.println(node_change_num);
		OUT_CSV_FILE.flush();
	}
}
