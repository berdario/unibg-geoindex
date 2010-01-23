package geotag;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

import principale.Spatialindex.IData;
import principale.Spatialindex.INode;
import principale.Spatialindex.ISpatialIndex;
import principale.Spatialindex.IVisitor;
import principale.Spatialindex.Region;

import principale.r_tree.RTree;
import principale.storagemanager.DiskStorageManager;
import principale.storagemanager.IBuffer;
import principale.storagemanager.IStorageManager;
import principale.storagemanager.PropertySet;
import principale.storagemanager.RandomEvictionsBuffer;

public class query_rtree {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 * @throws FileNotFoundException 
	 * @throws NullPointerException 
	 * @throws SecurityException 
	 */
	public static vettori query(String path,double a,double b,double c,double d) throws SecurityException, NullPointerException, FileNotFoundException, IllegalArgumentException, IOException {
		// TODO Auto-generated method stub

		double f1[]=new double[2],f2[]=new double[2];
		vettori codici=new vettori();
		//---------------------------APERTURA R-TREE------------------------------//
		PropertySet ps = new PropertySet();        
		ps.setProperty("FileName", path+"db"+File.separator+"datiscritti");
		IStorageManager diskfile = new DiskStorageManager(ps);
		IBuffer filebuffer = new RandomEvictionsBuffer(diskfile, 10, false);
		PropertySet ps2 = new PropertySet();
		Integer i = new Integer(1);
		ps2.setProperty("IndexIdentifier", i);
		ISpatialIndex tree = new RTree(ps2, filebuffer);
		MyVisitor vis = new MyVisitor();
		
		Region r;
		f2[0]=a;
		f2[1] =b;
		f1[0] = c;
		f1[1] = d;
		r = new Region(f1,f2);	
		//tree.intersectionQuery(r, vis);
		tree.containmentQuery(r, vis);
		
		for(int num=0;num<vis.visitati.size();num++){
			
			IData dati_rtree = ((IData)vis.visitati.elementAt(num));
			codici.codici.add(String.valueOf((dati_rtree.getIdentifier())));
			codici.nomi.add(new String(dati_rtree.getData()));
		}
		return codici;
	}

}
class MyVisitor implements IVisitor
{
	public int m_indexIO = 0;
	public int m_leafIO = 0;
	public Vector<IData> visitati=new Vector<IData>();
	public void visitNode(final INode n)
	{
		if (n.isLeaf()) m_leafIO++;
		else m_indexIO++;
	}

	public void visitData(final IData d)
	{   /*
		String a=new String(d.getData());
		System.out.print(d.getIdentifier());
		System.out.print(" "+a+" ");
		System.out.println(" "+d.getShape());
		*/
		visitati.addElement(d);
	}
}
