package taka;

import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class ImageUtils {

    public static void ShowMatImage(Mat mat, String title) {
        BufferedImage img = ImageUtils.Mat2BufferedImage(mat);//move the image to memory
        ImageIcon icon = new ImageIcon(img);//for showing image.
        JFrame frame = new JFrame(title);//frame for the window
        frame.setLayout(new FlowLayout());//layout
        frame.setSize(img.getWidth(null) + 30,//frame size.
                img.getHeight(null) + 50);
        JLabel lbl = new JLabel();
        lbl.setIcon(icon);//put image in icontype to label to show.
        frame.add(lbl);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void ShowBufferedImage(BufferedImage img, String title) {//for showing final output
        ImageIcon icon = new ImageIcon(img);
        JFrame frame = new JFrame(title);
        frame.setLayout(new FlowLayout());
        frame.setSize(img.getWidth(null) + 30,
                img.getHeight(null) + 50);
        JLabel lbl = new JLabel();
        lbl.setIcon(icon);
        frame.add(lbl);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /**
     * @param mat the Mat format image to be converted to BufferedImage type
     */
    public static BufferedImage Mat2BufferedImage(Mat mat) {

        byte[] data = new byte[mat.rows() * mat.cols() * (int) (mat.elemSize())];
        mat.get(0, 0, data);//convertin the mat data into a primitive java type 
        if (mat.channels() == 3) {
            for (int i = 0; i < data.length; i += 3) {
                byte temp = data[i];
                data[i] = data[i + 2];
                data[i + 2] = temp;
            }
        }
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), BufferedImage.TYPE_3BYTE_BGR);
        image.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), data);
        
        return image;
//-------------------------------------------------------------------------------------------------------------------        
//        int type = BufferedImage.TYPE_BYTE_GRAY;
//        if (mat.channels() > 1) {
//            type = BufferedImage.TYPE_3BYTE_BGR;
//        }
//        int bufferSize = mat.channels() * mat.cols() * mat.rows();
//        byte[] b = new byte[bufferSize];
//        mat.get(0, 0, b);
//        BufferedImage bufferedImage = new BufferedImage(mat.cols(), mat.rows(), type);
//        final byte[] targetPixels = ((DataBufferByte) bufferedImage.getRaster()
//                .getDataBuffer()).getData();
//        System.arraycopy(b, 0, targetPixels, 0, b.length);
//        return bufferedImage;
    }
    
    /**
     * @param img the BufferedImage format image to be converted to Mat type
     */
    public static Mat BufferedImage2Mat(BufferedImage img) {
       
        byte[] data = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(img.getHeight(), img.getWidth(), CvType.CV_8UC3); // CV_8UC3
        mat.put(0, 0, data);//convertin the mat data into a primitive java type 

        return mat;
    }

}
