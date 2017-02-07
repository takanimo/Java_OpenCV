package taka;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.highgui.Highgui;

import taka.ImageProcessing;
import taka.ImageUtils;

public class Main2 {

	public static void main(String[] args) {
		
		 System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		 //////////count number of total jpg images in the folder
		 File f=new File("C:/Users/takan/workspace/Panorama1/Panorama1/resource/forward3_2/");
		 int counter=0;
		 for(File file : f.listFiles()){
			 if(file.getName().endsWith(".jpg")){
				 counter++;
			 }
		 }
		 System.out.println("Number of files: "+counter);
		 Mat[]matarray=new Mat[counter];
		 Mat[]newImageArray=new Mat[counter];
		 Rect[]r=new Rect[counter];
		 BufferedImage FINAL=null;
		 //////////repeat reading images as Mat until count stoppes. 
		 for(int i=0;i<counter;i++){
			 matarray[i]= Highgui.imread("C:/Users/takan/workspace/Panorama1/Panorama1/resource/forward3_2/"+i+".jpg");
			
			 Point edge1 = new Point(0, (matarray[i].height()/4)*3 - 150);
		     Point edge2 = new Point(matarray[i].width(), matarray[i].height());
		     r[i] = new Rect(edge1, edge2);
			 newImageArray[i]=new Mat(matarray[i],r[i]);
			 //Highgui.imwrite("NewImg"+i+".jpg", newImageArray[i]);
			 
		 }
		 System.out.println("how many??:::"+newImageArray.length);
		 //for(int j=0;j<newImageArray.length-1;j++){
		 for(int j=0;j<newImageArray.length-1;j++){
			 System.out.println("stitchhhh"+j);
			 if(j==0){
			 FINAL = ImageProcessing.ImageStitching(ImageUtils.Mat2BufferedImage(newImageArray[j]), ImageUtils.Mat2BufferedImage(newImageArray[j+1]));
			 }else{
			 FINAL = ImageProcessing.ImageStitching(FINAL, ImageUtils.Mat2BufferedImage(newImageArray[j+1]));
			 }
		 }
		 ImageUtils.ShowBufferedImage(FINAL, "Stitch");
		 System.out.println("Finished!!Taka");
		 
		//output final image
	        File fi=new File("C:/Users/takan/workspace/Panorama1/Panorama1/final.jpg");
	        try {
				ImageIO.write(FINAL, "jpg", fi);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

}
