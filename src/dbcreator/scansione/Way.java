package dbcreator.scansione;

//import java.util.ArrayList;
import geotag.GeoApplication;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.RecordManagerOptions;
import jdbm.btree.BTree;


public class Way {
	RecordManager mydb;
	Double[] as = new Double[4];

	Object o=new Object();
	Double g;
	Long cont=new Long(0);
	BTree tw=new BTree();
//	BTree twLonMax=new BTree();
//	BTree twLatMin=new BTree();
//	BTree twLatMax=new BTree();
	String relativePath;
//	boolean commitLAMin=true;
//	boolean commitLAMax=true;
//	boolean commitLOMin=true;
//	boolean commitLOMax=true;
//private ArrayList<Double> latmin=new ArrayList<Double>();
//private ArrayList<Double> latmax=new ArrayList<Double>();
//private ArrayList<Double> lonmin=new ArrayList<Double>();
//private ArrayList<Double> lonmax=new ArrayList<Double>();
	public void close() throws IOException{
		mydb.close();
//		mydbLonMax.close();
//		mydbLatMin.close();
//		mydbLatMax.close();	
	}
	
	public void delete() throws IOException{
		
		File fs=new File(relativePath+"/Way-LonMax.lg");
    	fs.delete();
    	File fs1=new File(relativePath+"/Way-LonMax.db");
    	fs1.delete();
    	
    	File fs2=new File(relativePath+"/Way-LonMin.lg");
    	fs2.delete();
    	File fs22=new File(relativePath+"/Way-LonMin.db");
    	fs22.delete();
    	
		File fs3=new File(relativePath+"/Way-LatMin.lg");
    	fs3.delete();
    	File fs33=new File(relativePath+"/Way-LatMin.db");
    	fs33.delete();
    	
    	File fs4=new File(relativePath+"/Way-LatMax.lg");
    	fs4.delete();
    	File fs44=new File(relativePath+"/Way-LatMax.db");
    	fs44.delete();

	}
public Way(String relativePath){
	this.relativePath=relativePath;
	Serial a=new Serial();
	try
 	{	
		Properties props = new Properties();
        props.put( RecordManagerOptions.CACHE_SIZE, "1000" );

		mydb = RecordManagerFactory.createRecordManager(relativePath+"/Way", GeoApplication.getDefaultRecordManagerOptions());
 		tw = ScanXML.loadOrCreateBTree(mydb, "Way-LonMin", a );
 	}
	catch (java.io.IOException e)
 	{	}
//	try
// 	{	
//		Properties props = new Properties();
//        props.put( RecordManagerOptions.CACHE_SIZE, "10000" );
//
//		mydbLonMax = RecordManagerFactory.createRecordManager(relativePath+"/Way-LonMax", new Properties());
// 		twLonMax = ScanXML.loadOrCreateBTree(mydbLonMax, "Way-LonMax", a );
// 	}
//	catch (java.io.IOException e)
// 	{	}
//	try
// 	{	
//		Properties props = new Properties();
//        props.put( RecordManagerOptions.CACHE_SIZE, "10000" );
//
//		mydbLatMin = RecordManagerFactory.createRecordManager(relativePath+"/Way-LatMin", new Properties());
// 		twLatMin = ScanXML.loadOrCreateBTree(mydbLatMin, "Way-LatMin", a );
// 	}
//	catch (java.io.IOException e)
// 	{	}
//	try
// 	{	
//		Properties props = new Properties();
//        props.put( RecordManagerOptions.CACHE_SIZE, "10000" );
//
//		mydbLatMax = RecordManagerFactory.createRecordManager(relativePath+"/Way-LatMax", new Properties());
// 		twLatMax = ScanXML.loadOrCreateBTree(mydbLatMax, "Way-LatMax", a );
// 	}
//	catch (java.io.IOException e)
// 	{	}
}
public void setCont(Long i){
	cont=i;
}
public Long add(Long id,Double lamin,Double lamax,Double lomin,Double lomax ){
	String value=String.valueOf(lamin)+" "+String.valueOf(lamax)+" "+String.valueOf(lomin)+" "+String.valueOf(lomax);
	ScanXML.insert(tw, id,value);
//	ScanXML.insert(twLatMin,  id, lamin);
//	ScanXML.insert(twLatMax,  id, lamax);
//	ScanXML.insert(twLonMin,  id, lomin);
//	ScanXML.insert(twLonMax,  id, lomax);
//	latmin.add(lamin);
//	latmax.add(lamax);
//	lonmin.add(lomin);
//	lonmax.add(lomax);
	cont+=1;
	return cont-1;
}
public void commit(){
	try
 	{
	mydb.commit();
//	mydbLatMax.commit();
//	mydbLonMin.commit();
//	mydbLonMax.commit();
 	}
 	catch (java.io.IOException e)
 	{	}
}

public void set(Double lamin,Double lamax,Double lomin,Double lomax,Long index){
	String value=String.valueOf(lamin)+" "+String.valueOf(lamax)+" "+String.valueOf(lomin)+" "+String.valueOf(lomax);
	ScanXML.insert(tw, index,value);
		
//	ScanXML.insert(twLatMin, index, lamin);
//	
//	ScanXML.insert(twLatMax, index, lamax);
//	
//	ScanXML.insert(twLonMin, index, lomin);
//	
//	ScanXML.insert(twLonMax, index, lomax);
//	latmin.set(index,lamin);
//	latmax.set(index,lamax);
//	lonmin.set(index,lomin);
//	lonmax.set(index,lomax);
}
public Long size(){
	return cont-1;
}

//public Double getLatMin(Long index){
//	
//	o=ScanXML.ricerca(index, tw);
//	
//	//g=lon.get(index);
//	if(o!=null){
//		String value=(String)o;
//		String a[]=value.split(" ");
//		
//		return Double.parseDouble(a[0]);
//	}
//	return null;
//}
//public Double getLatMax(Long index){
//	
//	o=ScanXML.ricerca(index, tw);
//	//g=lon.get(index);
//	if(o!=null){
//		String value=(String)o;
//		String a[]=value.split(" ");
////		System.out.println(a[0]);
//		return Double.parseDouble(a[1]);
//	}
//	return null;
//}
public Double[] get(Long index){
	
	o=ScanXML.ricerca(index, tw);
	//g=lon.get(index);
	if(o!=null){
		String value=(String)o;
		String a[]=value.split(" ");

		as[0]=Double.parseDouble(a[0]);
		as[1]=Double.parseDouble(a[1]);
		as[2]=Double.parseDouble(a[2]);
		as[3]=Double.parseDouble(a[3]);
//		System.out.println(a[0]);
		return as;
	}
	return null;
}
//public Double getLonMax(Long index){
//	
//	o=ScanXML.ricerca(index, tw);
//	//g=lon.get(index);
//	if(o!=null){
//		String value=(String)o;
//		String a[]=value.split(" ");
//		
//		return Double.parseDouble(a[3]);
//	}
//	return null;
//}


}
