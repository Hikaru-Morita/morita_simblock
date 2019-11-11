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

package SimBlock.node;
import static SimBlock.settings.SimulationConfiguration.*;	//add
import static SimBlock.simulator.Timer.*;		//add

//add
import java.util.HashSet;
import java.util.Set;


public class Block {
	private int height;
	private Block parent;
	private Node creator;
	private long generatedTime;
	private int id;
	private static int latestId = 0;
	private Set<Node> node_contain = new HashSet<Node>();

	private int hops;

	public Block(int height, Block parent, Node creator,long generatedTime){
		this.height = height;
		this.parent = parent;
		this.creator = creator;
		this.generatedTime = generatedTime;
		this.id = latestId;
		this.hops = 0;
		latestId++;
	}

	public int getHeight(){return this.height;}
	public Block getParent(){return this.parent;}
	public Node getCreator(){return this.creator;}
	public long getTime(){return this.generatedTime;}
	public int getId() {return this.id;}
	public int getHops(){return this.hops;}

	public Set<Node> getContainNodes(){return node_contain;}

	public void addHops(){hops++;}

	//add
	public void addContainList(Node node){
		node_contain.add(node);
		if(node_contain.size() >= NUM_OF_NODES){
			Score.addBFT(getCurrentTime() - this.generatedTime);
		}
	}


	// return ancestor block that height is {height}
	public Block getBlockWithHeight(int height){
		if(this.height == height){
			return this;
		}else{
			return this.parent.getBlockWithHeight(height);
		}
	}

}
