/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.georeference;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Properties;
import java.util.Vector;

import java.util.logging.Level;
import java.util.logging.Logger;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.btree.BTree;
import bTree.Serial;
import bTree.Serializ;
import com.mallardsoft.tuple.Pair;
import com.mallardsoft.tuple.Tuple;
import geotag.GeoApplication;
import geotag.RTreeReader;
import geotag.words.GeoRefDoc;
import geotag.words.StringOperation;
import geotag.words.GeoWordsOperation;
import geotag.words.GeographicWord;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import jdbm.RecordManagerOptions;

/**
 * Classe che ha il compito di georeferenziare la Locazione, ovvero la zona ricevuta
 * come punto di riferimento intorno alla quale valutare la bontà geografica della query.
 * @author Giorgio Ghisalberti
 */
public class GeoRefLocation {
    String path,dbpath,slash;
    RTreeReader rtree;
    /**
     * Costruttore della classe
     */
    public GeoRefLocation(){
        this.path=GeoApplication.getPath();
        this.slash=File.separator;
        this.dbpath=path+"db"+slash;
        this.rtree=GeoApplication.rtree;
    }


    
	public static synchronized BTree loadOrCreateBTree( RecordManager aRecordManager,String aName, Comparator aComparator ) throws IOException

 	{
 
 	  long recordID = aRecordManager.getNamedObject( aName );
 	  BTree tree = null;
 	  Serializ s=new Serializ();
 	  if ( recordID == 0 )
 	  {  
 	   
 	    tree = BTree.createInstance( aRecordManager, aComparator,s,s,1024);
 	    aRecordManager.setNamedObject( aName, tree.getRecid() );
 	   
 	  }
 	  else
 	  { 
 	    tree = BTree.load( aRecordManager, recordID );

 	  }
 	  return tree;
 	} 


     /**
     * Metodo principale della classe che gestisce l'intera fase della georeferenziazione, 
     * dall'accesso al DataBase fino alla scelta della locazione corretta.
     * Riceve in ingresso la zona da cercare da questa restituisce una sola zona geografica.
     * @param location : stringa che descrive la zona di interesse
     * @return la GeoWord relativa alla locazione
     */
    public GeographicWord getGeoLocation(String location) {

        GeographicWord geoLocation = new GeographicWord();
        try {
            Vector<GeographicWord> locationWordVector = new Vector<GeographicWord>();
            Vector<GeographicWord> locationWordVector2 = new Vector<GeographicWord>();
            Vector<GeographicWord> locationWordVector3 = new Vector<GeographicWord>();
            int locationId = 0;
            Vector<String> idList = new Vector<String>();
            location = StringOperation.correctString(location);
            String searchingName = StringOperation.convertString(location);
            //NUOVO CODICE
            Serial a = new Serial();
            RecordManager mydbinter;
            BTree tinter = new BTree();
            Properties options = new Properties();
            options.setProperty(RecordManagerOptions.DISABLE_TRANSACTIONS, "");
            mydbinter = RecordManagerFactory.createRecordManager(dbpath + "albero_Btree_Intermedio", options); //occhio: qui c'erano 2 separator
            tinter = loadOrCreateBTree(mydbinter, "intermedio", a);
            BTree tgaz;
            Serial a1 = new Serial();
            RecordManager mydbgaz;
            tgaz = new BTree();
            mydbgaz = RecordManagerFactory.createRecordManager(dbpath + "albero_Btree_Gazetteer", options); //occhio: qui c'erano 2 separator
            tgaz = loadOrCreateBTree(mydbgaz, "gazetteer", a1);
            Object results = tinter.find(searchingName);
            String[] dati;
            if (results != null) {
                dati = ((String) results).split("�#");
                int i = 0;
                for (i = 0; i < dati.length; i++) {
                    Object resultsGaz = tgaz.find(dati[i]);
                    if (resultsGaz != null) {
                        //Popolo wordVectorResult3
                        String[] datiGaz = ((String) resultsGaz).split("�#");
                        GeographicWord newGeoWord = populateGeoWord(datiGaz);
                        
                        //newGeoWord = population(result);
                        //newGeoWord.setPosition(pos);
                        //newGeoWord.setZoneDocName(searchingName);
                        //??????????????????????????E' una zona amministrativa se è uno stato o una regione
//        	        if(datiGaz[23].equalsIgnoreCase("1") || datiGaz[24].equalsIgnoreCase("1"))
//        	        	newGeoWord.setAdminZone(true); //TRUE se è una zona amministrativa
                        locationWordVector.add(newGeoWord);
                        //Controllo che c'era nella alternatesearch
//        	        if(shift2 == 1 && !wordVectorResult.isEmpty())
//                        wordVectorResult = multiWordControl(wordVectorResult);
                    }
                }
            }
            //	VECCHIO CODICE
//        
//        //Cerco nelel tabelle "countryinfo" e "admin1codesascii"
//        idList = selectIDQuery(searchingName, "countryinfo", "name", stmt);
//        if(idList.isEmpty())
//            idList = selectIDQuery(searchingName, "admin1codesascii", "name", stmt);
//        if(!idList.isEmpty()){            
//            for(int j = 0; j < idList.size(); j++){
//                locationWordVector3 = selectLocationQuery(idList.elementAt(j), "", "geonameid", stmt);
//                for(int i = 0; i < locationWordVector3.size(); i++){
//                    locationWordVector.add(locationWordVector3.elementAt(i));
//                }
//            }
//        }
//        
//        //Se non ho trovato risultati cerco nelle tabelle "geoname" e "alternatename"
//        if(locationWordVector3.isEmpty()){
//            locationWordVector = findLocationProperty(searchingName, locationWordVector, stmt);                        
//        }
            //Elimino i termini con geonameid uguale
            locationWordVector = GeoWordsOperation.eraseEquals(locationWordVector);
            //Seleziono una sola locazione tra quelle trovate
            if (locationWordVector.size() == 0) {
                geoLocation.setLocation(false);
                return geoLocation;
            } else {
                geoLocation = valuation(locationWordVector);
            }
            geoLocation.setZoneDocName(location);
            geoLocation.setLocation(true);
        } catch (ParseException ex) {
            Logger.getLogger(GeoRefLocation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GeoRefLocation.class.getName()).log(Level.SEVERE, null, ex);
        }

        return geoLocation;
    }

    /**
     * Metodo che si occupa di popolare i vari campi della GeoWord, reperendoli
     * dai dati ricevuti come parametro.
     * @param datiGaz : stringa reperita dal gazetteer
     * @return una GeoWord con i nuovi valori
     */
    public GeographicWord populateGeoWord(String datiGaz[]) throws ParseException{

            GeographicWord newGeoWord = new GeographicWord();

            newGeoWord.setGeonameid(Integer.parseInt(datiGaz[0]));
            newGeoWord.setName(datiGaz[1]);
            newGeoWord.setAsciiName(datiGaz[2]);
            newGeoWord.setAlternateNames(datiGaz[3]);
            newGeoWord.setLatitude(Float.parseFloat(datiGaz[4]));
            newGeoWord.setLongitude(Float.parseFloat(datiGaz[5]));
            newGeoWord.setFeatureClass(datiGaz[6]);
            newGeoWord.setFeatureCode(datiGaz[7]);
            newGeoWord.setCountryCode(datiGaz[8]);
            newGeoWord.setCc2(datiGaz[9]);
            newGeoWord.setAdmin1Code(datiGaz[10]);
            newGeoWord.setAdmin2Code(datiGaz[11]);
            newGeoWord.setAdmin3Code(datiGaz[12]);
            newGeoWord.setAdmin4Code(datiGaz[13]);
            newGeoWord.setPopulation(Integer.parseInt(datiGaz[14]));
            newGeoWord.setElevation(Integer.parseInt(datiGaz[15]));
            newGeoWord.setGtopo30(Integer.parseInt(datiGaz[16]));
            newGeoWord.setTimeZone(datiGaz[17]);
            newGeoWord.setModificationDate(new SimpleDateFormat("yyyy-MM-dd").parse(datiGaz[18]));

            newGeoWord.setmbr_x1(Double.parseDouble(datiGaz[19]));
            newGeoWord.setmbr_y1(Double.parseDouble(datiGaz[20]));
            newGeoWord.setmbr_x2(Double.parseDouble(datiGaz[21]));
            newGeoWord.setmbr_y2(Double.parseDouble(datiGaz[22]));
            return newGeoWord;
        }
    
    /**
     * Metodo che seleziona un'unica GeoWord tra tutte quelle presenti nel vettore.
     * Seleziona di base quella con la popolazione maggiore,
     * se la popolazione non c'è allo quella con feature class uguale a "P".
     * @param locationWordVector : vettore con le GeoWord trovate
     * @return la GeoWord relativa alla locazione
     */
    public GeographicWord valuation(Vector<GeographicWord> locationWordVector){
        GeographicWord geoLocation = new GeographicWord();
        int popMax = 0;
        
        if(locationWordVector.size() > 1){
            //Tra tutti i risultati ottenuti, ne estraggo solo uno
            for(int i = 0; i < locationWordVector.size(); i++){
                GeographicWord gw = locationWordVector.elementAt(i);

                //Seleziono quello con la popolazione maggiore
                if(gw.getPopulation() > popMax){
                    popMax = gw.getPopulation();
                    geoLocation = gw;
                }
            }
            
            if(popMax == 0){ //La zona non ha popolazione
                for(int i = 0; i < locationWordVector.size(); i++){
                    GeographicWord gw = locationWordVector.elementAt(i);

                    if(gw.getFeatureClass().equals("P"))
                        geoLocation = gw;
                    else
                        geoLocation = gw;
                }
            }
        }else{
            geoLocation = locationWordVector.elementAt(0);
        }
        
        return geoLocation;
    }
    
    /**
     * Metodo che prende i risultati di una ricerca testuale e li filtra/unisce in base alla stringa che identifica un luogo in ingresso
     * nel processo aggiunge anche ad ogni  documento georeferenziato le geoword attinenti
     * @param results risultati di una ricerca testuale
     * @param location   luogo in base al quale filtrare
     * @return vettore di GeoRefDoc
     */
    public Vector<GeoRefDoc> mergeLocation(Vector<GeoRefDoc> results, String location) {
        try {
            Vector<GeoRefDoc> resultsmerge = new Vector<GeoRefDoc>();

            GeographicWord geoLocation;
            geoLocation = getGeoLocation(location);

            // cerco nell'r-tree con l'mbr del paese (in futuro se si vogliono trovare
            // più risultati bisogna allargare l'mbr.

            double allarga = 0;
            int cerca = 0;
            boolean paeselocalizzato = false;
            boolean documentotrovato = false;

            int numeroresults = 0;
            ArrayList<Pair<String,String>> codici = rtree.query(geoLocation.getmbr_x1() + allarga, geoLocation.getmbr_y1() + allarga, geoLocation.getmbr_x2() - allarga, geoLocation.getmbr_y2() - allarga);

            for (int trovati = 0; trovati < codici.size(); trovati++) {

                //TODO estrarre questi pezzi in una funzione di query sull'indice geografico apposta
                String line = null;
                FileReader fileletto = null;
                LineNumberReader lr = null;

                try {
                    String codice = Tuple.get1(codici.get(trovati));
                    fileletto = new FileReader(dbpath + (Integer.parseInt(codice) / 1000) + slash + codice);
                    // file.write(nameFiles[i].getName()+"�#"+gw.getGeoScore()+"\r\n");
                    lr = new LineNumberReader(fileletto);
                    line = lr.readLine();
                } catch (IOException e) {
                }

                paeselocalizzato = false;
                documentotrovato = false;

                while (line != null) {
                    String data[];
                    data = line.split("�#");
                    GeoRefDoc documentoref = new GeoRefDoc();
                    documentotrovato = false;
                    numeroresults = 0;
                    //for(int numeroresults=0;numeroresults<results.size();numeroresults++)
                    while (numeroresults < results.size() && documentotrovato == false) {
                        if (results.elementAt(numeroresults).id.equalsIgnoreCase(data[0])) {
                            documentoref = results.elementAt(numeroresults);
                            if (paeselocalizzato == false) {
                                try {
                                    geoLocation = getGeoLocation(Tuple.get2(codici.get(trovati)));
                                } catch (Exception e) {
                                    // 	TODO: handle exception
                                    }
                                paeselocalizzato = true;
                            }
                            boolean trova = false;
                            cerca = 0;
                            //for(int cerca=0;cerca<resultsmerge.size();cerca++)
                            while (cerca < resultsmerge.size() && trova == false) {
                                if (resultsmerge.elementAt(cerca).id.equalsIgnoreCase(data[0])) {
                                    resultsmerge.elementAt(cerca).addGeoWord(geoLocation,new Double(data[2]));
                                    resultsmerge.elementAt(cerca).setHaveGeoRef(true);
                                    trova = true;
                                }
                                cerca++;
                            }
                            if (trova == false) {
                                documentoref.addGeoWord(geoLocation,new Double(data[2]));
                                resultsmerge.add(documentoref);
                            }
                        }
                        numeroresults++;
                    }

                    line = lr.readLine();
                }

                if (fileletto != null) {
                    fileletto.close();
                }
            }
            results = resultsmerge;
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        return results;

    }
    
}
