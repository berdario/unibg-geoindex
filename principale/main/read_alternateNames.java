package principale.main;

import java.awt.Frame;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Properties;
import java.util.Vector;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.btree.BTree;
import principale.ricercapernome.Compara;
import principale.ricercapernome.Serial;
import principale.ricercapernome.Serializ;

public class read_alternateNames {

	/**
	 * @param args
	 * @throws IOException 
	 */
	private formdicaricamento frame;
	
	public read_alternateNames(){}
	
	public read_alternateNames(formdicaricamento frame)
	{
		this.frame=frame;
	}
	public void carica(String path) throws IOException {
		// TODO Auto-generated method stub
		
		LineNumberReader lr = null;
		File file =new File(path+"alternateNames.txt");//il file dev'essere scompattato manualmente, TODO sistemare?
		FileReader fi =new FileReader(file);
		lr = new LineNumberReader(fi);
		float peso=file.length();
		float peso_decrementato=peso;
		//-------------------  APERTURA FILE B-TREE DEL DB INTERMEDIO -------------------//
		Serial a=new Serial();
		RecordManager mydbinter;
		BTree tinter=new BTree();
		mydbinter = RecordManagerFactory.createRecordManager(path+"db"+File.separator+"albero_Btree_Intermedio", new Properties());
		tinter = loadOrCreateBTree(mydbinter, "intermedio", a );
		
		//-------------------  APERTURA FILE B-TREE ALTERNATENAMESID ------------------
		RecordManager mydbalternatenamesId;
		BTree talternatenamesId=new BTree();
		mydbalternatenamesId = RecordManagerFactory.createRecordManager(path+"db"+File.separator+"albero_alternatenamesId", new Properties());
		talternatenamesId = loadOrCreateBTree(mydbalternatenamesId, "alternatenamesId", a );

		//-------------------  APERTURA FILE B-TREE ALTERNATENAMES ------------------
		RecordManager mydbalternatenames;
		BTree talternatenames=new BTree();
		mydbalternatenames = RecordManagerFactory.createRecordManager(path+"db"+File.separator+"albero_alternatenames", new Properties());
		talternatenames = loadOrCreateBTree(mydbalternatenames, "alternatenames", a );
		
		
		String line;
		int in,conta=0;
		line = lr.readLine();
		Long inizio=System.currentTimeMillis();
		
		while (line != null)
		{
			String parola[]=line.split("\t");
			String trovato;
			String indici[];
			boolean indice_trovato;
			
			// inserimento nel db intermedio
			
			trovato=(String) tinter.find(parola[3]);
			
			if(trovato!=null){
				indici=trovato.split("�#");
				indice_trovato=false;
				for(in=0;in<indici.length;in++)
					if(indici[in].equalsIgnoreCase(parola[1]))
						indice_trovato=true;
				if(indice_trovato==false)
					tinter.insert(parola[3], trovato+"�#"+parola[1], true);
			}
			else
				tinter.insert(parola[3], parola[1], true);

			
			trovato=(String) talternatenamesId.find(parola[1]);
			
			if(trovato!=null){
				indici=trovato.split("�#");
				indice_trovato=false;
				for(in=0;in<indici.length;in++)
					if(indici[in].equalsIgnoreCase(parola[3]))
						indice_trovato=true;
				if(indice_trovato==false)
					talternatenamesId.insert(parola[1], trovato+"�#"+parola[3], true);
			}
			else
				talternatenamesId.insert(parola[1], parola[3], true);
			
			
			talternatenames.insert(parola[3], parola[3], true);
				
			
			peso_decrementato=peso_decrementato-line.getBytes().length-2;
			if (frame != null){
				frame.put(100-(int)((peso_decrementato*100)/peso));
			}
			
			if(((++conta)%1000)==0){
				mydbalternatenamesId.commit();
				mydbalternatenames.commit();
				mydbinter.commit();
			}
			line = lr.readLine();
		}
		if(((conta)%1000)!=0){
			mydbalternatenamesId.commit();
			mydbalternatenames.commit();
			mydbinter.commit();
		}
		mydbalternatenamesId.close();
		mydbalternatenames.close();
		mydbinter.close();
		System.out.println((System.currentTimeMillis()-inizio)/1000);
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
