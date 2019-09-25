package SimBlock.node;

import java.util.HashMap;
import java.util.Map;

public class Score{
	private Map<Node,Double> scores = new HashMap<Node,Double>();
	private double score = 0;
	private double para = 0.3;
	private Node worst;
	public static double average_bft;
	public static long count;

	public static void addBFT(long bft){
		average_bft = average_bft + bft;
		count = count + 1;
	}

	public static double getAverageBFT(){
		return average_bft/count;
	}

	public 	Map<Node,Double> getScores(){return scores;}
	public double getScore(Node node){return scores.get(node);}
	public double getAverageScore(){
		int i = 0;	
		double average_score = 0;
		
		for(double val : scores.values()){
			average_score = average_score + val;
			i++;
		}
		return average_score/i;
	}

	public Node gerWorstNode(){
		for(int i = 0; ; ){

		}
	}

	public void addScore(Node from, long t_inv, long t_block){
		if(scores.get(from) == null){
			score = (t_inv-t_block);
			scores.put(from, score);
			// System.out.println("first");
		}
		else{
			score = scores.get(from);
			scores.remove(from);
			score = (1-para)*(score)+para*(t_inv-t_block);
			scores.put(from, score);
			// System.out.println("more than once");
		}
	}
	// public long getAverageScore(){
	// 	for(Node score; scores)
	// }


}