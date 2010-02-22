/*
 * GeoApplication.java
 *
 * Created on August 27, 2008, 9:49 AM
 */

package geotag;

import com.mallardsoft.tuple.Pair;
import com.mallardsoft.tuple.Triple;
import com.mallardsoft.tuple.Tuple;
import geotag.analysis.Filter;
import geotag.analysis.GeoCandidateIdentification;
import geotag.analysis.Score;
import geotag.analysis.WordAnalyzer;
import geotag.georeference.GeoRef;
import geotag.georeference.GeoRefLocation;
import geotag.indices.ContentIndexer;
import geotag.output.CreateOutput;
/*import geotag.parser.HTMLParser;
import geotag.parser.PDFParser;
import geotag.parser.XMLParser;*/
import geotag.parser.DocumentWrapper;
import geotag.parser.DocumentWrapper.UnsupportedFileException;
import geotag.search.ContentSearcher;
import geotag.search.DistanceSearcher;
import geotag.words.GeoRefDoc;
import geotag.words.GeographicWord;
import geotag.words.StringOperation;
import geotag.words.Word;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.charset.CodingErrorAction;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
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
public final class GeoApplication {

    Logger logger = Logger.getLogger(this.getClass().getName());

    private int maxCachedResults = 300;
    CacheHashMap<Triple<String,String,Double>,Vector<GeoRefDoc>> cachedResults = new CacheHashMap<Triple<String, String, Double>, Vector<GeoRefDoc>>(maxCachedResults);
    CacheHashMap<Pair<String,String>,Vector<GeoRefDoc>> cachedUnsortedResults = new CacheHashMap<Pair<String, String>, Vector<GeoRefDoc>>(maxCachedResults);

    public static RTreeReader rtree;

    private String swLanguage;
    
    NumberFormat formatter = new DecimalFormat("#0.00");  //Formatto il numero a 2 cifre decimali
    
    double goodGeoScore = 0.65; //0.65   
    double uniqueGWScore = 0.2;
    
    private static String path,dbpath,cachepath,configfile,slash;
    private ArrayList<File> indexDirs;

    GeoCandidateIdentification geoAnalysis;
	
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
                                    geoWordVector = geoAnalysis.analyze(filterWordVector, wordVector, documentContent, importanceValue, upperDateLine);

                                    //Tra elementi con uguale geonameid ne prendo solo 1 con peso e importanza maggiore 
                                    finalGeoWordVector = importanceControl(geoWordVector, finalGeoWordVector); 
                                    finalWordVector = update(finalWordVector, wordVector);
                                    finalFilterWordVector = update(finalFilterWordVector, filterWordVector);
                                    
                                    //Indicizzo il contenuto del file 
                                    //PRIMA ERA COMMENTATO
                                     contInd.indexing(documentContent, geoDoc, hash);
                                                                        
                                    
                                 
                                
                                
                                    // Fase di SCORING   
                                    Score newScore = new Score();
                                    finalGeoWordVector = newScore.updateGeoScore(finalGeoWordVector, finalWordVector, swLanguage, finalFilterWordVector);
                                    
                                    //Elimino le GeoWord con peso sotto lo 0.6
                                    finalGeoWordVector = selectGeoWords(finalGeoWordVector);

                                    //GEOREFERENZIAZIONE
                                    GeoRef geoReferencing = new GeoRef(finalGeoWordVector);
                                    HashMap<GeographicWord, Double> scores = geoReferencing.calculateGeoRefValue();
                                    geoDoc.setScores(scores);
                                    
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
                                        logger.log(Level.FINER,finalGeoWordVector.elementAt(ind).getName() + " - geoscore: "
                                                + formatter.format(finalGeoWordVector.elementAt(ind).getGeoScore()) + " - georefvalue: "
                                                + formatter.format(scores.get(gw)));
                                        logger.log(Level.FINEST," codici: ");
                                        
                                        ArrayList<Pair<String,String>> codici = rtree.query(gw.getmbr_x1(), gw.getmbr_y1(),gw.getmbr_x2(), gw.getmbr_y2());
                                        boolean trovato=false;
                                        for(int trovati=0;trovati<codici.size();trovati++)
                                        {
                                            String codice = Tuple.get1(codici.get(trovati));
                                            logger.log(Level.FINEST,codice+" ");
                                        	File file = new File(dbpath+(Integer.parseInt(codice)/1000)+slash+codice);
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
                                        		FileWriter file2=new FileWriter(dbpath+(Integer.parseInt(codice)/1000)+slash+codice,true);
                                        		file2.write(hash+"�#"+gw.getGeoScore()+"�#"+scores.get(gw)+"\r\n");
                                        		file2.close();
                                        	}
                                        }
                                        System.out.println();
                                    }
                                    //FINE MODIFICA
                                    
                                    //Creazione dei file di output                 
                                    CreateOutput output = new CreateOutput(geoDoc, documentName);
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


    public Vector<GeoRefDoc> search(String keyWords, String location){
       return search(keyWords, location, 0.5);
    }

    public Vector<GeoRefDoc> search(String keyWords, String location, Double weigth) {

        Vector<GeoRefDoc> results = cachedResults.get(Tuple.from(keyWords,location,weigth));
        if (results != null){
            return results;
        }

        results = cachedUnsortedResults.get(Tuple.from(keyWords,location));

        if (results == null){

            ContentSearcher content = new ContentSearcher();
            results = content.createTextualRankig(keyWords);

            GeoRefLocation grLoc = new GeoRefLocation();
            results = grLoc.mergeLocation(results, location);

            DistanceSearcher distanceSorter = new DistanceSearcher();
            distanceSorter.createDistanceRanking(results, grLoc.getGeoLocation(location));
            
            cachedUnsortedResults.put(Tuple.from(keyWords, location), results);
        }

        //ordinamento dei risultati bilanciato fra place e keyword
        results = createRanking(results, weigth);

        cachedResults.put(Tuple.from(keyWords,location,weigth), results);

        return results;
    }
	
	public static String getPath(){
		return path;
	}
	
    public GeoApplication(String cfgpath) {
        slash = System.getProperty("file.separator");

        if (cfgpath != null) {
            configfile = cfgpath;
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
                configfile = System.getenv("APPDATA") + slash + "geosearch" + slash + "config";
            } else if (os.startsWith("Mac")) {
                configfile = homepath + "/Library/Application Support/geosearch/" + "config";
            } else {
                configfile = System.getProperty("user.dir") + "geosearch" + slash + "config";
            }
        }

        loadConfiguration();
        try {
            rtree = new RTreeReader(dbpath);
        } catch (SecurityException ex) {
            Logger.getLogger(GeoApplication.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            Logger.getLogger(GeoApplication.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GeoApplication.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(GeoApplication.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GeoApplication.class.getName()).log(Level.SEVERE, null, ex);
        }

        geoAnalysis = new GeoCandidateIdentification();
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
	
	public void updateIndexConfig(String inputpath) {//da aggiungere controllo? permette di aggiungere in continuazione lo stesso path
		try {
			PropertiesConfiguration config = new PropertiesConfiguration(configfile);
			ArrayList<File> innerIndexDirs=getIndexedDirs();
			
			if (inputpath!=null){
				File inputfile=new File(inputpath);
				if (!innerIndexDirs.contains(inputfile)){
					innerIndexDirs.add(new File(inputpath));
					config.setProperty("indexdirs", innerIndexDirs);
					config.save();
				}
			}
			
			indexDirs = innerIndexDirs;
		} catch (ConfigurationException e) {		
			e.printStackTrace();
		}
	}
	
	private ArrayList<File> getIndexedDirs() {
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
     * Metodo che ordina il vettore dei documenti reperiti rispetto al campo "sortScore"
     * @param results : vettore dei documenti
     * @param weigth : costante che indica il peso da attribuire fra contenuto e distanceScore
     * @return il vettore dei documenti ordinato
     */
    public static Vector<GeoRefDoc> createRanking(Vector<GeoRefDoc> results, double weigth) {
        //Calcolo il sortScore
        //TODO: probabilmente si può migliorare inserendo direttamente i risultati in un nuovo vector mentre vengono calcolati i sortscore uno per uno
        //comunque non è di sicuro un collo di bottiglia: durante il calcolo del distanceScore a monte ci sono ottimizzazioni simili da fare (con 100000 risultati da ordinare si intaserebbe prima li che qui)
        for (int i = 0; i < results.size(); i++) {
            GeoRefDoc doc = results.elementAt(i);
            doc.setSortScore(weigth * doc.getDistanceScore() + (1 - weigth) * doc.getTextScore());
        }

        Collections.sort(results,Collections.reverseOrder());
        
        return results;
    }	


}
