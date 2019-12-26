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
	private static ArrayList<Double> scoreList = new ArrayList<Double>();
	private double score = 0;
	public static double para_ = 5;
	public static double para = para_/10;
	private Node worst;
	private Node selfNode;

	private static long score_count = 0;
	private static double average_score = 0;

	Score(Node node){
		selfNode = node;
	}

	public Map<Node,Double> getScores(){return scores;}
	public Map<Node,Double> getAllScores(){return allScores;}
	public double getScore(Node node){return scores.get(node);}
	public int getScoresSize(){return scores.size();}
	public static double getPara(){return para;}

	public List<Node>getPreNodes(){
		List<Node> nodes = new ArrayList<Node>(allScores.keySet());
		// System.out.println(previous nodesSize: +nodes.size());
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
		// System.out.println(after nodesSize: +nodes.size());
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

		return average_score/scores.size();
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
			// System.out.println(test1);
		}
		else{
			score = allScores.get(from);
			allScores.remove(from);
			score = (1-para)*(score)+para*(t_inv-t_block);
		}
		allScores.put(from, score);
		// calcuate average of all neighbor nodes score 
		if(selfNode.getOutbounds().contains(from))scores.put(from,score);

		// if(selfNode.getNodeID() == 10)System.out.println(getAverageScore() + +scores.size()+ + allScores.size());

		// if(selfNode.getNodeID()==10)System.out.println(getAverageScore() + +scores.size()+ + allScores.size());
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

		if(selfNode.getNodeID()==10)System.out.println("worst score:" +scores.get(worst));
		// System.out.println(scores: +scores.size());
		scores.remove(worst);
		// System.out.println(scores: +scores.size());

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

