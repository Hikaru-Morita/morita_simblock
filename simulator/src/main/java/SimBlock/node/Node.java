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

import static SimBlock.settings.SimulationConfiguration.*;
import static SimBlock.simulator.Main.*;
import static SimBlock.simulator.Network.*;
import static SimBlock.simulator.Simulator.*;
import static SimBlock.simulator.Timer.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import SimBlock.node.routingTable.AbstractRoutingTable;
import SimBlock.task.AbstractMessageTask;
import SimBlock.task.BlockMessageTask;
import SimBlock.task.InvMessageTask;
import SimBlock.task.MiningTask;
import SimBlock.task.RecMessageTask;
import SimBlock.task.Task;

//add
import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class Node {
	private int region;
	private int nodeID;
	private long miningPower;
	private AbstractRoutingTable routingTable;

	private Block block;
	private Set<Block> orphans = new HashSet<Block>();

	private Task executingTask = null;

	private boolean sendingBlock = false;
	private ArrayList<RecMessageTask> messageQue = new ArrayList<RecMessageTask>();
	private Set<Block> downloadingBlocks = new HashSet<Block>();

	// ホップのカウント用　フォークを考慮している
	private Map<Block, Integer> hop_count = new HashMap<Block, Integer>();

	private long processingTime = 2;

	//add
	private Score score = new Score(this);
	private Random rand = new Random();
	private Map<Block,Integer> block_prop = new HashMap<Block,Integer>();

	public Node(int nodeID,int nConnection ,int region, long miningPower, String routingTableName){
		this.nodeID = nodeID;
		this.region = region;
		this.miningPower = miningPower;
		try {
			this.routingTable = (AbstractRoutingTable) Class.forName(routingTableName).getConstructor(Node.class).newInstance(this);
			this.setnConnection(nConnection);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getNodeID(){ return this.nodeID; }
	public Block getBlock(){ return this.block; }
	public long getMiningPower(){ return this.miningPower; }
	public Set<Block> getOrphans(){ return this.orphans; }
	public void setRegion(int region){ this.region = region; }
	public int getRegion(){ return this.region; }

	public boolean addNeighbor(Node node){ return this.routingTable.addNeighbor(node); }
	public boolean removeNeighbor(Node node){ return this.routingTable.removeNeighbor(node); }
	public ArrayList<Node> getNeighbors(){ return this.routingTable.getNeighbors(); }
	public AbstractRoutingTable getRoutingTable(){ return this.routingTable; }
	public void setnConnection(int nConnection){ this.routingTable.setnConnection(nConnection); }
	public int getnConnection(){ return this.routingTable.getnConnection(); }


	//add
	private ArrayList<Node> workerList = new ArrayList<Node>();
	public int getScoresSize(){return score.getScoresSize();}
	public double getScore(Node node){return score.getScore(node);}
	public ArrayList<Node> getOutbounds(){return routingTable.getOutbounds();}
	public ArrayList<Node> getInbounds(){return routingTable.getInbounds();}

	public void joinNetwork(){
		this.routingTable.initTable();
	}

	public void genesisBlock(){
		Block genesis = new Block(1, null, this, 0);
		this.receiveBlock(genesis);

		//add
		hop_count.put(genesis,0);
	}

	public void addToChain(Block newBlock) {
		if(this.executingTask != null){
			removeTask(this.executingTask);
			this.executingTask = null;
		}
		this.block = newBlock;
		printAddBlock(newBlock);
		arriveBlock(newBlock, this);

		newBlock.addRecievedNodeCount();
	}

	private void printAddBlock(Block newBlock){
	}

	public void addOrphans(Block newBlock, Block correctBlock){
		if(newBlock != correctBlock){
			this.orphans.add(newBlock);
			this.orphans.remove(correctBlock);
			if(newBlock.getParent() != null && correctBlock.getParent() != null){
				this.addOrphans(newBlock.getParent(),correctBlock.getParent());
			}
		}
	}

	public void mining(){
		Task task = new MiningTask(this);
		this.executingTask = task;
		putTask(task);
	}

	public void sendInv(Block block){
		for(Node to : this.routingTable.getNeighbors()){
			AbstractMessageTask task = new InvMessageTask(this,to,block);
			putTask(task);
		}
	}


	public void receiveBlock(Block receivedBlock){
		Block sameHeightBlock;

		if(this.block == null){
			this.addToChain(receivedBlock);
			this.mining();
			this.sendInv(receivedBlock);

		}else if(receivedBlock.getHeight() > this.block.getHeight()){
			sameHeightBlock = receivedBlock.getBlockWithHeight(this.block.getHeight());
			if(sameHeightBlock != this.block){
				this.addOrphans(this.block, sameHeightBlock);
			}
			this.addToChain(receivedBlock);
			this.mining();
			this.sendInv(receivedBlock);

		}else if(receivedBlock.getHeight() <= this.block.getHeight()){
			sameHeightBlock = this.block.getBlockWithHeight(receivedBlock.getHeight());
			if(!this.orphans.contains(receivedBlock) && receivedBlock != sameHeightBlock){
				this.addOrphans(receivedBlock, sameHeightBlock);
				arriveBlock(receivedBlock, this);
			}
		}

	}

	public void receiveMessage(AbstractMessageTask message){
		Node from = message.getFrom();

		if(message instanceof InvMessageTask){
			Block block = ((InvMessageTask) message).getBlock();
			if(!this.orphans.contains(block) && !this.downloadingBlocks.contains(block)){
				if(this.block == null || block.getHeight() > this.block.getHeight()){
					AbstractMessageTask task = new RecMessageTask(this,from,block);
					putTask(task);
					downloadingBlocks.add(block);
				}else{

					// get orphan block
					if(block != this.block.getBlockWithHeight(block.getHeight())){
						AbstractMessageTask task = new RecMessageTask(this,from,block);
						putTask(task);
						downloadingBlocks.add(block);
					}
				}

				//reload score  //add
				InvMessageTask m = (InvMessageTask) message;
				if(block.getTime() != -1){
					score.addScore(m.getFrom(),(int)getCurrentTime(),(int)m.getBlock().getTime());	
					// score.addScore2(m.getFrom(),(int)getCurrentTime(),(int)m.getBlock().getTime());
				}
			}

			//add
			// if(!block_prop.containsKey(block)){
			// 	block_prop.put(block,1);
			// }else{
			// 	int num = block_prop.get(block)+1;
			// 	block_prop.put(block,num);
			// 	if(num>=8){
			// 		System.out.println(block.getHeight()+ +(getCurrentTime()-block.getTime()));
			// 		block_prop.remove(block);
			// 		// num = 0;
			// 	}
			// }
		}

		if(message instanceof RecMessageTask){
			this.messageQue.add((RecMessageTask) message);
			if(!sendingBlock){
				this.sendNextBlockMessage();
			}
		}

		if(message instanceof BlockMessageTask){
			Block block = ((BlockMessageTask) message).getBlock();
			downloadingBlocks.remove(block);
			this.receiveBlock(block);

			//add
			if(block.getId()% 60== 0 && block.getId()>1){
				 checkFrequency();
			}else if(block.getId()%10 == 0 && block.getHeight()>1){
				changeNeighbors();
				// changeNeighbors_v2();
			}
			
			//add
			BlockMessageTask m = (BlockMessageTask) message;

			//add
			hop_count.put(block, message.getFrom().getHopCount(block)+1);
			m.getInterval();

			// System.out.println("check workers");
			if(!workerList.contains(m.getFrom()))workerList.add(m.getFrom());
			// System.out.println("end :check workers");
			
		}
	}

	// send a block to the sender of the next queued recMessage
	public void sendNextBlockMessage(){
		if(this.messageQue.size() > 0){

			sendingBlock = true;

			Node to = this.messageQue.get(0).getFrom();
			Block block = this.messageQue.get(0).getBlock();
			this.messageQue.remove(0);
			long blockSize = BLOCKSIZE;
			long bandwidth = getBandwidth(this.getRegion(),to.getRegion());
			long delay = blockSize * 8 / (bandwidth/1000) + processingTime;
			BlockMessageTask messageTask = new BlockMessageTask(this, to, block, delay);

			putTask(messageTask);

			//add
			addBF(block,this,to);
			
		}else{
			sendingBlock = false;
		}
	}

	//add
	public void checkNode(){routingTable.checkNode();}

	//add
	public void changeNeighbors(){
		Random rand = new Random();
		Node removeNode;
		Node addNode;

		if(this.getNodeID()==10){
			System.out.println(score.getScores().values());
			// System.out.println(removeNode + +addNode);
		}

		removeNode = score.getWorstNodeWithRemove();	
		if(removeNode == this) return;
	
		removeNeighbor(removeNode);
		workerList.remove(removeNode);

		List<Node> keys = new ArrayList<Node>(score.getPreNodes());

		// System.out.println("keys: "+keys.size());

		while(true){
			// System.out.println(keys.get(rand.nextInt(keys.size())));
			if(keys.size()>8){
				addNode = keys.get(rand.nextInt(keys.size()));
				// System.out.println(addNode);
			}else{
				addNode = getSimulatedNodes().get(rand.nextInt(NUM_OF_NODES-1));
			}

			if(addNode.getInbounds().size()>30){
			}else if(addNode==removeNode){
			}else if(addNeighbor(addNode))break;
		}
		//System.out.println("after outbounds  :" + getOutbounds().size());	

		// while(true){
		// 	addNode = getSimulatedNodes().get(rand.nextInt(NUM_OF_NODES-1));
		// 	if(addNode.getInbounds().size()>30){
		// 	}else if(addNeighbor(addNode))break;
		// }

		if(this.getNodeID()==10){
			System.out.println(score.getScores().values());
			// System.out.println(removeNode + +addNode);
		}
		// checkNode();
	}

	//add
	public void changeNeighbors_v2(){
		
		Node removeNode;
		Node addNode;
		// System.out.println("before outbounds :" + getOutbounds().size());
		// System.out.println("before inbounds :" + getInbounds().size());
		int count = 1;

		changeNeighbors();

		Map<Node,Double> scores = new HashMap<>();

		for(Map.Entry<Node,Double> i: score.getScores().entrySet()){
			scores.put(i.getKey(),i.getValue());
		}

		for(Map.Entry<Node,Double> i: scores.entrySet()){
			if(i.getValue()>=score.getAverageAllScore()){
			// if(i.getValue()>=score.getMedianScore()){

				removeNode = score.getWorstNodeWithRemove_v2();
				if(removeNode == this) return;
				removeNeighbor(removeNode);

				workerList.remove(removeNode);

				List<Node> keys = new ArrayList<Node>(score.getPreNodes());

				while(true){
					// System.out.println(keys.get(rand.nextInt(keys.size())));
					if(keys.size()>8){
						addNode = keys.get(rand.nextInt(keys.size()));
						// System.out.println(addNode);
					}else{
						addNode = getSimulatedNodes().get(rand.nextInt(NUM_OF_NODES-1));
					}
					if(addNode.getInbounds().size()>30){
					}else if(addNode==removeNode){
					}else if(addNeighbor(addNode)){
						count++;
						break;
					}
				}
			}
		}
		nodeChangeNum(count);
		// System.out.println("after outbounds  :" + getOutbounds().size());
		return;
	}

	//add
	public void checkFrequency(){
		ArrayList<Node> neighbors = this.getOutbounds();
		Node addNode;
		int count = 0;


		for(int i=0;i<neighbors.size();i++){

			Node node = neighbors.get(i);
			score.removeScore(node);
			if(!workerList.contains(node) && removeNeighbor(node)){
				List<Node> keys = new ArrayList<Node>(score.getPreNodes());

				while(true){
					// System.out.println(keys.get(rand.nextInt(keys.size())));
					if(keys.size()>4){
						addNode = keys.get(rand.nextInt(keys.size()));
						// System.out.println(addNode);
					}else{
						addNode = getSimulatedNodes().get(rand.nextInt(NUM_OF_NODES-1));
					}

					if(addNode.getInbounds().size()>30){
					}else if(addNode==node){
					}else if(addNeighbor(addNode)){
						count++;
						break;
					}
				}
				//System.out.println("after outbounds  :" + getOutbounds().size());	

				// System.out.println("changed one neighbor");
			}
		}
		nodeChangeNum(count);
		return ;
	}

	public int getHopCount(Block blcok){
		if(hop_count.containsKey(block)){
			System.out.println(hop_count.get(block));
			return hop_count.get(block);
		}else{
			return 0;
		}
	}
}

