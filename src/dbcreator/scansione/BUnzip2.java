package dbcreator.scansione;
import java.io.BufferedInputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import dbcreator.main.formdicaricamento;

import org.apache.tools.ant.BuildException;
import org.apache.tools.bzip2.CBZip2InputStream;
import org.xml.sax.InputSource;


import java.io.*;

public class BUnzip2 extends Thread {
	String filename="d";
	int cont=1;
	Long nodes=new Long(0);
	Long ways=new Long(0);
	Long relations=new Long(0);
	ScanXMLThread scan=null;
	Thread tScan;
	
	private String RelativePath="";
	//private formdicaricamento frm=new formdicaricamento();
	private formdicaricamento frm=null;
	public BUnzip2(String path){
		RelativePath=path;
		frm=null;
	}
	
	public BUnzip2(String path,formdicaricamento form){
    	RelativePath=path;
    	frm=form;
	}
    public void run(){
    	
    	
    	File f=new File(RelativePath);
//    	String nameFile=f.getName().substring(0,f.getName().length()-4);
    	boolean trovato=false;
 
    	CBZip2InputStream zIn = null;
    	
        InputStream fis = null;
        BufferedInputStream bis = null;
//        int conts = 0;
        try {
//        	String path=f.getParent()+ File.separator + nameFile+ "_part"+ conts+".osm";
        	
            fis = new FileInputStream(RelativePath);
            bis = new BufferedInputStream(fis);
            int b = bis.read();
            
            if (b != 'B') {
                throw new BuildException("Invalid bz2 file.");
            }
            b = bis.read();
            if (b != 'Z') {
                throw new BuildException("Invalid bz2 file.");
            }
            
            zIn = new CBZip2InputStream(bis);
           
            int count = -1;
            
            int c=0;
            float contatore=fis.available();
            float massimovalore=contatore;
            
           
            PipedOutputStream pout1 = new PipedOutputStream();
		      PipedInputStream pin1 = new PipedInputStream(pout1);
		      InputSource source = new InputSource(pin1);
		      source.setEncoding("UTF-8");
		      Writer out = new OutputStreamWriter(pout1, "UTF8");
		      scan=new ScanXMLThread(this,source,f.getParent(),nodes,ways,false);
          	tScan=new Thread(scan);
          	tScan.setName("Scansione osm");
          	tScan.start();
          	byte buffer[]=new byte[640*1024];
          	count = zIn.read(buffer,0,buffer.length);
          	String st;
          	BufferedInputStream bin=new BufferedInputStream(pin1);
          
//          	OutputStreamWriter outs = 
//      	      new OutputStreamWriter(new FileOutputStream(""), "UTF-8");
    		
            do {
            		st=new String(buffer,0,count,"UTF-8");
            		if((count!=-1)&&((c%10==0)||(trovato))){
            			int index;
            			index = st.lastIndexOf("</relation>");
                    	if(index!=-1){
                    		out.write(st.substring(0, index+11));
//                    		outs.write(st.substring(0, index+11));
//                    		outs.write("---------------------------------------------------------------------------");
//                    		outs.flush(); 
                    		out.flush();
                    		
                    		 
                     		 c=0;
                     		if(index+11<st.length()){
                     			out.write(st.substring(index+11, st.length()));
//                         		outs.write(st.substring(index+11, st.length()));
//                         		outs.flush();
                     		}
                     		 
                     		
                     		trovato=false;
                    	}else{
                    		index = st.lastIndexOf("</way>");
                        	if(index!=-1){
                        		out.write(st.substring(0, index+6));
//                        		outs.write(st.substring(0, index+6));
//                        		outs.write("---------------------------------------------------------------------------");
//                        		outs.flush(); 
                        		out.flush();
                        		 
                         		 c=0;
                         		if(index+6<st.length()){
                         			 out.write(st.substring(index+6, st.length()));
//                              		outs.write(st.substring(index+6, st.length()));
//                              		outs.flush();	
                         		}
                         		
                         		trovato=false;
                        	}else{
                        		 index = st.lastIndexOf("</node>");
                                 if(index!=-1){
              
                                 		 out.write(st.substring(0, index+7));
//                                 		outs.write(st.substring(0, index+7));
//                                 		outs.write("---------------------------------------------------------------------------");
//                                 		outs.flush();
                                 		 out.flush();
                                 		
                                 		 c=0;
                                 		 if(index+7<st.length()){
                                 			out.write(st.substring(index+7, st.length()));
//                                     		outs.write(st.substring(index+7, st.length()));
//                                     		outs.flush();
                                 		 }
                                 		 
                                 		trovato=false;
                                 }else{
                                	 out.write(st); 
//                                	 outs.write(st); 
                                	trovato=true;
                                 }
                                	 
                        	}
                        		
                    	}
            		}else{
            			out.write(st);
//            			outs.write(st);
            		}
            		
            		
                if(contatore!=0 && frm!=null){
                	try{
                		contatore=fis.available();
                		frm.put((int)(100-(100*(contatore)/massimovalore)));
                	}
                	catch (Exception e) {
						// TODO: handle exception
					}
                }
                
                count = zIn.read(buffer,0,buffer.length);
                
                c+=1;
              
            } while (count != -1);
            
        	out.close();
        	
        } catch (IOException ioe) {
            //String msg = "Problem expanding bzip2 " + ioe.getMessage();
            //throw new BuildException(msg, ioe);
        } finally {
        }
       
       
        if(tScan!=null){
    		try{
    	      tScan.join();
    		}
    	    catch (InterruptedException ioe) {}
    	}     
   
	if (frm!=null){
        frm.put(100);
        //eliminafile(f.getParent());
        frm.finito();
	}

    }
    public void eliminafile(String p){
     File fs1=new File(p+"node.db");
     fs1.delete();
   	 File fs2=new File(p+"node.lg");
   	 fs2.delete();
   	 
   	 File fs3=new File(p+"way.db");
   	 fs3.delete();
   	 File fs4=new File(p+"way.lg");
   	 fs4.delete();
   	File fs5=new File(p+"Way-LonMax.lg");
	fs5.delete();
	File fs6=new File(p+"Way-LonMax.db");
	fs6.delete();
	
	File fs7=new File(p+"Way-LonMin.lg");
	fs7.delete();
	File fs8=new File(p+"Way-LonMin.db");
	fs8.delete();
	
	File fs9=new File(p+"Way-LatMin.lg");
	fs9.delete();
	File fs10=new File(p+"Way-LatMin.db");
	fs10.delete();
	
	File fs11=new File(p+"Way-LatMax.lg");
	fs11.delete();
	File fs12=new File(p+"Way-LatMax.db");
	fs12.delete();
   	 
	File fs13=new File(p+"node-lon.lg");
	fs13.delete();
	File fs14=new File(p+"node-lon.db");
	fs14.delete();
	File fs15=new File(p+"node-lat.lg");
	fs15.delete();
	File fs16=new File(p+"node-lat.db");
	fs16.delete(); 
   	 
    }
    
    public void start(){
	
}
}