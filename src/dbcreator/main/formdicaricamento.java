/*
 * Created by JFormDesigner on Tue Apr 21 22:12:52 CEST 2009
 */

package dbcreator.main;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * @author marco marco
 */
public class formdicaricamento extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public formdicaricamento() {
		initComponents();
	}
	public synchronized void put(int valore) {
		if(valore<=100)
		progresso.setValue(valore);
	} 
	public void finito(){
		this.dispose();
	}

	private void thisWindowClosing(WindowEvent e) {
	}

	
	
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner Evaluation license - Marc marco
		progresso = new JProgressBar();
		label1 = new JLabel();

		//======== this ========
		setTitle("Caricamento");
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setResizable(false);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				thisWindowClosing(e);
			}
		});
		Container contentPane = getContentPane();

		//---- progresso ----
		progresso.setStringPainted(true);

		//---- label1 ----
		label1.setText("Avanzameno operazione");
		label1.setFont(new Font("Arial", Font.BOLD, 14));

		GroupLayout contentPaneLayout = new GroupLayout(contentPane);
		contentPane.setLayout(contentPaneLayout);
		contentPaneLayout.setHorizontalGroup(
			contentPaneLayout.createParallelGroup()
				.addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
					.addContainerGap(106, Short.MAX_VALUE)
					.addComponent(label1, GroupLayout.PREFERRED_SIZE, 185, GroupLayout.PREFERRED_SIZE)
					.addGap(101, 101, 101))
				.addGroup(contentPaneLayout.createSequentialGroup()
					.addGap(83, 83, 83)
					.addComponent(progresso, GroupLayout.PREFERRED_SIZE, 226, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(83, Short.MAX_VALUE))
		);
		contentPaneLayout.setVerticalGroup(
			contentPaneLayout.createParallelGroup()
				.addGroup(contentPaneLayout.createSequentialGroup()
					.addGap(74, 74, 74)
					.addComponent(label1, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
					.addGap(18, 18, 18)
					.addComponent(progresso, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(89, Short.MAX_VALUE))
		);
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - Marc marco
	private JProgressBar progresso;
	private JLabel label1;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
