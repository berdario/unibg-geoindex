package principale.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.logging.Logger;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import principale.scansione.BUnzip2;
import principale.scansione.ScanXML;

public class Dbcreator {
	private static String path,dbpath,cachepath,configfile,slash;
	File requiredFiles[];
	public int step=0;
	
	public Dbcreator(String configpath){
		//set default paths
		String os=System.getProperty("os.name");
		String homepath=System.getProperty("user.home");
		slash=System.getProperty("file.separator");
		if (os.startsWith("Linux")){
			path=System.getenv("XDG_DATA_HOME");
			if (path==null){
				path=homepath+"/.local/share/";
			}
			path+="geosearch/";
			
			configfile=System.getenv("XDG_CONFIG_HOME");
			if (configfile==null){
				configfile=homepath+"/.config/";
			}
			configfile+="geosearch/config";
			
			cachepath=System.getenv("XDG_CACHE_HOME");
			if (cachepath==null){
				cachepath=homepath+"/.cache/";
			}
			cachepath+="geosearch/";
			
		} else if (os.startsWith("Windows")){
			path=System.getenv("APPDATA")+slash+"geosearch"+slash;
			configfile=path+"config";
			cachepath=path+"cache"+slash;
		} else if (os.startsWith("Mac")){
			path=homepath+"/Library/Application Support/geosearch/";
			configfile=path+"config";
			cachepath=path+"cache/";
		} else {
			path=System.getProperty("user.dir")+"geosearch"+slash;
			configfile=path+"config";
			cachepath=path+"cache"+slash;
		}
		dbpath=path+"db"+slash;
		
		if (configpath!=null){
			configfile=configpath;
		}
		
		loadConfiguration();
	}
	
	public void loadConfiguration(){
		File cfgfile=new File(configfile);
		if (!cfgfile.exists()){
			createDefaultConfiguration();
		}
		try {
			PropertiesConfiguration config = new PropertiesConfiguration(configfile);
			
			path=config.getString("basepath");
			dbpath=path+config.getString("dbdirectory")+slash;
			
			String[] requiredFileNames=config.getStringArray("requiredfiles");
			String[] requiredDbFileNames=config.getStringArray("requireddbfiles");
			File[] innerRequiredFiles=new File[requiredDbFileNames.length+requiredFileNames.length];
			
			int i=0;
			for (String rf : requiredFileNames){
				innerRequiredFiles[i]=new File(path+rf);
				i++;
			}
			
			for (String rf : requiredDbFileNames){
				innerRequiredFiles[i]=new File(dbpath+rf);
				i++;
			}
			requiredFiles=innerRequiredFiles;
			
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void createDefaultConfiguration() {
		String input=null,cacheinput="";
		
		File cfgfile=new File(configfile);
		File cfgpath=cfgfile.getParentFile();
		if (!cfgpath.exists()){
			cfgpath.mkdirs();
		}
		
		String filenames[]={"datiscritti.dat","datiscritti.idx","albero_Btree_osm.db",
				"albero_Btree_population.db","albero_Btree_population2.db",
				"albero_Btree_Gazetteer.db", "albero_Btree_Intermedio.db",
				"albero_alternatenames.db","albero_alternatenamesId.db",
				"albero_admin1codeascii.db","albero_countryInfo.db",
				"albero_featurecodes.db"};
		try{//fare attenzione: tutte le funzioni interne si aspettano path, assoluti... TODO sanitarizzare
			System.out.println("Missing configuration file, do you want to create one? [Y/n]");
			BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
			while (true){
				input = in.readLine();
				if (input.equalsIgnoreCase("y")||input.equals("")){
					System.out.println("Please insert data path ["+path+"]:");
					input=in.readLine();
					if (!input.equals("")){
						cachepath=input+"cache/";
						System.out.println("Please insert cache path ["+cachepath+"]:");
						cacheinput=in.readLine();
					}
					break;
				}else if (input.equalsIgnoreCase("n")){
					System.exit(0);
				}
			}
			
			cfgfile.createNewFile();
			
			PropertiesConfiguration config=new PropertiesConfiguration(configfile);
			if (input.equals("")){
				config.setProperty("basepath", path);
				config.setProperty("cachepath", cachepath);
			} else {
				config.setProperty("basepath", input);
				if (cacheinput.equals("")){
					config.setProperty("cachepath", cachepath);
				} else {
					config.setProperty("cachepath", cacheinput);
				}
			}
			config.setProperty("dbdirectory", "db");
			config.setProperty("requiredfiles", "coordinate.txt");
			config.setProperty("requireddbfiles", filenames);
			config.setProperty("languagefile", "englishSW.txt");
			
			config.save();
		} catch (ConfigurationException e) {		
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public boolean verificaFile(){
		boolean flag=true;
		
		File basepath = new File(path);
		if (!basepath.exists()){
			basepath.mkdirs();
			System.out.println("Per favore inserisci i file necessari in "+path);
			System.exit(1);
		} else{
			System.out.println("Verifica presenza strutture dati:");
			try {
				for (File f : requiredFiles) {
					System.out.println(f.getCanonicalPath() + ": "+ (f.exists() ? "OK" : "MANCA"));
					if (!f.exists()) {
						flag = false;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if (!flag){//se bisogna creare il database, allora controllo che ci siano tutti i file necessari

			//TODO per ora sono fissi: eventualmente parametrizzare/inserire in un file di configurazione e permettere di caricare stopwords in lingua diversa o comunque altri file... it.txt e alterNames dovrebbero poter venire accettati anche zippati...
			String[] originFilenames={"stopWords","stopWords"+slash+"englishSW.txt","admin1CodesASCII.txt","allCountries.zip","alternateNames.txt","iso-languagecodes.txt","countryInfo.txt","featureCodes.txt","geoStopwords.txt","IT.txt","italy.osm.bz2","street.txt"};
			
			boolean originFilesFlag=true;
			String warning="";

			for (String f : originFilenames){
				if (!(new File(path+f)).exists()){
					if (originFilesFlag){
						originFilesFlag=false;
						warning+="\nPlease insert into "+path+" the following files:\n";
					}
					warning+=f+"\n";
				}
			}
			
			if (!warning.equals("")){
				System.out.println(warning);
				System.exit(1);
			}
		}
		
		return flag;
		
		/*File verificar_tree1=new File(dbpath+"datiscritti.dat");
        File verificar_tree2=new File(dbpath+"datiscritti.idx");
        File verificar_tree3=new File(dbpath+"albero_Btree_osm.db");
        
        File verificapop1=new File(dbpath+"albero_Btree_population.db");
        File verificapop2=new File(dbpath+"albero_Btree_population2.db");
        
        
        File verificagaz1=new File(dbpath+"albero_Btree_Gazetteer.db");
        File verificagaz2=new File(dbpath+"albero_Btree_Intermedio.db");
        
        File verificaalter1=new File(dbpath+"albero_alternatenames.db");
        File verificaalter2=new File(dbpath+"albero_alternatenamesId.db");
    
        File verificaresto1=new File(dbpath+"albero_admin1codeascii.db");    
        File verificaresto2=new File(dbpath+"albero_countryInfo.db");
        File verificaresto3=new File(dbpath+"albero_featurecodes.db");*/
		
		
	}
	
	public void createDB(int step){
		
		switch (step) {
		case 0:
			caricaOSM();
			break;
		case 1:
			CaricaRtree();
			break;
		case 2:
			CaricaPopulation();
			break;
		case 3:
			CaricaGazetteer();
			break;
		case 4:
			CaricaAlternames();
			break;
		case 5:
			caricaAltro();
			break;
		}
	}
	
	public void createDB(boolean interactive){
		if (interactive){
			BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
			String[] step={"OSM","Rtree","dati popolazione","gazetteer","alternames","file restanti"};
			String input=null;
			for (int i=0;i<step.length;i++){
				while (true) {
					System.out.println("caricare " + step[i] + "? [Y/n]");
					try {
						input = in.readLine();
					} catch (IOException e) {
						e.printStackTrace();
					}
					if (input.equalsIgnoreCase("y") || input.equals("")) {
						createDB(i);
						break;
					} else if (input.equalsIgnoreCase("n")) {
						break;
					}
				}
			}

		} else {
			caricaOSM();

			CaricaRtree();

			CaricaPopulation();

			CaricaGazetteer();

			CaricaAlternames();

			caricaAltro();
		}
		
	}	

	private void caricaOSM(){
		
			String osmpath=path+"italy.osm.bz2"; //TODO
			/*
			 * prima era OBBLIGATORIO passarlo come parametro, ora è meglio: usa
			 * autonomamente il file, ma bisognerebbe migliorarlo (se esiste un
			 * solo osm.bz2 scegliere quello? presentare una scelta all'utente?
			 * metterlo fisso nel file di config è troppo limitante, ma
			 * chiederlo ogni volta non va bene comunque imho)
			 */
			//long Tempo1=System.currentTimeMillis();
			//System.out.println(path[0].substring(path[0].length()-3,path[0].length() ));
	    	if(osmpath.substring(osmpath.lastIndexOf('.')+1, osmpath.length()).equalsIgnoreCase("bz2")){
	    		BUnzip2 b=new BUnzip2(osmpath);
	    		Thread g=new Thread(b);
	    		g.setName("Scompatta");
	        	g.start();
	        	try{
	        	g.join();
	        	}
	        	catch (Exception e) {
					// TODO: handle exception
				}
	    	}

	    	if(osmpath.substring(osmpath.lastIndexOf('.')+1, osmpath.length()).equalsIgnoreCase("osm")){
	        	sistemaxml a=new sistemaxml();
	        	a.sistema(osmpath,osmpath+".out");

	    		ScanXML parsing=new ScanXML(osmpath+".out",0L,0L,true,true);
	    		
	    		
	    	}		
	    	//long Tempo2=System.currentTimeMillis();
		
	}
	
	private void CaricaRtree(){
		
			
			RTreeLoad Rtree =new RTreeLoad();
	        Rtree.carica();
		
	}

	private void CaricaPopulation(){
		
			
			readpopulation r=new readpopulation();
		    try{
		    	r.carica(path);
		    }
		    catch (Exception e) {
				// TODO: handle exception
			}
		
	}

	private void CaricaGazetteer(){
		
			
			readgazetteer r=new readgazetteer();
	        try{
	        	r.carica(path);
	        }
	        catch (Exception e) {
				// TODO: handle exception
			}
		
	}
	
	private void CaricaAlternames(){
		
			
			read_alternateNames r=new read_alternateNames();
	        try{
	        	r.carica(path);
			}catch (Exception e) {
				// TODO: handle exception
			}
		
	}
	
	private void caricaAltro(){
		try {
			admin1codeascii.carica(path);
			countryInfo.carica(path);
			featurecodes.carica(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String getPath(){
		return path;
	}
	
	public static String getDBPath(){
		return dbpath;
	}
	
	public static int pid() {
		String id = ManagementFactory.getRuntimeMXBean().getName();
		String[] ids = id.split("@");
		return Integer.parseInt(ids[0]);
		}


		public static void listOpenFiles() throws IOException {
		int pid = pid();
		Runtime runtime = Runtime.getRuntime();
		Process process = runtime.exec("lsof -p " + pid);
		InputStream is = process.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String line;

		while ((line = br.readLine()) != null) {
		System.out.println(line);
		}
	}
}
