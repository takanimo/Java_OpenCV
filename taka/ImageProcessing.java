package taka;

import java.awt.Color;
import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_BYTE_BINARY;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.imageio.ImageIO;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
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
//import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ImageProcessing {

    FeatureDetector surfDetector;
    DescriptorExtractor surfExtractor;
    Mat image1, descriptors;
    MatOfKeyPoint keyPoint01;
    MatOfKeyPoint keyPoint02 = new MatOfKeyPoint();
    BufferedImage[] images = null;

    public ImageProcessing() {

    }

    public static BufferedImage ImageStitching(BufferedImage img1, BufferedImage img2) {

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//CREATING THE FEATURES DETECTOR NEEDED FOT THE STITCH ALGORITHM
        FeatureDetector surfDetector = FeatureDetector.create(FeatureDetector.SURF);
        DescriptorExtractor surfExtractor = DescriptorExtractor.create(DescriptorExtractor.SURF);
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);

//CREATING THE VARIABLES TO BE MANIPULATE THE IMAGES
        Mat image1 = ImageUtils.BufferedImage2Mat(img1);
        Mat image2 = ImageUtils.BufferedImage2Mat(img2);
        Mat descriptors1 = new Mat(image1.rows(), image1.cols(), image1.type());
        Mat descriptors2 = new Mat(image2.rows(), image2.cols(), image2.type());
        MatOfDMatch matchs = new MatOfDMatch();
        BufferedImage finalImage = null;

//KEYPOINTS OF EACH IMAGE
        MatOfKeyPoint keyPoint01 = new MatOfKeyPoint();
        surfDetector.detect(image1, keyPoint01);
        MatOfKeyPoint keyPoint02 = new MatOfKeyPoint();
        surfDetector.detect(image2, keyPoint02);
//EXTRACT KEYPOINTS AND PUT INTO THE DESCRIPTORS
        surfExtractor.compute(image1, keyPoint01, descriptors1);
        surfExtractor.compute(image2, keyPoint02, descriptors2);
//MATCH THE POINTS OF THE TWO IMAGES
        matcher.match(descriptors1, descriptors2, matchs);

        List<DMatch> matchesList = matchs.toList();
//REFERENCE DISTANCE FOR THE GOOD POINTS
        Double max_dist = 0.0;
        Double min_dist = 10.0;

        for (int i = 0; i < matchesList.size(); i++) {
            Double dist = (double) matchesList.get(i).distance;
            if (dist < min_dist) {
                min_dist = dist;
            }
            if (dist > max_dist) {
                max_dist = dist;
            }
        }
//LIST OF THE GOOD MATCHES
        LinkedList<DMatch> good_matches = new LinkedList<DMatch>();
        MatOfDMatch gm = new MatOfDMatch();

        for (int i = 0; i < matchesList.size(); i++) {
            if (matchesList.get(i).distance < (3 * min_dist)) {
                good_matches.addLast(matchesList.get(i));
            }
        }

        gm.fromList(good_matches);
//DRAW THE GOOD MATCHES 
        Mat img_matches = new Mat();
        Features2d.drawMatches(image1, keyPoint01, image2, keyPoint02, gm, img_matches, new Scalar(255, 0, 0),
                new Scalar(0, 0, 255), new MatOfByte(), 2);
       // Core.circle(img_matches, new Point(img_matches.width()*0.5, img_matches.height()*0.5), 40,new Scalar(0,255,0),Core.FILLED);
	    
        Highgui.imwrite("DETECTION.JPG", img_matches);
        LinkedList<Point> objList = new LinkedList<Point>();
        LinkedList<Point> sceneList = new LinkedList<Point>();

//Draw centroid (of 2 images)
        Mat img_matches2 = new Mat();
        Features2d.drawMatches(image1, keyPoint01, image2, keyPoint02, gm, img_matches2, new Scalar(255, 0, 0),
                new Scalar(0, 0, 255), new MatOfByte(), 2);
        Core.circle(img_matches2, new Point(img_matches2.width()*0.5, img_matches2.height()*0.5), 40,new Scalar(0,255,0),Core.FILLED);
	    
        Highgui.imwrite("Centroid.jpg", img_matches2);
        //LinkedList<Point> objList = new LinkedList<Point>();
        //LinkedList<Point> sceneList = new LinkedList<Point>();
        
        
        
        
//LIST WITH THE SCENE AND OBJECT KEYPOINTS 
        List<KeyPoint> keypoints_objectList = keyPoint01.toList();
        List<KeyPoint> keypoints_sceneList = keyPoint02.toList();

        for (int i = 0; i < good_matches.size(); i++) {
            sceneList.addLast(keypoints_objectList.get(good_matches.get(i).queryIdx).pt);
            objList.addLast(keypoints_sceneList.get(good_matches.get(i).trainIdx).pt);
        }

        MatOfPoint2f obj = new MatOfPoint2f();
        obj.fromList(objList);

        MatOfPoint2f scene = new MatOfPoint2f();
        scene.fromList(sceneList);
//HOMOGRAPHY
//        Mat H = Calib3d.findHomography(obj, scene);
        Mat H = Calib3d.findHomography(obj, scene, Calib3d.RANSAC, 5);
        
        
        Mat warpimg = image1.clone();
        Size ims = new Size(image1.cols(), image1.rows());
        Imgproc.warpPerspective(image2, warpimg, H, ims);

        BufferedImage buff = ImageUtils.Mat2BufferedImage(warpimg);
        int counter, aux, height = -1;
        for (int rows = 0; rows < buff.getHeight(); rows++) {
            aux = -1;
            counter = -1;
            height++;
            for (int cols = 0; cols < buff.getWidth(); cols++) {
                Color color = new Color(buff.getRGB(cols, rows));
                int rgb = color.getRGB();
                if (rgb != aux) {
                    counter++;
                    aux = rgb;
                }
            }
            if (counter == 0) {
                break;
            }
        }

        Rect rec = new Rect(0,0,image1.width(), height);
        //Rect rec = new Rect(0,0,image1.width(), image1.height());
        Mat intersection = new Mat(warpimg, rec);
        ImageUtils.ShowMatImage(intersection, "INTERSECTION");
        Point p1 = new Point(0, height);
        //Point p1 = new Point(0, image1.height());
        Point p2 = new Point(image1.width(), image1.height());
        Rect newRec = new Rect(p1, p2);
        Mat cropped = new Mat(image1, newRec);
        ImageUtils.ShowMatImage(cropped, "NEWIMAGE");

//CONCATENATING THE NEW IMAGES
        BufferedImage buffImages0 = ImageUtils.Mat2BufferedImage(image2); // TOP IMAGE
        BufferedImage buffImages1 = ImageUtils.Mat2BufferedImage(cropped);// NEW BOTTOM IMAGE

        int actualROW = 0, AuX;
        finalImage = new BufferedImage(buffImages0.getWidth(), buffImages0.getHeight() + buffImages1.getHeight(), BufferedImage.TYPE_3BYTE_BGR);

        for (int rows1 = 0; rows1 < buffImages0.getHeight(); rows1++) {
            for (int cols1 = 0; cols1 < buffImages0.getWidth(); cols1++) {
                Color color = new Color(buffImages0.getRGB(cols1, rows1));
                int rgb = color.getRGB();
                finalImage.setRGB(cols1, rows1, rgb);
            }
            actualROW++;
        }
        for (int rows2 = 0; rows2 < buffImages1.getHeight(); rows2++) {
            for (int cols2 = 0; cols2 < buffImages1.getWidth(); cols2++) {
                Color color = new Color(buffImages1.getRGB(cols2, rows2));
                int rgb = color.getRGB();
                AuX = rows2 + actualROW;
                finalImage.setRGB(cols2, AuX, rgb);
            }
        }

        ImageUtils.ShowBufferedImage(finalImage, "FINAL");
        return finalImage;
    }

//----------------------------------------------------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------------------------------------------------
    
    
    
    public static BufferedImage PanoramicStitch(BufferedImage img1, BufferedImage img2) throws IOException {
//LOADING THE LATEST OPENCV LIBRARY INSTALLED
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
      
//CREATING THE FEATURES DETECTOR NEEDED FOT THE STITCH ALGORITHM
        FeatureDetector surfDetector = FeatureDetector.create(FeatureDetector.SURF);
        DescriptorExtractor surfExtractor = DescriptorExtractor.create(DescriptorExtractor.SURF);
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);

//CREATING THE VARIABLES TO BE MANIPULATE THE IMAGES
        Mat image1 = ImageUtils.BufferedImage2Mat(img1);
        Mat image2 = ImageUtils.BufferedImage2Mat(img2);
        Mat descriptors1 = new Mat(image1.rows(), image1.cols(), image1.type());
        Mat descriptors2 = new Mat(image2.rows(), image2.cols(), image2.type());
        MatOfDMatch matchs = new MatOfDMatch();
        BufferedImage finalImage = null;
        
////CHANGING TO GRAYSCALE
//        Imgproc.cvtColor(image1, image1, Imgproc.COLOR_RGB2GRAY);
//        Imgproc.cvtColor(image2, image2, Imgproc.COLOR_RGB2GRAY);
        
//KEYPOINTS OF EACH IMAGE
        MatOfKeyPoint keyPoint01 = new MatOfKeyPoint();
        surfDetector.detect(image1, keyPoint01);
        MatOfKeyPoint keyPoint02 = new MatOfKeyPoint();
        surfDetector.detect(image2, keyPoint02);
//EXTRACT KEYPOINTS AND PUT INTO THE DESCRIPTORS
        surfExtractor.compute(image1, keyPoint01, descriptors1);
        surfExtractor.compute(image2, keyPoint02, descriptors2);
//MATCH THE POINTS OF THE TWO IMAGES
        matcher.match(descriptors1, descriptors2, matchs);

        List<DMatch> matchesList = matchs.toList();
//REFERENCE DISTANCE FOR THE GOOD POINTS
        Double max_dist = 0.0;
        Double min_dist = 100.0;

        for (int i = 0; i < matchesList.size(); i++) {
            Double dist = (double) matchesList.get(i).distance;
            if (dist < min_dist) {
                min_dist = dist;
            }
            if (dist > max_dist) {
                max_dist = dist;
            }
        }
//LIST OF THE GOOD MATCHES
        LinkedList<DMatch> good_matches = new LinkedList<DMatch>();
        MatOfDMatch gm = new MatOfDMatch();

        for (int i = 0; i < matchesList.size(); i++) {
            if (matchesList.get(i).distance < (3 * min_dist)) {
                good_matches.addLast(matchesList.get(i));
            }
        }

        gm.fromList(good_matches);
//DRAW THE GOOD MATCHES
        Mat img_matches = new Mat();
        Features2d.drawMatches(image1, keyPoint01, image2, keyPoint02, gm, img_matches, new Scalar(255, 0, 0),
                new Scalar(0, 0, 255), new MatOfByte(), 2);

        Highgui.imwrite("DETECTION.JPG", img_matches);
        LinkedList<Point> objList = new LinkedList<Point>();
        LinkedList<Point> sceneList = new LinkedList<Point>();

//LIST WITH THE SCENE AND OBJECT KEYPOINTS 
        List<KeyPoint> keypoints_objectList = keyPoint01.toList();
        List<KeyPoint> keypoints_sceneList = keyPoint02.toList();

        for (int i = 0; i < good_matches.size(); i++) {
            sceneList.addLast(keypoints_objectList.get(good_matches.get(i).queryIdx).pt);
            objList.addLast(keypoints_sceneList.get(good_matches.get(i).trainIdx).pt);
        }

        MatOfPoint2f obj = new MatOfPoint2f();
        obj.fromList(objList);

        MatOfPoint2f scene = new MatOfPoint2f();
        scene.fromList(sceneList);
//HOMOGRAPHY
        Mat H = Calib3d.findHomography(obj, scene);

        Mat obj_corners = new Mat(4, 1, CvType.CV_32FC2);
        Mat scene_corners = new Mat(4, 1, CvType.CV_32FC2);

//IT SHOULD BE THE IMAGE THAT IS BEING CROPPED, BUT IT OLNY WORKS WITH THE IMAGE THAT IS NOT BEING CROPPED
        obj_corners.put(0, 0, new double[]{0, 0});
        obj_corners.put(1, 0, new double[]{image2.cols(), 0});
        obj_corners.put(2, 0, new double[]{image2.cols(), image2.rows()});
        obj_corners.put(3, 0, new double[]{0, image2.rows()});
//MAKE A MATRIX OF POINTS         
        Core.perspectiveTransform(obj_corners, scene_corners, H);

//READ THE FIRST IMAGE TO DO THE CROP
        Mat img = ImageUtils.BufferedImage2Mat(img1);  // SHOULD BE THE BOTTON ONE

//CALCULATES THE POINTS OF THE IMAGE TO BE CROPPED
        Point a = new Point(scene_corners.get(0, 0));
        Point b = new Point(scene_corners.get(1, 0));
        Point c = new Point(scene_corners.get(2, 0));
        Point d = new Point(scene_corners.get(3, 0));
        
//DRAW THE LINES         
        Core.line(img, new Point(scene_corners.get(0, 0)), new Point(scene_corners.get(1, 0)), new Scalar(0, 255, 0), 4);
        Core.line(img, new Point(scene_corners.get(1, 0)), new Point(scene_corners.get(2, 0)), new Scalar(0, 255, 0), 4);
        Core.line(img, new Point(scene_corners.get(2, 0)), new Point(scene_corners.get(3, 0)), new Scalar(0, 255, 0), 4);
        Core.line(img, new Point(scene_corners.get(3, 0)), new Point(scene_corners.get(0, 0)), new Scalar(0, 255, 0), 4);
        ImageUtils.ShowMatImage(img, "Lines?");
        
//VALUES OF THE REC TO BE CROPPED
        int[] vals = recValues(a, b, c, d);
//CREATE THE REC WITH THE SPECIFIED VALUES
        Rect rec = new Rect(vals[0], vals[1], vals[2], vals[3]);
        System.out.println("-------------------------------- REC " + vals[0] + " " + vals[1] + " " + vals[2] + " " + vals[3] + " -----------------------------------------");
//CREATE THE CROPPED IMAGE WITH THE PART THAT BOTH IMAGES HAS
        Mat intersection = new Mat(img, rec);
        
        //Highgui.imwrite("DRONEINTERSECTION.JPG", intersection);
//CALCULATE THE POINTS OF THE COMPLEMENT REC
        Point edge1 = new Point(0, intersection.height());
        Point edge2 = new Point(image1.width(), image1.height());
//REC WITH THE COMPLEMENT VALUES OF THE OTHER REC, WHICH IS THE PART OF THE IMAGE THAT IS NOT EQUAL
        Rect rec1 = new Rect(edge1, edge2);
        Mat newImg2 = new Mat(image1, rec1);
        Highgui.imwrite("NEWIMAGE.JPG", newImg2);
//CONCATENATING THE NEW IMAGES

        BufferedImage buffImages0 = img2; // TOP IMAGE
        BufferedImage buffImages1 = ImageUtils.Mat2BufferedImage(newImg2);// NEW BOTTOM IMAGE

        int actualROW = 0, AuX, actualCol = buffImages0.getWidth();
        finalImage = new BufferedImage(buffImages0.getWidth() + buffImages1.getWidth(), buffImages0.getHeight(), BufferedImage.TYPE_3BYTE_BGR);

        for (int rows1 = 0; rows1 < buffImages0.getHeight(); rows1++) {
            for (int cols1 = 0; cols1 < buffImages0.getWidth(); cols1++) {
                Color color = new Color(buffImages0.getRGB(cols1, rows1));
                int rgb = color.getRGB();
                finalImage.setRGB(cols1, rows1, rgb);
            }
            actualROW++;
        }
        for (int rows2 = 0; rows2 < buffImages1.getHeight(); rows2++) {
            for (int cols2 = 0; cols2 < buffImages1.getWidth(); cols2++) {
                Color color = new Color(buffImages1.getRGB(cols2, rows2));
                int rgb = color.getRGB();
                AuX = rows2 + actualCol;
                finalImage.setRGB(cols2, AuX, rgb);
            }
        }

        //ImageUtils.ShowBufferedImage(finalImage, "FINAL");
        //File outputfile = new File("NEW_IMAGE.jpg");
        //ImageIO.write(finalImage, "jpg", outputfile);
        return finalImage;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        FeatureDetector surfDetector = FeatureDetector.create(FeatureDetector.SURF);
        DescriptorExtractor surfExtractor = DescriptorExtractor.create(DescriptorExtractor.SURF);
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);

        String file1 = "0.jpg";
        String file2 = "3.jpg";

        Mat image1 = Highgui.imread(file1);
        Mat image2 = Highgui.imread(file2);

        Mat descriptors1 = new Mat(image1.rows(), image1.cols(), image1.type());
        Mat descriptors2 = new Mat(image2.rows(), image2.cols(), image2.type());
        MatOfDMatch matchs = new MatOfDMatch();

        MatOfKeyPoint keyPoint01 = new MatOfKeyPoint();
        surfDetector.detect(image1, keyPoint01);
        MatOfKeyPoint keyPoint02 = new MatOfKeyPoint();
        surfDetector.detect(image2, keyPoint02);

        surfExtractor.compute(image1, keyPoint01, descriptors1);
        surfExtractor.compute(image2, keyPoint02, descriptors2);

        matcher.match(descriptors1, descriptors2, matchs);

//-------------------------------------------------------------------------------------------------------------------------------------------------------
        List<DMatch> matchesList = matchs.toList();

        Double max_dist = 0.0;
        Double min_dist = 100.0;

        for (int i = 0; i < matchesList.size(); i++) {
            Double dist = (double) matchesList.get(i).distance;
            if (dist < min_dist) {
                min_dist = dist;
            }
            if (dist > max_dist) {
                max_dist = dist;
            }
        }

        System.out.println("-- Max dist : " + max_dist);
        System.out.println("-- Min dist : " + min_dist);

        LinkedList<DMatch> good_matches = new LinkedList<DMatch>();
        MatOfDMatch gm = new MatOfDMatch();

        for (int i = 0; i < matchesList.size(); i++) {
            if (matchesList.get(i).distance < (3 * min_dist)) {
                good_matches.addLast(matchesList.get(i));
            }
        }

        gm.fromList(good_matches);

        Mat img_matches = new Mat();
        Features2d.drawMatches(image1, keyPoint01, image2, keyPoint02, gm, img_matches, new Scalar(255, 0, 0),
                new Scalar(0, 0, 255), new MatOfByte(), 2);

        Highgui.imwrite("DETECTION1.jpg", img_matches);

//------------------------------------------------------------------------------------------------------------------------------------------------       
        LinkedList<Point> objList = new LinkedList<Point>();
        LinkedList<Point> sceneList = new LinkedList<Point>();

        List<KeyPoint> keypoints_objectList = keyPoint01.toList();
        List<KeyPoint> keypoints_sceneList = keyPoint02.toList();

        for (int i = 0; i < good_matches.size(); i++) {
            sceneList.addLast(keypoints_objectList.get(good_matches.get(i).queryIdx).pt);
            objList.addLast(keypoints_sceneList.get(good_matches.get(i).trainIdx).pt);
        }

        MatOfPoint2f obj = new MatOfPoint2f();
        obj.fromList(objList);

        MatOfPoint2f scene = new MatOfPoint2f();
        scene.fromList(sceneList);

        Mat H = Calib3d.findHomography(obj, scene);

//--------------------------------------------------------------------------------------------------------------------------------------------------
        Mat obj_corners = new Mat(4, 1, CvType.CV_32FC2);
        Mat scene_corners = new Mat(4, 1, CvType.CV_32FC2);

// FIND OUT THE ERROR -------------------------------------------------------------------------------------------------------------------------------        
// IT SHOULD BE THE IMAGE THAT IS BEING CROPPED, BUT IT OLNY WORKS WITH THE IMAGE THAT IS NOT BEING CROPPED
        obj_corners.put(0, 0, new double[]{0, 0});
        obj_corners.put(1, 0, new double[]{image2.cols(), 0});
        obj_corners.put(2, 0, new double[]{image2.cols(), image2.rows()});
        obj_corners.put(3, 0, new double[]{0, image2.rows()});

        Core.perspectiveTransform(obj_corners, scene_corners, H);

//--------------------------------------------------------------------------------------------------------------------------------------------------
        //READ THE SECOND IMAGE TO DO THE CROP
        Mat img = Highgui.imread(file1, Highgui.CV_LOAD_IMAGE_COLOR);  // SHOULD BE THE BOTTON ONE

        //CALCULATES THE POINTS OF THE IMAGE TO BE CROPPED
        Point a = new Point(scene_corners.get(0, 0)); //System.out.println("P "+a.x+" "+a.y);
        Point b = new Point(scene_corners.get(1, 0)); //System.out.println("P "+b.x+" "+b.y);
        Point c = new Point(scene_corners.get(2, 0)); //System.out.println("P "+c.x+" "+c.y);
        Point d = new Point(scene_corners.get(3, 0)); //System.out.println("P "+d.x+" "+d.y);

        //DRAW THE LINES         
        Core.line(img, new Point(scene_corners.get(0, 0)), new Point(scene_corners.get(1, 0)), new Scalar(255, 0, 0), 4);
        Core.line(img, new Point(scene_corners.get(1, 0)), new Point(scene_corners.get(2, 0)), new Scalar(255, 0, 0), 4);
        Core.line(img, new Point(scene_corners.get(2, 0)), new Point(scene_corners.get(3, 0)), new Scalar(0, 255, 0), 4);
        Core.line(img, new Point(scene_corners.get(3, 0)), new Point(scene_corners.get(0, 0)), new Scalar(0, 255, 0), 4);
        Highgui.imwrite("REC.jpg", img);

        //VALUES OF THE REC OF THE IMAGE TO BE CROPPED 
        int[] vals = recValues(a, b, c, d);

        //CREATE THE REC WITH THE SPECIFIED VALUES
        Rect rec = new Rect(vals[0], vals[1], vals[2], vals[3]);
        System.out.println("FIRST REC: " + vals[0] + " " + vals[1] + " " + vals[2] + " " + vals[3] + " ");

        //CREATE THE CROPPED IMAGE WITH THE PART THAT BOTH IMAGES HAS
        Mat intersection = new Mat(img, rec);
        Highgui.imwrite("INTERSECTION.jpg", intersection);
        /*        
         Mat warpimg = image1.clone();
         Size ims = new Size(image1.cols(), image1.rows());
         Imgproc.warpPerspective(image1, warpimg, H, ims);
         Highgui.imwrite("WRAPING.jpg", warpimg);
         */

        //CALCULATE THE POINTS OF THE COMPLEMENT REC
        Point edge1 = new Point(0, intersection.height());
        System.out.println("NEW REC " + edge1.x + " " + edge1.y);
        Point edge2 = new Point(image1.width(), image1.height());
        System.out.println("NEW REC " + edge2.x + " " + edge2.y);

        //REC WITH THE COMPLEMENT VALUES OF THE OTHER REC, WHICH IS THE PART OF THE IMAGE THAT IS NOT EQUAL
        Rect rec1 = new Rect(edge1, edge2);
        Mat newImg2 = new Mat(image1, rec1);
        Highgui.imwrite("newImage2.jpg", newImg2);

//-------- CONCATENATING THE NEW IMAGES -----------------------------------------------------------------------------------------------------------------
        File imgFiles0 = new File(file2); // TOP IMAGE
        File imgFiles1 = new File("newImage2.jpg"); // NEW BOTTOM IMAGE

        BufferedImage buffImages0 = ImageIO.read(imgFiles0);
        BufferedImage buffImages1 = ImageIO.read(imgFiles1);

        int actualRow = 0, aux;
        BufferedImage finalImage = new BufferedImage(buffImages0.getWidth(), buffImages0.getHeight() + buffImages1.getHeight(), BufferedImage.TYPE_INT_RGB);

        for (int rows1 = 0; rows1 < buffImages0.getHeight(); rows1++) {
            for (int cols1 = 0; cols1 < buffImages0.getWidth(); cols1++) {
                Color color = new Color(buffImages0.getRGB(cols1, rows1));
                int rgb = color.getRGB();
                finalImage.setRGB(cols1, rows1, rgb);
            }
            actualRow++;
        }
        for (int rows2 = 0; rows2 < buffImages1.getHeight(); rows2++) {
            for (int cols2 = 0; cols2 < buffImages1.getWidth(); cols2++) {
                Color color = new Color(buffImages1.getRGB(cols2, rows2));
                int rgb = color.getRGB();
                aux = rows2 + actualRow;
                finalImage.setRGB(cols2, aux, rgb);
            }
        }

        File outputfile = new File("NEW_IMAGE.jpg");
        ImageIO.write(finalImage, "jpg", outputfile);

    }

    public static int[] recValues(Point a, Point b, Point c, Point d) {

        if (a.x < 0) {
            a.x = 0;
        }
        if (a.y < 0) {
            a.y = 0;
        }
        if (b.x < 0) {
            b.x = 0;
        }
        if (b.y < 0) {
            b.y = 0;
        }
        if (c.x < 0) {
            c.x = 0;
        }
        if (c.y < 0) {
            c.y = 0;
        }
        if (d.x < 0) {
            d.x = 0;
        }
        if (d.y < 0) {
            d.y = 0;
        }

        int[] vals = new int[]{ (int) a.x, (int) a.y, (int) (c.x - d.x) - 1, (int) (c.y - b.y)};
        return vals;
    }

}
