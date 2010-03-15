/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.output;

import geotag.Configuration;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

import geotag.words.GeoRefDoc;
import geotag.words.GeographicWord;
import java.util.HashMap;
import java.util.Iterator;

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
    public CreateOutput(GeoRefDoc geoDoc, String fileName) throws FileNotFoundException, IOException {
        HashMap<GeographicWord, Double> scores = geoDoc.getScores();
        Iterator scoreList = scores.keySet().iterator();
        double maxLat = 0.0;
        double maxLong = 0.0;
        double minLat = 0.0;
        double minLong = 0.0;
        
        String path=Configuration.getOutputPath();
        File outputdir=new File(path);
        
        if (!outputdir.exists()){
        	outputdir.mkdirs();
        }
                  
        //Calcolo le cooordinate minime e massime di ogni documento
        int j=0;
        while(scoreList.hasNext()){
            GeographicWord gw = (GeographicWord) scoreList.next();
            if(gw.getGeoScore() > 0.5){
                if(j == 0){
                    minLat = gw.getLatitude();
                    minLong = gw.getLongitude();
                }
                if(gw.getLatitude() > maxLat){
                    maxLat = gw.getLatitude();
                }
                if(gw.getLongitude() > maxLong){
                    maxLong = gw.getLongitude();
                }
                if(gw.getLatitude() < minLat){
                    minLat = gw.getLatitude();
                }
                if(gw.getLongitude() > minLong){
                    minLong = gw.getLongitude();
                }
                j++;
            }    

            
            
            GPX gpxFile = new GPX();
            gpxFile.create(scores, fileName, maxLat, minLat, maxLong, minLong);
            GMaps gMapsFile = new GMaps();
            gMapsFile.create(scores, fileName);
            KML kmlFile = new KML();
            kmlFile.create(scores, fileName);
            TXT txtFile = new TXT();
            txtFile.create(scores, fileName);
        }
     
    }
}
