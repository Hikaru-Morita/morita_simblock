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
import java.util.LinkedList;

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
	private static Map<Integer,Integer[]> node_has_block = new HashMap<Integer, Integer[]>();
	private static Integer[] init_list = {0,0,0};
	
	private long processingTime = 2;

	// スコアが意味を成していない場合の確認用
	private static Map<Integer, Integer> bad_score_count = new HashMap<Integer, Integer>();
	private static int average_count =0;

	// 働いていないノードの確認
	private int worker_num = 0;
	private static int worker_count = 0;
	private static int average_worker_num =0;

	private static int average_neighbor_num = 0;

	// 隣接ノード数
	private static int sum_neighbor_num = 0;

	// ワーカーカウント変数
	private int worker_tmp = 0;

	private int inbound_num = 0;
	private int outbound_num = 0;

	// ブロック伝播が Inbound, Outbound どちらからかをカウントする
	private int propByInbound_num = 0;
	private int propByOutbound_num = 0;

	// 自身が block message を送信した inbound カウント用
	private LinkedList<Node> active_inbound = new LinkedList<Node>();

	// ノードが伝播するブロックの間隔を調べる
	private int received_block_count = 0;
	private Map<Node,Integer> nodes_blockFreq_count = new HashMap<Node,Integer>();
	private static Map<Integer,Integer> freq_count = new HashMap<Integer,Integer>();

	// add
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
	private LinkedList<Node> workerList = new LinkedList<Node>();
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
		node_has_block.put(0,init_list);
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
		// if(this.getNodeID()!=5);
		// else if(this.getNodeID()!=500);
		// else if(this.getNodeID()!=900);
		// else return;
		// if(this.getRegion()==4);
		// else if(this.getNodeID()!=500);
		// else if(this.getNodeID()!=900);
		// else return;
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
		// Integer[] list = {0,0,0};
		// // ノードがブロックを受信した場合にカウント
		// if(!node_has_block.containsKey(receivedBlock.getId())){
		// 	list[0] = list[0]+1;
		// 	node_has_block.put(receivedBlock.getId(),list);
		// }else{
		// 	list = node_has_block.get(receivedBlock.getId());
		// 	list[0] = list[0]+1;
		// 	node_has_block.put(receivedBlock.getId(),list);
		// 	// System.out.println("received block:"+ node_has_block.get(receivedBlock.getId()) + ":" + receivedBlock);
		// 	if(node_has_block.get(receivedBlock.getId())[0]%10==0){
		// 		System.out.println(receivedBlock +" "+ receivedBlock.getHeight() +  " 伝播率:" + node_has_block.get(receivedBlock.getId())[0] + " =" + node_has_block.get(receivedBlock.getId())[1] + "/" + node_has_block.get(receivedBlock.getId())[2]);
		// 	}
		// }

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

					receivedNodeCount(block);
					vaild_inv_count = node_has_block.get(block.getId())[1];
					node_has_block.get(block.getId())[1] = addInvCount(vaild_inv_count,block);
				}else{

					// get orphan block
					if(block != this.block.getBlockWithHeight(block.getHeight())){
						AbstractMessageTask task = new RecMessageTask(this,from,block);
						putTask(task);
						downloadingBlocks.add(block);
						
						if(node_has_block.containsKey(block.getId())&&node_has_block.get(block.getId())[0]==490){
							System.out.println(block + ":" + block.getId() + ":" + node_has_block.get(block.getId()));
						}

						receivedNodeCount(block);
						vaild_inv_count = node_has_block.get(block.getId())[1];
						node_has_block.get(block.getId())[1] = addInvCount(vaild_inv_count,block);
					}
				}

				//reload score  //add
				InvMessageTask m = (InvMessageTask) message;
				if(block.getTime() != -1){
					score.addScore(m.getFrom(),(int)getCurrentTime(),(int)m.getBlock().getTime());
				}
			}

			// 総invメッセージのカウント
			all_inv_count = node_has_block.get(block.getId())[2];
			node_has_block.get(block.getId())[2] = addInvCount(all_inv_count,block);

			// if(block.getHeight()==ENDBLOCKHEIGHT)System.out.println(vaild_inv_count + " : " + all_inv_count);

			//add
			// if(!block_prop.containsKey(block)){
			// 	block_prop.put(block,1);
			// }else{
			// 	int num = block_prop.get(block)+1;
			// 	block_prop.put(block,num);
			// 	if(num>=OUTBOUND_NUM){
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

			// inbound 選択
			// 上流の inbound を選択、自身にブロックを送信するノードを選ぶ
			// if(this.getInbounds().contains(message.getFrom())){
			// 	active_inbounds.put(message.getFrom(),1);
			// }

			// ブロック伝播時に Inbound, Outbound どちらからなのか判別
			// ここを使ってる
			if(this.getInbounds().contains(message.getFrom())){
				propByInbound_num++;
			}else{
				propByOutbound_num++;
			}

			// System.out.println("Inbound : " + propByInbound_num + " \tOutbound : " + propByOutbound_num);
			// System.out.println(propByInbound_num);
			// System.out.println(propByOutbound_num);

			// //add
			// // if(block.getId()%BLOCK_FREQ == 0 && block.getId()>1){
			// if(block.getId()%BLOCK_FREQ == 0 && this.getNodeID()>NODE_PERCENT){
			// 	// // inbound, outbound の数を確認
			// 	// for(Node i: workerList){
			// 	// 	if(routingTable.getOutbounds().contains(i)){
			// 	// 		outbound_num ++;
			// 	// 	}else if(routingTable.getInbounds().contains(i)){
			// 	// 		inbound_num ++;
			// 	// 	}
			// 	// }
			// 	// // System.out.println(inbound_num + " : "+ outbound_num);
			// 	// System.out.println(inbound_num + outbound_num);
			// 	// outbound_num = 0;
			// 	// inbound_num = 0;

			// 	if(SIMULATION_TYPE == 2){
			// 		checkFrequency();

			// 		workerList = new LinkedList<Node>();
			// 	}else if(SIMULATION_TYPE == 1){
					// changeNeighbors();
			// 	// changeNeighbors_v2();
			// 	}
			// }

			//add
			BlockMessageTask m = (BlockMessageTask) message;

			// ノードごとのブロック伝播頻度を確認
			received_block_count = received_block_count + 1;
			Node node_from = m.getFrom();
			if(this.getOutbounds().contains(node_from)){
				if(block.getHeight()>(ENDBLOCKHEIGHT-5000) && nodes_blockFreq_count.containsKey(node_from)){
					// System.out.println((received_block_count - nodes_blockFreq_count.get(node_from)));
					int freq_num = received_block_count - nodes_blockFreq_count.get(node_from);
					if(freq_num<=200){
						if(!freq_count.containsKey(freq_num)){
							freq_count.put(freq_num,1);
						}else{
							freq_count.put(freq_num,freq_count.get(freq_num)+1);
						}
					}
				}
			}
			nodes_blockFreq_count.put(node_from,received_block_count);

			if(block.getHeight()==ENDBLOCKHEIGHT){
				System.out.println("-------------");
				for(Integer num : freq_count.keySet()){
					System.out.println(num + "\t" + freq_count.get(num));
				}
				System.out.println("-------------");
			}

			// //add
			// hop_count.put(block, message.getFrom().getHopCount(block)+1);
			countInterval(m.getInterval());

			// Outbound をスライド方式で更新
			Node working_node = m.getFrom();
			ArrayList<Node> removing_nodes = new ArrayList<Node>();

			if(workerList.size()<BLOCK_FREQ){
				workerList.add(working_node);
			}else{
				// 更新間隔 + 1 になるよう add
				workerList.add(working_node);

				ArrayList<Node> neighbors = this.getOutbounds();

				// workerList に含まれない node を Outbounds から削除
				for(Node node: neighbors){
					if(!workerList.contains(node)){
						removing_nodes.add(node);
					}
				}

				// System.out.println(removing_nodes);

				for(Node node: removing_nodes){
					if(!this.removeNeighbor(node))System.out.println("faild to remove neighbor node.");
				}
				// Outboundノードを既定数まで増やす
				while(this.getOutbounds().size()<OUTBOUND_NUM){
					Node addNode = getSimulatedNodes().get(rand.nextInt(NUM_OF_NODES-1));
					if(addNode!=this && !this.getOutbounds().contains(addNode) && !this.getInbounds().contains(addNode)){
						this.addNeighbor(addNode);
					}
				}

				// System.out.println("before removeFirst()" + "\t size: " + workerList.size() + "\t node: " + workerList.getFirst());
				workerList.removeFirst();
				// System.out.println("after  removeFirst()" + "\t size: " + workerList.size() + "\t node: " + workerList.getFirst());
			}
		
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

			// inbound を選ぶ
			// 自分からみて下流を選ぶ
			Node working_node = to;
			ArrayList<Node> target_inbounds = this.getInbounds();
			ArrayList<Node> removing_node = new ArrayList<Node>();
			if(active_inbound.size()<BLOCK_FREQ){
				active_inbound.add(working_node);
			}else{
				// 更新間隔 + 1 になるよう add
				active_inbound.add(working_node);
				active_inbound.removeFirst();

				for(Node node: target_inbounds){
					if(active_inbound.contains(node)){
						removing_node.add(node);
					}
				}

				for(Node node : removing_node){
					if(!active_inbound.contains(node)){
						node.removeNeighbor(this);
						while(node.addNeighbor(getSimulatedNodes().get(rand.nextInt(NUM_OF_NODES-1))));	
					}
				}
			}
		}else{
			sendingBlock = false;
		}
	}

	//add
	public void checkNode(){routingTable.checkNode();}

	// //add
	public void changeNeighbors(){
		Random rand = new Random();
		Node removeNode;
		Node addNode;

		removeNode = score.getWorstNodeWithRemove();
		if(removeNode == this) return;

		removeNeighbor(removeNode);
		// workerList.remove(removeNode);

		while(true){
			addNode = getSimulatedNodes().get(rand.nextInt(NUM_OF_NODES));

			if(addNode.getInbounds().size()>=INBOUND_NUM){
			}else if(addNode==removeNode){
			}else if(addNeighbor(addNode))break;
			removeNeighbor(addNode);
		}
		// workerList = new HashMap<Node,int[]>();
	}

	// //add
	// public void changeNeighbors_v2(){
	
	// 	Node removeNode;
	// 	Node addNode;
	// 	int count = 0;

	// 	changeNeighbors();

	// 	Map<Node,Double> scores = new HashMap<>();

	// 	for(Map.Entry<Node,Double> i: score.getScores().entrySet()){
	// 		scores.put(i.getKey(),i.getValue());
	// 	}

	// 	for(Map.Entry<Node,Double> i: scores.entrySet()){
	// 		if(i.getValue()>=score.getAverageAllScore()){

	// 			removeNode = score.getWorstNode();

	// 			if(removeNode == this) return;
	// 			removeNeighbor(removeNode);
	// 			workerList.remove(removeNode);

	// 			while(true){
	// 				addNode = getSimulatedNodes().get(rand.nextInt(NUM_OF_NODES-1));
				
	// 				if(addNode.getInbounds().size()>OUTBOUND_NUM){
	// 				}else if(addNode==removeNode){
	// 				}else if(addNeighbor(addNode)){
	// 					count++;
	// 					score.removeScore(removeNode);
	// 					break;
	// 				}
	// 			}
	// 		}
	// 	}
	// 	nodeChangeNum(count);
	// 	return;
	// }

	// //add
	// public void checkFrequency(){
	// 	ArrayList<Node> neighbors = this.getOutbounds();
	// 	ArrayList<Node> node_over30Inbounds = new ArrayList<Node>();

	// 	Node addNode = getSimulatedNodes().get(rand.nextInt(NUM_OF_NODES-1));
	// 	while(addNode == this)addNode = getSimulatedNodes().get(rand.nextInt(NUM_OF_NODES-1));

	// 	int count = 0;

	// 	for(int i=0;i<neighbors.size();i++){

	// 		Node node = neighbors.get(i);
	// 		// Node node = neighbors.get(rand.nextInt(NUM_OF_NODES-1));
	// 		score.removeScore(node);
	// 		if(!workerList.containsKey(node) && removeNeighbor(node)){
	// 			while(true){
	// 				int size = score.getPreNodes().size();
	// 				if(size>OUTBOUND_NUM && addNode!=this){
	// 					// addNode = score.getBestNodeFromAllScores(node_over30Inbounds);
	// 					addNode = getSimulatedNodes().get(rand.nextInt(NUM_OF_NODES-1));
	// 				}else{
	// 					addNode = getSimulatedNodes().get(rand.nextInt(NUM_OF_NODES-1));
	// 				}

	// 				if(addNode.getInbounds().size()>INBOUND_NUM){
	// 				}else if(addNode==node){
	// 				}else if(addNeighbor(addNode)){
	// 					count++;
	// 					break;
	// 					// return;
	// 				}
	// 				node_over30Inbounds.add(addNode);
	// 			}
	// 			// if(count==1)break;
	// 		}
	// 	}
	// 	// workerList = new ArrayList<Node>();
	// 	nodeChangeNum(count);
	// 	return ;
	// }

	// public int getHopCount(Block blcok){
	// 	if(hop_count.containsKey(block)){
	// 		// System.out.println(hop_count.get(block));
	// 		return hop_count.get(block);
	// 	}else{
	// 		return 0;
	// 	}
	// }

	// public int addInvCount(int inv_count, Block block){
	// 	if(node_has_block.containsKey(block.getId())){
	// 		if(block.getHeight()>(ENDBLOCKHEIGHT/2)){
	// 			inv_count++;
	// 		}
	// 	}

	
	// 	return inv_count;
	// }

	public int addInvCount(int inv_count, Block block){
		if(node_has_block.containsKey(block.getId())){
			if(block.getHeight()>(ENDBLOCKHEIGHT/2)&&node_has_block.get(block.getId())[0]<=(NUM_OF_NODES)){
				inv_count++;
			}
		}else inv_count++;
		return inv_count;
	}

	public void receivedNodeCount(Block receivedBlock){
		Integer[] list = {0,0,0};

		// ノードがブロックを受信した場合にカウント
		if(!node_has_block.containsKey(receivedBlock.getId())){
			list[0] = list[0]+1;
			node_has_block.put(receivedBlock.getId(),list);
		}else{
			list = node_has_block.get(receivedBlock.getId());
			list[0] = list[0]+1;
			node_has_block.put(receivedBlock.getId(),list);
			// System.out.println("received block:"+ node_has_block.get(receivedBlock.getId()) + ":" + receivedBlock);
			if(receivedBlock.getHeight()>(ENDBLOCKHEIGHT/2)&&node_has_block.get(receivedBlock.getId())[0]%10==0){
				// System.out.println(receivedBlock +" "+ receivedBlock.getHeight() + " " + this.getNeighbors().size() + " currentTime:" + (getCurrentTime()-receivedBlock.getTime()) + " 伝播率:" + node_has_block.get(receivedBlock.getId())[0] + " =" + node_has_block.get(receivedBlock.getId())[1] + "/" + node_has_block.get(receivedBlock.getId())[2]);

				average_neighbor_num = average_neighbor_num + this.getNeighbors().size();
				// for(Map.Entry<Node,Double> i: score.getScores().entrySet()){
				// 	if(!workerList.contains(i.getKey())){
				// 		worker_num++;
				// 	}
				// }
				worker_count++;
				// average_worker_num = average_worker_num+worker_num;
				// System.out.println("average worker: " + average_worker_num/worker_count + ":" + worker_num + ":" + worker_count);
				// worker_num=0;
				
				// System.out.println("average_neighbor_num: " + average_neighbor_num/worker_count);
				
			}
		}
	}

}