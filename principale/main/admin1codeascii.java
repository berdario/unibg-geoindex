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

public class admin1codeascii {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void carica(String path) throws IOException {
		// TODO Auto-generated method stub
		Serial a=new Serial();
		RecordManager mydbadmin1;
		BTree tadmin1=new BTree();
		mydbadmin1 = RecordManagerFactory.createRecordManager(path+"db"+File.separator+"albero_admin1codeascii", new Properties());
		tadmin1 = loadOrCreateBTree(mydbadmin1, "admin1", a );
		
		LineNumberReader lr = null;
		File file =new File(path+"admin1CodesASCII.txt");
		FileReader fi =new FileReader(file);
		lr = new LineNumberReader(fi);
		String line = lr.readLine();
		String data[];
		while (line != null)
		{
			data=line.split("\t");
			tadmin1.insert(data[0],data[1]+"£#"+data[2]+"£#"+data[3], true);
			//System.out.println(data[0]+" "+data[1]+" "+data[2]+" "+data[3]);
			line = lr.readLine();	
		}
		mydbadmin1.commit();
		
		mydbadmin1.close();
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
