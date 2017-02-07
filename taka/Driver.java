package taka;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.plaf.synth.SynthSeparatorUI;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;






public class Driver {
	public static void main(String[] args) throws IOException {
		  System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		  int totalNumFrames=imageOut();	//getting frameimages
		  int framenum=0,flagNumForKmeansOutFile=0; 
    while(framenum!=(totalNumFrames-1)){//compare 2 image and find matching keys. so totalframe-1 is the number of compared images
	//capturing images
		  	  Mat imageLeft=new Mat();
			  Mat imageRight=new Mat();
			  try{
				  imageLeft=Highgui.imread("./resources/StitchedFrames/stitch"+framenum+".jpg");  //get frameimage put into Matrix
			  }catch(Exception e){
				  System.out.println("Not working(Left)");
				  System.exit(0);
				}
			  framenum++;
			  try{
				  imageRight=Highgui.imread("./resources/RetrievedImages/"+framenum+".jpg");
		  	  }catch(Exception e){
		  		  System.out.println("Not working(Right)");
		  		  System.exit(0);
		  	  }
			
			// ---------  convert to gray images ------------	  
			  Mat grayImageLeft=new Mat(imageLeft.rows(),imageLeft.cols(),imageLeft.type());//convert to grayimage
				Imgproc.cvtColor(imageLeft, grayImageLeft, Imgproc.COLOR_BGRA2GRAY);
				Core.normalize(grayImageLeft, grayImageLeft,0,255,Core.NORM_MINMAX);
			  Mat grayImageRight=new Mat(imageRight.rows(),imageRight.cols(),imageRight.type());//convert to grayimage
				Imgproc.cvtColor(imageRight, grayImageRight, Imgproc.COLOR_BGRA2GRAY);
				Core.normalize(grayImageRight, grayImageRight,0,255,Core.NORM_MINMAX);
				
			// ------ SURF START ------
			    FeatureDetector surfDetector = FeatureDetector.create(FeatureDetector.SURF);
			    DescriptorExtractor surfExtractor = DescriptorExtractor.create(DescriptorExtractor.SURF);
					
			 //------- FINDING KEY POINTS -------				
			    MatOfKeyPoint keyPoint1 = new MatOfKeyPoint();
			    	surfDetector.detect(grayImageLeft, keyPoint1);
			    	List<KeyPoint>kp1=keyPoint1.toList();//Storing all Keypoints into list
							 
			    MatOfKeyPoint keyPoint2 = new MatOfKeyPoint();
			    	surfDetector.detect(grayImageRight, keyPoint2);
			    	List<KeyPoint>kp2=keyPoint2.toList();//Storing all Keypoints into list
							 
			 //------- Descriptors --------
			    Mat descriptors1 = new Mat(imageLeft.rows(),imageLeft.cols(),imageLeft.type());
			    	surfExtractor.compute(grayImageLeft, keyPoint1, descriptors1);
			    Mat descriptors2 = new Mat(imageRight.rows(),imageRight.cols(),imageRight.type());
			    	surfExtractor.compute(grayImageRight,keyPoint2,descriptors2);
			    	
			 //------- Matches ----------   	
			    MatOfDMatch matchs=new MatOfDMatch();
			    	DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
			    	matcher.match(descriptors1, descriptors2, matchs);
			    
			 //-------- Filter the resulting matches --------   	
			    List<DMatch>matchesList=matchs.toList();	
			    int rowCount=matchesList.size();
			    List<DMatch>goodMatchesList=new ArrayList<DMatch>();
			    for(int i=0;i<rowCount;i++){
			    	if(matchesList.get(i).distance<0.07){//filtering the matchpoints by hamming distance. filtering. lower number is better, but less points.
			    		goodMatchesList.add(matchesList.get(i));
			    	}
			    }
		
			    	int num_matches=goodMatchesList.size();
			    	System.out.println("After 1st filter> kp1 size:"+kp1.size());//original kp1 size.
			    	System.out.println("After 1st filter> kp2 size:"+kp2.size());//original kp2 size.
			    	
			    	
			    	List<KeyPoint>newkp1=new ArrayList<KeyPoint>();//preparing List of keypoints for filtered matches.
			    	List<KeyPoint>newkp2=new ArrayList<KeyPoint>();//preparing List of keypoints for filtered matches.
			    	
			    	System.out.println("Number of good matches are:"+num_matches);
			    	for(int i=0;i<num_matches;i++){//iterate the number of matches.
			    			int idx1=goodMatchesList.get(i).queryIdx;//index searching for keypoints in left image.
			    			int idx2=goodMatchesList.get(i).trainIdx;//index searching for keypoints in right image.
			    			
				    		newkp1.add(kp1.get(idx1));//Storing all matched keypoints into list
				    		newkp2.add(kp2.get(idx2));//Storing all matched keypoints into list
			    	}		
			    	
			    	
//Second filter by x,y values. If it changes too much, the point is false-positive. Need to be filtered
//average was not good enough. when you have most is 0.00,,,and few is huge number. average gets big.
//so used the "most frequent "change values" in integer value". logic is mostly in findPopular() method.

			    	double xChange=0, yChange=0;	    	
			    	ArrayList<Integer>mostFrequentXChangeNum=new ArrayList<Integer>();
			    	ArrayList<Integer>mostFrequentYChangeNum=new ArrayList<Integer>();
			    	//0,mostFrequentYChangeNum=0;
			    	//get all changes for x and y values and store it as an integer value
			    	for(int i=0;i<num_matches;i++){		
			    		xChange=Math.abs(((newkp2.get(i).pt.x)-(newkp1.get(i).pt.x)));
			    		yChange=Math.abs(((newkp2.get(i).pt.y)-(newkp1.get(i).pt.y)));			 
			    		mostFrequentXChangeNum.add((int)xChange);
			    		mostFrequentYChangeNum.add((int)yChange);
			    	}
			    	
			    	//finding out the most frequent int value. then based on that, we can filter.
			    	int freqXval=findPopular(mostFrequentXChangeNum);
			    	int freqYval=findPopular(mostFrequentYChangeNum);
			    	System.out.println("Popular x change value is: "+freqXval);
			    	System.out.println("Popular y change value is: "+freqYval);
			    	
		    		//preparing List of keypoints for 2nd filtered matches.
		    		List<KeyPoint>newnewkp1=new ArrayList<KeyPoint>();
			    	List<KeyPoint>newnewkp2=new ArrayList<KeyPoint>();
			    	
			    	
			    	//if the change is smaller than average, store to 2nd filter List.
		    		for(int i=0;i<num_matches;i++){
		    			if((((int)Math.abs((newkp2.get(i).pt.x)-(newkp1.get(i).pt.x)))==freqXval)||//if X or Y change is smaller than average, store them.
		    			(((int)Math.abs((newkp2.get(i).pt.y)-(newkp1.get(i).pt.y)))<freqYval)){
		    				newnewkp1.add(newkp1.get(i));//Storing all matched keypoints into list
				    		newnewkp2.add(newkp2.get(i));//Storing all matched keypoints into list
		    			}
			    	}
				    
		    		
		    		//Convert to MatOfKeyPoint for printing the results.
				    MatOfKeyPoint newnewkeyPoint1 = new MatOfKeyPoint();
				    newnewkeyPoint1.fromList(newnewkp1);		    
				    MatOfKeyPoint newnewkeyPoint2 = new MatOfKeyPoint();
				    newnewkeyPoint2.fromList(newnewkp2);

				    //new computation based on filtered great keypoints.
			    	surfExtractor.compute(grayImageLeft, newnewkeyPoint1, descriptors1);
			    	surfExtractor.compute(grayImageRight,newnewkeyPoint2,descriptors2);
			    	
			 //------- Matches ----------   	
			    	matchs=new MatOfDMatch();
			    	matcher.match(descriptors1, descriptors2, matchs);
				    
			    	
			    	List<DMatch>newnewmatchesList=matchs.toList();	
				    int newnewrowCount=newnewmatchesList.size();
				    List<DMatch>newnewgoodMatchesList=new ArrayList<DMatch>();
				    for(int i=0;i<newnewrowCount;i++){
				    		newnewgoodMatchesList.add(newnewmatchesList.get(i));
				    }
				    MatOfDMatch newnewgoodMatches=new MatOfDMatch();
				    newnewgoodMatches.fromList(newnewgoodMatchesList);
			    				    
				    Mat matchedImage = new Mat(imageLeft.rows(),imageLeft.cols()*2,imageLeft.type());
			    	Features2d.drawMatches(imageLeft, newnewkeyPoint1, imageRight, newnewkeyPoint2, newnewgoodMatches, matchedImage);
				    Highgui.imwrite("./resources/matchingResult/matchResultL"+(framenum-1)+"R"+framenum+".jpg", matchedImage);
				    	
		    		System.out.println("After 2nd filter> kp1 size:"+newnewkp1.size());
		    		System.out.println("After 2nd filter> kp2 size:"+newnewkp2.size());

		    		
		    		////////done with filters./////////////
		    		
//--------Kmeans Clustering.
			    	 Kmeans_KPver kmeans1=new Kmeans_KPver(newnewkp1);
			    	 
			    	 //left image: calculating the triangle and returning the point value of the center of triangle
					 Point leftTrigCent=kmeans1.wholeWork(imageLeft,flagNumForKmeansOutFile);
					 System.out.println("*********");
					 System.out.println("L"+(framenum-1)+"R"+framenum+"(Left)Center of Triangle is:("+leftTrigCent.x+" , "+leftTrigCent.y+")");
					 flagNumForKmeansOutFile++;
					 
					//right image: calculating the triangle and returning the point value of the center of triangle
					 Kmeans_KPver kmeans2=new Kmeans_KPver(newnewkp2);
					 Point rightTrigCent=kmeans2.wholeWork(imageRight,flagNumForKmeansOutFile);
					 System.out.println("L"+(framenum-1)+"R"+framenum+"(Right)Center of Triangle is:("+rightTrigCent.x+" , "+rightTrigCent.y+")");
					 System.out.println("*********");
					 flagNumForKmeansOutFile++;
					 
					 System.out.println("-----------------------------------------");				 
					 
//----------Stitch two images using the point!!!-	
					 
					 
					// if(framenum==1){//test. just for two first images.
						 //imageRight
						 Point edge1=new Point(0,0);
						 Point edge2=new Point(Math.abs(rightTrigCent.x-leftTrigCent.x) , imageLeft.height());//for crop
						 //Point edge2=new Point(imageLeft.width(),Math.abs(rightTrigCent.x-leftTrigCent.x));//for crop
						 Rect r1=new Rect(edge1,edge2);//rect for crop
						 Mat newImgcrop=new Mat(imageRight,r1);
						 Highgui.imwrite("./resources/cropped/crop"+(framenum-1)+".jpg", newImgcrop);
						 
				
						 
						 
						 //Point edgeNew1=new Point(0,0);
						 //Point edgeNew2=new Point(newImgcrop.width()+imageLeft.width(),imageLeft.height());//for new image
							
						// Rect r2=new Rect(edgeNew1,edgeNew2);
						 Mat newImgBigger=new Mat();
						 Size sz=new Size(newImgcrop.width()+imageLeft.width(),imageLeft.height());
						 Imgproc.resize(imageLeft, newImgBigger, sz);
						 //System.out.println("width,"+newImgBigger.width()+"height"+newImgBigger.height());
	
 
								 
						 File f1=new File("./resources/StitchedFrames/stitch"+(framenum-1)+".jpg");
						 File f2=new File("./resources/cropped/crop"+(framenum-1)+".jpg");
						 BufferedImage buffImage0=ImageIO.read(f1);
						 BufferedImage buffImage1=ImageIO.read(f2);

						 int actualRow = 0, aux;
						 BufferedImage finalImage=new BufferedImage(buffImage0.getWidth()+buffImage1.getWidth(),buffImage0.getHeight(),BufferedImage.TYPE_INT_RGB);
						 ////BufferedImage finalImage=new BufferedImage(buffImage0.getWidth(),buffImage0.getHeight(),BufferedImage.TYPE_INT_RGB);
						 
					/*	 for (int rows1 = 0; rows1 < buffImage0.getHeight(); rows1++) {
					            for (int cols1 = 0; cols1 < buffImage0.getWidth(); cols1++) {
					                Color color = new Color(buffImage0.getRGB(cols1, rows1));
					                int rgb = color.getRGB();
					                finalImage.setRGB(cols1, rows1, rgb);
					            }
					            //actualRow++;
					        }*/
					  /*      for (int rows2 = 0; rows2 < buffImage1.getHeight(); rows2++) {
					            for (int cols2 = 0; cols2 < buffImage1.getWidth(); cols2++) {
					                Color color = new Color(buffImage1.getRGB(cols2, rows2));
					                int rgb = color.getRGB();
					                aux = rows2; //+ actualRow;
					                finalImage.setRGB(cols2, aux, rgb);
					            }
					        }
*/
					       // File outputfile = new File("./resources/StitchedFrames/stitch"+framenum+".jpg");
					       // ImageIO.write(finalImage, "jpg", outputfile);
					        Highgui.imwrite("./resources/StitchedFrames/stitch"+framenum+".jpg", newImgBigger);
						 
								 
						
					     
					     //output final image
					   //  File f=new File("C:/Users/takan/workspace/SurfKmeansTrigFromMovie_copied7/final.jpg");
					//     try {
					//		ImageIO.write(FINAL, "jpg", f);
					//	} catch (IOException e) {
							// TODO Auto-generated catch block
					//		e.printStackTrace();
					//	}
				//	 }
					 
    	}//end of while loop
		  System.out.println("\n------------done------------");

		  		    // ------ SURF END ------
	}//end of main method
	
	
	
	
		static int imageOut(){	//getting imageframes from video chosen
			int counter=0,timecounter=0;
			boolean moreframe=true;
			System.out.println("Welcome to our image-stitching application\n\n<<Type location of your video file.>>");
			Scanner scan=new Scanner(System.in);
			String path1=scan.nextLine();
			Mat originalSizeImg=new Mat();
			VideoCapture capture=new VideoCapture(path1);
			//VideoCapture capture=new VideoCapture("forward.mp4");
			if(capture.isOpened()){
				while(moreframe){
					//capture.retrieve(originalSizeImg);//maybe this one.
					if(!capture.grab()){
						moreframe=false;
						capture.release();
						break;
					}
					capture.read(originalSizeImg);
					if( (timecounter%5==0)){//if you get error, maybe its here. change to bigger number.
						Mat resizedImage=new Mat();
						Size sz=new Size(640,360);
						Imgproc.resize(originalSizeImg, resizedImage, sz);
						Highgui.imwrite("./resources/RetrievedImages/"+counter+".jpg", resizedImage);
						if(counter==0){
							Highgui.imwrite("./resources/StitchedFrames/stitch"+counter+".jpg", resizedImage);
						}
						System.out.println("Frame "+counter);
						counter++;			
					}				
					timecounter++;
				}			
			}
			System.out.println("\n------------Retrieving image is done------------\n");
			return counter;
		}
		
		
		static int findPopular(ArrayList<Integer>a){//finding the most popular "changing values"
			if(a.isEmpty()){
				System.out.println("findPopular() is not invoked");
			}
			Collections.sort(a);
			
			int previous =a.get(0).intValue();
			int popular = a.get(0).intValue();
			int count=1;
			int maxCount=1;
			
			for(int i=1;i<a.size();i++){
				if(a.get(i).intValue()==previous){
					count++;
				}else{
					if(count > maxCount){
						popular =a.get(i-1).intValue();
						maxCount=count;
					}
					previous=a.get(i).intValue();
					count=1;
				}
			}
			return count > maxCount ? a.get(a.size()-1):popular;

		}
	}

