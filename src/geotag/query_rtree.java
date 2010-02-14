package geotag;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

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

		double f1[]=new double[2],f2[]=new double[2];
		vettori codici=new vettori();
		//---------------------------APERTURA R-TREE------------------------------//
		IStorageManager diskfile = new DiskStorageManager(path+"db"+File.separator+"datiscritti");
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
    @Override
	public void visitNode(final INode n)
	{
		if (n.isLeaf()) m_leafIO++;
		else m_indexIO++;
	}

    @Override
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
