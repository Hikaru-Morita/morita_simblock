 
package SimBlock.node;
import SimBlock.simulator.Main;
import SimBlock.simulator.Simulator;	//add

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class Score{
	private Map<Node,Double> scores = new HashMap<Node,Double>();
	private static Map<Node,Double> allScores = new HashMap<Node,Double>();
	private static ArrayList<Double> scoreList = new ArrayList<Double>();
	private double score = 0;
	public static double para = 0.3;
	private Node worst;
	private Node selfNode;

	private static long score_count = 0;
	private static double average_score = 0;

	Score(Node node){
		selfNode = node;
	}

	public Map<Node,Double> getScores(){return scores;}
	public double getScore(Node node){return scores.get(node);}
	public int getScoresSize(){return scores.size();}

	public List<Node>getPreNodes(){
		List<Node> nodes = new ArrayList<Node>(allScores.keySet());
		for(Node i :scores.keySet()){
			if(nodes.contains(i)){
				nodes.remove(i);
			}
		}
		return nodes;
	}

	public static double getMedianScore(){
		List<Double> list = new ArrayList<>();
		for(Map.Entry<Node,Double> i : allScores.entrySet()){
			list.add(i.getValue());
		}
		Double[] List = list.toArray(new Double[list.size()]);

		return Simulator.median(Simulator.bubble_sort(List));
	}

	public static double getAverageScore(){
		double average_score=0;

		for(Map.Entry<Node,Double> i: allScores.entrySet()){
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
		if(selfNode.getOutbounds().contains(from)){
			// calculate score
			if(scores.get(from) == null){
				score = (t_inv-t_block);
				scores.put(from, score);
			}
			else{
				score = scores.get(from);
				scores.remove(from);
				score = (1-para)*(score)+para*(t_inv-t_block);
				scores.put(from, score);
			}
		}
		// calcuate average of all neighbor nodes score 
		allScores.put(from,score);
		//平均値がおかしい
	}

	public Node getWorstNode(){
		if(scores.keySet() != null){
			for(Node i: scores.keySet()){
				worst = i;
				break;
			}
			for(Node i: scores.keySet()){
				if(scores.get(worst)<scores.get(i)){
					worst = i;
				}
			}
		}else{
			System.out.println("getWorstNode: error     " + worst);
			worst = selfNode;
		}
		return worst;
	}

	public Node getWorstNodeWithRemove(){
		// System.out.println(scores.size());
		if(scores.size() == 0) return selfNode;
		worst = scores.keySet().iterator().next();
		int num = 0;
		for(Node i: scores.keySet()){
			if(scores.get(worst)<scores.get(i)){
				worst = i;
			}
		}

		scores.remove(worst);
		return worst;
	}

	public Node getWorstNodeWithRemove_v2(){
		// System.out.println(scores.size());
		if(scores.size() == 0) return selfNode;
		worst = scores.keySet().iterator().next();
		int num = 0;
		double worst_score = getAverageScore();
		for(Node i: scores.keySet()){
			if(scores.get(worst)<scores.get(i)){
				worst = i;
				worst_score = scores.get(i);
			}
		}
		if(worst_score > this.getAverageScore()){
			scores.remove(worst);
			return worst;
		}
		return selfNode;
	}

}