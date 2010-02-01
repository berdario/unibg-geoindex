/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.georeference;

import geotag.analysis.GeoDistance;
import java.util.Vector;
import geotag.words.GeographicWord;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Classe responsabile del calcolo del valore identificativo del georiferimento di
 * ogni singola GeoWord reperita.
 * @author Giorgio Ghisalberti
 */
public class GeoRef {
    private double constantDistance = 0.3;
    private double constantFrequency = 0.3;
    private double constantMajority = 0.2;
    private double constantImportance = 0.1;
    private double constantPopulation = 0.1;
    private double geoRefLim = 0.1;
    private double geoScoreLim = 0.7;
    private Vector<GeographicWord> geoWordVector;
    private HashMap<GeographicWord, Double> scores;
    private int numberOfWords;
    private Iterator scoreList;
    
    
    /**
     * Costruttore della classe
     */
    public GeoRef(Vector<GeographicWord> geoWordVector){
        this.geoWordVector = geoWordVector;
        numberOfWords = this.geoWordVector.size();
        scores = new HashMap<GeographicWord, Double>(numberOfWords);
    }
    /**
     * Metodo responsabile del calcolo del peso relativo alla georeferenziazione del documento. 
     * Viene assegnato un peso (geoRefValue) ad ogni GeoWord, compreso tra 0 ed 1, in base
     * alle regole basate su: 
     *      - Distanza
     *      - Maggioranza
     *      - Frequenza
     *      - Importanza
     *      - Popolazione
     * @param geoWordVector : elenco di tutte le GeoWord 
     * @return il vettore delle GeoWord con il campo geoRefValue aggiornato
     */
    public HashMap<GeographicWord, Double> calculateGeoRefValue(){
        int popMax = 0;
        double minDist = 0.0;
        GeoDistance dist = new GeoDistance();
        
        
        if(numberOfWords > 2500){
             return null;//ci si aspetta che si rompa? non restituiva i geoRefValue
        }
        
        //Creo una matrice (quadrata) di distanze tra le varie zone geografiche
        double[][] distMatr = new double[numberOfWords][numberOfWords];
        int countryMaxFreq = 0;
        Vector<String> countryCodeVector = new Vector<String>();    // Vettore con l'elenco dei countryCode
        int[] countryCodeFreq = new int[numberOfWords];  // Vettore con countryCodeFreq
        int maxFreq = 0;
        
       for(int i = 0; i < numberOfWords; i++){
            GeographicWord gw = geoWordVector.elementAt(i);
            scores.put(gw, (double) 0);
            
            //Calcolo la popolazione maggiore
            if(gw.getPopulation() > popMax && !gw.isAdminZone())
                popMax = gw.getPopulation();
            
            //Calcolo la frequenza massima 
            if(gw.getFrequency() > maxFreq)
                maxFreq = gw.getFrequency();
            
            //Popolo il vettore con i countryCode e calcolo la rispettiva frequenza
            boolean stop = false;
            int pos = 0;
            if(countryCodeVector.size() == 0)
                countryCodeVector.add(gw.getCountryCode());
            for(int j = 0; j < countryCodeVector.size(); j++){
                if(gw.getCountryCode().equals(countryCodeVector.elementAt(j))){
                    countryCodeFreq[j]++;
                    stop = true;
                }
                pos = j;
            }
            if(stop == false){
                countryCodeVector.add(gw.getCountryCode());
                countryCodeFreq[pos+1]++;
            }
         
            //Popolazione della matrice delle distanze
            for (int k=0; k < distMatr.length; k++){
                GeographicWord gw2 = geoWordVector.elementAt(k);
                distMatr[i][k] = dist.calculateDistance(gw.getLatitude(), gw.getLongitude(), gw2.getLatitude(), gw2.getLongitude(), 'K');
            }            
        }
       
        /* COUNTRY CODE */
        //Calcolo la freq max dei country code
        for(int j = 0; j < countryCodeFreq.length; j++){
            if(countryCodeFreq[j] > countryMaxFreq)
                countryMaxFreq = countryCodeFreq[j];
        }
        
        
        /* DISTANCE */
        //Calcolo la distanza minima (deve essere maggiore di 0.1, ovvero almeno di un metro)
        boolean first = true;
        double sumDist = 0.0;
        int distTot = 0;
        double avgDist = 0.0;
        double avgDistNorm = 0.0;
        for (int i=0; i < distMatr.length; i++) {        // scandisce righe
            GeographicWord gw1 = geoWordVector.elementAt(i);
            for (int j=0; j < distMatr.length; j++){     // scandisce colonne
                GeographicWord gw2 = geoWordVector.elementAt(j);
                
                //La prima volta
                if(distMatr[i][j] > 0.1 && first == true){
                    minDist = distMatr[i][j];
                    first = false;
                }
                
                //Calcolo distanza minima (che deve essere almeno di 1 metro :-)
                if(distMatr[i][j] > 0.1 && distMatr[i][j] < minDist)
                    minDist = distMatr[i][j];
                
                //Calcolo distanza totale (serve per la media)
                if(distMatr[i][j] > 0.1){
                    sumDist = sumDist + distMatr[i][j];
                    distTot++;
                }
            }
        }                 
         avgDist = sumDist / distTot;           //Distanza media
         avgDistNorm = (minDist / avgDist);     //Distanza media normalizzata
         

        scoreList = scores.keySet().iterator();

        //TODO molte delle seguenti funzioni all'interno continuano a ciclare... normalizzazione dei valori a parte, si dovrebbe poter riuscire a calcolare ogni geoRefValue in un solo passo
        populationInfluence(popMax);
        distanceInfluence(distMatr, minDist, avgDistNorm);
        majorityInfluence(countryCodeVector, countryCodeFreq, countryMaxFreq);
        frequencyInfluence(maxFreq);        
        importanceInfluence();
    
        //Controllo che geoRefValue NON sia maggiore di 1
        for(int i = 0; i < numberOfWords; i++){
            GeographicWord gw = geoWordVector.elementAt(i);
            if(scores.get(gw) > 1.0){
                scores.put(gw, 1.0);
            }
        }
        
        //Normalizzo i valori
        normalizeGeoRefValue();

        //ripulisco valori anomali
        clean();
        
        return scores;
    }
    
    /**
     * Metodo che calcola il valore da aggiungere al geoRefValue dovuto alla popolazione
     * Massimizza le popolazioni di tutte le GeoWord rispetto alla popMax.
     * Nel fare questa operazione però non considero le Nazioni, altrimenti il loro
     * valore della popolazione distorcerebbe gli altri risultati.
     * @param popMax : indice della popolazione più alta
     */
    public void populationInfluence(int popMax){
        double geoRefValue;
        double increase;
        while (scoreList.hasNext()){
            GeographicWord gw = (GeographicWord) scoreList.next();
            if(!gw.isAdminZone()){
                geoRefValue = scores.get(gw);

                if(popMax != 0){
                    increase = (double) gw.getPopulation() / popMax;
                }else{
                    increase = 1;
                }

                scores.put(gw,geoRefValue + increase * constantPopulation);
            }
        }
    }
    
    
    /**
     * Metodo che calcola l'incremento del geoRefValue in base alla distanza tra le GeoWord
     * @param distMatr : matrice delle distanze
     * @param minDist : distanza minima
     */
    public void distanceInfluence(double[][] distMatr, double minDist, double avgDistNorm){
         double[][] distMatrNorm = new double[geoWordVector.size()][geoWordVector.size()];
         double increase = 0.0;
         double[] sumDistNormRow = new double[distMatr.length];
         
         //Creo la matrice delle distanze Normalizzata rispetto alla distanza minima
         for (int i = 0; i < distMatr.length; i++) {        // scandisce righe
            GeographicWord gw1 = geoWordVector.elementAt(i);
            for (int j = 0; j < distMatr.length; j++){     // scandisce colonne
                GeographicWord gw2 = geoWordVector.elementAt(j);
                distMatrNorm[i][j] = minDist / distMatr[i][j];               
            }
         }
         
         //Aumento il geoRefValue delle GeoWord in base alla distanza
         int count = 0;
         for (int i = 0; i < distMatrNorm.length; i++) {        // scandisce righe
            GeographicWord gw1 = geoWordVector.elementAt(i);
            double sumDistNorm = 0.0;
            
            for (int  j= 0; j < distMatrNorm.length; j++){      // scandisce colonne
                GeographicWord gw2 = geoWordVector.elementAt(j);
                if(!gw1.getName().equals(gw2.getName()) //Se hanno nomi diversi
                        && distMatrNorm[i][j] > 0.001){ 
                    sumDistNorm = sumDistNorm + distMatrNorm[i][j];
                    count++;
                }
            }
              
            //Aggiungo al vettore la somma delle distanze normalizzate della riga
            sumDistNormRow[i] = sumDistNorm;
         }
         
         //Trovo somma massima
         double maxSumDistNormRow = 0.0;
         for (int  j= 0; j < sumDistNormRow.length; j++){
             if(sumDistNormRow[j] > maxSumDistNormRow)
                 maxSumDistNormRow = sumDistNormRow[j];
         }
         
         //Calcolo l'incremento da dare ad ogni GeoWord
         for (int i = 0; i < distMatrNorm.length; i++) {
             GeographicWord gw1 = geoWordVector.elementAt(i);
             double geoRefValue = scores.get(gw1);

             if(maxSumDistNormRow != 0){
                 increase = sumDistNormRow[i] / maxSumDistNormRow;                     
                 
             }else{
                increase = 1;
             }
             scores.put(gw1, geoRefValue + increase * constantDistance);

         }
    }
    
    
    /**
     * Calcolo dell'incremento del geoRefValue in base alla regione con più GeoWord
     * @param countryCodeVector : vettore delle nazioni
     * @param countryCodeFreq : frequenza delle nazioni
     * @param countryMaxFreq : frequenza massima
     */
    public void majorityInfluence(Vector<String> countryCodeVector, int[] countryCodeFreq, int countryMaxFreq){
        double increase;
        for (int i = 0; i < geoWordVector.size(); i++) {
            GeographicWord gw = geoWordVector.elementAt(i);
            double geoRefValue = scores.get(gw);
            
            for (int j = 0; j < countryCodeVector.size(); j++) {
                String cc = countryCodeVector.elementAt(j);
                
                if(gw.getCountryCode().equals(cc)){
                    if(countryMaxFreq != 0){
                        increase = (double) countryCodeFreq[j] / countryMaxFreq;                  
                        
                    }else{
                        increase = 1;
                    }
                    scores.put(gw, geoRefValue + increase* constantMajority);
                }                
            }            
        }
    }
    
    
    /**
     * Calcola l'aumento del geoRefVAlue in base alla frequenza della parola nel testo
     * @param freqMax : frequenza massima tra tutte le GeoWord
     */
    public void frequencyInfluence(int maxFreq){
        double increase;
        for (int i = 0; i < geoWordVector.size(); i++) {
            GeographicWord gw = geoWordVector.elementAt(i);
            double geoRefValue = scores.get(gw);
            
            String n = gw.getName();
            double f = gw.getFrequency();
            
            if(maxFreq != 0){
                increase = (double) gw.getFrequency() / maxFreq;
                
            }else{
                increase = 1;
            }
            scores.put(gw, geoRefValue + increase * constantFrequency);
        }    
    }
    
    
    
    /**
     * Incremento del geoRefValue in base all'importanza della GeoWord
     */
    public void importanceInfluence(){
        
        for (int i = 0; i < geoWordVector.size(); i++) {
            GeographicWord gw = geoWordVector.elementAt(i);
            double geoRefValue = scores.get(gw);
            
            double increase = gw.getImportance() * constantImportance;
            
            scores.put(gw, geoRefValue + increase);
        }
    }
    
    
    /**
     * Metodo che NORMALIZZA il peso relativo alla georeferenziaizone delle GeoWord
     * rispetto al GeoRef MAX.
     */
    public void normalizeGeoRefValue(){
        HashMap<GeographicWord, Double> normalizedScores = new HashMap<GeographicWord, Double>(numberOfWords);
        double maxGeoRefValue = 0.0;
        
        //Trovo il geoRefValue MAX
        for (int i = 0; i < geoWordVector.size(); i++) {
            GeographicWord gw = geoWordVector.elementAt(i);
            double geoRefValue = scores.get(gw);
            if(geoRefValue > maxGeoRefValue)
                maxGeoRefValue = geoRefValue;
        }
        
        for (int i = 0; i < geoWordVector.size(); i++) {
            GeographicWord gw = geoWordVector.elementAt(i);
            double geoRefValue = scores.get(gw);
            
            double newGeoRefValue = geoRefValue / maxGeoRefValue;
            normalizedScores.put(gw, newGeoRefValue);  //Normalizzato
        }
        
        scores = normalizedScores;
    }
    
    /**
     * Metodo che controlla il vettore delle GeoWords già georeferenziate.
     * Se le geoWord hanno geoRefValue < geoRefLim e geoScore < geoScoreLim vengono eliminate dal vettore
     * perché spesso rappresentano degli errori, mentre negli altri casi sono entità
     * geografiche corrette ma che non costituiscono un georiferimento.
     */
    public void clean() {
        
        for(int i = 0; i < numberOfWords; i++){
            GeographicWord gw = geoWordVector.elementAt(i);
            
            if(scores.get(gw) < geoRefLim && gw.getGeoScore() < geoScoreLim ){
                    //&& (gw.getFeatureClass().equals("P") || gw.getFeatureClass().equals("A"))))
                scores.remove(gw);
            }
        }
    }
    
    
}
