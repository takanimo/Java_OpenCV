package guiRelated;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class MainGUI extends JFrame implements ActionListener{
	JLabel lbloutImage;
	JTextArea absoPathshow;
	public static void main(String[] args) {
		MainGUI mainGUI=new MainGUI();
		mainGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainGUI.setBounds(700,200,600,400);
		mainGUI.setTitle("Multi-media/multi-direction stitching application");
		mainGUI.setVisible(true);
	}
	
	MainGUI(){
		JPanel mainPanel=new JPanel(new BorderLayout());
		JPanel textPanel=new JPanel(new FlowLayout(FlowLayout.CENTER));//toppanel
		JPanel btnPanel=new JPanel(new FlowLayout(FlowLayout.CENTER));//middle panel
		JPanel btnStartP=new JPanel(new FlowLayout(FlowLayout.CENTER));//bottom panel
	//prepare Components for adding to panels
		JLabel description2=new JLabel("2. Path for the media shows up: ");
		absoPathshow=new JTextArea();
		JLabel description1=new JLabel("1. Select File for stitching: ");
		JButton btnVideo=new JButton("Select video");
		JButton btnImage=new JButton("Select image");
		JLabel description3=new JLabel("3. Click Start: ");
		JButton btnStart=new JButton("START");
		
	//top panel include
		textPanel.add(description2);
		textPanel.add(absoPathshow);
		textPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
	//bottom panel include
		btnPanel.add(description1);
		btnPanel.add(btnImage);
		btnPanel.add(btnVideo);
		btnPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
	//middle panel include
		btnStartP.add(description3);
		btnStartP.add(btnStart);
		btnStartP.setBorder(BorderFactory.createLineBorder(Color.BLUE));
		
	//add all panel to main panel
		mainPanel.add(textPanel,BorderLayout.NORTH);
		mainPanel.add(btnStartP,BorderLayout.CENTER);
		mainPanel.add(btnPanel,BorderLayout.SOUTH);
	//actions for buttons
		btnVideo.addActionListener(this);
		btnImage.addActionListener(this);
		btnStart.addActionListener(this);
	//add panel to contentpane
		getContentPane().add(mainPanel);		
		
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		//if()
		JFileChooser fileChooser=new JFileChooser();
		int selected=fileChooser.showOpenDialog(this);
		if(selected==JFileChooser.APPROVE_OPTION){
			String filename1=fileChooser.getSelectedFile().getAbsolutePath();
			String replacesymbol=filename1.replace('\\', '/');
			absoPathshow.setText("");    
			absoPathshow.append(replacesymbol);

		}
	}
	
}
