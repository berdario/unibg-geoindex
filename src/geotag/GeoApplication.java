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
import org.apache.commons.io.FileUtils;

/**
 *
 * @author  giorgio
 */
public final class GeoApplication {

    public static Logger logger;

    Configuration config;

    private int maxCachedResults = 300;
    CacheHashMap<Triple<String,String,Double>,ArrayList<GeoRefDoc>> cachedResults = new CacheHashMap<Triple<String, String, Double>, ArrayList<GeoRefDoc>>(maxCachedResults);
    CacheHashMap<Pair<String,String>,ArrayList<GeoRefDoc>> cachedUnsortedResults = new CacheHashMap<Pair<String, String>, ArrayList<GeoRefDoc>>(maxCachedResults);

    public static RTreeReader rtree;

    private String swLanguage;
    
    NumberFormat formatter = new DecimalFormat("#0.00");  //Formatto il numero a 2 cifre decimali
    
    double goodGeoScore = 0.65; //0.65   
    double uniqueGWScore = 0.2;
    
    private static String cachepath,slash;
    private ArrayList<File> indexDirs;

    GeoCandidateIdentification geoAnalysis;
    private final String indexpath;
	
	public String innerCreateIndex(File curDir){
        String errortext="";
        String documentExtension = "";
        String documentName = "";
        
        String hash;
        ContentIndexer contInd = new ContentIndexer();
        
   
        if(curDir.isDirectory()){
            File[] nameFiles = curDir.listFiles();           
            for (int i = 0; i < nameFiles.length; i++){
                if (!nameFiles[i].isDirectory()) {
                    ArrayList<Word> wordVector = new ArrayList<Word>();
                    ArrayList<Word> filterWordVector = new ArrayList<Word>();
                    ArrayList<GeographicWord> geoWordVector = new ArrayList<GeographicWord>();
                    ArrayList<GeographicWord> finalGeoWordVector = new ArrayList<GeographicWord>();
                    ArrayList<Word> finalWordVector = new ArrayList<Word>();
                    ArrayList<Word> finalFilterWordVector = new ArrayList<Word>();
                    boolean alreadyIndexed = false;

                    // Gestione di ogni file contenuto nella directory
                    String name = nameFiles[i].getName();
                    documentExtension = nameFiles[i].getName().substring(nameFiles[i].getName().lastIndexOf('.') + 1, nameFiles[i].getName().length());

                    documentName = nameFiles[i].getName().substring(0, nameFiles[i].getName().lastIndexOf('.'));

                    DocumentWrapper doc;
                    
                    try {
                        doc = new DocumentWrapper(nameFiles[i], documentExtension);
                    } catch (UnsupportedFileException ex) {
                        Logger.getLogger(GeoApplication.class.getName()).log(Level.INFO, nameFiles[i].getName() + " is not a supported file", ex);
                        continue;
                    }
                    GeoRefDoc geoDoc = new GeoRefDoc(doc);


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
                        	hash = DigestUtils.md5Hex("file://"+nameFiles[i].toString());
                                //non uso più .toURI() per fare in modo che il numero di slash dopo "file:" sia consistente con lo standard freedesktop
                                //TODO: eventualmente cambiare in futuro... 
                        	
                        	//Controllo se il file è già stato indicizzato                              

                        	
                            alreadyIndexed = contInd.control(hash);
                            
                            logger.log(Level.INFO,"\nFile name: " + documentName);
                            
                            if(!alreadyIndexed){
                            	
                            	//TODO verificare il comportamento con file con lo stesso nome
                            	FileUtils.copyFile(nameFiles[i], new File(cachepath + hash +"."+ documentExtension));
                            	
                                
                                    // Estrazione delle Word candidate                                    
                                    WordAnalyzer generator = new WordAnalyzer(swLanguage);
                                    wordVector = generator.getWordVector(doc); 

                                    // Fase di FILTRO
                                    Filter myFilter = new Filter();
                                    filterWordVector = myFilter.filter(wordVector);

                                    // Fase di GEO-VALUTAZIONE                                                                                                               
                                    geoWordVector = geoAnalysis.analyze(filterWordVector, wordVector, doc);

                                    //Tra elementi con uguale geonameid ne prendo solo 1 con peso e importanza maggiore 
                                    finalGeoWordVector = importanceControl(geoWordVector, finalGeoWordVector); 
                                    finalWordVector = update(finalWordVector, wordVector);
                                    finalFilterWordVector = update(finalFilterWordVector, filterWordVector);
                                    
                                    //Indicizzo il contenuto del file 
                                     contInd.indexing(doc, hash);
                                    
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
                                        GeographicWord gw = finalGeoWordVector.get(ind);
                                        // cerco nell'r-tree
                                        logger.log(Level.FINER,finalGeoWordVector.get(ind).getName() + " - geoscore: "
                                                + formatter.format(finalGeoWordVector.get(ind).getGeoScore()) + " - georefvalue: "
                                                + formatter.format(scores.get(gw)));
                                        logger.log(Level.FINEST," codici: ");
                                        
                                        ArrayList<Pair<String,String>> codici = rtree.query(gw.getmbr_x1(), gw.getmbr_y1(),gw.getmbr_x2(), gw.getmbr_y2());
                                        boolean trovato=false;
                                        for(int trovati=0;trovati<codici.size();trovati++)
                                        {
                                            String codice = Tuple.get1(codici.get(trovati));
                                            logger.log(Level.FINEST,codice+" ");
                                        	File file = new File(indexpath+(Integer.parseInt(codice)/1000)+slash+codice);
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
                                        		FileWriter file2=new FileWriter(indexpath+(Integer.parseInt(codice)/1000)+slash+codice,true);
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
                        logger.log(Level.INFO,"Time: " + (end.getTime() - start.getTime()) / 1000 + " seconds");
                        
                }
            }
        }
        contInd.closeIndex();   
		return errortext;
		
	}

        public String createIndex(String indexpath){
            indexDirs = config.updateIndexConfig(indexpath);
            return innerCreateIndex(new File(indexpath));
        }

    public String createIndex() {
        indexDirs = config.updateIndexConfig(null);
        String errortext = "";
        for (File f : indexDirs) {
            errortext += innerCreateIndex(f);
            //ContentIndexer.closeIndex();
        }
        return errortext;
    }

	public ArrayList<Word>  update (ArrayList<Word> finalWordVector, ArrayList<Word> wordVector){
	    
	    if(finalWordVector.size() == 0){
	        for(int i = 0; i < wordVector.size(); i++){
	            finalWordVector.add(wordVector.get(i));
	        }
	    }else{
	        for(int i = 0; i < finalWordVector.size(); i++){
	            Word gw1 = finalWordVector.get(i);
	            
	            for(int j = 0; j < wordVector.size(); j++){
	                Word gw2 = wordVector.get(j);
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
	public ArrayList<GeographicWord>  importanceControl (ArrayList<GeographicWord> geoWordVector, ArrayList<GeographicWord> finalGeoWordVector){
	    ArrayList<GeographicWord> newGeoWordVector = new ArrayList<GeographicWord>();
	    boolean diverso = true;
	    
	    //Se è la prima volta che popolo il vettore
	    if(finalGeoWordVector.size() == 0){
	        for(int i = 0; i < geoWordVector.size(); i++){
	            newGeoWordVector.add(geoWordVector.get(i));
	        }
	    }else{       
	        for(int i = 0; i < finalGeoWordVector.size(); i++){
	            GeographicWord gw1 = finalGeoWordVector.get(i);
	            diverso = true;
	            
	            for(int j = 0; j < geoWordVector.size(); j++){
	                GeographicWord gw2 = geoWordVector.get(j);
	                if(gw1.getGeonameid() == gw2.getGeonameid()){                                              
	                    
	                    //Nuova freq
	                    int newFreq = gw1.getFrequency() + gw2.getFrequency();                                                  
	                    
	                    //Nuova Importance                                
	                    double newImp = gw1.getImportance() + gw2.getImportance();                            
	                   
	                    GeographicWord newGW = new GeographicWord();
	                    newGW = gw1;
	                    newGW.setFrequency(newFreq);
	                    newGW.importance = newImp;
	                    
	                    newGeoWordVector.add(newGW);
	                    diverso = false;                        
	                }
	            }
	
	            if(diverso)
	                newGeoWordVector.add(gw1);    
	        }
	        
	        //Inserisco le NUOVE geoWord trovate
	        for(int i = 0; i < geoWordVector.size(); i++){
	            GeographicWord gw3 = geoWordVector.get(i);
	            boolean different = true;
	            
	            for(int j = 0; j < newGeoWordVector.size(); j++){
	                GeographicWord gw4 = newGeoWordVector.get(j);
	                
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
	public ArrayList<GeographicWord> selectGeoWords(ArrayList<GeographicWord> geoWordVector){
	    ArrayList<GeographicWord> newGeoWordVector = new ArrayList<GeographicWord>();
	    boolean control = false;
	    double geoScoreBoundary;
	    
	    
	    
	    for(int j = 0; j < geoWordVector.size(); j++){
	        GeographicWord gw = geoWordVector.get(j);
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
	        GeographicWord gw = newGeoWordVector.get(0);
	        double score = gw.getGeoScore();
	        if(score >= goodGeoScore && ((score + goodGeoScore) < 1.0)){
	            gw.setGeoScore(score + uniqueGWScore);
	        }
	    }
	
	
	    return newGeoWordVector;
	}


    public ArrayList<GeoRefDoc> search(String keyWords, String location){
       return search(keyWords, location, 0.5);
    }

    public ArrayList<GeoRefDoc> search(String keyWords, String location, Double weigth) {

        ArrayList<GeoRefDoc> results = cachedResults.get(Tuple.from(keyWords,location,weigth));
        if (results != null){
            return results;
        }

        results = cachedUnsortedResults.get(Tuple.from(keyWords,location));

        if (results == null){

            ContentSearcher content = new ContentSearcher();
            results = content.createTextualRanking(keyWords);

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

    public ArrayList<GeoRefDoc> search(String keyWords, double lat, double lon, double weigth){

        //TODO: trovare un modo per il caching dei risultati
        //usare una coppia di double come chiave non mi pare una grande idea
        //forse però non è necessario: questa ricerca in teoria potrebbe essere meno onerosa, non dovendo chiamare
        //getGeoLocation per ottenere i codici, con qualche ottimizzazione potrebbe non averne bisogno

        ArrayList<Pair<String,String>> codes = rtree.query(lat, lon);

        ContentSearcher content = new ContentSearcher();
        ArrayList<GeoRefDoc> results = content.createTextualRanking(keyWords);

        GeoRefLocation grLoc = new GeoRefLocation();
        results = grLoc.innerMerge(codes, results);

        DistanceSearcher distanceSorter = new DistanceSearcher();
        distanceSorter.createDistanceRanking(results, lat, lon);
        results = createRanking(results, weigth);
        
        return results;
    }
	
    public GeoApplication(String cfgpath) {

        logger = Logger.getLogger(this.getClass().getName());

        config = new Configuration(cfgpath);

        cachepath = Configuration.getCachePath();
        indexpath = Configuration.getIndexPath();
        slash = Configuration.getSeparator();
        swLanguage = Configuration.getSWLanguage();

        
        try {
            rtree = new RTreeReader();
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
	
    /**
     * Metodo che ordina il vettore dei documenti reperiti rispetto al campo "sortScore"
     * @param results : vettore dei documenti
     * @param weigth : costante che indica il peso da attribuire fra contenuto e distanceScore
     * @return il vettore dei documenti ordinato
     */
    public static ArrayList<GeoRefDoc> createRanking(ArrayList<GeoRefDoc> results, double weigth) {
        //Calcolo il sortScore
        //TODO: probabilmente si può migliorare inserendo direttamente i risultati in un nuovo vector mentre vengono calcolati i sortscore uno per uno
        //comunque non è di sicuro un collo di bottiglia: durante il calcolo del distanceScore a monte ci sono ottimizzazioni simili da fare (con 100000 risultati da ordinare si intaserebbe prima li che qui)
        for (int i = 0; i < results.size(); i++) {
            GeoRefDoc doc = results.get(i);
            doc.setSortScore(weigth * doc.getDistanceScore() + (1 - weigth) * doc.getTextScore());
        }

        Collections.sort(results,Collections.reverseOrder());
        
        return results;
    }

}
