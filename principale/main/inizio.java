package principale.main;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

public class inizio {
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		menu frame = new menu();
		
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = frame.getSize();
		
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        frame.setLocation( ( screenSize.width - frameSize.width ) / 2, ( screenSize.height - frameSize.height ) / 2 );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.setVisible(true);

		
	}
}
