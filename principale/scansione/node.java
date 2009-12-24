package principale.scansione;

//import java.io.FileWriter;
//import java.io.PrintWriter;
//import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.RecordManagerOptions;
import jdbm.btree.BTree;


public class node {
	RecordManager mydb;
	Double[] as = new Double[2];
	Object o;
	Double g;
	BTree tn=new BTree();
//	BTree tnLat=new BTree();
	Long cont=0L;
	String relativePath;

//	private ArrayList<Double> lat=new ArrayList<Double>();
//	ArrayList<Double> lon=new ArrayList<Double>();
	public void close() throws IOException{
		mydb.close();
//		mydbLat.close();
	}
	public void delete() throws IOException{
		File fs=new File(relativePath+"/node-lon.lg");
    	fs.delete();
    	File fs1=new File(relativePath+"/node-lon.db");
    	fs1.delete();
    	File fs2=new File(relativePath+"/node-lat.lg");
    	fs2.delete();
    	File fs22=new File(relativePath+"/node-lat.db");
    	fs22.delete();
	}
	public node(String relativePath){
		Serial a=new Serial();
		try
 	{	
		this.relativePath=relativePath;
		Properties props = new Properties();
        props.put( RecordManagerOptions.CACHE_SIZE, "1000" );

		mydb = RecordManagerFactory.createRecordManager(relativePath+"/node", props);
 		tn = ScanXML.loadOrCreateBTree(mydb, "Node", a );
 	}
 	catch (java.io.IOException e)
 	{	}
// 	try
// 	{	
//		Properties props = new Properties();
//        props.put( RecordManagerOptions.CACHE_SIZE, "10000" );
// 		mydbLat = RecordManagerFactory.createRecordManager(relativePath+"/node-lat", new Properties());
// 		tnLat = ScanXML.loadOrCreateBTree(mydbLat, "Node-lat", a );
// 	}
// 	catch (java.io.IOException e)
// 	{	}
	}
	
	public void setCont(Long i){
		cont=i;
	}
	
	public void commit(){
		try
	 	{
			mydb.commit();
//			mydbLat.commit();
	 	}
	 	catch (java.io.IOException e)
	 	{	}
		
		
	}
	
	public Long add(Long id,Double latit,Double longit){
		String value=String.valueOf(latit)+" "+String.valueOf(longit);
		ScanXML.insert(tn, id, value);
//		ScanXML.insert(tnLat, id, latit);
		//lat.add(latit);
		//lon.add(longit);
		cont+=1;
        return cont-1;
        
	}
//	public BTree getNodeLat(){
//		return tnLat;
//	}
//	public BTree getNodeLon(){
//		return tnLon;
//	}
	public Double[]get(Long index){
		
		o=ScanXML.ricerca(index, tn);
		//g=lon.get(index);
		if(o!=null){
			
			String value=(String)o;
			String a[]=value.split(" ");

			as[0]=Double.parseDouble(a[0]);
			as[1]=Double.parseDouble(a[1]);
			
			return as;
		}
		return null;
	}
	
//	public Double getlat(Long index){
//		
//		o=ScanXML.ricerca(index, tn);
//		//g=lon.get(index);
//		if(o!=null){
//			String value=(String)o;
//			String a[]=value.split(" ");
//			
//			return Double.parseDouble(a[0]);
//		}
//		return null;
//	}
	public long size(){
		return cont-1;
		
	}
}
