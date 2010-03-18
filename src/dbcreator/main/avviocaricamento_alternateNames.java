package dbcreator.main;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;


public class avviocaricamento_alternateNames extends Thread{

	/**
	 * @param args
	 */
	String path;
	public void run() {
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
		
		//System.out.println(" Ci" +path[0]+" ");
        
        read_alternateNames r=new read_alternateNames(frame2);
        try{
        r.carica(path);
        }
        catch (Exception e) {
			// TODO: handle exception
		}
        //-------------QUERY-----------------------------
//        File fl=new File(path[0]);
        
//        String arg[]={fl.getParent()};
//    	long Tempo1=System.currentTimeMillis();
//    	RTreeQuery qu=new RTreeQuery(arg,"iseo");
//    	long Tempo2=System.currentTimeMillis();
//      System.out.println("Il tempo impiegato � : "+(Tempo2-Tempo1));
        
        
	}
	public void setpath(String path){
		this.path=path;
	}
}
