/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.search;

import java.util.Vector;
import geotag.analysis.GeoDistance;
import geotag.words.GeoRefDoc;
import geotag.words.GeographicWord;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Classe responsiabile del calcolo del punteggio da assegnare ai documenti
 * relativo alla distanza spaziale dalla location. Servirà per il calcolo del
 * ranking spaziale dei documenti.
 * @author Giorgio Ghisalberti
 */
public class DistanceSearcher {

    /**
     * Costruttore della classe
     */
    public DistanceSearcher(){
        
    }

    /**
     * Metodo responsabile del calcolo del ranking spaziale. Gestisce l'intera fase
     * di calcolo e assegnazione al documento dello spatialScore
     * @param results : elenco di documenti
     * @param geoLocation : entità geografica da prendere come riferimento per il calcolo della distanza spaziale
     */
    public void createDistanceRanking(ArrayList<GeoRefDoc> results, GeographicWord geoLocation) {
        createDistanceRanking(results, geoLocation.getLatitude(), geoLocation.getLongitude());
    }

    /**
     * Metodo responsabile del calcolo del ranking spaziale. Gestisce l'intera fase
     * di calcolo e assegnazione al documento dello spatialScore
     * @param results : elenco di documenti 
     * @param latitude : latitudine dell' entità geografica da prendere come riferimento per il calcolo della distanza spaziale
     * @param longitude : longitudine dell' entità geografica da prendere come riferimento per il calcolo della distanza spaziale
     */
    public void createDistanceRanking(ArrayList<GeoRefDoc> results, double latitude, double longitude ) {
        
        GeoDistance deoDist = new GeoDistance();
        double distMin = 0.1; //Non posso usare 0 km come distanza minima, perciò metto 0.1 km come minimo
        
        
        for(int k = 0; k < results.size(); k++){
            GeoRefDoc doc = results.get(k);
            
            if(doc.isGeoRef()){
                double[] dist = new double[doc.getScores().size()];
                double[] distNorm = new double[doc.getScores().size()];
                HashMap<GeographicWord, Double> scores = doc.getScores();
                Iterator keyList = scores.keySet().iterator();

                int i=0;
                while (keyList.hasNext()){
                    GeographicWord gw = (GeographicWord) keyList.next();
                    dist[i] = deoDist.calculateDistance(gw.getLatitude(), gw.getLongitude(), latitude, longitude, 'K');
                    i++;
                }


                    //Trovo la distanza MININA (diversa da zero)
                    for(int j = 0; j < dist.length; j++){
                        if(dist[j] != 0){
                            if(j == 0)
                                distMin = dist[j];

                            if(dist[j] < distMin)
                                distMin = dist[j];
                        }               
                    }
                }            
        }
        
        
        for(int i = 0; i < results.size(); i++){
            GeoRefDoc doc = results.get(i);
            if(doc.isGeoRef()){
                double[] distNorm = new double[doc.getScores().size()];
                double[] refAndDistScore = new double[doc.getScores().size()];
                double maxValue = 0.0;
                HashMap<GeographicWord, Double> scores = doc.getScores();
                Iterator keyList = scores.keySet().iterator();

                //Per prima cosa devo calcolare le DISTANZE NORMALIZZATE tra le gw del doc e la geoLocation
                distNorm = calculateDistance(doc, latitude, longitude, distMin);

                //Calcolo il valore relativo al MERGE tra il geoRiferimento e la distNormalizzata
                int j=0;
                while (keyList.hasNext()){
                    GeographicWord gw = (GeographicWord) keyList.next();
                    String name = gw.getZoneDocName();
                    double distnorm = distNorm[j];
                    refAndDistScore[j] = scores.get(gw) * distNorm[j];
                    double r = refAndDistScore[j];
                    j++;
                }

                
                //Trovo valore massimo
                for(int k = 0; k < refAndDistScore.length; k++){
                    if(refAndDistScore[k] > maxValue)
                        maxValue = refAndDistScore[k];
                }

                if(maxValue > 1.00)
                    maxValue = 1.00;
                
                doc.setDistanceScore(maxValue);
            }
        }      
    }
    
    /**
     * Metodo che calcola la distanza tra le GeoWrods del documento e la locazione
     * @param doc : documento comprensivo delle GeoWord
     * @param latitude : latitudine della locazione
     * @param longitude : longitudine della locazione
     * @return
     */
    public double[] calculateDistance(GeoRefDoc doc, double latitude, double longitude, double distMin) {
        double[] dist = new double[doc.getScores().size()];
        double[] distNorm = new double[doc.getScores().size()];
        GeoDistance deoDist = new GeoDistance();
        HashMap<GeographicWord, Double> hash = doc.getScores();
        Iterator keyList = hash.keySet().iterator();

        //Calcolo le distanze di ogni GeoWord dalla GeoLocation
        int i=0;
        while (keyList.hasNext()){
            GeographicWord gw = (GeographicWord) keyList.next();
            
            dist[i] = deoDist.calculateDistance(gw.getLatitude(), gw.getLongitude(), latitude, longitude, 'K');
            i++;
        }

        
        //Normalizzo le distanze rispetto a quella minima
        for(int k = 0; k < dist.length; k++){
            if(dist[k] != 0)
                distNorm[k] = distMin / dist[k];
            else
                distNorm[k] = 1.0;
        }
        
        return distNorm;
    }
    
    
}
