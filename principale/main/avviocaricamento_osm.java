package principale.main;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

import principale.scansione.BUnzip2;
import principale.scansione.ScanXML;

public class avviocaricamento_osm extends Thread{
	String path[]=null;
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
        
    	long Tempo1=System.currentTimeMillis();
		//System.out.println(path[0].substring(path[0].length()-3,path[0].length() ));
    	if(path[0].substring(path[0].length()-3,path[0].length()).equalsIgnoreCase("bz2")){
    		BUnzip2 b=new BUnzip2(path[0],frame2);
    		Thread g=new Thread(b);
    		g.setName("Scompatta");
        	g.start();
        	try{
        	g.join();
        	}
        	catch (Exception e) {
				// TODO: handle exception
			}
    	}
    	if(path[0].substring(path[0].length()-3,path[0].length()).equalsIgnoreCase("osm")){
        	sistemaxml a=new sistemaxml();
        	a.sistema(path[0],path[0]+"a");

    		ScanXML parsing=new ScanXML(path[0]+"a",0L,0L,true,true);
    		
    		frame2.put(100);
    		frame2.finito();
    	}		
    	long Tempo2=System.currentTimeMillis();
//        ScanXML parsing =(new ScanXML(frame2));
        System.out.println("Il tempo impiegato è : "+((Tempo2-Tempo1)/60000)+" minuti");
//        parsing.main(path);
        
	}
	public void setpath(String path[]){
		this.path=path;
	}

}
