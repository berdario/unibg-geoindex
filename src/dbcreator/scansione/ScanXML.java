package dbcreator.scansione;


import java.io.File;
import java.io.IOException;
import java.util.Comparator;

import jdbm.RecordManager;

import jdbm.btree.BTree;

import dbcreator.main.formdicaricamento;

import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import java.io.*;


public class ScanXML implements ContentHandler {
	 private Double[] as1= new Double[2];
	 private Double[] as= new Double[4];; 
	 private Double coordLatMax;
	 private Double coordLatMin;
	 private String livello;
	 private Double coordLonMax;
	 private Double coordLonMin;
	 private Long indexw=new Long(0);
     private boolean closen=true;
     private boolean closew=true;
     private BTree tn=new BTree();
     private boolean relation=false;
     private boolean paese=false;
     private String rel="";
     private boolean admin=false;
     private BTree tw=new BTree();
     private RecordManager mydbn,mydbw;
     private Double latmin=200.0;
     private Double latmax=-200.0;
     private Double lonmin=200.0;
     private Double lonmax=-200.0;
     private Double coordLat=0D;
     private Double coordLon=0D;
     private Object ris=null;
     private Long value=0L;
     private String nomepaese="";
     private String relativePath="";
     private File f;
     private PrintStream out;
     private Long idex;
     private boolean delete;
	 Way w;
     node n;
     formdicaricamento frame;
     
     public ScanXML(){
    	 
     }
     public ScanXML(formdicaricamento frame){
    	this.frame=frame; 
     }
     
     public void Inizialize(){
    	    
    	    closen=true;
    		latmin=200.0;
    		latmax=-200.0;
    		lonmin=200.0;
    		lonmax=-200.0;
    		indexw=new Long(0);
    		closen=true;
    		closew=true;  
    		relation=false;
    		paese=false;
    		rel="";
    		admin=false;
    		idex=0L;
     }
     public void Delete(String p){
    	 File fs=new File(p);
    	 fs.delete();
    	 try{
    	 mydbn.close();
    	 mydbw.close();
    	 w.close();
    	 n.close();
    	 
    	 }
    	 catch (Exception e) {
		}
    	 //System.out.println("Ho cancellato il file "+p +" "+fs.delete());
     }
     public static synchronized BTree loadOrCreateBTree( RecordManager aRecordManager,String aName, Comparator aComparator ) throws IOException

 	{
 
 	  long recordID = aRecordManager.getNamedObject( aName );
 	  BTree tree = null;
 	  Serializ s=new Serializ();
 	  if ( recordID == 0 )
 	  {  
 	   
 	    tree = BTree.createInstance( aRecordManager, aComparator,s,s,1024);
 	    aRecordManager.setNamedObject( aName, tree.getRecid() );
 	   
 	  }
            else {
                try {
                    tree = BTree.load(aRecordManager, recordID);
                } catch (RuntimeException e) {
                    System.out.println("Error while opening the db, have you tried deleting the old one and then recreate it?");
                    System.exit(0);
                }

            }
 	  return tree;
 	} 

     public static synchronized void insert(BTree tree,long id,Long value){
    	 //Inserimento nell'albero di id-value
    	 try
    	 	{	
 		tree.insert(id, value, true); 
 		
 		
    	 	}	
	catch (java.io.IOException e)
	{	}
	
 	} 
     public static synchronized void insert(BTree tree,long id,Double value){
    	 //Inserimento nell'albero di id-value
    	 try
    	 	{	
 		tree.insert(id, value, true); 

    	 	}	
	catch (java.io.IOException e)
	{	}
	
 	}
     public static synchronized void insert(BTree tree,long id,String value){
    	 //Inserimento nell'albero di id-value
    	 try
    	 	{	
 		tree.insert(id, value, true); 

    	 	}	
	catch (java.io.IOException e)
	{	}
	
 	} 
     public synchronized void ScanXMLThreadInputStream(InputSource xml,String relativePath,Long nod,Long way,boolean threads,boolean delete) {
//    	 File fss=new File(xmlFile);
//    	 relativePath=fss.getParent();
    	 System.out.println("Entrato in ScanXMLThreadInputStream");
    	 long Tempo1=System.currentTimeMillis();
    	 this.delete=delete;
   	 //if((nod==0)&&(way==0)){
    	 //System.out.println("Entrato");
    		 w=new Way(relativePath) ;
    		 n=new node(relativePath);
    //	 }
    		 
    	 if(threads){
    			Inizialize();
    			n.setCont(nod);
    			w.setCont(way); 
    	 }
	
		//-----------------ALBERO NODI-----------------------------------------------
		Serial a=new Serial();
		try
	 	{	//APERTURA FILE
			f= new File(relativePath+File.separator+"coordinate.txt");
			FileOutputStream fos=new FileOutputStream(f,true);
			out=new PrintStream(fos);
	 		
//			mydbn = RecordManagerFactory.createRecordManager(relativePath+File.separator+"node", new Properties());
//	 		tn = loadOrCreateBTree(mydbn, "node-id", a );
	 		
	 	}
	 	catch (java.io.IOException e)
	 	{	}
	   //-----------------------ALBERO WAY--------------------------------------------//
//		try
//	 	{	mydbw = RecordManagerFactory.createRecordManager(relativePath+File.separator+"way", new Properties());
//	 		tw = loadOrCreateBTree(mydbw, "way-id", a );
//	 	}
//	 	catch (java.io.IOException e)
//	 	{	}
		//PARSING DEL FILE XML
	      SAXParser parser = new SAXParser();
	      
	     parser.setContentHandler(this);
	      
	      try {
	    	  System.out.println("Lancio scansione");
			parser.parse(xml);
		} catch (SAXException e) {
			e.printStackTrace();
			e.getLocalizedMessage();
		} catch (IOException e) {
			e.printStackTrace();
		}
	      	        System.out.println("Finito scansione");
	      	      long Tempo2=System.currentTimeMillis();
	      	      System.out.println("Il tempo impiegato è "+(Tempo2-Tempo1));
	if(threads){
	    commitNode();
	  	commitWay();
	  	out.close();
	  	//Delete(xmlFile);
	  	
	}

	}
     
     public synchronized void ScanXMLThread(String xmlFile,Long nod,Long way,boolean threads,boolean delete) {
    	 File fss=new File(xmlFile);
    	 relativePath=fss.getParent();
    	 this.delete=delete;
   	 //if((nod==0)&&(way==0)){
    	 //System.out.println("Entrato");
    		 w=new Way(relativePath) ;
    		 n=new node(relativePath);
    //	 }
    		 
    	 if(threads){
    			Inizialize();
    			n.setCont(nod);
    			w.setCont(way); 
    	 }
	
		//-----------------ALBERO NODI-----------------------------------------------
		Serial a=new Serial();
		try
	 	{	//APERTURA FILE
			f= new File(relativePath+File.separator+"coordinate.txt");
			FileOutputStream fos=new FileOutputStream(f,true);
			out=new PrintStream(fos);
	 		
//			mydbn = RecordManagerFactory.createRecordManager(relativePath+File.separator+"node", new Properties());
//	 		tn = loadOrCreateBTree(mydbn, "node-id", a );
	 		
	 	}
	 	catch (java.io.IOException e)
	 	{	}
	   //-----------------------ALBERO WAY--------------------------------------------//
//		try
//	 	{	
////			mydbw = RecordManagerFactory.createRecordManager(relativePath+File.separator+"way", new Properties());
////	 		tw = loadOrCreateBTree(mydbw, "way-id", a );
//	 	}
//	 	catch (java.io.IOException e)
//	 	{	}
		//PARSING DEL FILE XML
	      SAXParser parser = new SAXParser();
	      
	      parser.setContentHandler(this);
	      
	     
	      try {
	        System.out.println("Avvio il parsing del file "+xmlFile);
	    	  parser.parse(fss.getPath());
	    	  
	      } 
	      catch (SAXException e) {
	    	  
	          System.err.println (e);
	      } 
	      	          catch (IOException e) {
	    	  System.out.println("ERRORE");
	    	  System.err.println (e);
	      }
//	      catch (Exception e) {
//	    	  
//	          System.err.println (e);
//	      }
	      	        System.out.println("Finito scansione");
	if(threads){
	    commitNode();
	  	commitWay();
	  	out.close();
	  	//Delete(xmlFile);
	  	
	}

	}
     
     public synchronized void  commitNode(){
		
//	        	mydbn.commit();
	        	n.commit();
	  
	}
	
     public synchronized void  commitWay(){
	
			w.commit();
		
	 	
     }
     
     public  ScanXML (String xmlFile,Long nod,Long way,boolean threads,boolean delete) 
	{
    	ScanXMLThread(xmlFile, nod, way, threads,delete);
    	
		}
	
     public ScanXML(InputSource xml,String relativePath,Long nod,Long way,boolean threads,boolean delete){
    	 ScanXMLThreadInputStream(xml,relativePath,nod,way,threads,delete);
     }
     
      
     public static synchronized Object ricerca(Long el,BTree b){
		
		try {
			return b.find(el);
	      } catch (IOException e) {
	          System.err.println (e);
	      }
	      return null; 
	}
     
     public synchronized void StartElementThread(String uri, String local, String qName,Attributes atts) {
    
//    	--------NODO---------------------------------------------
 		if(local=="node") 
          { 
 			idex=n.add(Long.parseLong(atts.getValue("id")),Double.valueOf(atts.getValue("lat")),Double.valueOf(atts.getValue("lon")));
//            insert(tn,Long.parseLong(atts.getValue("id")),idex);
            
          
 		}
 		//..........................WAY---------------------------------------------------
 		if(local=="way")
 	      { if(closen){
 	    	  System.out.println("Ultimo commit per i nodi");
 	    	  closen=false;
 	    	  commitNode();
 	      }
 	      value=Long.parseLong(atts.getValue("id"));
 			indexw=w.add(Long.parseLong(atts.getValue("id")),latmin, latmax, lonmin, lonmax);
// 	      insert(tw,Long.parseLong(atts.getValue("id")),indexw);
 			
 		}
 		//..............................nd................................................
 		if(local=="nd"){
 			
 			/////DEVO TRADURRE L'ID IN LAT E LONGITUDINE
 			//System.out.println("Il codice da cercare è : "+atts.getValue("ref")+" il puntatore al nodo è "+tn);
// 			ris=ricerca(Long.parseLong(atts.getValue("ref")),n.g);
 			
 			as1=n.get(Long.parseLong(atts.getValue("ref")));
 			if(as1!=null){
// 			  value=Long.parseLong(ris.toString());
// 			  coordLat=n.getlat(value);
 			
 			  coordLon=as1[1];
 			  coordLat=as1[0];
 			  if(coordLon==null){
   				System.out.println("Elemento non trovato nell'albero dei nodi");
 			  }
 			  else{
   				
   			//--------------------------------------------
 				if(coordLat>latmax){
 					latmax=coordLat;
 					
 				}
 				if(coordLat<latmin){
 					latmin=coordLat;
 					
 				}
 				if(coordLon>lonmax){
 					lonmax=coordLon;
 					
 				}
 				if(coordLon<lonmin){
 					lonmin=coordLon;
 					
 				}
 			 }
 			
 			}
 			else{
 				//System.out.println("nodo non trovato...");
 				System.out.println(Long.parseLong(atts.getValue("ref")));
 			}
       }
 		//---------------------------Relation---------------------------------------------------
 		if(local=="relation")
 	      { if(closew){
 	    	  System.out.println("Ultimo commit per i way");
 	    	  closew=false;
 	    	  commitWay();
 	      }
 	      
 			relation=true;
 	        rel=atts.getValue("id");
 	      }
 		if((local=="tag")&&(relation)){
 			if(atts.getValue("k").equalsIgnoreCase("admin_level")){
 				//if(atts.getValue("v").equalsIgnoreCase("8") || atts.getValue("v").equalsIgnoreCase("7") || atts.getValue("v").equalsIgnoreCase("9") || atts.getValue("v").equalsIgnoreCase("10"))
 				{
 				admin=true;
 				livello=atts.getValue("v");
 				}
 			}
 			if(atts.getValue("k").equalsIgnoreCase("name")){
 				paese=true;
 				nomepaese=atts.getValue("v");
 			}
 		
 		}
 		if(local=="member"){
 			if(atts.getValue("type").equalsIgnoreCase("way")){
// 				ris=ricerca(Long.parseLong(atts.getValue("ref")),tw);
 				
 				as=w.get(Long.parseLong(atts.getValue("ref")));
 				
 				if((as==null)){
 					//System.out.println("Way presente nella relation ma non come elemento way "+Long.parseLong(atts.getValue("ref")));
 				}
 				else
 				{
 					coordLatMax=as[1];
 	 				
 					
 	 				coordLatMin=as[0];
 	 				coordLonMax=as[3];
 	 				coordLonMin=as[2];
 				
 	
 				//System.out.println("Le coordinate trovate sono : "+coordLatMax+" "+coordLonMax+" "+coordLonMax+" "+coordLonMin+" "+value);
 			
 				if(value==null){
 					System.out.println("Elemento non trovato nell'albero dei way");
 				}
 				else{
 					if(coordLatMax>latmax){
 						latmax=coordLatMax;
 					}
 					if(coordLatMin<latmin){
 						latmin=coordLatMin;
 					}
 					if(coordLonMax>lonmax){
 						lonmax=coordLonMax;
 					}
 					if(coordLonMin<lonmin){
 						lonmin=coordLonMin;
 					}
 				}
 			}
 			}
 	      }
	}
    
     public void startElement (String uri, String local, String qName,Attributes atts)  {
    	 
    	 StartElementThread(uri, local, qName, atts);
}
	 public synchronized void endElementThread(String uri, String local, String qName) {
		 if(local=="node"){
				if(n.size()%10000==0){
					commitNode();
			}
			}
			if(local=="way"){
				if(w.size()%10000==0){
					commitWay();
				}
				
				w.set(latmin, latmax, lonmin, lonmax,value);
				latmin=200.0;
				latmax=-200.0;
				lonmin=200.0;
				lonmax=-200.0;
				
			}
			if(local=="relation"){
			
			  if((relation)&&(admin)&&(paese)){
				out.print(livello);
				out.print('$');	
				out.print(nomepaese);
				out.print('$');
				out.print(rel);
			    out.print('$'); 
				out.print(latmax);
				out.print('$');
				out.print(lonmax);
				out.print('$');
				out.print(latmin);
				out.print('$');
				out.print(lonmin);
				out.print("\r\n");
				latmin=200.0;
				latmax=-200.0;
				lonmin=200.0;
				lonmax=-200.0;
				rel="";
				relation=false;
				admin=false;
				paese=false;
			  }
			}
			if(local=="osm"){
				out.close();
			}
	}
     public void endElement (String uri, String local, String qName){	
		endElementThread(uri, local, qName);
	  }

     public void setDocumentLocator(Locator locator) {}
     public void startDocument() {}
     public void endDocument() {	}
     public void characters(char[] text, int start, int length){}
     public void startPrefixMapping(String prefix, String uri) {}
     public void endPrefixMapping(String prefix) {}
     public void ignorableWhitespace(char[] text, int start, int length) throws SAXException {}
     public void processingInstruction(String target, String data){}
     public void skippedEntity(String name) {}
     public void eliminafile(String p){
    	 try{ 
    	 File fs1=new File(p+"node.db");
    	 fs1.delete();
    	 File fs11=new File(p+"node.lg");
    	 fs11.delete();
    	 
    	 File fs2=new File(p+"way.db");
    	 fs2.delete();
    	 File fs22=new File(p+"way.lg");
    	 fs22.delete();
    	 
    	 n.delete();
    	 w.delete();
    	 
    	 }
      catch (IOException e) {
   	  System.out.println("ERRORE");
   	  System.err.println (e);
     }
     }

}

