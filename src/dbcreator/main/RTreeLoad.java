package dbcreator.main;

/* NOTE: Pagliacci e/o Spelgatti hanno copiato questo codice da:
 * http://svn2.assembla.com/svn/kump/R_Tree_For_MusicDB/regressiontest/
 * per questo prendete con le pinze i commenti e la logica (quella che non è stata modificata s'intende)
 * lascio comunque i commenti (come quello sulla page size) perchè possono venire utili in futuro
 */

import dbcreator.ricercapernome.Compara;
import dbcreator.ricercapernome.Serial;
import dbcreator.ricercapernome.Serializ;
import geotag.Configuration;
import java.io.*;
import java.util.*;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.btree.BTree;


//import principale.storagemanager.IStorageManager;

import spatialindex.storagemanager.DiskStorageManager;
import spatialindex.storagemanager.IStorageManager;
import spatialindex.storagemanager.RandomEvictionsBuffer;
import spatialindex.storagemanager.IBuffer;
import spatialindex.ISpatialIndex;
import spatialindex.rtree.RTree;
import spatialindex.storagemanager.PropertySet;
import spatialindex.Region;
import spatialindex.IVisitor;
import spatialindex.IData;
import spatialindex.INode;

public class RTreeLoad
{
	private formdicaricamento frame;
	String path,dbpath;
	Serial a=new Serial();
	RecordManager mydbn;
	
	public RTreeLoad(){
		path = Configuration.getPath();
		dbpath = Configuration.getDbPath();
		try {
			mydbn = RecordManagerFactory.createRecordManager(dbpath+"albero_Btree_osm", Configuration.getDefaultRecordManagerOptions());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public RTreeLoad(formdicaricamento frame)
	{
		this();
		this.frame=frame;	
	}
	
		
		
	public void carica()
	{
		
		File fdb=new File(dbpath);
		fdb.mkdir();
			
		String[] args = new String[3];
		args[0]="coordinate.txt";
		args[1]="albero_Rtree";
		args[2]="32";
		try
		{
			//-------APERTURA FILE PER IL B-TREE (NOME COMUNE -MBR) --------------------------//
			BTree tn=new BTree();
						
			tn = loadOrCreateBTree(mydbn, "osm", a );
			//APERTURA PER L' R-TREE
			
			File fi =new File(path+args[0]);

			float peso=fi.length();
			float peso_decrementato=peso;
			
			LineNumberReader lr = null;

			try
			{
				
				FileReader file =new FileReader(path+args[0]);
				lr = new LineNumberReader(file);
			}
			catch (FileNotFoundException e)
			{
				System.err.println("Cannot open data file " + args[0] + "."+lr);
				System.exit(-1);
			}

			// Create a disk based storage manager.
			
			Boolean b = new Boolean(true);
			System.out.println(dbpath+File.separator+"datiscritti");
			
			Integer pageSize = new Integer(args[2]);//8k
			pageSize=12+pageSize*40+pageSize*20;


			IStorageManager diskfile = new DiskStorageManager(dbpath+File.separator+"datiscritti",pageSize);
                        // .idx and .dat extensions will be added.
			// specify the page size. Since the index may also contain user defined data
			// there is no way to know how big a single node may become. The storage manager
			// will use multiple pages per node if needed. Off course this will slow down performance.

			IBuffer file = new RandomEvictionsBuffer(diskfile, 10, false);

			PropertySet ps2 = new PropertySet();

			Double f = new Double(0.7);
			ps2.setProperty("FillFactor", f);

			pageSize = new Integer(args[2]);
			ps2.setProperty("IndexCapacity", pageSize);
			ps2.setProperty("LeafCapacity", pageSize);
				// Index capacity and leaf capacity may be different.

			pageSize = new Integer(2);
			ps2.setProperty("Dimension", pageSize);

			ISpatialIndex tree = new RTree(ps2, file);

			int count = 0;
			
			int id;
			double x1, x2, y1, y2;
			double[] f1 = new double[2];
			double[] f2 = new double[2];

			long start = System.currentTimeMillis();
			String line = lr.readLine();
			String livello;
			String paese;
			StringTokenizer st;
			
			FileWriter documenti;
			while (line != null)
			{
				st = new StringTokenizer(line);
				livello=(st.nextToken("$"));
				paese=new String(st.nextToken("$"));
				//st.nextToken("$");
				id = new Integer(st.nextToken("$")).intValue();
				x2 = new Double(st.nextToken("$")).doubleValue();
				y2 = new Double(st.nextToken("$")).doubleValue();
				x1 = new Double(st.nextToken("$")).doubleValue();
				y1 = new Double(st.nextToken("$")).doubleValue();
				//insert
				f1[0] = x1; f1[1] = y1;
				f2[0] = x2; f2[1] = y2;
				Region r = new Region(f1, f2);

				// ------ INSERIMENTO R-TREE (NOME-MBR-ID) --------//
				
				tree.insertData(paese.getBytes(), r, id);
				
				//   INSERISCI NEL B-TREE NOME PAESE + MBR///
				tn.insert(paese.toLowerCase(),String.valueOf(x2)+" "+String.valueOf(y2)+" "+String.valueOf(x1)+" "+String.valueOf(y1)+" ", true);
				
				//   FINE INSERIMENTO    ///
				
				File cartella=new File(dbpath+File.separator+Integer.toString(id/1000));
				if(!cartella.exists()){
					cartella.mkdir();
				}
				
				documenti=new FileWriter(dbpath+File.separator+Integer.toString(id/1000)+File.separator+Integer.toString(id));
				documenti.close();
				
				count++;
				peso_decrementato=peso_decrementato-line.getBytes().length-2;
				if (frame!=null){
					frame.put(100-(int)((peso_decrementato*100)/peso));
				}

				line = lr.readLine();
				if ((count % 100) == 0)
				  mydbn.commit();
			}
			if ((count % 100) != 0)
			mydbn.commit();
			mydbn.close();
			long end = System.currentTimeMillis();

			//System.err.println("Operations: " + count);
			//System.err.println(tree);
			System.out.println("Secondi: " + ((end - start) / 1000.0f)+"  ");

			// since we created a new RTree, the PropertySet that was used to initialize the structure
			// now contains the IndexIdentifier property, which can be used later to reuse the index.
			// (Remember that multiple indices may reside in the same storage manager at the same time
			//  and every one is accessed using its unique IndexIdentifier).
			//Integer indexID = (Integer) ps2.getProperty("IndexIdentifier");
			//System.err.println("Index ID: " + indexID);

			boolean ret = tree.isIndexValid();
			if (ret == false) System.err.println("Structure is INVALID!");

			// flush all pending changes to persistent storage (needed since Java might not call finalize when JVM exits).
			tree.flush();
			if (frame!=null){
				frame.finito();
			}
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			try {
				Dbcreator.listOpenFiles();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	public static Object ricerca(String el,BTree b){
		Object o=new Object();
		try {
			o = b.find(el);
	         
	      } catch (IOException e) {
	          System.err.println (e);
	      }
	      return o; 
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

	// example of a Visitor pattern.
	// see RTreeQuery for a more elaborate example.
	class MyVisitor implements IVisitor
	{
		public void visitNode(final INode n) {}

		public void visitData(final IData d)
		{
			System.out.println(d.getIdentifier());
				// the ID of this data entry is an answer to the query. I will just print it to stdout.
		}
	}
}
