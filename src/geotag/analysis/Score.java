/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import bTree.Serial;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.btree.BTree;

import bTree.*;
import geotag.GeoApplication;
import geotag.words.StringOperation;
import geotag.indices.AnalyzerUtils;
import geotag.words.GeographicWord;
import geotag.words.Word;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Classe che ha il compito di incrementare il peso geografico delle GeoWord reperite 
 * nel documento. Il campo geoScore viene aggiornato secondo le seguenti regole
 *      0) In base alla presenza di termini multipli
 *      1) In base al contesto (Nazione, Regione, Provincia)
 *      2) In base alla descrizione geografica della zona  
 *      3) In base alla maggioranza (Nazione e Continente) 
 *      4) Delle divizioni amministrative (Continent, Country, Region)
 *      5) In base alla popolazione
 *      6) In base alla vicinanza spaziale tra le varie GeoWord
 *      7) In base alla presenza di termini significativi
 * @author Giorgio Ghisalberti
 */
public class Score {
    Vector<GeographicWord> geoAdminZones = new Vector<GeographicWord>();
    double constantDist = 0.0;
    
    int shifFeature = 5;
    int distNear = 3;
    String path,dbpath,slash;
    double incrScoreMulti = 0.3;
    double incrScoreContext = 0.7;
    double incrScoreFeature = 0.4;
    double incrScorePop = 0.4;
    double incrScoreMajority = 0.2;
    double incrScoreGeoAdminZone = 0.4;
    double incrScoreDistance = 0.6;
    double rho = 0.25;
    double decrScoreGeoStopWords = -0.5;

    MyStandardAnalyzer myStdAnalyzer;
    ArrayList<String> geoStopwords = new ArrayList();

    /**
     * Costruttore della classe
     */
    public Score(){
        this.path=GeoApplication.getPath();
        this.slash=File.separator;
        this.dbpath=path+"db"+slash;
        try {
            BufferedReader geoStopwordFile = new BufferedReader(new FileReader(new File(GeoApplication.getPath() + "geoStopwords.txt")));
            String line = "";

            while ((line = geoStopwordFile.readLine()) != null) {
                geoStopwords.addAll(Arrays.asList(line.split(",")));
            }
            geoStopwordFile.close();
        } catch (IOException ex) {
            System.err.println("Errore nell'apertura del file geoStopwords.txt");
            ex.printStackTrace();
        }
    }
    
    /**
     * Metodo che gestisce le varie fasi di scoring delle GeoWord.
     * Scorre il vettore delle GeoWord e per ognuna di esse esegue le regole sopra descritte.
     * @param finalWordVector : vettore contenente tutte le GeoWord reperite
     * @param allWordVector : vettore contenente tutte le Word del documento
     * @param swLanguage : indica il file di riferimento contenente le stopWords di interesse
     * @param filterWordVector : vettore delle Word che hanno superato la fase di filtro
     * @return l'elenco delle GeoWord con il campo geoScore aggiornato
     */
    public Vector<GeographicWord> updateGeoScore(Vector<GeographicWord> finalWordVector, Vector<Word> allWordVector, String swLanguage, Vector<Word> filterWordVector) throws IOException{        
        myStdAnalyzer = new MyStandardAnalyzer(swLanguage);
        Vector<GeographicWord> newFinalWordVector = new Vector<GeographicWord>();
        Vector<Word> countryRank = new Vector<Word>();
        int countryFreqMax = 0;
        double popAvg = 0;
        String countryCodeWithMaxFreq = "";
        
        
        //Creo la "classifica" delle Nazioni di tutte le GeoWords
        countryRank = createNationRanking(finalWordVector); 
        //Calcolo la Nazione con la freq MAX
        for(int i = 0; i < countryRank.size(); i++){ 
            if(countryRank.elementAt(i).getFrequency() > countryFreqMax){
                countryFreqMax = countryRank.elementAt(i).getFrequency();
                countryCodeWithMaxFreq = countryRank.elementAt(i).getName();
            }
        }
        
        popAvg = getMaxPop(finalWordVector);
        
      
        //Scorro il vetore delle GeoWord e ne analizzo una alla volta
        for(int i = 0; i < finalWordVector.size(); i++){ 
            GeographicWord geoWord = finalWordVector.elementAt(i);
            String nome = geoWord.getName();
            GeographicWord newGeoWord = new GeographicWord();
                 
            // Valutazione termini multipli
            newGeoWord = searchMulti(geoWord);
      
            // 0) In base alla presenza di termini significativi
            newGeoWord = searchGeoStopwords(geoWord, allWordVector);
                 
            // 1) In base al contesto (Nazione, Regione, Provincia)
            newGeoWord = searchContext(geoWord, finalWordVector, allWordVector, filterWordVector);
   
            // 2) In base alla descrizione geografica della zona
            newGeoWord = searchFeature(geoWord, allWordVector, countryCodeWithMaxFreq);
                                 
            // 3) In base alla maggioranza (Nazione e Continente)
            newGeoWord = searchMajority(geoWord, countryRank, countryFreqMax);
           
            // 4) In base alla POPOLAZIONE
            newGeoWord = searchPopulation(geoWord, popAvg);
/*            
            System.out.println(geoWord.getZoneDocName() + 
                    " - " + geoWord.getName() + " - " + geoWord.getCountryCode() +
                    " - " + geoWord.getGeoScore());  

           
*/            
             newFinalWordVector.add(newGeoWord);
        }        
        // 5) Modifica il peso delle zone amministrative: Continent, Country, Region 
        newFinalWordVector = increaseGeoAdminZoneScore(newFinalWordVector);                               
            
        // 6) In base alla vicinanza spaziale tra le varie GeoWord
            newFinalWordVector = searchDistance(newFinalWordVector, filterWordVector);
            
        /*
                for(int d = 0; d < newFinalWordVector.size(); d++){
            System.out.println(newFinalWordVector.elementAt(d).getZoneDocName() + 
                    " - " + newFinalWordVector.elementAt(d).getName() +
                    " - " + newFinalWordVector.elementAt(d).getCountryCode() +
                    " - " + newFinalWordVector.elementAt(d).getGeoScore());
        }   
        
        */
                      
        //Elimino uguali e tengo solo quelli che hanno GeoScore maggiore
        newFinalWordVector = mergeGeoWords(newFinalWordVector);
        newFinalWordVector = mergeGeoWords2(newFinalWordVector);
        
        return newFinalWordVector;
    }
    
    /**
     * Aumento il GeoScore delle paroole formate da più termini e di un valore più basso
     * quelle formate da un termine solo. Le località negli Stati Uniti ricevono
     * un peso dimezzato
     * @param geoWord : termine o insieme di termini in esame
     * @return la geoWord con il GeoScore aggiornato
     */
    public GeographicWord searchMulti(GeographicWord geoWord){
        double score = geoWord.getGeoScore();
        
        if(!geoWord.getCountryCode().equals("US") && !geoWord.isMultiLow()){           
            if(geoWord.isMulti())    
                geoWord.setGeoScore(score + incrScoreMulti);
            else
                geoWord.setGeoScore(score + 0.5 * incrScoreMulti);
        }
        else{
            if(geoWord.isMulti())    
                geoWord.setGeoScore(score + 0.5 * incrScoreMulti);
            else
                geoWord.setGeoScore(score + 0.25 * incrScoreMulti);
        }
        
        return geoWord;
    }
    
    /**
     * Metodo che scorre l'intero vettore delle GeoWord e per ogni termine 
     * controlla se è presente nel file "geoStopwords.txt". 
     * Se è presente viene abbassato il GeoScore.
     * @param geoWord : termine o insieme di termini in esame
     * @param allWordVector : insieme di tutte le Word reperite nel docuimento
     * @return la geoWord con il GeoScore aggiornato
     */
    public GeographicWord searchGeoStopwords(GeographicWord geoWord, Vector<Word> allWordVector){
        for (int i = 0; i < geoStopwords.size(); i++) {
            if (geoStopwords.get(i).equals(geoWord.getName()) || geoStopwords.get(i).equals(geoWord.getZoneDocName())) {
                double score = geoWord.getGeoScore();
                geoWord.setGeoScore(score + decrScoreGeoStopWords * 1);
            }
        }
        return geoWord;
    }

       
    /**
     * Metodo che per la geoWord che riceve come parametro cerca nel DataBase la Nazione,
     * la Capitale, la Regione ed il Continente di appartenenza.
     * Una volta trovati questi elementi controlla se nel testo esistono termini uguali;
     * in caso affermativo aumenta il peso della GeoWord.
     * @param geoWord : termine o insieme di termini in esame
     * @param finalWordVector : vettore contenente tutte le GeoWord reperite
     * @param allWordVector : vettore contenente tutte le Word del documento
     * @param filterWordVector : vettore delle Word che hanno superato la fase di filtro
     * @return la geoWord con il GeoScore aggiornato
     */
     public GeographicWord  searchContext(GeographicWord geoWord, Vector<GeographicWord> finalWordVector, Vector<Word> allWordVector, Vector<Word> filterWordVector){
        String countryCode = geoWord.getCountryCode();  // IT --> Italia
        String admin1Code = geoWord.getAdmin1Code();    // 9 --> Lombardia
        String admin2Code = geoWord.getAdmin2Code();    // BG --> Bergamo
        String searchingName = "";
        Vector<String> stringResult = new Vector<String>(); //Vettore dei risultati della query
        Vector<String> continentGeonameId = new Vector<String>(); //0-->NAME 1-->ID del continente
        double alpha = 1;
        double beta = 2;
        double gamma = 3;
        
        Vector<String> countryNames = new Vector<String>();     //Vettore con i nomi della Nazione
        Vector<String> alternateCountryNames = new Vector<String>();    //Vettore con i nomi alternativi della Nazione 
        Vector<String> continentNames = new Vector<String>();   //Vettore con i nomi del Continente
        Vector<String> regionNames = new Vector<String>();      //Vettore con i nomi della Regione
        Vector<String> admin1names = new Vector<String>();      //Vettore delle zone amministrative 1
        double score = geoWord.getGeoScore();
        
        //Reperisco dalla tabella "countryinfo" NomeNazione, Continente a partire dal CountryCode
        searchingName = StringOperation.convertString(countryCode);
        stringResult = selectCountryInfoQuery(searchingName); 
        
        //TROVO I NOMI DI TUTTE LE DIVISIONI ADMIN 1
        if(!admin1Code.equalsIgnoreCase("00")){
            String composeField = countryCode + "." + admin1Code; // IT.9
            searchingName = StringOperation.convertString(composeField);
            admin1names = selectAdmin1CodesQuery(searchingName); //Cerco i nomi della Regione

            for(int j = 0; j < admin1names.size()-1; j++){ //Cerco i nomi alternativi della Regione
                String c = StringOperation.convertString(admin1names.elementAt(j));
                String geonameid = admin1names.elementAt(2);
                regionNames = selectGeonameQuery(c, geonameid); 
            }
            regionNames = eraseEquals(regionNames); //Elimino uguali e nulli
            //Incremento il peso
            //if(!regionNames.isEmpty())            
            //    geoWord = incrementScore(geoWord, regionNames, finalWordVector, regionNames.elementAt(0), 3, upperCaseWordVector);  // 3 sta per REGION
        }
        
        //TROVO I NOMI DI TUTTI I CONTINENTI e DELLE NAZIONI
        //continent
    	/*
		AF : Africa			geonameId=6255146
		AS : Asia			geonameId=6255147
		EU : Europe			geonameId=6255148
		NA : North America		geonameId=6255149
		OC : Oceania			geonameId=6255151
		SA : South America		geonameId=6255150
		AN : Antarctica			geonameId=6255152
    	 */
        
       
        if(!stringResult.isEmpty()){
            //continent
            if(!stringResult.elementAt(2).isEmpty()){
            	/// ---------CONTINENT--------------------------------------
            	if(stringResult.elementAt(2).equalsIgnoreCase("AF")) {
             	   continentGeonameId.add("Africa"); //Name                
                    continentGeonameId.add("6255146"); //Geonameid  
                }
                if(stringResult.elementAt(2).equalsIgnoreCase("AS")) {
             	   continentGeonameId.add("Asia"); //Name                
                    continentGeonameId.add("6255147"); //Geonameid
             	   
                }
                if(stringResult.elementAt(2).equalsIgnoreCase("EU")) {
             	   continentGeonameId.add("Europa"); //Name                
                    continentGeonameId.add("6255148"); //Geonameid
             	   
                }
                if(stringResult.elementAt(2).equalsIgnoreCase("NA")) {
             	   continentGeonameId.add("North Europa"); //Name                
                    continentGeonameId.add("6255149"); //Geonameid
             	   
                }
                if(stringResult.elementAt(2).equalsIgnoreCase("OC")) {
             	   continentGeonameId.add("Oceania"); //Name                
                    continentGeonameId.add("6255151"); //Geonameid
             	   
                }
                if(stringResult.elementAt(2).equalsIgnoreCase("SA")) {
             	   continentGeonameId.add("South America"); //Name                
                    continentGeonameId.add("6255150"); //Geonameid
             	   
                }
                if(stringResult.elementAt(2).equalsIgnoreCase("AN")) {
             	   continentGeonameId.add("Antarctica"); //Name                
                    continentGeonameId.add("6255152"); //Geonameid
             	   
                }
         	//---------------------------------------------------------------------------
         	
                 
                //continentGeonameId = selectContinentCodesQuery(stringResult.elementAt(2), stmt);
                continentNames = selectGeonameIdQuery(continentGeonameId.elementAt(1), "alternatename");
                continentNames = eraseEquals(continentNames); //Elimino uguali e nulli
            }
            
            //country
            if(!stringResult.elementAt(3).isEmpty()){
                countryNames = selectGeonameIdQuery(stringResult.elementAt(3), "geoname");               
                alternateCountryNames = selectGeonameIdQuery(stringResult.elementAt(3), "alternatename");
                for(int i = 0; i < alternateCountryNames.size(); i++){
                    countryNames.add(alternateCountryNames.elementAt(i));
                }        
                countryNames = eraseEquals(countryNames); //Elimino uguali e nulli  
            }
        }
        
        double incr = (alpha *isNear(continentNames, geoWord.getZoneDocName(), filterWordVector) + alpha * 2 * isNear(countryNames, geoWord.getZoneDocName(), filterWordVector) + 3 * alpha * isNear(regionNames, geoWord.getZoneDocName(), filterWordVector) + 4 * alpha * isNear(admin2Code, geoWord.getZoneDocName(), filterWordVector)) / (alpha*4 + alpha + 2*alpha + 3*alpha);geoWord.setGeoScore(score + incrScoreContext * incr);

        return geoWord;
    }
     
     
    /**
     * Metodo che cerca nella tabella "featurecodes" la descrizione della zona.
     * Per ogni termine trovato cerco un match con le parole vicine alla Word ed
     * in caso affermativo incremento il peso.
     * @param geoWord : termine o insieme di termini in esame
     * @param allWordVector : vettore contenente tutte le Word del documento
     * @param swLanguage 
     * @param countryCodeWithMaxFreq : codice della nazione che possiede più Word nel testo
     * @return la geoWord con il GeoScore aggiornato
     * @throws java.io.IOException
     */
    public GeographicWord  searchFeature(GeographicWord geoWord, Vector<Word> allWordVector, String countryCodeWithMaxFreq) throws IOException{
        String featureClass = geoWord.getFeatureClass();
        String featureCode = geoWord.getFeatureCode();
        String code = featureClass + "." + featureCode; // H.STMQ
        Vector<String> descriptionString = new Vector<String>();
        Vector<String> descriptionTerm = new Vector<String>();
        int j = 0;
        int control = 10;
        int k = 0; //numero di match tra parole testo e parole tabella
        
        //Ottengo il NOME e la DESCRIZIONE della zona
        descriptionString = selectFeatureQuery(code);
        
        // Devo selezionare solo le parole UTILI (sfrutto l'analizzatore da me creato)
        AnalyzerUtils descriptionAnalyzer = new AnalyzerUtils();
        for(int i = 0; i < descriptionString.size(); i++){
            descriptionTerm = descriptionAnalyzer.getNameTokens(myStdAnalyzer, descriptionString.elementAt(i));
        }
        
        // Verifico nelle vicinanze della GeoWord (shifFeature termini prima e shifFeature dopo) 
        // se c'è un termine uguale a quelli trovati (descriptionTerm).
        int geoWordPosition = geoWord.getPosition();
        if(geoWordPosition > shifFeature)
            j = geoWordPosition-shifFeature;
        for(int i = 0; i < descriptionTerm.size(); i++){
            while((j < geoWordPosition+10 && j < allWordVector.size()) && control > 0){ //Cerco nei termini intorno alla geoWord
                if(descriptionTerm.elementAt(i).equals(allWordVector.elementAt(j).getName())){
                    k++;
                    //double score = geoWord.getGeoScore();
                    //geoWord.setGeoScore(score + incrScoreFeature);
                    control--;
                }
                j++;
            }
        }
        
        //incremento
        double score = geoWord.getGeoScore();
        double incr = 0.0;
        if(( geoWord.getFeatureClass().equals("T") || geoWord.getFeatureClass().equals("H")
                 || geoWord.getFeatureClass().equals("V") )) {
            if(geoWord.getCountryCode().equals(countryCodeWithMaxFreq)){
                incr = 0.5 * (1 + 0.2 * k);
            }
            else
                incr = 0.5 * (0.5 + 0.2 * k);
        }
        
        geoWord.setGeoScore(score + incrScoreFeature * incr);
         
        
        return geoWord;
    }
    
    /**
     * Metodo aggiorna il peso di ogni singola geoWord in base alla posizione della
     * Nazione a cui appartiene rispetto al vettore countryrank, che contiene 
     * la classifica delle nazioni più citate nel documento
     * @param geoWord : GeoWord in esame
     * @param countryRank : vettore contenente Nome e Frequenza della nazione
     * @param countryFreqMax : frequenza della Nazione presente più volte tra le GeoWord
     * @return la GeoWord con il campo geoScore aggiornato
     */
    public GeographicWord searchMajority(GeographicWord geoWord, Vector<Word> countryRank, int countryFreqMax){
        double score = geoWord.getGeoScore();
        
        //Aumento peso in base alla maggioranza
        for(int i = 0; i < countryRank.size(); i++){
            if(geoWord.getCountryCode().equals(countryRank.elementAt(i).getName())){
                double freqRel = (double) countryRank.elementAt(i).getFrequency() / countryFreqMax;
                double scoreRise = (double) freqRel * incrScoreMajority;
                geoWord.setGeoScore(score + scoreRise);
            }
        }      
        
        return geoWord;
    }
    

    /**
     * Aumento il peso della Word in base alla popolazione
     * @param geoWord : Word in esame
     * @param avgPop : popolazione media 
     * @return la GeoWord con il campo geoScore aggiornato
     */
    public GeographicWord searchPopulation(GeographicWord geoWord, double avgPop){
        int geoPopulation = geoWord.getPopulation();
        double score = geoWord.getGeoScore();        
        double incr = 0.0;
        
        if(geoPopulation >= avgPop)
            incr = 1;
        else{
            double popAvg = geoPopulation / avgPop;            
            incr = Math.pow(popAvg, 0.3);
        }

        geoWord.setGeoScore(score + incr * incrScorePop);

        return geoWord;
    }

       
    /**
     * Metodo che calcola la matrice delle distanze tra tutte le geoWOrd reperite.
     * In base ai valori ottenuti incremena il GeoScore alle zone che sono geograficamente
     * vicine tra loro.
     * @param finalWordVector : vettore contenente tutte le GeoWord reperite
     * @param filterWordVector : vettore delle Word che hanno superato la fase di filtro
     * @return il vettore delel GeoWord con il GeoScore aggiornato
     */
    public Vector<GeographicWord> searchDistance(Vector<GeographicWord> finalWordVector, Vector<Word> filterWordVector){         
        Vector<GeographicWord> geoMaxWordVector = new Vector<GeographicWord>(); //termini con peso > 0.4
        Vector<GeographicWord> geoMinWordVector = new Vector<GeographicWord>(); //termini con peso < 0.4
        double maxDist = 0.0;   //Distanza massima all'interno della matrice
        double sumTot = 0.0;    //Somma di tutte le distanze (escluso dist = 0)
        double mediaTot = 0.0;  //Media tra tutte le distanze della matrice
        int distTot = 0;        //Numero totale delle distanze (escluso dist = 0)
        double referenceValue = 0.0;    //Valore di riferimento per il calcolo del peso
                                        //Media Normalizzata

        GeoDistance dist = new GeoDistance();
     
         //Considero solo i termini con peso > 0.3        
        for(int i = 0; i < finalWordVector.size(); i++){
            if(finalWordVector.elementAt(i).getGeoScore() >= 0.2 && finalWordVector.elementAt(i).getName().length() > 1)
                geoMaxWordVector.add(finalWordVector.elementAt(i));
            else
                geoMinWordVector.add(finalWordVector.elementAt(i));
        }
        
        int numZoneTot = geoMaxWordVector.size();
        if(numZoneTot <= 1)
            numZoneTot = 2;
        
        
         //Matrici delle distanze
         
         if(geoMaxWordVector.size() > 2500)
             return geoMaxWordVector;
         
         double[][] distMatr = new double[geoMaxWordVector.size()][geoMaxWordVector.size()];
         //double[][] distMatrNorm = new double[geoMaxWordVector.size()][geoMaxWordVector.size()];
         double[] maxRowDist = new double[geoMaxWordVector.size()];        
         
         //Popolazione della matrice 
         for (int i=0; i < distMatr.length; i++) {        // scandisce righe
            GeographicWord gw1 = geoMaxWordVector.elementAt(i);
            for (int j=0; j < distMatr.length; j++){     // scandisce colonne
                GeographicWord gw2 = geoMaxWordVector.elementAt(j);
                distMatr[i][j] = dist.calculateDistance(gw1.getLatitude(), gw1.getLongitude(), gw2.getLatitude(), gw2.getLongitude(), 'K');
            }
         }
         
         //Calcolo la distanza massima per ogni riga e totale
         for (int i=0; i < distMatr.length; i++) {        // scandisce righe
            GeographicWord gw1 = geoMaxWordVector.elementAt(i);
            double max = 0.0;
            for (int j=0; j < distMatr.length; j++){     // scandisce colonne
                GeographicWord gw2 = geoMaxWordVector.elementAt(j);
                if(distMatr[i][j] > max)
                    max = distMatr[i][j];
                if(distMatr[i][j] > maxDist)
                    maxDist = distMatr[i][j];
                if(distMatr[i][j] > 0){
                    sumTot = sumTot + distMatr[i][j];
                    distTot++;
                }
            }
            maxRowDist[i] = max;
         }
         mediaTot = sumTot / distTot;           //Distanza media
         referenceValue = (mediaTot / maxDist);   //Valore di riferimento per la selezione delle zone vicine
                                                  //E' la media normalizzata rispetto alla dist MAX
         
         /* */
         for (int i=0; i < distMatr.length; i++) {        // scandisce righe
            GeographicWord gw1 = geoMaxWordVector.elementAt(i);
            double sum = 0.0;
            double contr = 0.0;
            double sumContr = 0.0;
            double incr = 0.0;
        
            for (int j=0; j < distMatr.length; j++){      // scandisce colonne
                GeographicWord gw2 = geoMaxWordVector.elementAt(j);
                if(!gw1.getName().equals(gw2.getName())){ //Se hanno nomi diversi
                     
                    if(distMatr[i][j] <= 0.3 * mediaTot){
                        contr = (rho * mediaTot - distMatr[i][j]) / (rho * mediaTot);
                    }
                    else
                        contr = 0;   
                    
                    sumContr = sumContr + contr;
                } 
            }
            
            if(sumContr != 0)
                incr = sumContr / (geoMaxWordVector.size() - 1);
            else
                incr = 0;
            gw1.setGeoScore(gw1.getGeoScore() + incr * incrScoreDistance);
         }
     
        //Aggiungo le GeoWord a cui NON ho aumentato il peso (hanno peso < 0.3)
        for(int i = 0; i < geoMinWordVector.size(); i++){
            geoMaxWordVector.add(geoMinWordVector.elementAt(i));
        }
         
         return geoMaxWordVector;
     }

    
    /**
     * Interrogo il DB cercando nella tabella countryinfo la Nazione, la Capitale
     * e il Continente corrispondenti al countryCode ricevuto come parametro
     * @param searchingName : nome della Word in esame
     * @return 4 termini nel seguente ordine: nome della Nazione, Capitale, Continente, geonameId
     */
    public Vector<String> selectCountryInfoQuery(String searchingName){       
        Vector<String> stringResult = new Vector<String>();

        try {
            // Eseguo la qeury reperendo tutte le info
//            ResultSet result = stmt.executeQuery("SELECT name, capital, continent, geonameid FROM countryinfo " +
//                    "WHERE iso_alpha_2='" + searchingName + "'");
            Serial a=new Serial();
            String dati[]=new String[4];
            RecordManager mydbCountryInfo;
            BTree tContryInfo=new BTree();
            mydbCountryInfo=RecordManagerFactory.createRecordManager(dbpath+"albero_countryInfo", new Properties());
            tContryInfo=loadOrCreateBTree(mydbCountryInfo, "country", a);
            Object results=tContryInfo.find(searchingName);
            if(results!=null){
            	dati=((String)results).split("�#");
           
                stringResult.add(dati[0]); //name
                stringResult.add(dati[1]); //capital
                stringResult.add(dati[2]); //continent
                stringResult.add(dati[3]); //geonameid
            }

//            result.close();
            mydbCountryInfo.close();
         
//        } catch (SQLException ex) {
//            Logger.getLogger(Score.class.getName()).log(Level.SEVERE, null, ex);
       }
    catch (Exception e) {
		// TODO: handle exception
	}
        
        return stringResult;
    }
    
    
    /**
     * Ricerca nel DataBase dei nomi alternativi del luogo che ha il geonameid 
     * ricevuto come parametro. 
     * @param searchingName : nome della Word di interesse
     * @param table : nome della tabella su cui eseguire la query
     * @return l'elenco dei nomi alternativi trovati
     */
    public Vector<String> selectGeonameIdQuery(String searchingId, String table){       
        //----------------------------------------------------------------
   	 Vector<String> stringResult = new Vector<String>();

        try {
           
            Serial a=new Serial();
            String dati[];
            RecordManager mydbalternatenamesId;
            BTree talternatenamesId=new BTree();
            //Input GeoID
            //Output name
            mydbalternatenamesId=RecordManagerFactory.createRecordManager(dbpath+"albero_alternatenameId", new Properties());
            talternatenamesId=loadOrCreateBTree(mydbalternatenamesId, "alternatenameId", a);
            Object results=talternatenamesId.find(searchingId);
            
            if(results!=null){
            	dati=((String)results).split("�#");
            	int i=0;
            	for(i=0;i<dati.length;i++){
            		stringResult.add(dati[i]);// AlternateName OR Name
            		stringResult.add(dati[i]);// AsciiAlternateName OR AsciiName
            	}
                
           
            }
            mydbalternatenamesId.close();
         
//        } catch (SQLException ex) {
//            Logger.getLogger(Score.class.getName()).log(Level.SEVERE, null, ex);
       }
    catch (Exception e) {
		// TODO: handle exception
	}    
    
       return stringResult;  
    	       
//        try {
//            // Eseguo la query reperendo tutte le info
//            ResultSet result = stmt.executeQuery("SELECT " + field1 + ", " + field2 + " FROM " + table +
//                    " WHERE geonameid='" + searchingId + "'");
//
//
//            while (result.next()) { 
//                stringResult.add(result.getString(1)); // AlternateName OR Name          
//                stringResult.add(result.getString(2)); // AsciiAlternateName OR AsciiName       
//            }
//
//            result.close();
//
//         
//        } catch (SQLException ex) {
//            Logger.getLogger(Score.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        
//        return stringResult;
    }
    
    
    /**
     * Eliminazione dal vettore di tutti i nomi doppi
     * @param stringVector : vettore di stringhe 
     * @return il vettore senza i nomi doppi
     */
    public Vector<String> eraseEquals(Vector<String> stringVector){
        Vector<String> newStringVector = new Vector<String>(); 
        Vector<String> app = stringVector; //Vettore di appoggio
        Vector<String> equals = new Vector<String>(); //Vettore con i nomi uguali
        
        for(int i = 0; i < stringVector.size(); i++){  
            String name = stringVector.elementAt(i);
            
            if(!name.isEmpty()){ //Se il nome NON è vuoto           
                if(equal(name, app)){ //Se nel vettore in analisi c'è un elemento con lo stesso nome
                    if(!equal(name, equals)){ //Se il nome NON è nel vettore con i nomi doppi lo metto
                        equals.add(name);     //e lo aggiungo anche nel vettore finale
                        newStringVector.add(name);
                    }
                }
                else //Se nel vettore in analisi NON c'è un elemento con lo stesso nome lo aggiungo al nuovo vettore
                    newStringVector.add(name);
            }
        }
        
        return newStringVector;
    }
    
    
    /**
     * Restituisce TRUE se name è uguale ad un termine nel vettore
     * FALSE se non c'è nessun termine uguale
     * @param name : stringa da confrontare
     * @param stringVector : vettore con i nomi da confrontare
     * @return
     */
    public boolean equal(String name, Vector<String> stringVector){
        boolean result = false;
        
        for(int k = 0; k < stringVector.size(); k++){   
            if(!name.isEmpty())
                if(name.equalsIgnoreCase(stringVector.elementAt(k)))
                    result = true;
        }
                
        return result;
    }
    
    /**
     * Interrogazione della tabella "geoname" e reperimento dei nomi (name, asciiname e alternatenames)
     * della zona ricevuta come parametro.
     * @param searchingName : nome della zona di interesse
     * @param geonameid : id della zona di interesse
     * @return
     */
    public Vector<String> selectGeonameQuery(String searchingName, String geonameid){
        //----------------------------------------------------------------
    	 Vector<String> stringResult = new Vector<String>();

         try {
            
             Serial a=new Serial();
             String dati[];
             RecordManager mydbalternatenamesId;
             RecordManager mydbalternatenames;
             BTree talternatenames=new BTree();
             BTree talternatenamesId=new BTree();
             //Input GeoID
             //Output name
             mydbalternatenamesId=RecordManagerFactory.createRecordManager(dbpath+"albero_alternatenameId", new Properties());
             talternatenamesId=loadOrCreateBTree(mydbalternatenamesId, "alternatenameId", a);
             Object results=talternatenamesId.find(geonameid);
             
             if(results!=null){
             	dati=((String)results).split("�#");
             	int i=0;
             	for(i=0;i<dati.length;i++){
             		stringResult.add(dati[i]);
             		stringResult.add(dati[i]);
             	}
                 
            
             }
             mydbalternatenamesId.close();
             //Input Alternate
             //output 
             
             mydbalternatenames=RecordManagerFactory.createRecordManager(dbpath+"albero_alternatename", new Properties());
             talternatenames=loadOrCreateBTree(mydbalternatenames, "alternatename", a);
             Object results1=talternatenames.find(searchingName);
             if(results1!=null){
              	if(!stringResult.contains(((String) results1))){
              		stringResult.add(((String) results1));
              		stringResult.add(((String) results1));
              	}
                  
             
              }
             mydbalternatenames.close();
//         } catch (SQLException ex) {
//             Logger.getLogger(Score.class.getName()).log(Level.SEVERE, null, ex);
        }
     catch (Exception e) {
 		// TODO: handle exception
 	}    
     //-----------------------------------------------------------------------
    	
    	
//        String alternatenames = "";
//        String [] temp = null;
//        
//        try {
//            // Eseguo la qeury reperendo tutte le info
//            ResultSet result = stmt.executeQuery("SELECT alternatename, asciialternatename FROM alternatename " +
//                    "WHERE alternatename='" + searchingName + "' OR asciialternatename='" + searchingName + 
//                    "' OR geonameid='" + geonameid + "'");
//
//            while (result.next()) { // process results one row at a time
//                stringResult.add(result.getString(1)); //name
//                stringResult.add(result.getString(2)); //asciialternatename
//                
//                
//                
//            }
//            
//            result.close();
//         
//        } catch (SQLException ex) {
//            Logger.getLogger(Score.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        
        return stringResult;    
    }
    
    
    /**
     * Esecuzione di una query di tipo select sulla tabella "admin1codesascii" e 
     * reperimento dei nomi della regione
     * @param searchingName : nome della zona di interesse
     * @return
     */
    public Vector<String> selectAdmin1CodesQuery(String searchingName){
  

            // Eseguo la qeury reperendo tutte le info
//            ResultSet result = stmt.executeQuery("SELECT name, nameascii, geonameid FROM admin1codesascii " +
//                    "WHERE code='" + searchingName + "'");
           //__________________________________________________________________ 
            Vector<String> stringResult = new Vector<String>();

            try {
               
                Serial a=new Serial();
                String dati[]=new String[4];
                RecordManager mydbadmin1;
                BTree tadmin1=new BTree();
                mydbadmin1=RecordManagerFactory.createRecordManager(dbpath+"albero_admin1codeascii", new Properties());
                tadmin1=loadOrCreateBTree(mydbadmin1, "admin1", a);
                Object results=tadmin1.find(searchingName);
                if(results!=null){
                	dati=((String)results).split("�#");
               
                    stringResult.add(dati[0]); //name
                    stringResult.add(dati[1]); //asciiname
                    stringResult.add(dati[2]); //geonameid
               
                }
                mydbadmin1.close();
             
//            } catch (SQLException ex) {
//                Logger.getLogger(Score.class.getName()).log(Level.SEVERE, null, ex);
           }
        catch (Exception e) {
    		// TODO: handle exception
    	}    
        //_________________________________________________________________________    
            
//            while (result.next()) { // process results one row at a time
//                stringResult.add(result.getString(1)); //name
//                stringResult.add(result.getString(2)); //asciiname
//                stringResult.add(result.getString(3)); //geonameid
//            }

            //result.close();
         
        
        
        return stringResult;
    }
    
    /**
     * Metodo che interroga il DataBase sulla tabella "featurecodes" ed estrae il NOME e
     * la DESCRIZIONE della zona geografica.
     * @param code : codice della nazione
     * @return
     */
    public Vector<String> selectFeatureQuery(String code){ 
    	Vector<String> stringResult = new Vector<String>();

        try {
           
            Serial a=new Serial();
            String dati[];
            RecordManager mydbfeaturecodes;
            BTree tfeaturecodes=new BTree();
            //Input GeoID
            //Output name
            mydbfeaturecodes=RecordManagerFactory.createRecordManager(dbpath+"albero_featurecodes", new Properties());
            tfeaturecodes=loadOrCreateBTree(mydbfeaturecodes, "featurecodes", a);
            Object results=tfeaturecodes.find(code);
            
            if(results!=null){
            	dati=((String)results).split("�#");
            		stringResult.add(dati[1]);//name
            		stringResult.add(dati[2]);//description
            }
            mydbfeaturecodes.close();
         
//        } catch (SQLException ex) {
//            Logger.getLogger(Score.class.getName()).log(Level.SEVERE, null, ex);
       }
    catch (Exception e) {
		// TODO: handle exception
	}    
    
       return stringResult;  
//        Vector<String> stringResult = new Vector<String>();
//
//        try {
//            // Eseguo la query reperendo tutte le info
//            ResultSet result = stmt.executeQuery("SELECT * FROM featurecodes " +
//                    "WHERE code='" + code + "'");
//
//            while (result.next()) { // process results one row at a time
//                stringResult.add(result.getString(2)); //name
//                stringResult.add(result.getString(3)); //description
//            }
//
//            result.close();
//         
//        } catch (SQLException ex) {
//            Logger.getLogger(Score.class.getName()).log(Level.SEVERE, null, ex);
//        } 
//        
//        return stringResult;
    }
    
    
    public double getMaxPop(Vector<GeographicWord> finalWordVector){
        int sumPop = 0;
        int count = 0;
        double avgPop = 0;
        
        for(int i = 0; i < finalWordVector.size(); i++){
            if(finalWordVector.elementAt(i).getPopulation() != 0){
                sumPop = sumPop + finalWordVector.elementAt(i).getPopulation();
                count++;
            }
        }
        
        if(count != 0)
            avgPop = sumPop / count;
        
        return avgPop;
    }
    
    
    /**
     * Metodo che ha il compito di creare un ranking delle Nazioni 
     * delle GeoWords. Al primo posto vanno le Nazioni che hanno più GeoWord nel documento.
     * @param finalWordVector : vettore delle GeoWord
     * @return vettore delle Nazioni ordinate in maniera decrescente a partire da quella con maggiori GeoWord
     */
    public Vector<Word> createNationRanking(Vector<GeographicWord> finalWordVector){
        Vector<String> allCountryVector = new Vector<String>();
        Vector<String> app = new Vector<String>();
        Vector<Word> countryVector = new Vector<Word>();
        String countryCode = null;
        Vector<String> equals = new Vector<String>(); //Vettore con i nomi uguali
         
        //Creo un vettore formato da tutti i codici delle Country
        for(int i = 0; i < finalWordVector.size(); i++){  
            countryCode = finalWordVector.elementAt(i).getCountryCode();
            
            //if(!countryCode.equals("US")){
                allCountryVector.add(countryCode);

                // Seleziono le varie Nazioni (mi fermo a 210 stati, inutile cercare oltre)             
                if(i < 210){
                    if(equal(countryCode, app)){ //Se nel vettore in analisi c'è un elemento con lo stesso nome
                        if(!equal(countryCode, equals)){ //Se il nome NON è nel vettore con i nomi doppi lo metto
                            equals.add(countryCode);     //e lo aggiungo anche nel vettore finale
                        }
                    }
                    else //Se nel vettore in analisi NON c'è un elemento con lo stesso nome lo aggiungo al nuovo vettore
                        app.add(countryCode);
                }
            //}
        }
        
        
        //Conto le frequenze e creo un nuovo vettore di Country con il numero di occorrenze
        for(int j = 0; j < app.size(); j++){
            int freq = 0;
            for(int k = 0; k < allCountryVector.size(); k++){
                String s1 = app.elementAt(j);
                String s2 = allCountryVector.elementAt(k);
                if(s1.equals(s2)){
                    String s = allCountryVector.elementAt(k);
                    freq++;
                }
            }
            Word newCountry = new Word();
            newCountry.setName(app.elementAt(j));   //Cod Nazione
            newCountry.setFrequency(freq);          //Frequenza del codice
            countryVector.add(newCountry);
        }       
        
        return countryVector;
    }
    
      
 
    /**
     * Scorro il vettore delle word che hanno superato la fase di filtro e verifico se la geoWord 
     * in esame e la geoZoneDocName (Continente, Nazione o Regione della geoWord) sono vicine.
     * @param geoWordName : nome da analizzare (continent, nation, ...)
     * @param geoZoneDocName : nome con cui eseguire il confronto (GW nel testo)
     * @param filterWordVector : elenco delle Word che hanno superato la fase di filtro
     * @return TRUE se la geoWordName e geoZoneDocName sono vicine nel testo
     */
    public double isNear(String geoWordName, String geoZoneDocName, Vector<Word> filterWordVector){
        double result = 0;
        int start = 0;
        int stop = 0;
        
        if(geoWordName.isEmpty())
            result = 0;
        else{
            for(int i = 0; i < filterWordVector.size(); i++){
                String upWord = filterWordVector.elementAt(i).getName();
                if(upWord.equals(geoWordName)){
                    result = 0.5;
                                                         
                    //Setto partenza
                    if(i > distNear)
                        start = i - distNear;
                    else
                        start = i;
                    //Setto fine
                    if(i < filterWordVector.size()-distNear)
                        stop = i + distNear;
                    else
                        stop = filterWordVector.size();
                    
                    String first = getFirstTerm(geoZoneDocName);
                    
                    //Controllo le word vicine
                    for(int j = start; j < stop; j++){
                        if(filterWordVector.elementAt(j).getName().equals(first))
                            if(i != j)
                                result = 1;
                            else
                                result = 0;
                    }

                    addZones(upWord);
                }
            }
        }
        
        return result;
    }
    
    
    public String getFirstTerm(String multiTerm){
        String[] terms = null;
        String result = "";
        
        terms = multiTerm.split(" ");
        
        if(terms.length > 1)
            result = terms[0];
        else
            result = multiTerm;
        
        return result;
    }
    
    
    public double isNear(Vector<String> geoWordName, String geoZoneDocName, Vector<Word> filterWordVector){
        double part = 0.0;
        double max = 0.0;
        
        if(!geoWordName.isEmpty()){
            for(int i = 0; i < geoWordName.size(); i++){
                String div = geoWordName.elementAt(i);

                part = isNear(div, geoZoneDocName, filterWordVector);
                    
                if(part > max)
                    max = part;
            }
        }
        
        return max;
    }
    

    /**
     * Metodo che riceve il nome di una zona geografica (Continente, Nazione o Regione)
     * che è stata trovata nel documento ed a cui appartiene una GeoWord.
     * Questo metodo deve controllare se la parola esiste già nel vettore geographicZones
     * Se esiste aumenta la frequenza di uno altrimenti la aggiunge al vettore e mette la frequenza a 1.
     * @param geographicName : nome della zona geografica da aggiungere al vettore
     */
    public void addZones(String geoZoneName){
        Vector<String> geographicNames = new Vector<String>();
        
        //Creo un vettore String da passare al metodo equal
        for(int i = 0; i < geoAdminZones.size(); i++){
            geographicNames.add(geoAdminZones.elementAt(i).getName());
        }
        
        if(!equal(geoZoneName, geographicNames)){   //Don't exists...
            GeographicWord w = new GeographicWord();
            w.setName(geoZoneName);
            w.setFrequency(1);
            geoAdminZones.add(w);
        }
        else{ //Already exists...
            for(int i = 0; i < geoAdminZones.size(); i++){
                if(geoAdminZones.elementAt(i).getName().equals(geoZoneName)){
                    int freq = geoAdminZones.elementAt(i).getFrequency();
                    geoAdminZones.elementAt(i).setFrequency(freq + 1);
                }                   
            }
        }           
        
    }
    
    
    /**
     * Metodo che aumenta il peso delle Nazioni, Province e dei Continenti.
     * Il peso viene aumentato in base alla freqeunza della geographicZone: essa infatti
     * indica quante volte la zona geografica ha avuto un match con altre Word all'interno del documento.
     * @param newFinalWordVector : vettore con le GeoWords
     * @return il vettore delle GeoWord con il peso aggiornato
     */
    public Vector<GeographicWord> increaseGeoAdminZoneScore(Vector<GeographicWord> newFinalWordVector){
        Vector<String> app = new Vector<String>();
        boolean find = false;
        Vector<String> nameAsciinameAlternateName = new Vector<String>();
        String[] temp = null;
        int sum = 0;
        double max = 0.0;
        double param = 0.0;
        
        //Calcolo la freqeunza massima tra tutte le zone
        for(int j = 0; j < geoAdminZones.size(); j++){
            GeographicWord gz = geoAdminZones.elementAt(j);
            String s = gz.getName();
            app.add(gz.getName());
            if(gz.getFrequency() > 1 && gz.getFrequency()>max){               
                sum = sum + gz.getFrequency();
                max = gz.getFrequency();
            }
        }
        
        //Scorro il vettore con le geoAdminZone e cerco match con vettore GeoWord
        for(int j = 0; j < geoAdminZones.size(); j++){
            GeographicWord gz = geoAdminZones.elementAt(j);
            String s = gz.getName();
            find = false;
            for(int i = 0; i < newFinalWordVector.size(); i++){
                if(!find){
                    GeographicWord gw = newFinalWordVector.elementAt(i);
                    String name = StringOperation.convertString(gw.getZoneDocName());
                    
                    if(name.equals(gz.getName()) && gz.getFrequency() > 1){
                        double score = gw.getGeoScore();                     
                        double freqRel = (double) gz.getFrequency() / max; //ottengo la freqeunza normalizzata rispetto al MAX
                        
                        //Controllo se è un continente
                        if (gw.getName().equals("Europe") || name.equals("Africa") || name.equals("Asia") 
                                || name.equals("Antarctica") || name.equals("Oceania")) {
                            param = 0.5;
                        }
                        else if(name.equals("America") && (gw.getGeonameid() == 6255150 || gw.getGeonameid() == 6255149)){
                            param = 0.5;
                        }      
                        else
                            param = 0.25;
                        
                        double incr = param + freqRel * 0.5;
                        
                        gw.setGeoScore(score + incr * incrScoreGeoAdminZone); 
                        find = true;
                        gw.setAdminZone(true);
                    }
                }
            }
        }
 
        return newFinalWordVector;
    }
    
   
    
    /**
     * Restituisce un vettore di geoWord senza nomi ripetuti. Tra le GeoWord con
     * uguale nome prende quella con peso maggiore.
     * @param newFinalWordVector : vettore con le GeoWord
     * @return un vettore con le GeoWord senza zone doppie
     */
    public Vector<GeographicWord> mergeGeoWords(Vector<GeographicWord> newFinalWordVector) {
        Vector<String> allGeoWordName = new Vector<String>();
        Vector<GeographicWord> newResultVector = new Vector<GeographicWord>(); 
        Vector<String> app = new Vector<String>(); //Vettore di appoggio
        Vector<String> equals = new Vector<String>(); //Vettore con i nomi uguali
        double max = 0.0;
        
        for(int i = 0; i < newFinalWordVector.size(); i++){
            allGeoWordName.add(newFinalWordVector.elementAt(i).getZoneDocName());
        }
        
        
        for(int i = 0; i < newFinalWordVector.size(); i++){ 
            GeographicWord gw = newFinalWordVector.elementAt(i);
            String name = gw.getZoneDocName(); 
                  
            if(equal(name, allGeoWordName)){ //Se nel vettore in analisi c'è un elemento con lo stesso nome
                if(haveMaxScore(gw, newFinalWordVector)) //Controllo se ha peso max tra quelli con nome uguale
                   newResultVector.add(gw); 
            }else //Se nel vettore in analisi NON c'è un elemento con lo stesso nome lo aggiungo al nuovo vettore
                newResultVector.add(gw);
        }
        
        
        
        return newResultVector;
    }
    
    /**
     * Restituisce un vettore di geoWord senza nomi ripetuti. Tra le GeoWord con
     * uguale nome prende quella con popolaz maggiore.
     * @param newFinalWordVector : vettore con le GeoWord
     * @return un vettore con le GeoWord senza zone doppie
     */
    public Vector<GeographicWord> mergeGeoWords2(Vector<GeographicWord> newFinalWordVector) {
        Vector<String> allGeoWordName = new Vector<String>();
        Vector<GeographicWord> newResultVector = new Vector<GeographicWord>(); 
        Vector<String> app = new Vector<String>(); //Vettore di appoggio
        Vector<String> equals = new Vector<String>(); //Vettore con i nomi uguali
        double max = 0.0;
        
        for(int i = 0; i < newFinalWordVector.size(); i++){
            allGeoWordName.add(newFinalWordVector.elementAt(i).getZoneDocName());
        }
        
        
        for(int i = 0; i < newFinalWordVector.size(); i++){ 
            GeographicWord gw = newFinalWordVector.elementAt(i);
            String name = gw.getZoneDocName(); 
                  
            if(equal(name, allGeoWordName)){ //Se nel vettore in analisi c'è un elemento con lo stesso nome
                if(haveMaxScore(gw, newFinalWordVector)) //Controllo se ha peso max tra quelli con nome uguale
                   newResultVector.add(gw); 
            }else //Se nel vettore in analisi NON c'è un elemento con lo stesso nome lo aggiungo al nuovo vettore
                newResultVector.add(gw);
        }
        
        
        
        return newResultVector;
    }
    
    /**
     * Metodo che restituisce TRUE se il peso della geoWord è massimo tra le 
     * geo words con lo stesso nome
     * @param geoWord : GeoWord in esame
     * @param geoWordVector : vettore con le GeoWord
     * @return
     */
    private boolean haveMaxScore(GeographicWord geoWord, Vector<GeographicWord> geoWordVector){
        boolean result = false;
        double max = 0.0;
        
        //Cerco il peso massimo tra le geoWord con nome uguale
        for(int i = 0; i < geoWordVector.size(); i++){
            if(geoWord.getZoneDocName().equals(geoWordVector.elementAt(i).getZoneDocName())){
                if(geoWordVector.elementAt(i).getGeoScore() > max)
                    max = geoWordVector.elementAt(i).getGeoScore();
            }
        }
        
        if(geoWord.getGeoScore() == max)
            result = true;
        
        return result;
    }
    
    /**
     * Metodo che restituisce TRUE se la popolazione della geoWord è massima tra le 
     * geo words con lo stesso nome
     * @param geoWord : GeoWord in esame
     * @param geoWordVector : vettore con le GeoWord
     * @return
     */
    private boolean haveMaxPop(GeographicWord geoWord, Vector<GeographicWord> geoWordVector){
        boolean result = false;
        double max = 0.0;
        
        //Cerco la popolaizone massima tra le geoWord con nome uguale
        for(int i = 0; i < geoWordVector.size(); i++){
            if(geoWord.getZoneDocName().equals(geoWordVector.elementAt(i).getZoneDocName())){
                if(geoWordVector.elementAt(i).getGeoScore() > max)
                    max = geoWordVector.elementAt(i).getPopulation();
            }
        }
        
        if(geoWord.getPopulation() == max)
            result = true;
        
        return result;
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

}

