package principale.main;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

public class avviocaricamento_gazetteerpop extends Thread{

	/**
	 * @param args
	 */
	String path;
	public void run() {
		// TODO Auto-generated method stub
        formdicaricamento frame2 = new formdicaricamento();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize2 = frame2.getSize();
        if (frameSize2.height > screenSize.height) {
            frameSize2.height = screenSize.height;
        }
        if (frameSize2.width > screenSize.width) {
            frameSize2.width = screenSize.width;
        }
        frame2.setLocation( ( screenSize.width - frameSize2.width ) / 2, ( screenSize.height - frameSize2.height ) / 2 );
        frame2.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE);
        frame2.setVisible(true);
		
		
        readpopulation r=new readpopulation(frame2);
        try{
        r.carica(path);//occhio che il file dev'essere szippato manualmente (TODO correggere?)
        }
        catch (Exception e) {
			// TODO: handle exception
		}
                
	}
	public void setpath(String path){
		this.path=path;
	}

}
