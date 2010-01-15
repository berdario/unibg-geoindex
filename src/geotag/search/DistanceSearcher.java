/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.search;

import java.util.Vector;
import geotag.analysis.GeoDistance;
import geotag.words.GeoRefDoc;
import geotag.words.GeographicWord;

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
    public void createDistanceRanking(Vector<GeoRefDoc> results, GeographicWord geoLocation) {
        
        GeoDistance deoDist = new GeoDistance();              
        double distMin = 0.1; //Non posso usare 0 km come distanza minima, perciò metto 0.1 km come minimo
        
        
        for(int k = 0; k < results.size(); k++){
            GeoRefDoc doc = results.elementAt(k);
            
            if(doc.isGeoRef()){
                double[] dist = new double[doc.getGeoWord().size()];
                double[] distNorm = new double[doc.getGeoWord().size()];

                
                    for(int i = 0; i < doc.getGeoWord().size(); i++){
                        GeographicWord gw = doc.getGeoWord().elementAt(i);

                        dist[i] = deoDist.calculateDistance(gw.getLatitude(), gw.getLongitude(), geoLocation.getLatitude(), geoLocation.getLongitude(), 'K');
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
            GeoRefDoc doc = results.elementAt(i);
            if(doc.isGeoRef()){
                double[] distNorm = new double[doc.getGeoWord().size()];
                double[] refAndDistScore = new double[doc.getGeoWord().size()];
                double maxValue = 0.0;

                //Per prima cosa devo calcolare le DISTANZE NORMALIZZATE tra le gw del doc e la geoLocation
                distNorm = calculateDistance(doc, geoLocation, distMin);

                //Calcolo il valore relativo al MERGE tra il geoRiferimento e la distNormalizzata
                for(int j = 0; j < doc.getGeoWord().size(); j++){
                    GeographicWord gw = doc.getGeoWord().elementAt(j);
                    String name = gw.getZoneDocName();
                    double georefvalue = gw.getGeoRefValue();
                    double distnorm = distNorm[j];
                    refAndDistScore[j] = gw.getGeoRefValue() * distNorm[j];
                    double r = refAndDistScore[j];
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
     * @param geoLocation : GeoWord corrispondente alla locazione
     * @return
     */
    public double[] calculateDistance(GeoRefDoc doc, GeographicWord geoLocation, double distMin) {
        double[] dist = new double[doc.getGeoWord().size()];
        double[] distNorm = new double[doc.getGeoWord().size()];
        GeoDistance deoDist = new GeoDistance();
        
        //Calcolo le distanze di ogni GeoWord dalla GeoLocation
        for(int i = 0; i < doc.getGeoWord().size(); i++){
            GeographicWord gw = doc.getGeoWord().elementAt(i);
            
            dist[i] = deoDist.calculateDistance(gw.getLatitude(), gw.getLongitude(), geoLocation.getLatitude(), geoLocation.getLongitude(), 'K');
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
