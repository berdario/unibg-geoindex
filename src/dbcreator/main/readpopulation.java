package dbcreator.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Properties;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.btree.BTree;

import dbcreator.ricercapernome.Compara;
import dbcreator.ricercapernome.Serial;
import dbcreator.ricercapernome.Serializ;
import geotag.Configuration;
import geotag.GeoApplication;

public class readpopulation {

	/**
	 * @param args
	 * @throws IOException 
	 */
	private formdicaricamento frame;
        Properties options;
        String dbpath;
	
	public readpopulation(){
            options = GeoApplication.getDefaultRecordManagerOptions();
            dbpath = Configuration.getDbPath();
        }
	
	public readpopulation(formdicaricamento frame)
	{
            this();
            this.frame=frame;
	}
	public void carica(String path) throws IOException {
		// TODO Auto-generated method stub
	// TODO Auto-generated method stub		
		LineNumberReader lr = null;
		//File file =new File(path+"allCountries.txt");
		File file =new File(path+"IT.txt");//il file dev'essere scompattato manualmente, TODO sistemare?
		//File file =new File(path+"CH.txt");
		//File file =new File(path+"FR.txt");
		//File file =new File(path+"AT.txt");
		FileReader fi =new FileReader(file);
		lr = new LineNumberReader(fi);
		float peso=file.length();
		float peso_decrementato=peso;
		//-------------------  APERTURA FILE B-TREE DEL GAZETTEER POPULATION -------------------//
		Serial a=new Serial();
		RecordManager mydbgazpop;
		BTree tgazpop = new BTree();
		mydbgazpop = RecordManagerFactory.createRecordManager(dbpath+"albero_Btree_population", options);
		tgazpop = loadOrCreateBTree(mydbgazpop, "population", a );

		//-------------------  APERTURA FILE B-TREE DEL GAZETTEER POPULATION2-------------------//

		RecordManager mydbgazpop2;
		BTree tgazpop2 = new BTree();
		mydbgazpop2 = RecordManagerFactory.createRecordManager(dbpath+"albero_Btree_population2", options);
		tgazpop2 = loadOrCreateBTree(mydbgazpop2, "population2", a );

		
		String line;
		line = lr.readLine();
		int count=0;
		while (line != null)
		{
			String parola[]=line.split("\t");
			// DI TUTTI I PAESI CONTENUTI NEL GAZETTEER ESTRAPOLO SOLO QUELLI AMMINISTRATIVI
			if(parola[6].equalsIgnoreCase("P"))
			{
				//if(((String)tcountryInfo.find(parola[8])).equalsIgnoreCase("EU"))
					count++;	
					if(!parola[13].equalsIgnoreCase("")){
						tgazpop2.insert(parola[13], parola[14], true);
						//System.out.println(parola[13]+" "+parola[14]);
					}
					else
						if(!parola[12].equalsIgnoreCase("")){
							tgazpop.insert(parola[12], parola[14], true);
							//System.out.println(parola[12]+" "+parola[14]);
						}
				if ((count % 100) == 0){
					  mydbgazpop.commit();
				}
						
			}
			peso_decrementato=peso_decrementato-line.getBytes().length-2;
			if (frame !=null){
				frame.put(100-(int)((peso_decrementato*100)/peso));
			}
			line = lr.readLine();
			
		}
		
		if ((count % 100) != 0 && count!=0)
			mydbgazpop.commit();
		mydbgazpop.close();
		if (frame!=null){
			frame.finito();
		}
		
	}
	public static BTree loadOrCreateBTree( RecordManager aRecordManager,String aName, Compara aComparator ) throws IOException

 	{
 
 	  long recordID = aRecordManager.getNamedObject( aName );
 	  BTree tree = null;
 	  Serializ s=new Serializ();
 	  if ( recordID == 0 )
 	  {  
 	    
 	    tree = BTree.createInstance( aRecordManager, aComparator,s,s,128 );
 	    aRecordManager.setNamedObject( aName, tree.getRecid() ); 	   
 	  }
 	  
 	  else
 	  {
 	    tree = BTree.load( aRecordManager, recordID );
 	  }
 	  
 	  return tree;
 	} 

}
