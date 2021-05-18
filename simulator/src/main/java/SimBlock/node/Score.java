package SimBlock.node;
import SimBlock.simulator.Main;
import SimBlock.simulator.Simulator;	//add

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class Score{
	private Map<Node,Double> scores = new HashMap<Node,Double>();
	private Map<Node,Double> allScores = new HashMap<Node,Double>();
	private double score = 0;
public static double para_ = 3;
	public static double para = para_/10;
	private Node worst;
	private Node selfNode;

	Score(Node node){
		selfNode = node;
	}

	public Map<Node,Double> getScores(){return scores;}
	public Map<Node,Double> getAllScores(){return allScores;}
	public double getScore(Node node){return allScores.get(node);}
	public int getScoresSize(){return scores.size();}
	public static double getPara(){return para;}


	public Node getBestNodeFromAllScores(List<Node> node_over30Inbounds){
		List<Node> nodes = this.getPreNodes();
		nodes.removeAll(node_over30Inbounds);
		Node node = selfNode;
		score = 1000000000;
		
		for(Node i : nodes){
			if(score < allScores.get(i)){
				score = allScores.get(i);
				node = i;
			}
		}

		return node;
	}

	//隣接ノード以外のノードリストを返す
	public List<Node>getPreNodes(){
		List<Node> nodes = new ArrayList<Node>(allScores.keySet());
		for(Node i :scores.keySet()){
			if(nodes.contains(i)){
				nodes.remove(i);
			}
		}
		for(Node i :selfNode.getInbounds()){
			if(nodes.contains(i)){
				nodes.remove(i);
			}	
		}
		return nodes;
	}

	public double getMedianScore(){
		List<Double> list = new ArrayList<>();
		for(Map.Entry<Node,Double> i : getAllScores().entrySet()){
			list.add(i.getValue());
		}
		Double[] List = list.toArray(new Double[list.size()]);

		return Simulator.median(Simulator.bubble_sort(List));
	}

	public double getAverageScore(){
		double average_score=0;

		for(Map.Entry<Node,Double> i: getScores().entrySet()){
			average_score=average_score+i.getValue();
		}

		return average_score/getScores().size();
	}

	public double getAverageAllScore(){
		double average_score=0;

		for(Map.Entry<Node,Double> i: getAllScores().entrySet()){
			average_score=average_score+i.getValue();
		}

		return average_score/allScores.size();	
	}
	
	public boolean contains(Node node){
		if(scores.containsKey(node))return true;
		return false;
	}

	public boolean removeScore(Node node){
		if(scores.remove(node)!=null)return true;
		return false;
	}

	public void addScore(Node from, long t_inv, long t_block){
			// calculate score
		if(allScores.get(from) == null){
			score = (t_inv-t_block);
		}
		else{
			score = allScores.get(from);
			allScores.remove(from);
			score = (1-para)*(score)+para*(t_inv-t_block);

		}
		allScores.put(from, score);
		// calcuate average of all neighbor nodes score 
		if(selfNode.getOutbounds().contains(from))scores.put(from,score);
	}

	public Node getWorstNode(){
		if(scores.size() == 0) return selfNode;
			worst = scores.keySet().iterator().next();

			for(Node i: scores.keySet()){

			// System.out.println("score " + i + ":" + scores.get(i));

			if(scores.get(worst)<scores.get(i)){
				worst = i;
			}
		}
		return worst;
	}

	public Node getWorstNodeWithRemove(){
		
		// System.out.println("before getWorstNodeWithRemove:" + scores.size());
		
		Node worst = getWorstNode();

		// if(selfNode.getNodeID()==10)System.out.println("worst score:" +scores.get(worst));
		// System.out.println("remove " + worst + ":" + scores.get(worst));
		scores.remove(worst);

		// System.out.println("after  getWorstNodeWithRemove:" + scores.size());
			
		return worst;
	}

}

