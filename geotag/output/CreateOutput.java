/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.output;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;
import geotag.words.GeographicWord;

/**
 * Classe che gestisce la creazione dei file di output che permettono
 * la visualizzazione dei risultati in vari formati.
 * @author Giorgio Ghisalnerti
 */
public class CreateOutput {

    /**
     * Costruttore della classe
     * @param geoWordVector : elenco delle GeoWord da assegnare ai vari file
     * @param fileName : nome del documento in esame
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public CreateOutput(Vector<GeographicWord> geoWordVector, String fileName) throws FileNotFoundException, IOException {
        double maxLat = 0.0;
        double maxLong = 0.0;
        double minLat = 0.0;
        double minLong = 0.0;
                  
        //Calcolo le cooordinate minime e massime di ogni documento
        for(int j = 0; j < geoWordVector.size(); j++){
            GeographicWord gw = geoWordVector.elementAt(j);               
            if(gw.getGeoScore() > 0.5){
                if(j == 0){
                    minLat = gw.getLatitude();
                    minLong = gw.getLongitude();
                }
                if(gw.getLatitude() > maxLat)
                    maxLat = gw.getLatitude();
                if(gw.getLongitude() > maxLong)
                    maxLong = gw.getLongitude();
                if(gw.getLatitude() < minLat)
                    minLat = gw.getLatitude();
                if(gw.getLongitude() > minLong)
                    minLong = gw.getLongitude();
            }    

            
            GPX gpxFile = new GPX();
            gpxFile.create(geoWordVector, fileName, maxLat, minLat, maxLong, minLong);
            GMaps gMapsFile = new GMaps();
            gMapsFile.create(geoWordVector, fileName);
            KML kmlFile = new KML();
            kmlFile.create(geoWordVector, fileName);
            TXT txtFile = new TXT();
            txtFile.create(geoWordVector, fileName);
        }
     
    }
}
