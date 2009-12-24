/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.analysis;

import geotag.DatabaseGazetteerConnection;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Properties;
//import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.btree.BTree;

import bTree.Serial;
import bTree.Serializ;
import geotag.words.StringOperation;
import geotag.words.GeoWordsOperation;
import geotag.words.GeographicWord;
import geotag.words.Word;

/**
 * Classe fondamentale dell'applicazione.
 * Ha il compito di selezionare dal documento l'insieme delle parole che potenzialmente
 * sono zone geografiche. Per eseguire questa operazione accede al DB.
 * Restituisce un vettore di GeoWords, ovvero di zone candidate ad essere un georiferimento del documento
 * @author Giorgio Ghisalberti
 */
public class GeoCandidateIdentification {
    private static DatabaseGazetteerConnection dbConnection = null;
    
    Vector<Word> filterWordVector = new Vector<Word>(); 
    Vector<Word> allWordVector = new Vector<Word>(); 
    Vector<Word> freqWordVector = new Vector<Word>();
    Vector<String> countryCodeVector = new Vector<String>();
    Vector<GeographicWord> noSearchWordVector = new Vector<GeographicWord>(); //Vettore con WORD appartenenti ad una Word multipla che quindi non devono essere più cercate nel DB
    
    int newPos = 0; //Nuova posizione di ricerca (usata per saltare termini formati da più parole)
    boolean isMultiWord = false;    
    boolean englishLanguage = false;
    String path="./";
    
    /**
     * Costruttore della classe
     */
    public GeoCandidateIdentification(){
    }
    
    /**
     * Metodo che si occupa di gestire l'intera fase di valutazione. 
     * Scorre il vettore delle Word e per ognuna di esse controlla se esiste una termine 
     * uguale all'interno del DB.
     * @param filterWordVector : vettore contenente le Word che superano la fase di Filtro
     * @param allWordVector : vettore contenente tutte le word del documento
     * @param stmt : Statement necessario per l'esecuzioni delel query sul DB
     * @return un vettore di GeoWord, ovvero di word che potenzialmente potrebbero rappresentare zone geografiche
     */
    public Vector<GeographicWord> analyzing (Vector<Word> filterWordVector, Vector<Word> allWordVector, String documentContent, double impValue, Statement stmt, boolean upperDateLine){
        this.filterWordVector = filterWordVector;
        this.allWordVector = allWordVector;
        Vector<GeographicWord> finalVectorResult = new Vector<GeographicWord>();
        Vector<GeographicWord> wordVectorResult = new Vector<GeographicWord>();
        Word analyzingWord = new Word();
        int p = 0;
        int pos = 0; // posizione della Word
                
        // Scorro il vettore filterWordVector e per ogni parola eseguo un controllo interrogando il DB
        for(int i = 0; i < filterWordVector.size(); i++){
            p = newPos;
            
            //Condizione affinché NON esegua ricerche su parole formate da più termini
            if(isMultiWord){
                for(int j = 0; j < filterWordVector.size(); j++){    
                    if(filterWordVector.elementAt(j).getPosition() == p)
                        i = j+1;
                }
                isMultiWord = false;
            }
            
            if(i < filterWordVector.size())
                analyzingWord = filterWordVector.elementAt(i); //parola da analizzare
                       
            pos = analyzingWord.getPosition();
            
            //Eseguo l'analisi della Word ed aggiorno il vettore dei risultati parziali
            wordVectorResult = geoAnalysis(analyzingWord, allWordVector, pos, stmt, upperDateLine);
 
            //Aggiorno il vettore contenente TUTTE le GeoWord trovate nel documento
            finalVectorResult = update(finalVectorResult, wordVectorResult);            
        }
        
                             
        //Elimino le vie 
        finalVectorResult = searchStreet(allWordVector, finalVectorResult); 
        
        //Elimino i nomi deguiti o preceduti da una lettera puntata
        finalVectorResult = searchAuthors(finalVectorResult, allWordVector, documentContent);
        
        //Elimino le parole ad inizio frase (solo nel testo del documento)
        if(impValue == 0.0)
            finalVectorResult = searchFirstWord(finalVectorResult, allWordVector, documentContent);
        
        //Elimino i termini con geonameid uguale
        finalVectorResult = GeoWordsOperation.eraseEquals(finalVectorResult);  
                           
        // Elimino acronimi o parole tutte maiuscole di max 3 lettere.
        finalVectorResult = searchUpperCaseWords(finalVectorResult);
        
        //Aggiorno il vettore con tutte le country
        for(int i = 0; i < finalVectorResult.size(); i++){
            countryCodeVector.add(finalVectorResult.elementAt(i).getCountryCode());
        }
        
        //Calcolo la FREQUENZA dei termini
        finalVectorResult = calculateFrequency(finalVectorResult, freqWordVector);
        
        //Calcolo l'importanza (solo se è il campo impValue è diverso da 0)
        if(impValue != 0)
            finalVectorResult = calculateImportanceIniz(finalVectorResult, impValue);
        
        return finalVectorResult;
    }
    
    
    /**
     * Metodo che ricerca nel testo parole tutte maiuscole lunghe al massimo 3 lettere.
     * Queste (sono spesso degli acronimi o delle sigle) e generano solo degli errori,
     * perciò le elimino dal vettore dei risultati
     * @param finalVectorResult : vettore contenente le word che hanno superato la fase di geo-valutazione
     * @return il vettore con i nuovi risultati
     */
    public Vector<GeographicWord> searchUpperCaseWords(Vector<GeographicWord> finalVectorResult){
        boolean upperCase = true;
        boolean isCountryCode = false;
        Vector<GeographicWord> newFinalVectorResult = new Vector<GeographicWord>();
        
        for(int i = 0; i < finalVectorResult.size(); i++){
            String gwZoneDocName = finalVectorResult.elementAt(i).getZoneDocName();
            upperCase = true;
            
            //Controllo solo le GeoWord lunghe meno di 3 lettere
            if(gwZoneDocName.length() < 4){
                
                //Controllo che NON sia uguale ad un "countryCode" dei campi trovati
                for (int k = 0; k < countryCodeVector.size(); k++) {
                    if(gwZoneDocName.equals(countryCodeVector.elementAt(k)))
                        isCountryCode = true;         
                }
                
                if(!isCountryCode){ //Se NON è uguale ad un CountryCode
                    for (int j = 0; j < gwZoneDocName.length(); j++) {
                        int ascii = (int) gwZoneDocName.charAt(j);
                        if(!(ascii > 64 &&  ascii < 91)) //Se il carattere NON è maiuscolo
                            upperCase = false;
                    }
                    if(!upperCase) //Se c'è una lettera minuscola aggiungo la GeoWord al nuovo vettore
                        newFinalVectorResult.add(finalVectorResult.elementAt(i));
                }
                else //Se è uguale ad un CountryCode aggiungo la GeoWord al nuovo vettore
                    newFinalVectorResult.add(finalVectorResult.elementAt(i));
            }
            else //Se la parola è + lunga di 3 lettere la aggiungo al nuovo vettore
                newFinalVectorResult.add(finalVectorResult.elementAt(i));
        }
       
        return newFinalVectorResult;
    }
    
    
    /**
     * Metodo che cerca le parole ad inizio frase e le elimina dal vettore dei risultati
     * @param finalVectorResult : vettore contenente le word che hanno superato la fase di geo-valutazione
     * @param allWordVector : vettore contenente tutte le word reperite dal documento in esame
     * @param documentContent : testo del documento
     * @return il vettore uguale a finalVectorResult senza le parole che nel documento sono ad inizio di una frase
     */
    public Vector<GeographicWord> searchFirstWord(Vector<GeographicWord> finalVectorResult, Vector<Word> allWordVector, String documentContent){
        Vector<GeographicWord> newFinalVectorResult = new Vector<GeographicWord>();
        String analyzingString = "";
        
        for(int i = 0; i < finalVectorResult.size(); i++){
            GeographicWord geoWord = finalVectorResult.elementAt(i);
            String n = geoWord.getName();
            int pos = geoWord.getPosition();
            
            //Seleziono la GeoWord precedente
            if((pos-2) >= 0){
                String preWordName = allWordVector.elementAt(pos -2).getName(); 
                analyzingString = preWordName + ". " + geoWord.getZoneDocName();
            }
            
            //Se il documento NON contiene la stringa "preWord. geoWord" copio la geoWord nel vettore
            if(!documentContent.contains(analyzingString)){
                newFinalVectorResult.add(geoWord);
            }                        
        }
        
        return newFinalVectorResult;
    }
    
    /**
     * Metodo che controlla se la GeoWord è preceduta o seguita da una lettera puntata
     * @param finalVectorResult : vettore contenente le word che hanno superato la fase di geo-valutazione
     * @param allWordVector : vettore contenente tutte le word reperite dal documento in esame
     * @param documentContent : testo del documento
     * @return il vettore senza le parole presecute o seguite da una lettera puntata
     */
    public Vector<GeographicWord> searchAuthors(Vector<GeographicWord> finalVectorResult, Vector<Word> allWordVector, String documentContent){
        Vector<GeographicWord> newFinalVectorResult = new Vector<GeographicWord>();
        String preWordName = "";
        String postWordName = "";
        int pos = 0;
        
        for(int i = 0; i < finalVectorResult.size(); i++){
            GeographicWord geoWord = finalVectorResult.elementAt(i);
            pos = geoWord.getPosition();
            
            //Seleziono la Word che precede e che segue la GeoWord in esame
            if( (pos-2)>= 0 && pos < allWordVector.size()){
                preWordName = allWordVector.elementAt(pos -2).getName();                
                postWordName = allWordVector.elementAt(pos).getName(); 
            }

            if(!pointName(preWordName, documentContent) && !pointName(postWordName, documentContent)){
                double score = geoWord.getGeoScore();
                newFinalVectorResult.add(geoWord);
            }
        }
  
        return newFinalVectorResult;
    }
    
    
    /**
     * Metodo che controlla se il nome rievuto come parametro è una lettera puntata,
     * ovvero se è lungo meno di 2 caratteri e inizia con la maiuscola
     * @param geoWordName : stringa di testo da controllare
     * @return TRUE se la stringa è una lettera puntata
     */
    public boolean pointName(String geoWordName, String documentContent){
        boolean result = false;
                         
        //Controllo se è formata da  2 caratteri
        if(geoWordName.length() < 2 && geoWordName.length() > 0){
            int ascii = (int) geoWordName.charAt(0);
            if(ascii > 64 &&  ascii < 91){ //Controllo se è maiuscola                   
                    if(documentContent.indexOf(geoWordName.charAt(0) + ".") > 0)
                        result = true;
            }
        }
             
        return result;
    }
    
    
    
    /**
     * Metodo che cerca nel file "street.txt" parole che hanno a che fare con vie, viali,
     * come ad esempio "st, street, avenue, via, viale, ecc". 
     * Le parole che precedono o seguono tali termini (questo dipende dal formato
     * dell'indirizzo, perciò dalla lingua in esame) vengono eliminati dalla ricerca,
     * in modo da non trovare il record ROMA se nel testo c'è scritto "Via Roma" o "Roma street".
     * Ovviamente questa soluzione sarebbe da ottimizzare per tutte le lingue, per 
     * ora il controllo viene eseguito solo per l'inglese e l'italiano
     * @param finalVectorResult : vettore contenente le word che hanno superato la fase di geo-valutazione
     * @param allWordVector : vettore contenente tutte le word reperite dal documento in esame
     * @return il vettore con i nuovi risultati
     */
    public Vector<GeographicWord> searchStreet(Vector<Word> allWordVector, Vector<GeographicWord> finalWordVector){
        String line = "";
        String[] terms = null;        
        String[] streetTerm = null; 
        Word preGewWord = new Word();
        Vector<Word> wordToErase = new Vector<Word>();
        Vector<GeographicWord> newFinalWordVector = new Vector<GeographicWord>();
        boolean equal = false;
        
        try {
            File fileName = new File("./src/geotag/street.txt");
            //Apertura file
            FileReader fr = new FileReader(fileName);
            BufferedReader streetFile = new BufferedReader(fr); 
     
                int k = 0;        
                while ((line = streetFile.readLine()) != null) {
                    terms = line.split("\t");
                    String language = terms[0]; //Lingua in esame                    
                    streetTerm = terms[1].split(","); //Elenco dei termini tipo "street", "road", ecc
                    
                    for (int i = 0; i < streetTerm.length; i++) {                        
                            String st = streetTerm[i];
                            for(int j = 1; j < allWordVector.size(); j++){ 
                                
                                if(st.equalsIgnoreCase(allWordVector.elementAt(j).getName())){
                                    String questa = allWordVector.elementAt(j).getName();
                                    Word w = new Word();
                                    if(language.equals("ENGLISH")){ //Seleziono la parola precedente
                                        String prec = allWordVector.elementAt(j-1).getName();
                                        int precPos = allWordVector.elementAt(j-1).getPosition();
                                        w.setName(prec);
                                        w.setPosition(precPos);
                                        wordToErase.add(w);
                                    }
                                    if(language.equals("ITALIAN")){ //Seleziono la parola successiva
                                        String post = allWordVector.elementAt(j+1).getName();
                                        int postPos = allWordVector.elementAt(j+1).getPosition();
                                        w.setName(post);
                                        w.setPosition(postPos);
                                        wordToErase.add(w);
                                    }
                                }                                
                            } //fine for delle Word                                             
                    } //fine for su streetTerm       
                }

            fr.close();            
        } catch (IOException ex) {
            System.err.println("Errore nell'apertura del file street.txt");
        } 
        

        /* 
         * Scorro il vettore dei record trovati nel DB
         * se trovo un termine uguale ad uno trovato nel vettore wordToErase
         * lo elimino dal vettore di base
        */
        for(int i = 0; i < finalWordVector.size(); i++){ 
            GeographicWord gw = finalWordVector.elementAt(i);
            String gwName = gw.getZoneDocName();
            int gwPos = gw.getPosition();
            equal = false;
            
            for(int j = 0; j < wordToErase.size(); j++){
                Word w = wordToErase.elementAt(j);
                String wName = w.getName();
                int wPos = w.getPosition();
                
                if(gw.getZoneDocName().equalsIgnoreCase(w.getName())
                        && gwPos == wPos)
                    equal = true;
            }
            
            if(!equal)
                newFinalWordVector.add(gw);      
        }

     
        return newFinalWordVector;
    }
    
    
    
    
    /**
     * Metodo principale della calsse: viene gestita l'intera fase di geo-valutazione delle Word.
     * Viene interrogato il DataBabe e creo un nuovo vettore con le GeoWord trovate.
     * La ricerca nel DB viene eseguita in maniera sequenziale secondo il seguente schema:
     *      1) eseguo la ricerca sulle tabelle "countryinfo" e "admin1codesascii": se trovo
     *         dei risultati contrassegno queste GeoWord come zone amministrative
     *      2) eseguo la ricerca sulla tabella principale del DB: "geoname"
     *      3) eseguo la ricerca sulla tabella principale "alternatename" 
     * Memorizzo TUTTI i risultati in un vettore di GeoWord
     * @param analyzingWord : parola che deve essere analizzata
     * @param allWordVector : elenco di tutti i token presenti nel documento
     * @param pos : posizione della parola in analisi
     * @param stmt : statement necessario per l'esecuzione delle query sul DataBase
     * @return un vettore di GeoWord 
     */
    public Vector<GeographicWord> geoAnalysis(Word analyzingWord, Vector<Word> allWordVector, int pos, Statement stmt, boolean upperDateLine) {
        Vector<GeographicWord> wordVectorResult = new Vector<GeographicWord>();
        Vector<GeographicWord> wordVectorResult3 = new Vector<GeographicWord>();
        Vector<String> geonameId = new Vector<String>();
        Vector<GeographicWord> wordVectorResult2 = new Vector<GeographicWord>();
        String searchingName = StringOperation.convertString(analyzingWord.getName());
        Word existingWord = analyzingWord;
        int wordPos = analyzingWord.getPosition()-1;
        int shift1 = 4;
        int shift2 = 4;
        double val = 0.0;
        
        
        try {
            /* 
             Per ogni parola maiuscola creo una parola composta da max 4 termini
             e reperisco quelli alla sua destra. 
             Cerco un match nel DataBase con 4 parole, poi con 3, e così via, fino alla
             ricerca di un singolo termine.
             Quando trovo un match nel DB mi fermo e restituisco il risultato.
            */
        	
        	Serial a=new Serial();
            RecordManager mydbinter;
    		BTree tinter=new BTree();
    		mydbinter = RecordManagerFactory.createRecordManager(path+"db"+File.separator+"albero_Btree_Intermedio", new Properties());
    		tinter = loadOrCreateBTree(mydbinter, "intermedio", a );
    		
    		BTree tgaz;
    		Serial a1=new Serial();
    		RecordManager mydbgaz;
    		tgaz = new BTree();
    		mydbgaz = RecordManagerFactory.createRecordManager(path+"db"+File.separator+"albero_Btree_Gazetteer", new Properties());
    		tgaz = loadOrCreateBTree(mydbgaz, "gazetteer", a1 );
    		
        	
        	//DO While finisce al primo gruppo di termini trovati !wordVectorResult.isEmpty()
            while(wordVectorResult.isEmpty() && shift1 > 0 && shift2 > 0){
                //Creo Word formata da + termini            
                analyzingWord = composeMultiRightWord(analyzingWord, allWordVector, wordPos, shift1);
                
                
                // 
                // searchingName = StringOperation.convertString(analyzingWord.getName());
                //-------MODIFICA GESTIONE BTREE----
                //cerco searchingName nel tabellone che mi restituisce tutto
                
                //COUNTRY INFO e ADMIN1CODESASCII
                if(analyzingWord.getName() != null){
                    searchingName = StringOperation.convertString(analyzingWord.getName());  
                    //INPUT : Name
                    //OUTPUT : ID
                    //Istanzio i 2 BTREE
                    
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
                    	        //newGeoWord = population(result);
                                if(!datiGaz[19].equalsIgnoreCase("") && !datiGaz[20].equalsIgnoreCase("")&& !datiGaz[21].equalsIgnoreCase("")&& !datiGaz[22].equalsIgnoreCase(""))
                    	        {
                                	newGeoWord.setmbr_x1(Double.parseDouble(datiGaz[19]));
                                	newGeoWord.setmbr_y1(Double.parseDouble(datiGaz[20]));
                                	newGeoWord.setmbr_x2(Double.parseDouble(datiGaz[21]));
                                	newGeoWord.setmbr_y2(Double.parseDouble(datiGaz[22]));
                    	        }
                                else
                                {
                                	newGeoWord.setmbr_x1(-200);
                                	newGeoWord.setmbr_y1(-200);
                                	newGeoWord.setmbr_x2(-200);
                                	newGeoWord.setmbr_y2(-200);
                                }
                                newGeoWord.setPosition(pos);
                    	        newGeoWord.setZoneDocName(searchingName);
                    	        //??????????????????????????E' una zona amministrativa se è uno stato o una regione
                    	        if(datiGaz[23].equalsIgnoreCase("1") || datiGaz[24].equalsIgnoreCase("1"))
                    	        	newGeoWord.setAdminZone(true); //TRUE se è una zona amministrativa
                    	        
                    	        wordVectorResult.add(newGeoWord);
                    	        //Controllo che c'era nella alternatesearch
                    	        if(shift2 == 1 && !wordVectorResult.isEmpty())
                                    wordVectorResult = multiWordControl(wordVectorResult);

                    		}
                    	}
                        
                   
                    }
                    
            		//-------CODICE VECCHIO--------------------------------
//                    geonameId = selectIDQuery(searchingName, "countryinfo", "name", stmt);
//                    if(geonameId.isEmpty())
//                        geonameId = selectIDQuery(searchingName, "admin1codesascii", "name", stmt);
//                    if(!geonameId.isEmpty()){            
//                        for(int j = 0; j < geonameId.size(); j++){
//                            try { 
//                                //TRUE indica che è una zona amministrativa
//                                wordVectorResult3 = selectGeonameQuery(searchingName, Integer.parseInt(geonameId.elementAt(j)), true, pos, stmt);
//                            } catch (SQLException ex) {
//                                Logger.getLogger(GeoCandidateIdentification.class.getName()).log(Level.SEVERE, null, ex);
//                            }
//                        }
//                    }
                }
            
                
                // GEONAME
//                if(wordVectorResult3.isEmpty() && analyzingWord.getName() != null){
//                    searchingName = StringOperation.convertString(analyzingWord.getName());                                      
//                    wordVectorResult = selectGeonameQuery(searchingName, "name", pos, stmt);
//                    if(wordVectorResult.isEmpty()) //Se in "name" NON trovo risultato cerco in "asciiname"
//                        wordVectorResult = selectGeonameQuery(searchingName, "asciiname", pos, stmt);
//                
//                    //ALTERNATE NAME
//                    wordVectorResult2 = alternateSearch(searchingName, shift2, pos, stmt);
//                    
//                    //Aggiungo i termini trovati nella tabella "alternatename"
//                    for(int i = 0; i < wordVectorResult2.size(); i++){
//                        wordVectorResult.add(wordVectorResult2.elementAt(i));
//                    }
//                    
//                }
                
                
                // fine tabellone
                
                
                shift2--;                
                shift1--;
            }//Fine while         
         

            //Se ho trovato Word multipla non devo fare ricerche per i termini coinvolti.
            //Perciò memorizzo la nuova posizione da cui partire
            if((shift1 > 0 && shift2 > 0) && !wordVectorResult.isEmpty()){
                isMultiWord = true;
                newPos = analyzingWord.getPosGap(); 
            }
            
            // Creo una nuova Word con il termine usato per la ricerca nel DataBase
            // e la aggiungo al vettore delel frequenza
            Word fw = new Word();
            fw.setName(searchingName);
            fw.setPosition(pos);
            freqWordVector.add(fw);

            //Elimino i termini trovati fino ad ora con geonameid uguale
            wordVectorResult = GeoWordsOperation.eraseEquals(wordVectorResult);        

//            //??????????????Aggiungo al vettore dei risultati i campi trovati nella ricerca nella tabella "countryinfo" e "admin1codes"
//            for(int i = 0; i < wordVectorResult3.size(); i++){
//                wordVectorResult.add(wordVectorResult3.elementAt(i));
//            }

            // Trovo località con pop maggiore
            int maxPop = 0;
            int position = 0;
            for(int i = 0; i < wordVectorResult.size(); i++){
                if(wordVectorResult.elementAt(i).getPopulation() > maxPop){
                    maxPop = wordVectorResult.elementAt(i).getPopulation();
                    position = i;
                }
            }
            if(position != 0){
                wordVectorResult.elementAt(position).setMaxPop(true);
            }
            
            //Trovo termini formati da più parole (multi)
            if(shift1 == 0 && shift2 == 0){
                for(int i = 0; i < wordVectorResult.size(); i++){
                    wordVectorResult.elementAt(i).setMulti(false);                      
                }
            }
            else {
                for(int i = 0; i < wordVectorResult.size(); i++){
                    wordVectorResult.elementAt(i).setMulti(true);
                }
            }
            
            //Setto 0.1 come peso alle parole multiple che sono state trovate 
            //nella tabella "alternatename" a partire da termini singoli
            //Solo però nel caso in cui quello trovato è l'UNICO record e NON è una zona amministrativa
            for(int i = 0; i < wordVectorResult.size(); i++){
                GeographicWord gw = wordVectorResult.elementAt(i);
                String n = gw.getName();
                if(gw.isMultiLow() && (gw.getFeatureClass().equals("A") || gw.getFeatureClass().equals("P")))
                    wordVectorResult.elementAt(i).setMultiLow(true);               
            }
            //wordVectorResult = setGeoScore(wordVectorResult, shift1, shift2, stmt);
            
//        } catch (SQLException ex) {
//            Logger.getLogger(GeoCandidateIdentification.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}                                   
                
        return wordVectorResult;                      
    }
    

   /**
     * Metodo che setta la frequenza delle GeoWord
     * @param geoWordVector : vettore contenente le word che hanno superato la fase di geo-valutazione
     * @param freqWordVector : vettore che contiene le Word per le quali è stato trovato un match nel DataBase
     * @return un nuovo vettore contenente tutte le GeoWord con la rispettiva frequenza
     */
    public Vector<GeographicWord> calculateFrequency(Vector<GeographicWord> geoWordVector, Vector<Word> freqWordVector){       
        //Conto le frequenze e creo un nuovo vettore con il numero di occorrenze
        for(int j = 0; j < geoWordVector.size(); j++){
            int freq = 0;
            
            for(int k = 0; k < freqWordVector.size(); k++){
                String gwDocName = geoWordVector.elementAt(j).getZoneDocName();
                String fwName = freqWordVector.elementAt(k).getName();
        
                //Controllo se GeoWord e termine nel doc sono uguali
                if(gwDocName.equalsIgnoreCase(fwName)){
                    freq++;
                }
            }            
            geoWordVector.elementAt(j).setFrequency(freq);          
        } 
    
        return geoWordVector;
    }
  
   
    /**
     * Metodo che ricerca la parola in esame nella tabella "alternatename"
     * @param searchingName : parola in esame
     * @param wordVectorResult : vettore dei risultati
     * @param shift2 : parametro che indica da quanti termini è formata la parola
     * @param pos : posizione nel testo
     * @return i risultati della ricerca nella tabella "alternatename"
     * @throws java.sql.SQLException
     */
    public Vector<GeographicWord> alternateSearch(String searchingName, int shift2, int pos, Statement stmt) throws SQLException{
        Vector<String> geonameId = new Vector<String>();
        Vector<GeographicWord> wordVectorResult = new Vector<GeographicWord>();
        Vector<GeographicWord> wordVectorResult2 = new Vector<GeographicWord>();
        
        //ALTERNATE
        geonameId = selectIDQuery(searchingName, "alternatename", "alternatename", stmt);
        //Se trovo risultato cerco nella tabella "geoname" i valori della Word, basandomi sul'GEONAMEID
        if(geonameId.isEmpty()) //Se in "name" NON trovo risultato cerco in "asciiname"
            geonameId = selectIDQuery(searchingName, "alternatename", "asciialternatename", stmt);

        if(!geonameId.isEmpty()){            
            for(int j = 0; j < geonameId.size(); j++){
                try {     
                    //FALSE indica che NON è una zona amministrativa
                    wordVectorResult2 = selectGeonameQuery(searchingName, Integer.parseInt(geonameId.elementAt(j)), false, pos, stmt);
                    //Controllo se da un termine singolo estraggo un termne multiplo
                    if(shift2 == 1 && !wordVectorResult2.isEmpty())
                        wordVectorResult2 = multiWordControl(wordVectorResult2);
                    //Aggiungo al vettore dei risultati i campi trovati dopo la ricerca nella tabella "alternatename"
                    for(int i = 0; i < wordVectorResult2.size(); i++){
                        wordVectorResult.add(wordVectorResult2.elementAt(i));
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(GeoCandidateIdentification.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        return wordVectorResult;
    }
    
    
    
    /**
     * Metodo che ritorna una Word formata dalla parola in esame e le "numTerm" successive
     * @param word : parola in esame
     * @param allWordVector : vettore contenente tutte le word estratte daldocumento
     * @param wordPos : posizione della parola nel testo
     * @param numTerm : numero di termini da considerare
     * @return la parola composta
     */
    public Word composeMultiRightWord(Word word, Vector<Word> allWordVector, int wordPos, int numTerm){
        Word newWord = new Word();
        int posGap = 0;       
        int posiz = wordPos+numTerm;
        int tot = allWordVector.elementAt(allWordVector.size()-1).getPosition();
        
        //CONTROLLO: wordPos+numTerm deve essere <= di allWordVector.elementAt(allWordVector.size-1).getPosition()
        if( (wordPos+numTerm) <= allWordVector.elementAt(allWordVector.size()-1).getPosition()){
            for(int i = wordPos; i < wordPos+numTerm; i++){
                if(i == wordPos)
                    newWord.setName(allWordVector.elementAt(i).getName());
                else{
                    newWord.setName(newWord.getName() + " " + allWordVector.elementAt(i).getName());
                    newWord.setPosGap(allWordVector.elementAt(i).getPosition());
                }
            }
        }
        
        newWord.setPosition(wordPos);
        newWord.setFrequency(1);                 
        
        return newWord;    
    }
     
   
    /**
     * Metodo che esegue una query di tipo SELECT sulla tabelle "geoname", 
     * basando la ricerca sul nome della Word. Restituisce l'elenco delle GeoWord trovate 
     * popolate con le caratteristiche geografiche corrispondenti.
     * @param wordName : nome della Word in esame
     * @param field : campo su cui eseguire la ricerca
     * @param pos : posizione della Word
     * @param stmt : Statement necessario per l'esecuzioni delel query sul DB
     * @return il vettore con le GeoWords popolate con i campi trovati nel DataBase
     * @throws java.sql.SQLException
     */
    public static Vector<GeographicWord> selectGeonameQuery(String wordName, String field, int pos, Statement stmt) throws SQLException {       
        GeographicWord newGeoWord = new GeographicWord();
        Vector<GeographicWord> geoWordVector = new Vector<GeographicWord>();
                     
        ResultSet result = stmt.executeQuery("SELECT * FROM geoname WHERE " +
                field + "='" + wordName + "'");
                //field + "='" + wordName + "' AND countrycode='IT'");

        ResultSetMetaData meta = result.getMetaData();
        String[] colNames = new String[meta.getColumnCount()];
        Vector[] cells = new Vector[colNames.length];           

        while (result.next()) {
            newGeoWord = population(result);
            newGeoWord.setPosition(pos);
            newGeoWord.setZoneDocName(wordName);
            geoWordVector.add(newGeoWord);
        }                         

        result.close();
                 
        return geoWordVector;
    }
    
    
    /**
     * Metodo che prende i risultati trovati nel DataBase e crea una nuova GeoWord 
     * popolando i vari campi
     * @param result : risultati della query
     * @return la GeoWord popolata con i valori trovati nel DataBase
     */
    public static GeographicWord population(ResultSet result){
        GeographicWord newGeoWord = new GeographicWord();
        try {
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
        } catch (ParseException ex) {
            Logger.getLogger(GeoCandidateIdentification.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(GeoCandidateIdentification.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return newGeoWord;
    }
    
    /**
     * Metodo che esegue una query di tipo SELECT sulla tabelle "geoname", 
     * basando la ricerca sul geonameId della Word. Restituisce l'elenco delle GeoWord trovate 
     * popolate con le caratteristiche geografiche corrispondenti.
     * @param wordName : nome della Word in esame
     * @param field : campo su cui eseguire la ricerca
     * @param pos : posizione della Word
     * @param stmt : Statement necessario per l'esecuzioni delel query sul DB
     * @return il vettore con le GeoWords popolate con i campi trovati nel DataBase
     * @throws java.sql.SQLException
     */
    public static Vector<GeographicWord> selectGeonameQuery(String wordName, int geonameid, boolean adminZone, int pos, Statement stmt) throws SQLException {       
        GeographicWord newGeoWord = new GeographicWord();
        Vector<GeographicWord> geoWordVector = new Vector<GeographicWord>();
        
        ResultSet result = stmt.executeQuery("SELECT * FROM geoname " + 
                "WHERE geonameid='" + geonameid + "'");
                //"WHERE geonameid='" + geonameid + "' AND countrycode='IT'");    

        while (result.next()) { // process results one row at a time  
            newGeoWord = population(result);
            newGeoWord.setPosition(pos);
            newGeoWord.setZoneDocName(wordName);
            newGeoWord.setAdminZone(adminZone); //TRUE se è una zona amministrativa
            geoWordVector.add(newGeoWord);

        }

        result.close();
        
        return geoWordVector;
    }
   
    /**
     * Questo metodo verifica se la zona scelta è una zona amministrativa
     * ed in tal caso restituisce l'id trovato nel DB.
     * Esegue una query di tipo SELECT sulla tabella "alternatename"
     * (o "countryinfo" o "continentcode") e restitiusce l'elenco dei geonameid.
     * @param wordName : nome della parola in esame
     * @param field : campo su cui eseguire la ricerca
     * @param stmt : Statement necessario per l'esecuzioni delel query sul DB
     * @return il vettore contenente i nomi delle zone amministrative trovate
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
        }else if(table.equals("continentcodes")){
            while (result.next()) {
                geonameid = result.getString(3);
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
     * Metodo che riceve in input il vettore con i vecchi valori e i nuovi risultati,
     * ai vecchi valori aggiunge i nuovi e restituisce un nuovo vettore con i risultati finali
     * @param oldVector : vettore con i vechci valori
     * @param wordVectorResult : vettore con i nuovi valori
     * @return l'insieme dei vecchi e nuovi valori
     */
    public Vector<GeographicWord> update(Vector<GeographicWord> oldVector, Vector<GeographicWord> wordVectorResult){        
        for(int i = 0; i < wordVectorResult.size(); i++){
            oldVector.add(wordVectorResult.elementAt(i));
        }
        
        if(oldVector.size() == 0)
            oldVector = wordVectorResult;
        
        return oldVector;
    }
    
    
    
       
        
    /**
     * Cerco nel vettore dei risultati se la Word è formata da più termini,
     * in questo caso setto il campo "multi" a true
     * @param wordVectorResult : vettore delle Word
     * @return vettore delel Word con il campo Multi aggiornato
     */
    public Vector<GeographicWord> multiWordControl(Vector<GeographicWord> wordVectorResult) {
        String geoWordName = "";
        boolean exit = false;
        
        for(int j = 0; j < wordVectorResult.size(); j++){
            StringBuffer sb = new StringBuffer();
            geoWordName = wordVectorResult.elementAt(j).getName();
            for (int i = 0; i < geoWordName.length(); i++) {
                if (geoWordName.charAt(i) == ' ' && !exit){ 
                    wordVectorResult.elementAt(j).setMultiLow(true);
                    exit = true;
                }
            exit = false;
            }
        }    
        return wordVectorResult;
    }

    
    /**
     * Metodo per il calcolo dell'importanza del termine
     * @param geoWordVector : vettore delel GeoWord
     * @param impValue : valore da settare al campo impValue
     * @return vettore delle GeoWord con il campo impValue aggiornato
     */
    public Vector<GeographicWord> calculateImportanceIniz(Vector<GeographicWord> geoWordVector, double impValue){
        
        for(int i = 0; i < geoWordVector.size(); i++){
            geoWordVector.elementAt(i).setImportance(impValue);
        }
        
        return geoWordVector;
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
