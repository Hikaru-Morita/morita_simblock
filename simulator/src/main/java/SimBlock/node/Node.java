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
	private static Map<Integer, Integer> long_hop_count = new HashMap<Integer, Integer>();
	private static long longHopTime = 0;
	private static long totalLongHopTime = 0;

	// 有効invカウント用
	private static int vaild_inv_count = 0;
	private static int all_inv_count = 0;

	// ブロック所持ノード数カウント
	private static Map<Integer, Integer> node_has_block = new HashMap<Integer, Integer>();
	
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
		node_has_block.put(0,0);
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

		// ノードがあるブロックを受信した場合にカウント
		if(!node_has_block.containsKey(receivedBlock.getId())){
			node_has_block.put(receivedBlock.getId(),1);
		}else{
			node_has_block.put(receivedBlock.getId(),node_has_block.get(receivedBlock.getId())+1);
			// System.out.println("recieved block:"+ node_has_block.get(receivedBlock.getId()) + ":" + receivedBlock);
			if(node_has_block.get(receivedBlock.getId())%10==0){
				System.out.println(receivedBlock.getHeight() +  " 伝播率:" + node_has_block.get(receivedBlock.getId())/1000.0 + " vaild:" + vaild_inv_count + " all:" + all_inv_count);
			}
		}

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

					
					if(node_has_block.containsKey(block.getId())&&node_has_block.get(block.getId())==490){
						System.out.println(block + ":" + block.getId() + ":" + node_has_block.get(block.getId()));
					}
					vaild_inv_count = addInvCount(vaild_inv_count,block);
				}else{

					// get orphan block
					if(block != this.block.getBlockWithHeight(block.getHeight())){
						AbstractMessageTask task = new RecMessageTask(this,from,block);
						putTask(task);
						downloadingBlocks.add(block);
						
						if(node_has_block.containsKey(block.getId())&&node_has_block.get(block.getId())==490){
							System.out.println(block + ":" + block.getId() + ":" + node_has_block.get(block.getId()));
						}
						vaild_inv_count = addInvCount(vaild_inv_count,block);
					}
				}

				//reload score  //add
				InvMessageTask m = (InvMessageTask) message;
				if(block.getTime() != -1){
					score.addScore(m.getFrom(),(int)getCurrentTime(),(int)m.getBlock().getTime());
				}

				// 総invメッセージのカウント
				all_inv_count =addInvCount(all_inv_count,block);

				// if(block.getHeight()==ENDBLOCKHEIGHT)System.out.println(vaild_inv_count + " : " + all_inv_count);
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

			// if(!node_has_block.containsKey(block.getId())){
			// 	node_has_block.put(block.getId(),1);
			// }else{
			// 	node_has_block.put(block.getId(),node_has_block.get(block.getId())+1);
			// }

			// 異なる地域間でのブロック伝播
			if(message.getTo().getRegion()!=message.getFrom().getRegion()){
				if(!long_hop_count.containsKey(block.getHeight())){
					long_hop_count.put(block.getHeight(),0);
					totalLongHopTime = totalLongHopTime + longHopTime;
					longHopTime = 0;
				}
				long_hop_count.put(block.getHeight(), long_hop_count.get(block.getHeight())+1);
				// System.out.println(block.getHeight() + ": " + (getCurrentTime()-block.getTime()) + ": " + long_hop_count.get(block.getHeight()));
				longHopTime = getCurrentTime()-block.getTime();
				// if(block.getHeight()==500)System.out.println(long_hop_count);
				
				
				// System.out.println(totalLongHopTime);


			}

			//add
			if(block.getId()% 20== 0 && block.getId()>1){
				checkFrequency();
			}else if(block.getId()%10 == 0 && block.getHeight()>1){
				// changeNeighbors();
				// changeNeighbors_v2();
			}
			
			//add
			BlockMessageTask m = (BlockMessageTask) message;

			//add
			hop_count.put(block, message.getFrom().getHopCount(block)+1);
			countInterval(m.getInterval());
			if(!workerList.contains(m.getFrom()))workerList.add(m.getFrom());			
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

		removeNode = score.getWorstNodeWithRemove();
		if(removeNode == this) return;
	
		removeNeighbor(removeNode);
		workerList.remove(removeNode);

		while(true){
			addNode = getSimulatedNodes().get(rand.nextInt(NUM_OF_NODES));
			
			if(addNode.getInbounds().size()>=30){
			}else if(addNode==removeNode){
			}else if(addNeighbor(addNode))break;
		}
	}

	//add
	public void changeNeighbors_v2(){
	
		Node removeNode;
		Node addNode;
		int count = 0;

		changeNeighbors();

		Map<Node,Double> scores = new HashMap<>();

		for(Map.Entry<Node,Double> i: score.getScores().entrySet()){
			scores.put(i.getKey(),i.getValue());
		}

		for(Map.Entry<Node,Double> i: scores.entrySet()){
			if(i.getValue()>=score.getAverageAllScore()){

				removeNode = score.getWorstNode();

				if(removeNode == this) return;
				removeNeighbor(removeNode);
				workerList.remove(removeNode);

				while(true){
					addNode = getSimulatedNodes().get(rand.nextInt(NUM_OF_NODES-1));
				
					if(addNode.getInbounds().size()>30){
					}else if(addNode==removeNode){
					}else if(addNeighbor(addNode)){
						count++;
						score.removeScore(removeNode);
						break;
					}
				}
			}
		}
		nodeChangeNum(count);
		return;
	}

	//add
	public void checkFrequency(){
		ArrayList<Node> neighbors = this.getOutbounds();
		ArrayList<Node> node_over30Inbounds = new ArrayList<Node>();

		Node addNode = getSimulatedNodes().get(rand.nextInt(NUM_OF_NODES-1));
		while(addNode == this)addNode = getSimulatedNodes().get(rand.nextInt(NUM_OF_NODES-1));

		int count = 0;

		for(int i=0;i<neighbors.size();i++){

			Node node = neighbors.get(i);
			score.removeScore(node);
			if(!workerList.contains(node) && removeNeighbor(node)){
				while(true){
					int size = score.getPreNodes().size();
					if(size>8 && addNode!=this){
						// addNode = score.getBestNodeFromAllScores(node_over30Inbounds);
						addNode = getSimulatedNodes().get(rand.nextInt(NUM_OF_NODES-1));
					}else{
						addNode = getSimulatedNodes().get(rand.nextInt(NUM_OF_NODES-1));
					}

					if(addNode.getInbounds().size()>30){
					}else if(addNode==node){
					}else if(addNeighbor(addNode)){
						count++;
						break;
						// return;
					}
					node_over30Inbounds.add(addNode);
				}
			}
		}
		nodeChangeNum(count);
		return ;
	}

	public int getHopCount(Block blcok){
		if(hop_count.containsKey(block)){
			// System.out.println(hop_count.get(block));
			return hop_count.get(block);
		}else{
			return 0;
		}
	}

	// public int addInvCount(int inv_count, Block block){
	// 	if(node_has_block.containsKey(block.getId())){
	// 		if(block.getHeight()>(ENDBLOCKHEIGHT/2)&&node_has_block.get(block.getId())<=(NUM_OF_NODES/2)){
	// 			inv_count++;
	// 		}
	// 	}
	// 	return inv_count;
	// }
	public int addInvCount(int inv_count, Block block){
		if(node_has_block.containsKey(block.getId())){
			if(block.getHeight()>(ENDBLOCKHEIGHT/2)&&node_has_block.get(block.getId())<=(NUM_OF_NODES/3.4)){
				inv_count++;
			}
		}
		return inv_count;
	}

}
