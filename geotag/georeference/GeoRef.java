/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.georeference;

import geotag.analysis.GeoDistance;
import java.util.Vector;
import geotag.words.GeographicWord;

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
    
    
    /**
     * Costruttore della classe
     */
    public void GeoRef(){
        
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
    public Vector<GeographicWord> calculateGeoRefValue(Vector<GeographicWord> geoWordVector){
        int popMax = 0;
        double minDist = 0.0;
        GeoDistance dist = new GeoDistance();
        
        if(geoWordVector.size() > 2500)
             return geoWordVector;
        
        //Creo una matrice (quadrata) di distanze tra le varie zone geografiche
        double[][] distMatr = new double[geoWordVector.size()][geoWordVector.size()];
        int countryMaxFreq = 0;
        Vector<String> countryCodeVector = new Vector<String>();    // Vettore con l'elenco dei countryCode
        int[] countryCodeFreq = new int[geoWordVector.size()];  // Vettore con countryCodeFreq
        int maxFreq = 0;
        
       for(int i = 0; i < geoWordVector.size(); i++){
            GeographicWord gw = geoWordVector.elementAt(i);
            
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
         
                
        geoWordVector = populationInfluence(geoWordVector, popMax);
        geoWordVector = distanceInfluence(geoWordVector, distMatr, minDist, avgDistNorm);
        geoWordVector = majorityInfluence(geoWordVector, countryCodeVector, countryCodeFreq, countryMaxFreq);
        geoWordVector = frequencyInfluence(geoWordVector, maxFreq);        
        geoWordVector = importanceInfluence(geoWordVector);
    
        //Controllo che geoRefValue NON sia maggiore di 1
        for(int i = 0; i < geoWordVector.size(); i++){
            GeographicWord gw = geoWordVector.elementAt(i);
            if(gw.getGeoRefValue() > 1.0)
                gw.setGeoRefValue(1.0);
        }
        
        //Normalizzo i valori
        geoWordVector = normalizeGeoRefValue(geoWordVector);
        
        return geoWordVector;
    }
    
    /**
     * Metodo che calcola il valore da aggiungere al geoRefValue dovuto alla popolazione
     * Massimizza le popolazioni di tutte le GeoWord rispetto alla popMax.
     * Nel fare questa operazione però non considero le Nazioni, altrimenti il loro
     * valore della popolazione distorcerebbe gli altri risultati.
     * @param geoWordVector : elenco delle GeoWord
     * @param popMax : indice della popolazione più alta
     * @return il vettore delle GeoWord con il campo geoRefValue aggiornato
     */
    public Vector<GeographicWord> populationInfluence(Vector<GeographicWord> geoWordVector, int popMax){
        
        for(int i = 0; i < geoWordVector.size(); i++){
            GeographicWord gw = geoWordVector.elementAt(i);
            if(!gw.isAdminZone()){
                double geoRefValue = gw.getGeoRefValue();
                int p = gw.getPopulation();
                
                if(popMax != 0){
                    double increase = (double) gw.getPopulation() / popMax;
                    gw.setGeoRefValue(geoRefValue + increase * constantPopulation);  
                }else
                    gw.setGeoRefValue(geoRefValue + constantPopulation);
            }
        }
        
        return geoWordVector;
    }
    
    
    /**
     * Metodo che calcola l'incremento del geoRefValue in base alla distanza tra le GeoWord
     * @param geoWordVector : elenco delle GeoWord
     * @param distMatr : matrice delle distanze
     * @param minDist : distanza minima
     * @return il vettore delle GeoWord con il campo geoRefValue aggiornato
     */
    public Vector<GeographicWord> distanceInfluence(Vector<GeographicWord> geoWordVector, double[][] distMatr, double minDist, double avgDistNorm){
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
             double geoRefValue = gw1.getGeoRefValue();

             if(maxSumDistNormRow != 0){
                 increase = sumDistNormRow[i] / maxSumDistNormRow;                     
                 gw1.setGeoRefValue(geoRefValue + increase * constantDistance);
             }else
                 gw1.setGeoRefValue(geoRefValue + constantDistance);
         }
         
        
        return geoWordVector;
    }
    
    
    /**
     * Calcolo dell'incremento del geoRefValue in base alla regione con più GeoWord
     * @param geoWordVector : vettore delle GeoWord
     * @param countryCodeVector : vettore delle nazioni
     * @param countryCodeFreq : frequenza delle nazioni
     * @param countryMaxFreq : frequenza massima
     * @return il vettore delle GeoWord con il campo geoRefValue aggiornato
     */
    public Vector<GeographicWord> majorityInfluence(Vector<GeographicWord> geoWordVector, Vector<String> countryCodeVector, int[] countryCodeFreq, int countryMaxFreq){
        
        for (int i = 0; i < geoWordVector.size(); i++) {
            GeographicWord gw = geoWordVector.elementAt(i);
            double geoRefValue = gw.getGeoRefValue();
            
            for (int j = 0; j < countryCodeVector.size(); j++) {
                String cc = countryCodeVector.elementAt(j);
                
                if(gw.getCountryCode().equals(cc)){
                    if(countryMaxFreq != 0){
                        double increase = (double) countryCodeFreq[j] / countryMaxFreq;                  
                        gw.setGeoRefValue(geoRefValue + increase* constantMajority);
                    }else
                        gw.setGeoRefValue(geoRefValue + constantMajority);
                }                
            }            
        }
              
        return geoWordVector;
    }
    
    
    /**
     * Calcola l'aumento del geoRefVAlue in base alla frequenza della parola nel testo
     * @param geoWordVector : elenco delle GeoWord
     * @param freqMax : frequenza massima tra tutte le GeoWord
     * @return il vettore delle GeoWord con il campo geoRefValue aggiornato
     */
    public Vector<GeographicWord> frequencyInfluence(Vector<GeographicWord> geoWordVector, int maxFreq){
        
        for (int i = 0; i < geoWordVector.size(); i++) {
            GeographicWord gw = geoWordVector.elementAt(i);
            double geoRefValue = gw.getGeoRefValue();
            
            String n = gw.getName();
            double f = gw.getFrequency();
            
            if(maxFreq != 0){
                double increase = (double) gw.getFrequency() / maxFreq;
                gw.setGeoRefValue(geoRefValue + increase * constantFrequency);
            }else
                gw.setGeoRefValue(geoRefValue + constantFrequency);
        }    
        
        return geoWordVector;
    }
    
    
    
    /**
     * Incremento del geoRefValue in base all'importanza della GeoWord
     * @param geoWordVector : elenco delle GeoWord
     * @return il vettore delle GeoWord con il campo geoRefValue aggiornato
     */
    public Vector<GeographicWord> importanceInfluence(Vector<GeographicWord> geoWordVector){
        
        for (int i = 0; i < geoWordVector.size(); i++) {
            GeographicWord gw = geoWordVector.elementAt(i);
            double geoRefValue = gw.getGeoRefValue();
            
            double increase = gw.getImportance() * constantImportance;
            
            gw.setGeoRefValue(geoRefValue + increase);
        }
        
        return geoWordVector;
    }
    
    
    /**
     * Metodo che NORMALIZZA il peso relativo alla georeferenziaizone delle GeoWord
     * rispetto al GeoRef MAX.
     * @param geoWordVector : elenco delle GeoWord
     * @return il vettore delle GeoWord con il campo geoRefValue normalizzato
     */
    public Vector<GeographicWord> normalizeGeoRefValue(Vector<GeographicWord> geoWordVector){
        double maxGeoRefValue = 0.0;
        
        //Trovo il geoRefValue MAX
        for (int i = 0; i < geoWordVector.size(); i++) {
            GeographicWord gw = geoWordVector.elementAt(i);
            if(gw.getGeoRefValue() > maxGeoRefValue)
                maxGeoRefValue = gw.getGeoRefValue();
        }
        
        for (int i = 0; i < geoWordVector.size(); i++) {
            GeographicWord gw = geoWordVector.elementAt(i);
            double geoRefValue = gw.getGeoRefValue();
            
            double newGeoRefValue = geoRefValue / maxGeoRefValue;
            gw.setGeoRefValueNorm(newGeoRefValue);  //Normalizzato
        }
        
        return geoWordVector;
    }
    
    /**
     * Metodo che controlla il vettore delle GeoWords già georeferenziate.
     * Se le geoWord hanno geoRefValue < geoRefLim e geoScore < geoScoreLim vengono eliminate dal vettore
     * perché spesso rappresentano degli errori, mentre negli altri casi sono entità
     * geografiche corrette ma che non costituiscono un georiferimento.
     * @param finalGeoWordVector : elenco delle GeoWord
     * @return : elenco GeoWord ritenute valide
     */
    public Vector<GeographicWord> delete(Vector<GeographicWord> finalGeoWordVector) {
        Vector<GeographicWord> newGeoWordVector = new Vector<GeographicWord>();
        
        for(int i = 0; i < finalGeoWordVector.size(); i++){
            GeographicWord gw = finalGeoWordVector.elementAt(i);
            
            if(!(gw.getGeoRefValue() < geoRefLim && gw.getGeoScore() < geoScoreLim ))
                    //&& (gw.getFeatureClass().equals("P") || gw.getFeatureClass().equals("A"))))
                newGeoWordVector.add(gw);
        }
        
        return newGeoWordVector;
    }
    
    
}
