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

//add
import static SimBlock.settings.SimulationConfiguration.*;
import static SimBlock.simulator.Timer.*;

public class Block {
	private int height;
	private Block parent;
	private Node creator;
	private long generatedTime;
	private int id;
	private static int latestId = 0;

	//add
	private int recievedNodeCount = 0;
	private long BFT = 0;

	public Block(int height, Block parent, Node creator,long generatedTime){
		this.height = height;
		this.parent = parent;
		this.creator = creator;
		this.generatedTime = generatedTime;
		this.id = latestId;
		latestId++;
	}

	public int getHeight(){return this.height;}
	public Block getParent(){return this.parent;}
	public Node getCreator(){return this.creator;}
	public long getTime(){return this.generatedTime;}
	public int getId() {return this.id;}

	//add
	public long getBFT(){return this.BFT;}

	//add
	public void addRecievedNodeCount(){
		recievedNodeCount++;
<<<<<<< HEAD
		if(recievedNodeCount==NUM_OF_NODES*0.5){
=======
		if(recievedNodeCount==NUM_OF_NODES){
>>>>>>> 38435db6d904f0ca3a2240fd5f81ad6eeeda953b
			// System.out.println("BFT");
			BFT = getCurrentTime() - this.generatedTime;
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
