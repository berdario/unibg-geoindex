package principale.main;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Properties;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.btree.BTree;
import principale.ricercapernome.Compara;
import principale.ricercapernome.Serial;
import principale.ricercapernome.Serializ;

public class featurecodes {
	public static void carica(String path) throws IOException {
		// TODO Auto-generated method stub
		Serial a=new Serial();
		RecordManager mydbfeaturecodes;
		BTree tfeaturecodes=new BTree();
		mydbfeaturecodes = RecordManagerFactory.createRecordManager(path+"db"+File.separator+"albero_featurecodes", new Properties());
		tfeaturecodes = loadOrCreateBTree(mydbfeaturecodes, "featurecodes", a );
		
		LineNumberReader lr = null;
		File file =new File(path+"featureCodes.txt");
		FileReader fi =new FileReader(file);
		lr = new LineNumberReader(fi);
		String line = lr.readLine();
		String data[];
		String datoextra;
		while (line != null)
		{
			data=line.split("\t");
			if(data.length<3)
				datoextra="";
			else
				datoextra=data[2];
			
			tfeaturecodes.insert(data[0],data[1]+"£#"+datoextra, true);
			
			line = lr.readLine();	
		}
		mydbfeaturecodes.commit();
		
		mydbfeaturecodes.close();
	}
	public static BTree loadOrCreateBTree( RecordManager aRecordManager,String aName, Compara aComparator ) throws IOException

 	{
 
 	  long recordID = aRecordManager.getNamedObject( aName );
 	  BTree tree = null;
 	  Serializ s=new Serializ();
 	  if ( recordID == 0 )
 	  {  
 	    
 	    tree = BTree.createInstance( aRecordManager, aComparator,s,s,16 );
 	    aRecordManager.setNamedObject( aName, tree.getRecid() ); 	   
 	  }
 	  
 	  else
 	  {
 	    tree = BTree.load( aRecordManager, recordID );
 	  }
 	  
 	  return tree;
 	} 

}
