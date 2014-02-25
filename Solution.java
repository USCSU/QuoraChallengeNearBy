 
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.TreeSet;
class pointList{
	double x; //x-coordinate
	double y; //y-coordinate
	ArrayList<Integer> list; // This list is to store question id which mentioned this topic. 
	pointList(double x, double y){
		this.x = x;
		this.y = y;
		list = new ArrayList<Integer>(1000);
	}
}

public class Solution {
	  static HashMap<Integer,pointList> content=new HashMap<Integer,pointList>();; //Main structure to store all the STDIN information
	  //Calculate distance of two points
	  public static double distance(double x1, double y1, double x2, double y2){
		  return  Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
	  }
	  
	  //process query including t or q 
	  public static StringBuilder queryProcess(String[] info){
		  StringBuilder res = new StringBuilder();
		  if(info[0].equals("t")){
			 res.append(topicQuery(Integer.parseInt(info[1]),Double.parseDouble(info[2]),Double.parseDouble(info[3])));
		  }else if(info[0].equals("q")){
			 res.append(quesQuery(Integer.parseInt(info[1]),Double.parseDouble(info[2]),Double.parseDouble(info[3])));
		  }
		  return res;
	  }
	  
	  //initite a max heap to get top K nearest topics
	  public  static PriorityQueue<Map.Entry<Integer, pointList>> getNearestTopics(int num, final double x, final double y){
		  //re-write comparator
		  Comparator<Map.Entry<Integer, pointList>> cmp = new Comparator<Map.Entry<Integer, pointList>>(){//max heap
			  public int compare(Map.Entry<Integer, pointList> p1, Map.Entry<Integer, pointList> p2){
				  double dis2 = distance(p2.getValue().x,p2.getValue().y,x,y);
				  double dis1 = distance(p1.getValue().x,p1.getValue().y,x,y);
				  if(Math.abs(dis1-dis2)<0.002){
					 return p1.getKey() - p2.getKey();
				  }
				  else if(dis1>dis2)
					  return -1;
				  else return 1;
				  
			  }
		  };
		  PriorityQueue<Map.Entry<Integer, pointList>> q = new PriorityQueue<Map.Entry<Integer, pointList>>(num,cmp);
		  //refill priorityQueue to get top k entry with smallest distance
		  Iterator<Entry<Integer, pointList>> it = content.entrySet().iterator();
		  while(it.hasNext()){
			  Entry<Integer, pointList> temp = it.next();
			  if(q.size()<num)
				q.add(temp);
			  else{
				  double dis1 = distance(temp.getValue().x,temp.getValue().y,x,y);
				  double dis2 = distance(q.peek().getValue().x,q.peek().getValue().y,x,y);
				  if(dis1<dis2||Math.abs(dis1-dis2)<0.002){
					  q.poll();
					  q.add(temp);
				  }
			  }
		  }
		  return q;
	  }
      //The function is perform topic query
	  public static StringBuilder topicQuery(int num,   double x,   double y){
		 StringBuilder res = new StringBuilder();
		 if(num==0) return res;
		 //max heap
		 PriorityQueue<Entry<Integer, pointList>> s =  getNearestTopics(num,x,y);
		 
		 //reverse data order and output
		 while(!s.isEmpty())
			 res.insert(0,s.poll().getKey()+" ");
		 
		 //In order to make it platform-free, this way is better.
		 res.append(System.getProperty("line.separator"));
		 
		 return res;
	  }
	 
	  //num indicates how many output id needed; x is dest X coordinate, y is dest Y coordinate
	  public static StringBuilder quesQuery(int num,   double x,   double y){
		  StringBuilder res = new StringBuilder();
		  if(num==0)
			  return res;
		  //max heap of required data with topic ids; Here I use a num*12 as an appropriate number to make O(nlogk) happens, or else it's O(nlogn)
		  //the effect of num*11 is better than num*12 for some cases;
		  PriorityQueue<Entry<Integer, pointList>> pq =  getNearestTopics(num*12,x,y);
		  //make max heap in acesending order, or else the result would be wrong.
		  Stack<Entry<Integer,pointList>> p = new Stack<Entry<Integer,pointList>>();  
		  while(!pq.isEmpty())
			  p.add(pq.poll());
		  //Treeset here is to sort each group with tie of distance.
		  TreeSet<Integer> treeset = new TreeSet<Integer>(new Comparator<Integer>(){
			  public int compare(Integer i1, Integer i2){
			  return i2-i1;
		  }});
		  //hashset here to eliminate all the dup values; Structure can be tracked at the end of code.
		  HashSet<Integer> dup = new HashSet<Integer>(10000);
		  if(p.isEmpty()) return res;
		  //Get first element
		  Entry<Integer, pointList> rec = p.pop();
		  double disRec = distance(rec.getValue().x,rec.getValue().y,x,y);
		  
		  for(int i:rec.getValue().list){
			  if(!dup.contains(i)){
		  		treeset.add(i);
		  		dup.add(i);
			  }
		  }
		 
		  while(!p.isEmpty()&num!=0){
			  Entry<Integer, pointList> entry = p.pop();
			  double dis = distance(entry.getValue().x,entry.getValue().y,x,y);
			  ArrayList<Integer> temp = entry.getValue().list;
			  if(Math.abs(disRec - dis)>0.002){
				  while(!treeset.isEmpty()&num!=0){
					  res.append(treeset.pollFirst()+" ");
					  num--;
				  }
			  }
				  
			  for(int i:temp) 	{
				  if(!dup.contains(i)){
					  treeset.add(i);
					  dup.add(i);
				  }
			  }
		 
 			  disRec = dis;
			  rec = entry;
  		  }
		  while(!treeset.isEmpty()&num!=0)
			  res.append(treeset.pollFirst()+" ");
		  res.append(System.getProperty("line.separator"));
		  return res;
	  }
 
	  //This function is to store information from STDIN
	  //lineNum is # of line in the STDIN, tokens is info of each line; 
	  //tDelimiter indicates how many topic line from STDIN; qDelimeter indicates how many question line from STDIN 
	  public static void preProcess(int lineNum,String[] tokens, int tDelimiter, int qDelimiter){
		  
		  	if(lineNum <= tDelimiter+1){
		  		//store topic information
		  		content.put(Integer.parseInt(tokens[0]),new pointList(Double.parseDouble(tokens[1]),Double.parseDouble(tokens[2])));
		  		
		  	}else if(lineNum <= tDelimiter+qDelimiter+1){
		  		//store question information
		  		int qId = Integer.parseInt(tokens[0]);
		  		int qtNum = Integer.parseInt(tokens[1]);
		  		//this loop mainly for refilling information of arraylist
		  		for(int i = 2;i<2+qtNum;i++){
		  			content.get(Integer.parseInt(tokens[i])).list.add(qId);
		  		}
		  	}
		   
	  }
	  //data processing function
	  public static void Process() throws IOException{
		  BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		  BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out));
		  
		  StringBuilder output = new StringBuilder();
		  int line= 1;
		  String[] tokens = br.readLine().split(" ");
		  int tNum = Integer.parseInt(tokens[0]);//indicates # of topic lines from STDIN.
		  int qNum = Integer.parseInt(tokens[1]);//indicates # of QUESTION lines from STDIN.
		  int queryNum = Integer.parseInt(tokens[2]);
		  for( line = 2;line<=tNum+qNum+1;line++){
			  tokens = br.readLine().split(" ");
			  preProcess(line,tokens,tNum,qNum);
			  
		  }
			  
		  for(;line<=tNum+qNum+queryNum+1;line++){
			  tokens = br.readLine().split(" ");
			  output.append(queryProcess(tokens));
		  }
 
	        br.close();
	        out.write(output.deleteCharAt(output.length()-1).toString());
	        out.flush();
	        out.close();
	  }
	  public static void main(String[] args){
	 
		  try {				 
							Process();
				 
		  } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		  } 
	  }
	  
	  /*
	   After preProcess:
	   The structure content:
	   0 0.0 0.0 0->1->2
	   1 1.0 1.0 1->2->5
	   2 2.0 2.0 2->5
	   Output:
	   0 1
	   5 2 1 0
	   */
	  
}
