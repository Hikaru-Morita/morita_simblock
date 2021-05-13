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
import static SimBlock.settings.NetworkConfiguration.*;
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
	private int receivedNodeCount = 0;
	private long BFT = 0;
	private static long[] average_BFT =  {0,0,0};

	public Block(int height, Block parent, Node creator,long generatedTime){
		this.height = height;
		this.parent = parent;
		this.creator = creator;
		this.generatedTime = generatedTime;
		this.id = latestId;
		latestId++;


		// if(this.id==1000){
		// 	long[] num = {24000000, 65000, 10000000, 175000, 14000000,250000, 6 * 1000000};
		// 	change_dl_bw_2015(num);
		// }

		// if(this.id==2500){
		// 	long[] num = {65000, 10000000, 175000, 14000000,250000, 24000000, 6 * 1000000};
		// 	change_dl_bw_2015(num);
		// }

		// if(this.id==3000){
		// 	long[] num = {10000000, 175000, 14000000,250000, 24000000, 65000, 6 * 1000000};
		// 	change_dl_bw_2015(num);
		// }

		// if(this.id==4000){
		// 	long[] num = {175000, 14000000,250000, 24000000, 65000, 10000000, 6 * 1000000};
		// 	change_dl_bw_2015(num);
		// }
	}

	public int getHeight(){return this.height;}
	public Block getParent(){return this.parent;}
	public Node getCreator(){return this.creator;}
	public long getTime(){return this.generatedTime;}
	public int getId() {return this.id;}

	// return ancestor block that height is {height}
	public Block getBlockWithHeight(int height){
		if(this.height == height){
			return this;
		}else{
			return this.parent.getBlockWithHeight(height);
		}
	}

	//add
	// this method is for mesure the propagation time 10% to 90%
	public void addReceivedNodeCount(Block receivedBlock){
		receivedNodeCount++;
		int per = 50;
		if(receivedNodeCount==NUM_OF_NODES*30/100 && receivedBlock.getHeight()>ENDBLOCKHEIGHT/2){
			BFT = getCurrentTime() - this.generatedTime;
			average_BFT[0] = average_BFT[0] + BFT;
			// System.out.println("BFT_"+ 30 +"% :" + BFT);
		}else if(receivedNodeCount==NUM_OF_NODES*per/100 && receivedBlock.getHeight()>ENDBLOCKHEIGHT/2){
			BFT = getCurrentTime() - this.generatedTime;
			average_BFT[1] = average_BFT[1] + BFT;
			// System.out.println("BFT_"+ per +"% :" + BFT);
		}else if(receivedNodeCount==NUM_OF_NODES*80/100 && receivedBlock.getHeight()>ENDBLOCKHEIGHT/2){
			BFT = getCurrentTime() - this.generatedTime;
			average_BFT[2] = average_BFT[2] + BFT;
			// System.out.println("BFT_"+ 80 +"% :" + BFT);
		}

		if(receivedBlock.getHeight()==ENDBLOCKHEIGHT){
			System.out.println("30%:" + (average_BFT[0]/(ENDBLOCKHEIGHT/2)));
			System.out.println("50%:" + (average_BFT[1]/(ENDBLOCKHEIGHT/2)));
			System.out.println("80%:" + (average_BFT[2]/(ENDBLOCKHEIGHT/2)));
		}
	}

}
