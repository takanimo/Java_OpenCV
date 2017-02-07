package guiRelated;

import java.awt.Component;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class GUI1 {

	private JFrame frmMultidimentionStitcher;
	private JTextField txtPath;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI1 window = new GUI1();
					window.frmMultidimentionStitcher.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GUI1() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmMultidimentionStitcher = new JFrame();
		frmMultidimentionStitcher.setTitle("Multi-dimention Stitcher");
		frmMultidimentionStitcher.setBounds(100, 100, 838, 467);
		frmMultidimentionStitcher.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmMultidimentionStitcher.getContentPane().setLayout(null);
		
		JLabel lblStepChooseOriginal = new JLabel("Step1: Choose original media type");
		lblStepChooseOriginal.setBounds(104, 76, 228, 16);
		frmMultidimentionStitcher.getContentPane().add(lblStepChooseOriginal);
		
		JLabel lblStepCheckMedia = new JLabel("Step2: Check media path");
		lblStepCheckMedia.setBounds(104, 173, 201, 16);
		frmMultidimentionStitcher.getContentPane().add(lblStepCheckMedia);
		
		JLabel lblStepClickStart = new JLabel("Step3: Click Start");
		lblStepClickStart.setBounds(104, 272, 144, 16);
		frmMultidimentionStitcher.getContentPane().add(lblStepClickStart);
		
		JButton btnVideo = new JButton("Video");
		btnVideo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser=new JFileChooser();
				int selected=fileChooser.showOpenDialog((Component)arg0.getSource());
				if(selected==JFileChooser.APPROVE_OPTION){
					String filename1=fileChooser.getSelectedFile().getAbsolutePath();
					String replacesymbol=filename1.replace('\\', '/');
					txtPath.setText("");    
					txtPath.setText(replacesymbol);

				}
				
			}
		});
		btnVideo.setBounds(375, 72, 97, 25);
		frmMultidimentionStitcher.getContentPane().add(btnVideo);
		
		JButton btnPhotos = new JButton("Photo");
		btnPhotos.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser=new JFileChooser();
				int selected=fileChooser.showOpenDialog((Component)arg0.getSource());
				if(selected==JFileChooser.APPROVE_OPTION){
					String filename1=fileChooser.getSelectedFile().getAbsolutePath();
					String replacesymbol=filename1.replace('\\', '/');
					txtPath.setText("");    
					txtPath.setText(replacesymbol);

				}
			}
		});
		btnPhotos.setBounds(564, 72, 97, 25);
		frmMultidimentionStitcher.getContentPane().add(btnPhotos);
		
		txtPath = new JTextField();
		txtPath.setBounds(317, 170, 475, 22);
		frmMultidimentionStitcher.getContentPane().add(txtPath);
		txtPath.setColumns(10);
		
		JButton btnStart = new JButton("Start");
		btnStart.setBounds(470, 268, 97, 25);
		frmMultidimentionStitcher.getContentPane().add(btnStart);
	}
}
