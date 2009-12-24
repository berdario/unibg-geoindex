package principale.main;


import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

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

public class readgazetteer {

	/**
	 * @param args
	 * @throws IOExce
	 * ption 
	 */
	private formdicaricamento frame;
	private BTree tgaz;
	private BTree tcountryInfo;
	public readgazetteer(formdicaricamento frame)
	{
		this.frame=frame;
	}
	public void carica(String path) throws IOException {
		// TODO Auto-generated method stub		
		
		System.out.println("Inizio lettura gazetteer");
		LineNumberReader lr = null;
		//File file =new File(path+"allCountries.txt");
		File file =new File(path+"IT.txt");
		//File file =new File(path+"CH.txt");
		//File file =new File(path+"FR.txt");
		//File file =new File(path+"AT.txt");
		FileReader fi =new FileReader(file);
		Vector<String> file_di_log1=new Vector<String>();
		Vector<String> file_di_log2=new Vector<String>();
		lr = new LineNumberReader(fi);
		float peso=file.length();
		float peso_decrementato=peso;
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
		//-------------------  APERTURA FILE B-TREE DEL GAZETTEER -------------------//
		Serial a=new Serial();
		RecordManager mydbgaz;
		tgaz = new BTree();
		mydbgaz = RecordManagerFactory.createRecordManager(path+"db"+File.separator+"albero_Btree_Gazetteer", new Properties());
		tgaz = loadOrCreateBTree(mydbgaz, "gazetteer", a );

		//-------------------  APERTURA FILE B-TREE DEL DB INTERMEDIO -------------------//
		RecordManager mydbinter;
		BTree tinter=new BTree();
		mydbinter = RecordManagerFactory.createRecordManager(path+"db"+File.separator+"albero_Btree_Intermedio", new Properties());
		tinter = loadOrCreateBTree(mydbinter, "intermedio", a );

		//---------APERTURA FILE PER IL B-TREE OSM ------------------------------------//
		RecordManager mydbosm;
		BTree tosm=new BTree();
		
		mydbosm = RecordManagerFactory.createRecordManager(path+"db"+File.separator+"albero_Btree_osm", new Properties());
		
		tosm = loadOrCreateBTree(mydbosm, "osm", a );
		//---------APERTURA FILE PER IL B-TREE countryInfo ------------------------------------//
		RecordManager mydbcountryInfo;
		tcountryInfo = new BTree();
		mydbcountryInfo = RecordManagerFactory.createRecordManager(path+"db"+File.separator+"albero_countryInfo", new Properties());
		tcountryInfo = loadOrCreateBTree(mydbcountryInfo, "country", a );
		
		//-------------------  APERTURA FILE B-TREE DEL GAZETTEER POPULATION -------------------//
		RecordManager mydbgazpop;
		BTree tgazpop = new BTree();
		mydbgazpop = RecordManagerFactory.createRecordManager(path+"db"+File.separator+"albero_Btree_population", new Properties());
		tgazpop = loadOrCreateBTree(mydbgazpop, "population", a );

		//-------------------  APERTURA FILE B-TREE DEL GAZETTEER POPULATION2-------------------//

		RecordManager mydbgazpop2;
		BTree tgazpop2 = new BTree();
		mydbgazpop2 = RecordManagerFactory.createRecordManager(path+"db"+File.separator+"albero_Btree_population2", new Properties());
		tgazpop2 = loadOrCreateBTree(mydbgazpop2, "population2", a );

		//------------------- FINE CREAZIONE ALBERO B-TREE -------------------------//
		FileWriter file_log=new FileWriter(path+"log.txt");
		FileWriter file_di_prova=new FileWriter(path+"new_gazetteer.txt");
		
		String line;
		line = lr.readLine();
		int count=0;
		int nontrovati=0;
		int ritrovati=0;
		
		String paesename;
		String paeseasci;
		String altername;
		double[] f1 = new double[2];
		double[] f2 = new double[2];
		Region r;
		IData dati_rtree=null;
		IData dati_rtreeprescelto=null;
		double prescelto=0;
		double rismetrica=0;
		StringTokenizer st;
		double x1, x2, y1, y2,x3,y3;
		int trovati=0;
		int confermati=0;
		String mbr="";
		String mbr_gazzetter="";
		String punti[];
		String regione="";
		String nazione="";
		String multiplo="";
		//------------  SCORRO IL GAZETTEER E NE CREO UNO NUOVO 
		//-----------   INSERENDO PER OGNI PAESE ANCHE L' MBR
		//
		while (line != null)
		{
			String parola[]=line.split("\t");
			// DI TUTTI I PAESI CONTENUTI NEL GAZETTEER ESTRAPOLO SOLO QUELLI AMMINISTRATIVI
			if(parola[7].equalsIgnoreCase("ADM1") || parola[7].equalsIgnoreCase("ADM2") || parola[7].equalsIgnoreCase("ADM3") || parola[7].equalsIgnoreCase("ADM4"))
			{
				//if(((String)tcountryInfo.find(parola[8])).equalsIgnoreCase("EU"))
					count++;	
				
				paesename=parola[1];
				paeseasci=parola[2];
				altername=parola[3];
				paesename=(new String(paesename.getBytes(),"UTF-8")).toLowerCase();
				paesename=cambia_accenti(paesename);
				altername=cambia_accenti(altername);
				
				// UTILIZZANDO IL NOME PRESENTE NEL GAZETTEER QEST'ULTIMO LO RICERCO NEL FILE 
				// OTTENUTO DALLE MAPPE OSM IN MODO TALE DA RICAVARE L' MBR DEL PAESE 
				// QUINDI VENGONO EFFETTUATE 3 RICERCHE
				// LA 1A CERCA IL (NOME DEL PAESE)
				// LA 2A CERCA IL NOME (ASCI DEL PAESE)
				// LA 3A CERCA IL SUO (NOME ALTERNATIVO)
				mbr=(String) tosm.find(paesename);
				if(mbr==null){
					mbr=(String) tosm.find(paeseasci);
					if(mbr==null)
						mbr=(String) tosm.find(altername);
						if(mbr==null){
						//System.out.println(paesename+" non trovato");
						
						// SE LE 3 RICERCHE HANNO AVUTO ESITO NEGATIVO ALLORA CERCO TRAMITE R-TREE
							
						nontrovati++;
						f2[0]=new Double(parola[4]);
						f2[1] = new Double(parola[5]);
						f1[0] = new Double(parola[4]);
						f1[1] = new Double(parola[5]);
						r = new Region(f1,f2);	
						tree.intersectionQuery(r, vis);
						prescelto=0;
						rismetrica=0;
						// LA RICERCA TRAMITE R-TREE VIENE EFFETTUATA UTILIZZANDO IL CENTROIDE
						// QUINDI LA RICERCA RITORNA NOMI DI PIù PAESI CHE INTERSECANO CON IL CENTROIDE
						for(int num=0;num<vis.visitati.size();num++){
							dati_rtree=((IData)vis.visitati.elementAt(num));
							rismetrica=metrica.metric(paesename, new String(dati_rtree.getData()));
							if(rismetrica>prescelto){
								prescelto=rismetrica;
								dati_rtreeprescelto=dati_rtree;
							}
						}
						
						// DI QUESTI PAESI PRENDO QUELLO CHE HA "ASSOMIGLIA" DI PIù AL NOME DEL PAESE 
						// MA SOLO SE QUESTA PROBABILITà è PIù ALTA DEL 70%
						if(prescelto>0.70){
							ritrovati++;
							mbr_gazzetter=dati_rtreeprescelto.getShape().toString();
							punti=mbr_gazzetter.split(":");
							mbr_gazzetter=punti[1].split(" ")[0]+"£#"+punti[1].split(" ")[1]+"£#"+punti[0].split(" ")[0]+"£#"+punti[0].split(" ")[1];
						}
						else if(prescelto!=0){
							file_di_log1.addElement(paesename+" "+prescelto+"  "+(new String(dati_rtreeprescelto.getData()))+" "+dati_rtreeprescelto.getShape());
							mbr_gazzetter="0£#0£#0£#0";
						}
						r=null;
						vis.visitati.removeAllElements();
					}
				}
				// SE IL PAESE è STATO TROVATO VERIFICO CHE IL SUO CENTROIDE 
				// SIA EFFETTIVAMENTE DENTRO L' MBR
				if(mbr!=null){
					st = new StringTokenizer(mbr);
					x1 = new Double(st.nextToken(" ")).doubleValue();
					y1 = new Double(st.nextToken(" ")).doubleValue();
					x2 = new Double(st.nextToken(" ")).doubleValue();
					y2 = new Double(st.nextToken(" ")).doubleValue();
					x3 = new Double(parola[4]).doubleValue();
					y3 = new Double(parola[5]).doubleValue();
					trovati++;
					double distx=(x3-x1)>(x2-x3)?(x3-x1):(x2-x3);
					double disty=(y3-y1)>(y2-y3)?(y3-y1):(y2-y3);
					if(x3<x1 && x3>x2 && y3<y1 && y3>y2){
						confermati++;
						//mbr_gazzetter=mbr;
						mbr_gazzetter=mbr.split(" ")[0]+"£#"+mbr.split(" ")[1]+"£#"+mbr.split(" ")[2]+"£#"+mbr.split(" ")[3];
					}
					else{
						// SE IL CENTROIDE NON è NELL'MBR SIGNIFICA CHE NON è QUESTO IL PAESE CERCATO 
						// QUINDI EFFETTUO UNA NUOVA RICERCA SFRUTTANDO L' R-TREE
						f2[0]=new Double(parola[4]);
						f2[1] = new Double(parola[5]);
						f1[0] = new Double(parola[4]);
						f1[1] = new Double(parola[5]);
						r = new Region(f1,f2);	
						tree.intersectionQuery(r, vis);
						prescelto=0;
						rismetrica=0;
						for(int num=0;num<vis.visitati.size();num++){
							dati_rtree=((IData)vis.visitati.elementAt(num));
							rismetrica=metrica.metric(paesename, new String(dati_rtree.getData()));
							if(rismetrica>prescelto){
								prescelto=rismetrica;
								dati_rtreeprescelto=dati_rtree;
							}
						}
						if(prescelto>0.70){
							confermati++;
							mbr_gazzetter=dati_rtreeprescelto.getShape().toString();
							punti=mbr_gazzetter.split(":");
							mbr_gazzetter=punti[1].split(" ")[0]+"£#"+punti[1].split(" ")[1]+"£#"+punti[0].split(" ")[0]+"£#"+punti[0].split(" ")[1];
						}
						else if(prescelto!=0){
							file_di_log2.addElement(paesename+" "+parola[4]+" "+parola[5]+" "+mbr+" distanza longitudine="+(distx<0?0:distx)+" distanza latitudine="+(disty<0?0:disty));
							mbr_gazzetter="0£#0£#0£#0";//non confermato
						}
						r=null;
						vis.visitati.removeAllElements();
					}
					
				}
				// ORA AGGIUNGO AL NUOVO GAZETTEER ALTRE INFORMAZIONI COME (è NAZIONE? , è UNA REGIONE? , è COMPOSTA DA PIù PAROLE? )
				// QUESTE INFORMAZIONI SERVIRANNO POI NELLA FASE DI GEOTAGGING
				if(parola[10].equalsIgnoreCase("00") && parola[11].equalsIgnoreCase("") && parola[12].equalsIgnoreCase(""))
					nazione="1";
				else{
					nazione="0";
					if(!parola[10].equalsIgnoreCase("") && parola[11].equalsIgnoreCase("") && parola[12].equalsIgnoreCase(""))
						regione="1";
					else
						regione="0";
				}
				if(parola[1].contains(" "))
					multiplo="1";
				else
					multiplo="0";
				
				// CREO IL NUOVO GAZETTEER IN VERSIONE TESTUALE
				String scrittura="";
				String popolazione="";
				for(int numeroparole=0;numeroparole<parola.length;numeroparole++){
					
					if(numeroparole!=14)
					{
						if(numeroparole==15 && parola[15].equalsIgnoreCase(""))
							scrittura=scrittura+"0 ";
						else
							if(numeroparole==16 && parola[16].equalsIgnoreCase(""))
								scrittura=scrittura+"0 ";
							else
								scrittura=scrittura+parola[numeroparole]+" ";
					}
					else
					{
						if(!parola[13].equalsIgnoreCase("")){
							popolazione=(String)tgazpop2.find(parola[13]);
						}
						else
							if(!parola[12].equalsIgnoreCase("")){
								popolazione=(String)tgazpop.find(parola[12]);
							}	
						if(popolazione!=null)
							if(popolazione.equalsIgnoreCase(""))
								scrittura=scrittura+"0"+" ";
							else
								scrittura=scrittura+popolazione+" ";
						else
							scrittura=scrittura+"0"+" ";
					}
				}
				file_di_prova.write(scrittura+mbr_gazzetter+" "+nazione+" "+regione+" "+multiplo+"\r\n");
				// CREO IL NUOVO GAZETTEER IN VERSIONE B-TREE
				scrittura="";
				for(int numeroparole=0;numeroparole<parola.length;numeroparole++)
				{

					if(numeroparole!=14){
						
						if(numeroparole==15 && parola[15].equalsIgnoreCase(""))
							scrittura=scrittura+"0£#";
						else
							if(numeroparole==16 && parola[16].equalsIgnoreCase(""))
								scrittura=scrittura+"0£#";
							else
								scrittura=scrittura+parola[numeroparole]+"£#";		
					}
					else
					{
						if(!parola[13].equalsIgnoreCase("")){
							popolazione=(String)tgazpop2.find(parola[13]);
						}
						else
							if(!parola[12].equalsIgnoreCase("")){
								popolazione=(String)tgazpop.find(parola[12]);
							}	
						if(popolazione!=null)
							if(popolazione.equalsIgnoreCase(""))
								scrittura=scrittura+"0"+"£#";
							else
								scrittura=scrittura+popolazione+"£#";
						else
							scrittura=scrittura+"0"+"£#";
					}
				}
				
				tgaz.insert(parola[0], scrittura+mbr_gazzetter+"£#"+nazione+"£#"+regione+"£#"+multiplo, true);
				
				// OLTRE AL NUOVO GAZETTEER CREO ANCHE UN B-TREE INTERMEDIO 
				
				String trovato;
				String indici[];
				boolean indice_trovato;
				trovato=(String) tinter.find(parola[1]);
				
				for(int volte=0;volte<2;volte++)
				{
					if(trovato!=null){
						indici=trovato.split("£#");
						indice_trovato=false;
						for(int in=0;in<indici.length;in++)
							if(indici[in].equalsIgnoreCase(parola[0]))
								indice_trovato=true;
						if(indice_trovato==false)
							tinter.insert(parola[1+volte], trovato+"£#"+parola[0], true);
					}
					else
						tinter.insert(parola[1+volte], parola[0], true);
				}
				if ((count % 100) == 0){
					  mydbgaz.commit();
					  mydbinter.commit();
				}
						
			}
			peso_decrementato=peso_decrementato-line.getBytes().length-2;
			frame.put(100-(int)((peso_decrementato*100)/peso));

			line = lr.readLine();
			
		}
		file_di_prova.close();
		if ((count % 100) != 0 && count!=0)
			mydbgaz.commit();
		System.out.println("totale da cercare "+count);
		System.out.println("trovati "+trovati);
		System.out.println("confermati "+confermati);
		System.out.println("non trovati nel b-tree "+nontrovati);
		System.out.println("ritrovati nell'r-tree "+ritrovati);
		
		
		file_log.write("Riassunto ricerca:\r\n");
		file_log.write("totale da cercare "+count+"\r\n");
		file_log.write("trovati "+trovati+"\r\n");
		file_log.write("confermati "+confermati+"\r\n");
		file_log.write("non trovati nel b-tree "+nontrovati+"\r\n");
		file_log.write("ritrovati nell'r-tree "+ritrovati+"\r\n");
		file_log.write("\r\n---------------------------------------------------------------\r\n");		
		
		file_log.write("Paesi non trovati nel file albero_Btree_osm.db e non ritrovati nell'r-tree\r\n\r\n");
		
		for(int num=0;num<file_di_log1.size();num++)
			file_log.write(file_di_log1.elementAt(num)+"\r\n");
		file_log.write("\r\nTotale non trovati: "+(nontrovati-ritrovati)+" Paesi");
		file_log.write("\r\n---------------------------------------------------------------\r\n");
		file_log.write("Paesi trovati ma non confermati (il centroide è al di fuori del bounding box)\r\n\r\n");
		
		for(int num=0;num<file_di_log2.size();num++)
			file_log.write(file_di_log2.elementAt(num)+"\r\n");
		file_log.write("\r\nTotale non confermati: "+(trovati-confermati)+" Paesi");
		mydbgaz.close();
		mydbosm.close();
		mydbinter.close();
		file_log.close();
		frame.finito();
				
	}
	public static String cambia_accenti(String a){
		String b="";
		b=a.replace("e'","è");
		b=a.replace("i'","ì");
		b=a.replace("o'","ò");
		b=a.replace("u'","ù");
		b=a.replace("a'","à");
		return b;
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
