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
package SimBlock.node.routingTable;
import java.util.ArrayList;
import java.util.Collections;

import SimBlock.node.Node;
import static SimBlock.simulator.Simulator.*;
import static SimBlock.simulator.Timer.*;
import static SimBlock.simulator.Main.*;

public class BitcoinCoreTable extends AbstractRoutingTable {
	private ArrayList<Node> outbound = new ArrayList<Node>();
	private ArrayList<Node> inbound = new ArrayList<Node>();

	public BitcoinCoreTable(Node selfNode) {
		super(selfNode);
	}
	
	public ArrayList<Node> getNeighbors(){
		ArrayList<Node> neighbors = new ArrayList<Node>();
		neighbors.addAll(outbound);
		neighbors.addAll(inbound);
		return neighbors;
	}
	
	// set nConnection random nodes to table
	public void initTable(){
	    ArrayList<Integer> candidates = new ArrayList<Integer>();
	    for(int i = 0 ; i < getSimulatedNodes().size() ; i++) {
	    	candidates.add(i);	
	    }
		Collections.shuffle(candidates);
		for(int candidate:candidates){
			if(this.outbound.size() < this.getnConnection()){
				this.addNeighbor(getSimulatedNodes().get(candidate));
			}else{
				break;
			}
	    };
	}
	
	// add node to outbound and add selfnode to node's inbound 
	// if # of nodes in outbound is less than nConnection
	public boolean addNeighbor(Node node){

		//add for debug
		// System.out.println("addNeighbor: selfID:" + this.getSelfNode().getNodeID() + " addnodeID:" + node.getNodeID());
		// System.out.println("             connenction num:" + (this.getnConnection()-1));
		// System.out.println("             inbound num    :" + this.inbound.size());
		// System.out.println("             outbound num   :" + this.outbound.size());
		// System.out.println("             scores num     :" + this.getSelfNode().getScoresSize());	
		if(node == getSelfNode() || this.outbound.contains(node) || this.inbound.contains(node) || this.outbound.size() >= this.getnConnection()){
			// System.out.println("1;"+(node == getSelfNode()));
			// System.out.println("2;"+this.outbound.contains(node));
			// System.out.println("3;"+this.inbound.contains(node));
			// System.out.println("4;"+this.outbound.size());
			return false;
		}else if(node.getRoutingTable().addInbound(getSelfNode()) && this.outbound.add(node)){
			printAddLink(node);
			return true;
		}else{
			return false;
		}
	}
	
	// remove node to outbount and remove selfnode to node's inbount
	public boolean removeNeighbor(Node node){
		if(this.outbound.remove(node) && node.getRoutingTable().removeInbound(getSelfNode())){
			printRemoveLink(node);
			return true;
		}
		return false;
	}
	
	public boolean addInbound(Node from){
		if(this.inbound.add(from)){
			printAddLink(from);
			return true;
		}
		return false;
	}
	public boolean removeInbound(Node from){
		if(this.inbound.remove(from)){
			printRemoveLink(from);
			return true;
		}
		return false;
	}
	
	private void printAddLink(Node endNode){
		OUT_JSON_FILE.print("{");
		OUT_JSON_FILE.print(	"\"kind\":\"add-link\",");
		OUT_JSON_FILE.print(	"\"content\":{");
		OUT_JSON_FILE.print(		"\"timestamp\":" + getCurrentTime() + ",");
		OUT_JSON_FILE.print(		"\"begin-node-id\":" + getSelfNode().getNodeID() + ",");
		OUT_JSON_FILE.print(		"\"end-node-id\":" + endNode.getNodeID());
		OUT_JSON_FILE.print(	"}");
		OUT_JSON_FILE.print("},");
		OUT_JSON_FILE.flush();
	}
	
	private void printRemoveLink(Node endNode){
		OUT_JSON_FILE.print("{");
		OUT_JSON_FILE.print(	"\"kind\":\"remove-link\",");
		OUT_JSON_FILE.print(	"\"content\":{");
		OUT_JSON_FILE.print(		"\"timestamp\":" + getCurrentTime() + ",");
		OUT_JSON_FILE.print(		"\"begin-node-id\":" + getSelfNode().getNodeID() + ",");
		OUT_JSON_FILE.print(		"\"end-node-id\":" + endNode.getNodeID());
		OUT_JSON_FILE.print(	"}");
		OUT_JSON_FILE.print("},");
		OUT_JSON_FILE.flush();
	}

	//add
	public void checkNode(){
		//add for debug
		System.out.println("  selfID         :" + this.getSelfNode());
		// System.out.println("  connenction num:" + (this.getnConnection()));
		System.out.println("  inbound num    :" + inbound.size());
		System.out.println("  outbound num   :" + outbound.size());
		// System.out.println("  scores num     :" + this.getSelfNode().getScoresSize());	
	}

	//add
	public ArrayList<Node> getOutbounds(){return outbound;}
	public ArrayList<Node> getInbounds(){return inbound;}
	
}
