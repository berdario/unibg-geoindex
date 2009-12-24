package principale.main;

// NOTE: Please read README.txt before browsing this code.

import java.io.*;
import java.util.*;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.btree.BTree;

import principale.Spatialindex.IData;
import principale.Spatialindex.INode;
import principale.Spatialindex.ISpatialIndex;
import principale.Spatialindex.IVisitor;

import principale.Spatialindex.Region;

import principale.r_tree.RTree;
import principale.ricercapernome.Compara;
import principale.ricercapernome.Serial;
import principale.ricercapernome.Serializ;

import principale.storagemanager.DiskStorageManager;
import principale.storagemanager.IBuffer;
import principale.storagemanager.IStorageManager;
import principale.storagemanager.PropertySet;
import principale.storagemanager.RandomEvictionsBuffer;
/*
import spatialindex.spatialindex.*;
import spatialindex.storagemanager.*;
import spatialindex.rtree.*;
*/
public class RTreeLoad
{
	private formdicaricamento frame;
	Serial a=new Serial();
	
	public RTreeLoad(formdicaricamento frame)
	{
		this.frame=frame;
	}
	
		
		
	public void carica(String path)
	{
		
		File fdb=new File(path+"db");
		fdb.mkdir();
			
		String[] args = new String[3];
		args[0]="coordinate.txt";
		args[1]="albero_Rtree";
		args[2]="32";
		try
		{
			//-------APERTURA FILE PER IL B-TREE (NOME COMUNE -MBR) --------------------------//
			RecordManager mydbn;
			BTree tn=new BTree();
			
			
			mydbn = RecordManagerFactory.createRecordManager(path+"db"+File.separator+"albero_Btree_osm", new Properties());
			
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
			PropertySet ps = new PropertySet();

			Boolean b = new Boolean(true);
			ps.setProperty("Overwrite", b);
				//overwrite the file if it exists.
			System.out.println(path+"db"+File.separator+"datiscritti");
			ps.setProperty("FileName", path+"db"+File.separator+"datiscritti");
				// .idx and .dat extensions will be added.

			Integer i = new Integer(args[2]);//8k
			i=12+i*40+i*20;
			ps.setProperty("PageSize", i);
				// specify the page size. Since the index may also contain user defined data
				// there is no way to know how big a single node may become. The storage manager
				// will use multiple pages per node if needed. Off course this will slow down performance.

			IStorageManager diskfile = new DiskStorageManager(ps);

			IBuffer file = new RandomEvictionsBuffer(diskfile, 10, false);

			PropertySet ps2 = new PropertySet();

			Double f = new Double(0.7);
			ps2.setProperty("FillFactor", f);

			i = new Integer(args[2]);
			ps2.setProperty("IndexCapacity", i);
			ps2.setProperty("LeafCapacity", i);
				// Index capacity and leaf capacity may be different.

			i = new Integer(2);
			ps2.setProperty("Dimension", i);

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
				
				File cartella=new File(path+"db"+File.separator+Integer.toString(id/1000));
				FileWriter documenti;
				if(!cartella.exists())
						cartella.mkdir();
				documenti=new FileWriter(path+"db"+File.separator+Integer.toString(id/1000)+File.separator+Integer.toString(id));
				
			
				count++;
				peso_decrementato=peso_decrementato-line.getBytes().length-2;
				frame.put(100-(int)((peso_decrementato*100)/peso));

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
			System.out.print("Secondi: " + ((end - start) / 1000.0f)+"  ");

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
			frame.finito();
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
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
