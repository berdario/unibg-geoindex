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

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.btree.BTree;
import bTree.Serial;
import bTree.Serializ;
import geotag.words.StringOperation;
import geotag.words.GeoWordsOperation;
import geotag.words.GeographicWord;

/**
 * Classe che ha il compito di georeferenziare la Locazione, ovvero la zona ricevuta
 * come punto di riferimento intorno alla quale valutare la bontà geografica della query.
 * @author Giorgio Ghisalberti
 */
public class GeoRefLocation {
    
    /**
     * Costruttore della classe
     */
	String path="./";
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
    public GeoRefLocation(){
        
    }
    
    /**
     * Metodo principale della classe che gestisce l'intera fase della georeferenziazione, 
     * dall'accesso al DataBase fino alla scelta della locazione corretta.
     * Riceve in ingresso la zona da cercare da questa restituisce una sola zona geografica.
     * @param location : stringa che descrive la zona di interesse
     * @param stmt : Statement necessario per l'esecuzioni delel query sul DB
     * @return la GeoWord relativa alla locazione
     * @throws java.sql.SQLException
     * @throws java.text.ParseException
     * @throws IOException 
     */
    public GeographicWord getGeoLocation(String location, Statement stmt) throws SQLException, ParseException, IOException{
        Vector<GeographicWord> locationWordVector = new Vector<GeographicWord>();
        Vector<GeographicWord> locationWordVector2 = new Vector<GeographicWord>();
        Vector<GeographicWord> locationWordVector3 = new Vector<GeographicWord>();
        GeographicWord geoLocation = new GeographicWord();
        int locationId = 0;
        Vector<String> idList = new Vector<String>();
        
        location = StringOperation.correctString(location);
        String  searchingName = StringOperation.convertString(location);
        
        //NUOVO CODICE
        Serial a=new Serial();
        RecordManager mydbinter;
		BTree tinter=new BTree();
		mydbinter = RecordManagerFactory.createRecordManager(path+File.separator+"db"+File.separator+"albero_Btree_Intermedio", new Properties());
		tinter = loadOrCreateBTree(mydbinter, "intermedio", a );
		
		BTree tgaz;
		Serial a1=new Serial();
		RecordManager mydbgaz;
		tgaz = new BTree();
		mydbgaz = RecordManagerFactory.createRecordManager(path+File.separator+"db"+File.separator+"albero_Btree_Gazetteer", new Properties());
		tgaz = loadOrCreateBTree(mydbgaz, "gazetteer", a1 );
		Object results=tinter.find(searchingName);
		String dati[];
		
        if(results!=null){
        	dati=((String)results).split("�#");
        	int i=0;
        	for(i=0;i<dati.length;i++){
        		
        		Object resultsGaz=tgaz.find(dati[i]);
        		if(resultsGaz!=null){
        			//Popolo wordVectorResult3
        			String datiGaz[];
        			datiGaz=((String)resultsGaz).split("�#");
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
        if(locationWordVector.size() == 0){ 
            geoLocation.setLocation(false);
            return geoLocation;
        }else
            geoLocation = valuation(locationWordVector);
          
        geoLocation.setZoneDocName(location);
        geoLocation.setLocation(true);
        return geoLocation;
    }
    
    /**
     * Metodo che reperisce le proprietà delle zone trovate nel database che hanno
     * nome uguale a quello della location.
     * Esegue la ricerca prima sulla tabella "geoname" e poi su "alternatename"
     * @param searchingName
     * @param locationWordVector
     * @param stmt
     * @return
     * @throws java.sql.SQLException
     * @throws java.text.ParseException
     */
    public Vector<GeographicWord> findLocationProperty(String searchingName, Vector<GeographicWord> locationWordVector, Statement stmt) throws SQLException, ParseException{
        Vector<String> idList = new Vector<String>();
        Vector<GeographicWord> locationWordVector2 = new Vector<GeographicWord>();
                
        locationWordVector = selectLocationQuery(searchingName, "", "name", stmt);
        if(locationWordVector.isEmpty()) //Se in "name" NON trovo risultato cerco in "asciiname"
            locationWordVector = selectLocationQuery(searchingName, "", "asciiname", stmt);

        idList = selectIDQuery(searchingName, "alternatename", "alternatename", stmt);
        if(idList.isEmpty()) 
            idList = selectIDQuery(searchingName, "alternatename", "asciialternatename", stmt);

        if(!idList.isEmpty()){            
            for(int j = 0; j < idList.size(); j++){   
                //FALSE indica che NON è una zona amministrativa
                locationWordVector2 = selectLocationQuery(idList.elementAt(j), "", "geonameid", stmt);
                //Aggiungo al vettore dei risultati i campi trovati dopo la ricerca nella tabella "alternatename"
                for(int i = 0; i < locationWordVector2.size(); i++){
                    locationWordVector.add(locationWordVector2.elementAt(i));
                }
            }
        } 
        
        return locationWordVector;
    }
    
    
    /**
     * Metodo alternativo della classe che gestisce l'intera fase della georeferenziazione, 
     * dall'accesso al DataBase fino alla scelta della locazione corretta.
     * Riceve in ingresso la zona da cercare e la nazione corrispondente, 
     * da questa restituisce una sola zona geografica.
     * @param location : stringa che descrive la zona di interesse
     * @param stmt : Statement necessario per l'esecuzioni delel query sul DB
     * @return la GeoWord relativa alla locazione
     * @throws java.sql.SQLException
     * @throws java.text.ParseException
     */
    public GeographicWord getGeoLocation(String location, String country, Statement stmt) throws SQLException, ParseException {
        GeographicWord geoLocation = new GeographicWord();
        Vector<GeographicWord> locationWordVector = new Vector<GeographicWord>();
        
        location = StringOperation.correctString(location);
        String  searchingName = StringOperation.convertString(location);
        String countryCode = selectCountryCode(country, stmt);
        
        locationWordVector = selectLocationQuery(searchingName, countryCode, "name", stmt);
        if(locationWordVector.isEmpty()) //Se in "name" NON trovo risultato cerco in "asciiname"
            locationWordVector = selectLocationQuery(searchingName, countryCode, "asciiname", stmt);
               
        //Elimino i termini con geonameid uguale
        locationWordVector = GeoWordsOperation.eraseEquals(locationWordVector); 
                
        //Seleziono una sola locazione tra quelle trovate
        if(locationWordVector.size() == 0){ 
            geoLocation.setLocation(false);
            return geoLocation;
        }else
            geoLocation = valuation(locationWordVector);
          
        geoLocation.setZoneDocName(location);
        geoLocation.setLocation(true);
        return geoLocation;
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
     * Metodo che esegue una query di TIPO select sulle tabelle "alternatename"
     * o "countryinfo" oppure "admin1codesascii". Ritorna i geonameId delle zone trovate.
     * @param wordName : nome della parola da cercare
     * @param table : tabella su cui eseguire la query
     * @param field : campo della tabella su cui eseguire la query
     * @param stmt : Statement necessario per l'esecuzioni delel query sul DB
     * @return l'elenco dei geonameId
     * @throws java.sql.SQLException
     */
    public static Vector<String> selectIDQuery(String wordName, String table, String field, Statement stmt) throws SQLException {       
        String geonameid = "";
        Vector<String> elencoId = new Vector<String>();
                
         
        ResultSet result = stmt.executeQuery("SELECT * FROM " + table + " WHERE " +
                field + "='" + wordName + "'");

        if(table.equals("alternatename")){
            while (result.next()) { // process results one row at a time   
                geonameid = result.getString(2);
                elencoId.add(geonameid);
            }  
        }
        else if(table.equals("countryinfo")){
            while (result.next()) {
                geonameid = result.getString(12);
                elencoId.add(geonameid);
            }  
        }else{
            while (result.next()) {
                geonameid = result.getString(4);
                elencoId.add(geonameid);
            } 
        }

        result.close();
        
        return elencoId;
    }
    
    

    /**
     * Metodo che esegue una qeury di tipo SELECT sul campo ricevuto come parametro.
     * Una volta trovato un match popola un vettore di GeoWord con tutti i parametri trovati
     * nel DataBase.
     * @param locationName : nome del campo da cercare (locazione)
     * @param countryCode : codice della nazione
     * @param field : nome del campo della tabella su cui eseguire a query
     * @param stmt : Statement necessario per l'esecuzioni delel query sul DB
     * @return un vettore di GeoWord con i risultati trovati
     * @throws java.sql.SQLException
     * @throws java.text.ParseException
     */
    public static Vector<GeographicWord> selectLocationQuery(String locationName, String countryCode, String field, Statement stmt) throws SQLException, ParseException {       
        GeographicWord newGeoWord = new GeographicWord();
        Vector<GeographicWord> geoWordVector = new Vector<GeographicWord>();
        String query = "";
        
        if(countryCode.isEmpty())
            query = "SELECT * FROM geoname " + "WHERE " + field + "='" + locationName + "'";               
        else
            query = "SELECT * FROM geoname " + "WHERE " + field + "='" + locationName +
                    "' AND countrycode='" + countryCode + "'";

        ResultSet result = stmt.executeQuery(query);
                
        while (result.next()) {
            newGeoWord.setGeonameid(Integer.parseInt(result.getString(1)));
            newGeoWord.setName(result.getString(2));
            newGeoWord.setAsciiName(result.getString(3));
            newGeoWord.setAlternateNames(result.getString(4));
            newGeoWord.setLatitude(Float.parseFloat(result.getString(5)));
            newGeoWord.setLongitude(Float.parseFloat(result.getString(6)));
            newGeoWord.setFeatureClass(result.getString(7));
            newGeoWord.setFeatureCode(result.getString(8));
            newGeoWord.setCountryCode(result.getString(9));
            newGeoWord.setCc2(result.getString(10));
            newGeoWord.setAdmin1Code(result.getString(11));
            newGeoWord.setAdmin2Code(result.getString(12));
            newGeoWord.setAdmin3Code(result.getString(13));
            newGeoWord.setAdmin4Code(result.getString(14));
            newGeoWord.setPopulation(Integer.parseInt(result.getString(15)));
            newGeoWord.setElevation(Integer.parseInt(result.getString(16)));
            newGeoWord.setGtopo30(Integer.parseInt(result.getString(17)));
            newGeoWord.setTimeZone(result.getString(18));
            newGeoWord.setModificationDate(new SimpleDateFormat("yyyy-MM-dd").parse(result.getString(19)));          
            geoWordVector.add(newGeoWord);
        }                         

        result.close();
                 
        return geoWordVector;
    }
    
    /**
     * Metodo che accede al DataBase per prelevare i countryCode della
     * nazione selezionata
     * @param country
     * @param stmt
     * @return
     * @throws java.sql.SQLException
     */
    private String selectCountryCode(String country, Statement stmt) throws SQLException{
        String countryCode = "";

        ResultSet result = stmt.executeQuery("SELECT iso_alpha_2 FROM countryinfo" +
                " WHERE name='" + country + "'");

        while (result.next()) {
            countryCode = result.getString(1);
        }

        return countryCode; 
    }
    
}
