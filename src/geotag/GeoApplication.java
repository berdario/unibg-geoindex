/*
 * GeoApplication.java
 *
 * Created on August 27, 2008, 9:49 AM
 */

package geotag;

import geotag.analysis.Filter;
import geotag.analysis.GeoCandidateIdentification;
import geotag.analysis.Score;
import geotag.analysis.WordAnalyzer;
import geotag.georeference.GeoRef;
import geotag.georeference.GeoRefLocation;
import geotag.indices.ContentIndexer;
import geotag.indices.GeographicIndexer;
import geotag.output.CreateOutput;
/*import geotag.parser.HTMLParser;
import geotag.parser.PDFParser;
import geotag.parser.XMLParser;*/
import geotag.parser.DocumentWrapper;
import geotag.parser.DocumentWrapper.UnsupportedFileException;
import geotag.search.ContentSearcher;
import geotag.search.DistanceSearcher;
import geotag.visualization.CountryItemListener;
import geotag.visualization.GeoWordsTable;
import geotag.visualization.MarkerChart;
import geotag.visualization.ResultsTable;
import geotag.words.GeoRefDoc;
import geotag.words.GeographicWord;
import geotag.words.StringOperation;
import geotag.words.Word;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.CharBuffer;
import java.nio.charset.CodingErrorAction;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.queryParser.ParseException;
import org.jdom.JDOMException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
//import org.apache.commons.configuration.Configuration;
//import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author  giorgio
 */
public final class GeoApplication implements Runnable{
    //private Vector<GeoRefDoc> resultsmerge = new Vector<GeoRefDoc>();
    
    private String swLanguage;
    
    NumberFormat formatter = new DecimalFormat("#0.00");  //Formatto il numero a 2 cifre decimali
    
    double goodGeoScore = 0.65; //0.65   
    double uniqueGWScore = 0.2;
    
    private static String path,dbpath,cachepath,configfile,slash;
    private ArrayList<File> indexDirs;
	
	//public class GeoApplicationCmd{
		private void GeoApplicationCmd() {
			boolean flag=true;
			//InputStreamReader cin = new InputStreamReader(System.in);
			BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
			//CharBuffer target=CharBuffer.allocate(200);//sostituisco charbuffer con bufferedreader
			String buffer;
			String[] pair;
			System.out.print("LocationBasedSearch:\n" +
					"------------------------------------------------------------" +
					"--------------------\n" +
					"Interactive prompt: to quit type \"quit\" or press Ctrl-D\n");
			flag=true;
			do{
	    		try {
	    			System.out.print(">");
	    			buffer=in.readLine();
					
					if( buffer==null || buffer.equals("quit") ){
						flag=false;
					} else if ((pair=buffer.split(",")).length==2){
						cmdSearch(pair[0],pair[1]);
					} else if ((pair=buffer.split(" ")).length==2){
						cmdSearch(pair[0],pair[1]);
					}
					/*if (target.toString().equals("help")){
						
					}*/else if (!buffer.equals("")){
						System.out.print("unknown command\n");
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			} while (flag);
		}
		//TODO questa modifica non mi piace molto
		
		private void cmdSearch(String keywords, String location){
			Vector<GeoRefDoc> results=search(keywords,location);
			int i=0;
			for (GeoRefDoc doc : results) {
				i++;
				System.out.println(i+"° documento:\n"+doc.getNomeDoc()+
						"\ntextscore: "+doc.getTextScore()+
						"\nsortscore: "+doc.getSortScore()+
						"\ndistancescore: "+doc.getDistanceScore()+
						"\nid: "+doc.getId()+
                                                "\ntitle: "+doc.docTitle+
                                                "\ndescription: "+doc.docDescription+
                                                "\nkeywords: "+doc.docKeyWords+
                                                "\ndateline: "+doc.docDateLine+"\n");
			}
		}
		
	//}
	
	public String createIndex(File curDir){
        String errortext="";
        String documentContent = "";   
        String documentExtension = "";
        String documentName = "";
        double importanceValue = 0.0;  //E' un valore che indica l'importanza di un termine nel doc
        //non trattando più come prima titoli, descrizioni e keyword, non ne si ricerca la presenza di geoword all'interno
        //TODO valutare se è ancora utile (probabilmente no)
        String hash;
        ContentIndexer contInd = new ContentIndexer();
        
   
        if(curDir.isDirectory()){
            File[] nameFiles = curDir.listFiles();           
            for (int i = 0; i < nameFiles.length; i++){
                if (!nameFiles[i].isDirectory()) {
                    Vector<Word> wordVector = new Vector<Word>();
                    Vector<Word> filterWordVector = new Vector<Word>();
                    Vector<GeographicWord> geoWordVector = new Vector<GeographicWord>();
                    Vector<GeographicWord> finalGeoWordVector = new Vector<GeographicWord>();
                    Vector<Word> finalWordVector = new Vector<Word>();
                    Vector<Word> finalFilterWordVector = new Vector<Word>();
                    boolean alreadyIndexed = false;
                    boolean upperDateLine = false;

                    // Gestione di ogni file contenuto nella directory
                    String name = nameFiles[i].getName();
                    documentExtension = nameFiles[i].getName().substring(nameFiles[i].getName().lastIndexOf('.') + 1, nameFiles[i].getName().length());

                    documentName = nameFiles[i].getName().substring(0, nameFiles[i].getName().lastIndexOf('.'));

                    DocumentWrapper doc;
                    GeoRefDoc geoDoc = new GeoRefDoc();
                    try {
                        doc = new DocumentWrapper(nameFiles[i], documentExtension);
                        documentContent = doc.content;
                        geoDoc.docTitle = doc.title;
                        geoDoc.docDescription=doc.description;
                        geoDoc.docDateLine=doc.dateline;
                        geoDoc.docKeyWords=doc.keywords;
                    } catch (UnsupportedFileException ex) {
                        Logger.getLogger(GeoApplication.class.getName()).log(Level.INFO, nameFiles[i].getName() + " is not a supported file", ex);
                    }


                        //errortext+=""

                        /* commentato: nel caso lo si può riaggiungere, ma per evitare un if(gui) sarebbe necessario reimplementarlo con una sottoclasse e quindi ripensare l'architettura di geoapplication
                         * comunque pare che non essendo parte di un thread questo non possa comparire nella gui così facilmente
                         * 
                        operationsTextArea.append("**********\n");
                        operationsTextArea.append("File name: " + nameFiles[i].getName() + "\n");
                        operationsTextArea.append("Start analysis...\n");
                        */
                        
                        //Memorizzo ORA di inizio dell'elaborazione
                        Date start = new Date();
                                
                        try {
                        	hash = DigestUtils.md5Hex(nameFiles[i].toURI().toString());
                        	
                        	//Controllo se il file è già stato indicizzato                              

                        	
                            alreadyIndexed = contInd.control(hash);
                            
                            System.out.println("\n  File name: " + documentName);
                            
                            //DatabaseGazetteerConnection dbConnection = new DatabaseGazetteerConnection(DRIVER_CLASS_NAME, USER_NAME, PASSWORD, DB_CONN_STRING);
                            Statement stmt = null;//dbConnection.getStatement();
                            
                            if(!alreadyIndexed){
                            	
                            	//TODO verificare il comportamento con file con lo stesso nome
                            	FileUtils.copyFile(nameFiles[i], new File(cachepath + hash +"."+ documentExtension));
                            	
                                
                                    // Estrazione delle Word candidate                                    
                                    WordAnalyzer generator = new WordAnalyzer(); 
                                    wordVector = generator.getWordVector(documentContent, swLanguage); 

                                    // Fase di FILTRO
                                    Filter myFilter = new Filter();
                                    filterWordVector = myFilter.filtering(wordVector, documentContent, upperDateLine);

                                    // Fase di GEO-VALUTAZIONE                                                                                   
                                    GeoCandidateIdentification geoAnalysis = new GeoCandidateIdentification();                            
                                    geoWordVector = geoAnalysis.analyzing(filterWordVector, wordVector, documentContent, importanceValue, stmt, upperDateLine);

                                    //Tra elementi con uguale geonameid ne prendo solo 1 con peso e importanza maggiore 
                                    finalGeoWordVector = importanceControl(geoWordVector, finalGeoWordVector); 
                                    finalWordVector = update(finalWordVector, wordVector);
                                    finalFilterWordVector = update(finalFilterWordVector, filterWordVector);
                                    
                                    //Indicizzo il contenuto del file 
                                    //PRIMA ERA COMMENTATO
                                     contInd.indexing(documentContent, geoDoc, hash);
                                                                        
                                    
                                 
                                
                                
                                    // Fase di SCORING   
                                    Score newScore = new Score();
                                    finalGeoWordVector = newScore.updateGeoScore(finalGeoWordVector, finalWordVector, swLanguage, finalFilterWordVector, stmt);
                                    //stmt.close();
                                    
                                    //Elimino le GeoWord con peso sotto lo 0.6
                                    finalGeoWordVector = selectGeoWords(finalGeoWordVector);

                                    //GEOREFERENZIAZIONE
                                    GeoRef geoReferencing = new GeoRef();
                                    finalGeoWordVector = geoReferencing.calculateGeoRefValue(finalGeoWordVector);
                                    finalGeoWordVector = geoReferencing.delete(finalGeoWordVector);	//Controllo incrociato tra GeoScore e GeoRef

                                    /*for(int r=0; r < finalGeoWordVector.size(); r++){
                                        System.out.println(finalGeoWordVector.elementAt(r).getName() + " - "
                                                + formatter.format(finalGeoWordVector.elementAt(r).getGeoScore()) + " - "
                                                + formatter.format(finalGeoWordVector.elementAt(r).getGeoRefValue()));
                                    }*/

                                    //Aggiornamento dell'indice geografico
                                    //	VECCHIO CODICE
                                    //GeographicIndexer geoIndex = new GeographicIndexer(); //INDICIZZAZIONE delle GeoWords
                                    //geoIndex.indexing(finalGeoWordVector, nameFiles[i].getName(), swLanguage);

                                    //INIZIO MODIFICA
                                    for(int ind = 0; ind < finalGeoWordVector.size(); ind++){
                                        GeographicWord gw = finalGeoWordVector.elementAt(ind);                                    
                                        // cerco nell'r-tree
                                        System.out.print(finalGeoWordVector.elementAt(ind).getName() + " - geoscore: "
                                                + formatter.format(finalGeoWordVector.elementAt(ind).getGeoScore()) + " - georefvalue: "
                                                + formatter.format(finalGeoWordVector.elementAt(ind).getGeoRefValue())+" - codici: ");
                                        
                                        vettori vettoricodici=new vettori();
                                        vettoricodici=query_rtree.query(path,gw.getmbr_x1(), gw.getmbr_y1(),gw.getmbr_x2(), gw.getmbr_y2());
                                        boolean trovato=false;
                                        for(int trovati=0;trovati<vettoricodici.codici.size();trovati++)
                                        {
                                        	System.out.print(vettoricodici.codici.elementAt(trovati)+" ");
                                        	File file = new File(dbpath+(Integer.parseInt(vettoricodici.codici.elementAt(trovati))/1000)+slash+vettoricodici.codici.elementAt(trovati));
                                        	if (file.exists()){

                                        		FileReader reader=new FileReader(file);
                                        		LineNumberReader lr = new LineNumberReader(reader);
                                        		String line = lr.readLine();
                                        		String data[];
                                        		while (line != null)
                                        		{
                                        			data=line.split("�#");
                                        			if(data[0].equalsIgnoreCase(hash))
                                        				trovato=true;
                                        			line = lr.readLine();
                                        		}
                                        		reader.close();
                                        		
                                        	}
                                        	
                                        	if(trovato==false){
                                        		FileWriter file2=new FileWriter(dbpath+(Integer.parseInt(vettoricodici.codici.elementAt(trovati))/1000)+slash+vettoricodici.codici.elementAt(trovati),true);
                                        		file2.write(hash+"�#"+gw.getGeoScore()+"�#"+gw.getGeoRefValue()+"\r\n");
                                        		file2.close();
                                        	}
                                        }
                                        System.out.println();
                                    }
                                    //FINE MODIFICA
                                    
                                    //Creazione dei file di output                 
                                    CreateOutput output = new CreateOutput(finalGeoWordVector, documentName);
                                }
                            else{ //erano errorlabel, da ripristinare?
                                errortext+="Il file " + nameFiles[i].getName() +" è già stato indicizzato\n";
                            }
                                
                        } catch (IOException ex) {
                            errortext+="Error reading file\n";
                            ex.printStackTrace();
                        }/* catch (ParseException ex) {
                            errortext+="Error DataBase operation\n";
                        } */
	//                        catch (SQLException ex) {
	//                           errorLabel.setText("Error DataBase operation");
	//                        } 
	//                        catch (InstantiationException ex) {
	//                            errorLabel.setText("Error DataBase connection");
	//                        } 
	//                        catch (IllegalAccessException ex) {
	//                            errorLabel.setText("Error DataBase access");
	//                        } 
	//                        catch (ClassNotFoundException ex) {
	//                            errorLabel.setText("Error !!");
	//                        }
                    
                        //ORA di fine dell'elaborazione
                        Date end = new Date();
                        System.out.print("Time: " + (end.getTime() - start.getTime()) / 1000 + " seconds");                    
                        
                }
            }
        }
        contInd.closeIndex();   
		return errortext;
		
	}
	
	public String createIndex() {
		String errortext="";
		for (File f:indexDirs){
			errortext+=createIndex(f);
			//ContentIndexer.closeIndex();
		}
		return errortext;
	}

	public Vector<Word>  update (Vector<Word> finalWordVector, Vector<Word> wordVector){
	    
	    if(finalWordVector.size() == 0){
	        for(int i = 0; i < wordVector.size(); i++){
	            finalWordVector.add(wordVector.elementAt(i));
	        }
	    }else{
	        for(int i = 0; i < finalWordVector.size(); i++){
	            Word gw1 = finalWordVector.elementAt(i);
	            
	            for(int j = 0; j < wordVector.size(); j++){
	                Word gw2 = wordVector.elementAt(j);
	                if(gw1.getGeonameid() == gw2.getGeonameid()) 
	                    gw1.setFrequency(gw1.getFrequency() + 1);
	                else
	                    finalWordVector.add(gw2);
	            }  
	        }
	
	    }
	    
	    return finalWordVector;
	}

	/**
	 * Metodo che controlla se nel vettore ci sono GeoWord con lo stesso geonameId
	 * In questo caso seleziona solo la GeoWord con importanza maggiore
	 * @param finalGeoWordVector : vettore delle GeoWord
	 * @return il vettore delle GeoWord senza zone doppie
	 */
	public Vector<GeographicWord>  importanceControl (Vector<GeographicWord> geoWordVector, Vector<GeographicWord> finalGeoWordVector){
	    Vector<GeographicWord> newGeoWordVector = new Vector<GeographicWord>();
	    boolean diverso = true;
	    
	    //Se è la prima volta che popolo il vettore
	    if(finalGeoWordVector.size() == 0){
	        for(int i = 0; i < geoWordVector.size(); i++){
	            newGeoWordVector.add(geoWordVector.elementAt(i));
	        }
	    }else{       
	        for(int i = 0; i < finalGeoWordVector.size(); i++){
	            GeographicWord gw1 = finalGeoWordVector.elementAt(i);
	            diverso = true;
	            
	            for(int j = 0; j < geoWordVector.size(); j++){
	                GeographicWord gw2 = geoWordVector.elementAt(j);
	                if(gw1.getGeonameid() == gw2.getGeonameid()){                                              
	                    
	                    //Nuova freq
	                    int newFreq = gw1.getFrequency() + gw2.getFrequency();                                                  
	                    
	                    //Nuova Importance                                
	                    double newImp = gw1.getImportance() + gw2.getImportance();                            
	                   
	                    GeographicWord newGW = new GeographicWord();
	                    newGW = gw1;
	                    newGW.setFrequency(newFreq);
	                    newGW.setImportance(newImp);
	                    
	                    newGeoWordVector.add(newGW);
	                    diverso = false;                        
	                }
	            }
	
	            if(diverso)
	                newGeoWordVector.add(gw1);    
	        }
	        
	        //Inserisco le NUOVE geoWord trovate
	        for(int i = 0; i < geoWordVector.size(); i++){
	            GeographicWord gw3 = geoWordVector.elementAt(i);
	            boolean different = true;
	            
	            for(int j = 0; j < newGeoWordVector.size(); j++){
	                GeographicWord gw4 = newGeoWordVector.elementAt(j);
	                
	                if(gw3.getGeonameid() == gw4.getGeonameid())
	                    different = false;
	            }
	            
	            if(different){
	                gw3.setGeoScore(gw3.getGeoScore() + uniqueGWScore);
	                newGeoWordVector.add(gw3); 
	            }
	        }
	    }
	    return newGeoWordVector;
	}

	/**
	 * Metodo che seleziona le GeoWords da mantenere dopo la fase di pesatura e che quindi 
	 * verranno indicizzate e mostrate come output. 
	 * @param geoWordVector : elenco delel GeoWord
	 * @return elenco delle GeoWord ritenute valide per il documento
	 */
	public Vector<GeographicWord> selectGeoWords(Vector<GeographicWord> geoWordVector){
	    Vector<GeographicWord> newGeoWordVector = new Vector<GeographicWord>();
	    boolean control = false;
	    double geoScoreBoundary;
	    
	    
	    
	    for(int j = 0; j < geoWordVector.size(); j++){
	        GeographicWord gw = geoWordVector.elementAt(j);
	        double geoScore = gw.getGeoScore();
	        double geoReferenceValue = gw.getGeoRefValue();
	        int frequency = gw.getFrequency();
	        String geoName = gw.getName();
	        String zoneDocName = gw.getZoneDocName();
	        
	        /* Seleziono solo le Word che hanno un nome più lungo di una lettera
	         * e con peso > di un peso preso come limite minimo: goodGeoScore 
	        */ 
	        if(geoName != null && geoName.length()>1 && zoneDocName.length() > 1 && geoScore >= goodGeoScore){
	            //Alle zone con geoScore > 1 devo settarlo ad 1
	            if(geoScore > 1.0){
	                gw.setGeoScore(1.0);
	            }
	            
	            //Alle zone con frequenza uguale a 0 metto 1
	            if(frequency == 0){
	                gw.setFrequency(1);
	            }
	                
	            if(geoScore > goodGeoScore){   
	                //if(!(geoScore <= 0.7 && geoReferenceValue <= 0.2))
	                    newGeoWordVector.add(gw);
	            }
	                
	        }
	    }
	    
	    //Controllo se zona con peso > 0.6 è unica, in questo caso aumento di 0.1 il peso
	    if(newGeoWordVector.size() == 1){
	        GeographicWord gw = newGeoWordVector.elementAt(0);
	        double score = gw.getGeoScore();
	        if(score >= goodGeoScore && ((score + goodGeoScore) < 1.0)){
	            gw.setGeoScore(score + uniqueGWScore);
	        }
	    }
	
	
	    return newGeoWordVector;
	}

    public Vector<GeoRefDoc> search(String keyWords, String location) {

        Vector<GeoRefDoc> results = new Vector<GeoRefDoc>();


        ContentSearcher content = new ContentSearcher();
        results = content.createTextualRankig(keyWords);

        GeoRefLocation grLoc = new GeoRefLocation();
        results = grLoc.mergeLocation(results, location);

        DistanceSearcher distanceSorter = new DistanceSearcher();
        distanceSorter.createDistanceRanking(results, grLoc.getGeoLocation(location, null));

        ResultsTable resultSorter = new ResultsTable();
        //ordinamento dei risultati di default, bilanciato fra place e keyword
        results = resultSorter.createRanking(results, 50);


        return results;
    }
	
	public void run(){
		new GeoApplicationGui().setVisible(true);
	}
	
	public static String getPath(){
		return path;
	}
	
	public GeoApplication(String cfgpath){
		slash = System.getProperty("file.separator");
		
		if (cfgpath!=null){
			configfile=cfgpath;
		} else {
			// set default config path

			String os = System.getProperty("os.name");
			String homepath = System.getProperty("user.home");
			if (os.startsWith("Linux")) {

				configfile = System.getenv("XDG_CONFIG_HOME");
				if (configfile == null) {
					configfile = homepath + "/.config/";
				}
				configfile += "geosearch/config";

			} else if (os.startsWith("Windows")) {
				configfile = System.getenv("APPDATA") + slash + "geosearch"
						+ slash + "config";
			} else if (os.startsWith("Mac")) {
				configfile = homepath
						+ "/Library/Application Support/geosearch/" + "config";
			} else {
				configfile = System.getProperty("user.dir") + "geosearch"
						+ slash + "config";
			}
		}
		
		loadConfiguration();
	}
	
	public void loadConfiguration(){
		File cfgfile=new File(configfile);
		if (!cfgfile.exists()){
			System.out.println("Missing configuration file, please run Dbcreator first");
			System.exit(0);
		}
		PropertiesConfiguration config;
		try {
			config = new PropertiesConfiguration(configfile);
			path=config.getString("basepath");
			dbpath=path+config.getString("dbdirectory")+slash;
			cachepath=config.getString("cachepath");
			swLanguage=path+"stopWords"+slash+config.getString("languagefile");
		} catch (ConfigurationException e) {		
			e.printStackTrace();
		}
	}
	
	private ArrayList<File> updateIndexConfig(String inputpath) {//da aggiungere controllo? permette di aggiungere in continuazione lo stesso path
		try {
			PropertiesConfiguration config = new PropertiesConfiguration(configfile);
			ArrayList<File> innerIndexDirs=updateIndexConfig();
			
			if (inputpath!=null){
				File inputfile=new File(inputpath);
				if (!innerIndexDirs.contains(inputfile)){
					innerIndexDirs.add(new File(inputpath));
					config.setProperty("indexdirs", innerIndexDirs);
					config.save();
				}
			}
			
			return innerIndexDirs;
		} catch (ConfigurationException e) {		
			e.printStackTrace();
			return null;
		}
	}
	
	private ArrayList<File> updateIndexConfig() {
		try {
			PropertiesConfiguration config = new PropertiesConfiguration(configfile);
			String[] indexDirPaths=config.getStringArray("indexdirs");
			ArrayList<File> innerIndexDirs=new ArrayList<File>();
			
			for (String indf:indexDirPaths){
				innerIndexDirs.add(new File(indf));
			}
			return innerIndexDirs;
				
		} catch (ConfigurationException e) {		
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		GeoApplication mainApp;
		CommandLineParser parser = new PosixParser();
		Options options=new Options();
		options.addOption("g", "gui", false, "starts with the gui");
		options.addOption("h", "help", false, "print this message");
		//options.addOption("i", "index", false, "indexes the documents on the given path");
		options.addOption(OptionBuilder.withLongOpt("index").hasOptionalArg().withDescription("indexes the documents on the given path or reindex everything if without argument").create("i"));
		HelpFormatter usagehelp = new HelpFormatter();
		String cfgpath = null;
		try {
			CommandLine cmd = parser.parse(options, args);
			args = cmd.getArgs();

			if (cmd.hasOption("help")) {
				StackTraceElement[] stack = Thread.currentThread ().getStackTrace ();
				String programName = stack[stack.length - 1].getFileName();
				String usage=programName+" [configfile] --index [path]\n" +
				programName+" [configfile] keyword place";
				usagehelp.printHelp(usage, options);
			} else if (cmd.hasOption("gui")) {
				mainApp = new GeoApplication(cfgpath);
				java.awt.EventQueue.invokeLater(mainApp
				/*
				 * new Runnable() { public void run() { new
				 * GeoApplicationGui().setVisible(true); } }
				 */);
			} else {
				if (args.length==1 || args.length==3){
					cfgpath=args[0];
					if (args.length==3){
						String[] temp={args[1],args[2]};
						args=temp;
					}
				}
				if (cmd.hasOption("index")) {
					mainApp = new GeoApplication(cfgpath);
					String inputpath = cmd.getOptionValue("index");
					mainApp.indexDirs = mainApp.updateIndexConfig(inputpath);

					if (inputpath != null) {
						String errortext = mainApp.createIndex(new File(
								inputpath));
						System.out.println(errortext);
					} else {
						String errortext = mainApp.createIndex();
						System.out.println(errortext);
					}
				} else if (args.length == 2 || args.length == 3) {
					mainApp = new GeoApplication(cfgpath);
					Vector<GeoRefDoc> results = mainApp.search(args[0], args[1]);
					for (GeoRefDoc doc : results) {
						System.out.println(doc.getNomeDoc() + "\ntextscore: "
								+ doc.getTextScore() + "\nsortscore: "
								+ doc.getSortScore() + "\ndistancescore: "
								+ doc.getDistanceScore() + "\nid: "
								+ doc.getId() +
                                                "\ntitle: "+doc.docTitle+
                                                "\ndescription: "+doc.docDescription+
                                                "\nkeywords: "+doc.docKeyWords+
                                                "\ndateline: "+doc.docDateLine+"\n");
					}
				} else if (args.length == 0 || args.length == 1) {
					mainApp = new GeoApplication(cfgpath);
					mainApp.GeoApplicationCmd();
				}
			}
		} catch (org.apache.commons.cli.ParseException e) {
			e.printStackTrace();
		}
	}

	


}
