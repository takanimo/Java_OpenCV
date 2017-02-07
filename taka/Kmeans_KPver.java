package taka;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;

public class Kmeans_KPver {
	//number of clusters. this metric should be related to the number of points
		private int NUM_CLUSTERS=3;
		private int cluster_number=0;
		
		public List points=new ArrayList();
		private List clusters;
		public MatOfKeyPoint mkp=new MatOfKeyPoint();
		
		public Kmeans_KPver(){
			this.points=new ArrayList();
			this.clusters=new ArrayList();
		}
		
		public Kmeans_KPver(List<KeyPoint>allpoints){
			points.addAll(allpoints);
			this.clusters=new ArrayList();
			mkp.fromList(allpoints);
		}
		
		public Point wholeWork(Mat image,int framenum){
			Point centerTrig=new Point();
			init(image,framenum);
			centerTrig=calculate(image,framenum);
			return centerTrig;
		}
		
		//Initializes the process
		public void init(Mat image, int framenum){
			//create Clusters
			//set random centroids
			for(int i=0;i<NUM_CLUSTERS;i++){
				Cluster cluster=new Cluster(i);//creates 3 clusters
				//Point centroid=Point.createRandomPoint(MIN_COORDINATE, MAX_COORDINATE);
				Point centroid=new Point();
				centroid.x=image.width()*0.5+(i*20);//initial point of each centroid. can be anything.
				centroid.y=image.height()*0.5;//initial point of each centroid. can be anything.
				cluster.setCentroid(centroid);
				clusters.add(cluster);
				
			}
			//Print initial state and print the first status.
			Mat outputImage=new Mat(image.rows(),image.cols(),image.type());	//preparing outputimage
			Features2d.drawKeypoints(image, mkp, outputImage);
			
			for(int i=0;i<NUM_CLUSTERS;i++){
				Cluster c=(Cluster) clusters.get(i);
				c.plotCluster();
				if(i==0){
					Core.circle(outputImage, new Point(c.getCentroid().x, c.getCentroid().y), 8,new Scalar(255,0,0),Core.FILLED);
				}else if(i==1){
					Core.circle(outputImage, new Point(c.getCentroid().x, c.getCentroid().y), 8,new Scalar(0,255,0),Core.FILLED);
				}else{
					Core.circle(outputImage, new Point(c.getCentroid().x, c.getCentroid().y), 8,new Scalar(0,0,255),Core.FILLED);
				}
				
			}
			Highgui.imwrite("./resources/Before_KMeans/beforeKmeans_Frame_" +framenum+ ".jpg", outputImage);
			
		}
		
		private void plotClusters(Mat image, int counter,int framenum){
			Mat outputImage=new Mat(image.rows(),image.cols(),image.type());	//preparing outputimage
			//MatOfKeyPoint mkp=new MatOfKeyPoint();//just for drawing initialState.jpg
			//	mkp.fromList(points);
			//Core.circle(imageLeft, new Point(kp1.get(idx1).pt.x,kp1.get(idx1).pt.y), 5, new Scalar(255,255,0));
			Features2d.drawKeypoints(image, mkp, outputImage,new Scalar(255,255,0),0);
			for(int i=0;i<NUM_CLUSTERS;i++){
				Cluster c=(Cluster) clusters.get(i);
				c.plotCluster();
				if(i==0){
					Core.circle(outputImage, new Point(c.getCentroid().x, c.getCentroid().y), 8,new Scalar(255,0,0),Core.FILLED);
				}else if(i==1){
					Core.circle(outputImage, new Point(c.getCentroid().x, c.getCentroid().y), 8,new Scalar(0,255,0),Core.FILLED);
				}else{
					Core.circle(outputImage, new Point(c.getCentroid().x, c.getCentroid().y), 8,new Scalar(0,0,255),Core.FILLED);
				}
			}
			Highgui.imwrite("./resources/Iterations/Frame"+framenum+"_iteration"+counter + ".jpg", outputImage);
			
		}
		
		private Point plotFinalClusters(Mat image,int framenum){
			Mat outputImage=new Mat(image.rows(),image.cols(),image.type());	//preparing outputimage
			Features2d.drawKeypoints(image, mkp, outputImage,new Scalar(255,255,0),0);
			Point centerTrig=new Point();
			for(int i=0;i<NUM_CLUSTERS;i++){
				Cluster c=(Cluster) clusters.get(i);
				c.plotCluster();
				if(i==0){
					Core.circle(outputImage, new Point(c.getCentroid().x, c.getCentroid().y), 8,new Scalar(255,0,0),Core.FILLED);
				}else if(i==1){
					Core.circle(outputImage, new Point(c.getCentroid().x, c.getCentroid().y), 8,new Scalar(0,255,0),Core.FILLED);
					
				}else{
					Core.circle(outputImage, new Point(c.getCentroid().x, c.getCentroid().y), 8,new Scalar(0,0,255),Core.FILLED);
				}
				
				if(i==(NUM_CLUSTERS-1)){//last iteration. draw lines to make a triangle
					Cluster c0=(Cluster)clusters.get(0);
					Cluster c1=(Cluster)clusters.get(1);
					Cluster c2=(Cluster)clusters.get(2);
					Core.line(outputImage, new Point(c0.getCentroid().x, c0.getCentroid().y),new Point(c1.getCentroid().x, c1.getCentroid().y), new Scalar(255,0,0),3);
					Core.line(outputImage, new Point(c1.getCentroid().x, c1.getCentroid().y),new Point(c2.getCentroid().x, c2.getCentroid().y), new Scalar(0,255,0),3);
					Core.line(outputImage, new Point(c2.getCentroid().x, c2.getCentroid().y),new Point(c0.getCentroid().x, c0.getCentroid().y), new Scalar(0,0,255),3);
					//Find centroid OF the triangle.
					centerTrig=new Point();
					double centerX=(c0.getCentroid().x+c1.getCentroid().x+c2.getCentroid().x)/3;
					double centerY=(c0.getCentroid().y+c1.getCentroid().y+c2.getCentroid().y)/3;
					centerTrig.x=centerX;
					centerTrig.y=centerY;
					Core.circle(outputImage, new Point(centerTrig.x, centerTrig.y),5, new Scalar(255,51,255),Core.FILLED);
					
				}
			}
			Highgui.imwrite("./resources/Centroids_Results/Frame"+framenum + ".jpg", outputImage);	
			return centerTrig;
		}
		
		//The process to calculate the K means, with iterating method
		public Point calculate(Mat image,int framenum){
			Point centerTrig=new Point();
			boolean finish=false;
			int iteration=0;
			
			//add in new data, one at a time, recalculating centroids with each new one.
			while(!finish){
				clearClusters();//clear cluster state
				List lastCentroids=getCentroids();//get all current centroids.
				assignCluster(points);//Assign points to the closer cluster
				calculateCentroids();//Calculate new centroids
				iteration++;
				List currentCentroids=getCentroids();
				//calculate total distance between new and old centroids
				double distance=0;
				for(int i=0;i<lastCentroids.size();i++){
					distance+= distance((Point)lastCentroids.get(i), (Point)currentCentroids.get(i));
				}
				//System.out.println("################");
				//System.out.println("Iteration: "+iteration);
				//System.out.println("Centroid distances: "+distance);
				plotClusters(image, iteration,framenum);
				
				if(distance==0){
					centerTrig=plotFinalClusters(image,framenum);
					finish=true;
				}
				
			}
			return centerTrig;
		}
		
		private void clearClusters(){
			for(int i=0;i<clusters.size();i++){
				Cluster cluster=(Cluster)clusters.get(i);
				cluster.clear();
			}
		}
		
		private List getCentroids(){
			List centroids=new ArrayList(NUM_CLUSTERS);
			for(int i=0;i<clusters.size();i++){
				Cluster cluster=(Cluster)clusters.get(i);
				Point aux=cluster.getCentroid();
				Point point=new Point(aux.x,aux.y);
				centroids.add(point);
			}
			return centroids;
		}
		
		private void assignCluster(List<KeyPoint>allPoints){
			double max=Double.MAX_VALUE;//assign biggest number of double
			double min=max;
			int cluster=0;
			double distance=0.0;
			
			for(int i=0;i<allPoints.size();i++){//iterate number of all points
				KeyPoint point=allPoints.get(i);
				min=max;
				for(int j=0;j<NUM_CLUSTERS;j++){//iterate number of clusters. 
					Cluster c=(Cluster)clusters.get(j);
					distance=distance(point.pt, c.centroid);
					if(distance<min){//if distance is smaller than max double value,
						min=distance;//minimum is its distance.
						cluster=j;//
					}
				}			
			((Cluster)clusters.get(cluster)).addPoint(point);
			}
		}
		
		private double distance(Point p, Point centroid){
			return Math.sqrt(Math.pow((centroid.y-p.y), 2)
					+Math.pow((centroid.x-p.x), 2));
		}
		
		private void calculateCentroids(){
			for(int i=0;i<clusters.size();i++){//3 times. iterate
				Cluster cluster=(Cluster)clusters.get(i);
				double sumX=0;
				double sumY=0;
				List list=cluster.getPoints();					//getting points in the cluster
				int n_points=list.size();					//getting number of points in the cluster
				
				for(int j=0;j<n_points;j++){
					KeyPoint point=(KeyPoint) list.get(j);
					sumX+=point.pt.x;
					sumY+=point.pt.y;
				}
				
				Point centroid=cluster.getCentroid();
				if(n_points>0){
					double newX=sumX/n_points;
					double newY=sumY/n_points;
					//centroid.setX(newX);
					//centroid.setY(newY);
					centroid.x=newX;
					centroid.y=newY;
				}
			}
		}
}
