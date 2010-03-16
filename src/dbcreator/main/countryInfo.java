package dbcreator.main;

import java.io.File;
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

public class countryInfo {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void carica(String path) throws IOException {
                String dbpath = Configuration.getDbPath();

		Serial a=new Serial();
		RecordManager mydbcountryInfo;
		BTree tcountryInfo=new BTree();
		mydbcountryInfo = RecordManagerFactory.createRecordManager(dbpath + "albero_countryInfo", Configuration.getDefaultRecordManagerOptions());
		tcountryInfo = loadOrCreateBTree(mydbcountryInfo, "country", a );
		
		LineNumberReader lr = null;
		File file =new File(path+"countryInfo.txt");
		FileReader fi =new FileReader(file);
		lr = new LineNumberReader(fi);
		String line = lr.readLine();
		String data[];
		while (line != null)
		{
			if (line.charAt(0)!='#'){
				data=line.split("\t");
				tcountryInfo.insert(data[0],data[4]+"�#"+data[5]+"�#"+data[8]+"�#"+data[16], true);
				//System.out.println(data[0]+" "+data[4]+" "+data[5]+" "+data[8]+" "+data[16]);
				
				
			}
			line = lr.readLine();
		}
		mydbcountryInfo.commit();
		
		mydbcountryInfo.close();
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
