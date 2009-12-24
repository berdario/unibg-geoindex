package principale.scansione;
import org.xml.sax.InputSource;

import principale.main.formdicaricamento;
public class ScanXMLThread extends ScanXML implements Runnable{
	public ScanXMLThread(){
		
	}
	public ScanXMLThread(BUnzip2 unz,InputSource xml,String path,Long n,Long w,boolean delete){
		super();
		paths=path;
		ws=w;
		ns=n;
		Bunz=unz;
		this.delete=delete;
		in=xml;
	}
	public ScanXMLThread(BUnzip2 unz,String path,Long n,Long w,boolean delete){
		super();
		//System.out.println("SCANXMLThread(paths+way+nodi) "+path+" "+" "+w+" "+n);
		paths=path;
		ws=w;
		ns=n;
		Bunz=unz;
		this.delete=delete;
	}
	private InputSource in;
	private BUnzip2 Bunz;
	private String paths=new String("");
	private Long ns=new Long(0);
	private Long ws=new Long(0);

	private boolean delete;
	
//	public synchronized void setNodiWay(ScanXML a){
//		Bunz.nodes=a.n.cont;
//		Bunz.ways=a.w.cont;
//	}
	public void run(){
		//System.out.println("Entrato in ScanXMLThread" +paths+" "+ws+" "+ns);
		ScanXML a=new ScanXML(in,paths,ns,ws,true,delete);
		
//		setNodiWay(a);

		}
	

	
}
