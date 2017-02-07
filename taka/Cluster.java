package taka;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Point;
import org.opencv.features2d.KeyPoint;

public class Cluster {

	public List points;
	public Point centroid;
	public int id;
	
	//creates a new Cluster
	public Cluster(int id){
		this.id=id;
		this.points=new ArrayList();
		this.centroid=null;
	}
	
	public List getPoints(){
		return points;
	}
	
	public void addPoint(KeyPoint point){
		points.add(point);
	}
	
	public void setPoints(List points){
		this.points=points;
	}
	
	public Point getCentroid(){
		return centroid;
	}
	
	public void setCentroid(Point centroid){
		this.centroid=centroid;
	}
	
	public int getId(){
		return id;
	}
	
	public void clear(){
		points.clear();
	}
	
	public void plotCluster(){
	//	System.out.println("[Cluster: "+id+"]");
	//	System.out.println("[Centroid: "+centroid+"]");
	//	System.out.println("Points: \n");
		for(int i=0;i<this.getPoints().size();i++){
			if(i<5){//just test to see result. 5 is enough. dont wanna see 1000 or more points.
		//	System.out.println(this.getPoints().get(i));
			}
		}
	//	System.out.println("]");
	}
}
