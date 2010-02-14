package geotag;

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

public class RTreeReader {

    ISpatialIndex tree;
    MyVisitor visitor;

    /**
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 * @throws FileNotFoundException 
	 * @throws NullPointerException 
	 * @throws SecurityException 
    */
    public RTreeReader(String dbpath) throws SecurityException, NullPointerException, FileNotFoundException, IllegalArgumentException, IOException {
        //---------------------------APERTURA R-TREE------------------------------//
        IStorageManager diskfile = new DiskStorageManager(dbpath+ "datiscritti");
        IBuffer filebuffer = new RandomEvictionsBuffer(diskfile, 10, false);
        PropertySet ps2 = new PropertySet();
        ps2.setProperty("IndexIdentifier", 1);
        tree = new RTree(ps2, filebuffer);
        visitor = new MyVisitor();
    }


	
	public  vettori query(double a,double b,double c,double d) {

		double f1[]={c,d},f2[]={a,b};
		vettori codici=new vettori();
		tree.containmentQuery(new Region(f1,f2), visitor);
		
		for(int num=0;num<visitor.visited.size();num++){
			
			IData dati_rtree = ((IData)visitor.visited.elementAt(num));
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
	public Vector<IData> visited=new Vector<IData>();
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
		visited.addElement(d);
	}
}
