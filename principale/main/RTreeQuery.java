package principale.main;

// NOTE: Please read README.txt before browsing this code.


import java.io.File;
import java.util.*;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.btree.BTree;

import principale.Spatialindex.IData;
import principale.Spatialindex.IEntry;
import principale.Spatialindex.INode;
import principale.Spatialindex.IQueryStrategy;
import principale.Spatialindex.IShape;
import principale.Spatialindex.ISpatialIndex;
import principale.Spatialindex.IVisitor;

import principale.Spatialindex.Region;

import principale.r_tree.RTree;


import principale.storagemanager.DiskStorageManager;
import principale.storagemanager.IBuffer;
import principale.storagemanager.IStorageManager;
import principale.storagemanager.PropertySet;
import principale.storagemanager.RandomEvictionsBuffer;


public class RTreeQuery
{
	
	public RTreeQuery(String path,String paesedacercare,String mbr)
	{
		try
		{
						
			// Create a disk based storage manager.
			PropertySet ps = new PropertySet();
            
			ps.setProperty("FileName", path+"db"+File.separator+"datiscritti");
				// .idx and .dat extensions will be added.

			IStorageManager diskfile = new DiskStorageManager(ps);

			IBuffer file = new RandomEvictionsBuffer(diskfile, 10, false);
				// applies a main memory random buffer on top of the persistent storage manager
				// (LRU buffer, etc can be created the same way).

			PropertySet ps2 = new PropertySet();

			// If we need to open an existing tree stored in the storage manager, we only
			// have to specify the index identifier as follows
			Integer i = new Integer(1); // INDEX_IDENTIFIER_GOES_HERE (suppose I know that in this case it is equal to 1);
			ps2.setProperty("IndexIdentifier", i);
				// this will try to locate and open an already existing r-tree index from file manager file.

			ISpatialIndex tree = new RTree(ps2, file);

			
			int indexIO = 0;
			int leafIO = 0;
			double[] f1 = new double[2];
			double[] f2 = new double[2];

			long start = System.currentTimeMillis();
			String coordinate;
			if(mbr==null){
				BTree tn=new BTree();
				RecordManager mydbn;
				principale.ricercapernome.Serial a=new principale.ricercapernome.Serial();
				mydbn = RecordManagerFactory.createRecordManager(path+"db"+File.separator+"albero_Btree_osm", new Properties());
				tn = RTreeLoad.loadOrCreateBTree(mydbn, "osm", a );
				coordinate=(String)tn.find(paesedacercare);
				mydbn.close();
			}
			else
				coordinate=mbr;
			
			if(coordinate==null)
				System.out.print("paese non trovato!!");
			else
			{
			//System.out.println("iseo "+coordinate);
			StringTokenizer st = new StringTokenizer(coordinate);
			    
			f2[0]=new Double(st.nextToken(" "));
			f2[1] = new Double(st.nextToken(" "));
			f1[0] = new Double(st.nextToken(" "));
			f1[1] = new Double(st.nextToken(" "));
				
			MyVisitor vis = new MyVisitor();
			
			Region r = new Region(f1, f2);
			tree.intersectionQuery(r, vis);
			System.out.println("contiene:");
			tree.containmentQuery(r, vis);
			// this will find all data that intersect with the query range.
			
			
			
			indexIO += vis.m_indexIO;
			leafIO += vis.m_leafIO;
			// example of the Visitor pattern usage, for calculating how many nodes
			// were visited.
			//if ((count % 1000) == 0) System.err.println(count);
			}
			long end = System.currentTimeMillis();
			//MyQueryStrategy2 qs = new MyQueryStrategy2();
			//tree.queryStrategy(qs);
			//System.err.println("Indexed space: " + qs.m_indexedSpace);
			//System.err.println("Operations: " + count);
			//System.err.println(tree);
			//System.err.println("Index I/O: " + indexIO);
			//System.err.println("Leaf I/O: " + leafIO);
			System.out.println("MilliSecondi: " + ((end - start) ));

			// flush all pending changes to persistent storage (needed since Java might not call finalize when JVM exits).
			tree.flush();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// example of a Visitor pattern.
	// findes the index and leaf IO for answering the query and prints
	// the resulting data IDs to stdout.
	class MyVisitor implements IVisitor
	{
		public int m_indexIO = 0;
		public int m_leafIO = 0;
		
		public void visitNode(final INode n)
		{
			if (n.isLeaf()) m_leafIO++;
			else m_indexIO++;
		}

		public void visitData(final IData d)
		{   String a=new String(d.getData());
			System.out.print(d.getIdentifier());
			System.out.print(" "+a+" ");
			System.out.println(" "+d.getShape());
			
			
				// the ID of this data entry is an answer to the query. I will just print it to stdout.
		}
	}

	// example of a Strategy pattern.
	// traverses the tree by level.
	class MyQueryStrategy implements IQueryStrategy
	{
		private ArrayList<Integer> ids = new ArrayList<Integer>();

		public void getNextEntry(IEntry entry, int[] nextEntry, boolean[] hasNext)
		{
			Region r = entry.getShape().getMBR();

			System.out.println(r.m_pLow[0] + " " + r.m_pLow[1]);
			System.out.println(r.m_pHigh[0] + " " + r.m_pLow[1]);
			System.out.println(r.m_pHigh[0] + " " + r.m_pHigh[1]);
			System.out.println(r.m_pLow[0] + " " + r.m_pHigh[1]);
			System.out.println(r.m_pLow[0] + " " + r.m_pLow[1]);
			System.out.println();
			System.out.println();
				// print node MBRs gnuplot style!

			// traverse only index nodes at levels 2 and higher.
			if (entry instanceof INode && ((INode) entry).getLevel() > 1)
			{
				for (int cChild = 0; cChild < ((INode) entry).getChildrenCount(); cChild++)
				{
					ids.add(new Integer(((INode) entry).getChildIdentifier(cChild)));
				}
			}

			if (! ids.isEmpty())
			{
				nextEntry[0] = ((Integer) ids.remove(0)).intValue();
				hasNext[0] = true;
			}
			else
			{
				hasNext[0] = false;
			}
		}
	};

	// example of a Strategy pattern.
	// find the total indexed space managed by the index (the MBR of the root).
	/**
	 * @author  Computer Marco
	 */
	class MyQueryStrategy2 implements IQueryStrategy
	{
		/**
		 * @uml.property  name="m_indexedSpace"
		 * @uml.associationEnd  
		 */
		public Region m_indexedSpace;

		public void getNextEntry(IEntry entry, int[] nextEntry, boolean[] hasNext)
		{
			// the first time we are called, entry points to the root.
			IShape s = entry.getShape();
			m_indexedSpace = s.getMBR();

			// stop after the root.
			hasNext[0] = false;
		}
	}
}
