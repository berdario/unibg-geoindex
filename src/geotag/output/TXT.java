/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.output;

import geotag.Configuration;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Vector;

import geotag.words.GeographicWord;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Classe responsabile della creazione e scrittura di un file strutturato in formato 
 * TXT contenente le GeoWord con le rispettive proprietà.
 * @author Giorgio Ghisalberti
 */
public class TXT {
    NumberFormat formatter = new DecimalFormat("#0.00");  //Formatto il numero a 2 cifre decimali
    
    /**
     * Costruttore della classe
     */
    public TXT(){
    }
    
    /**
     * Metodo che crea e popola il file TXT
     * @param geoWordVector : elenco delle GeoWord da inserire nel file
     * @param nomeFile : nome da assegnare al nuovo file creato
     * @throws java.io.IOException
     */
    public void create(HashMap<GeographicWord, Double> scores, String nomeFile) throws IOException{
        Iterator scoreList = scores.keySet().iterator();
        File indexDir = new File(Configuration.getOutputPath() + "txt");
        if(!indexDir.isDirectory()){
            // Create one directory
            if(!indexDir.mkdir()){
                System.err.println("Problema nella creazione della directory " + indexDir);
            }
        }
        
        String completeFileName = indexDir + "/" + nomeFile + ".txt";
        File file = new File(completeFileName);
        Writer output = new BufferedWriter(new FileWriter(file));
        
            output.write("Name" + "\t");
            output.write("GeoRefValueNorm" + "\t");
            output.write("GeoRefValue" + "\t");
            output.write("GeoScore" + "\t");
            output.write("Frequency" + "\t");
            output.write("Name in Doc" + "\t");
            output.write("Ascii name" + "\t");
            output.write("Latitude" + "\t");
            output.write("Longitude" + "\t");
            output.write("Country Code" + "\t");
            output.write("Population" + "\t");
            output.write("Feature class" + "\t");
            output.write("feature code" + "\t");
            output.write("Admin 1 code" + "\t");
            output.write("Admin 2 code" + "\t");
            output.write("Admin 3 code" + "\t");
            output.write("Admin 4 code" + "\t");
            output.write("Geoname ID" + "\t");
            output.write("Alternate names" + "\t");
            output.write("Time Zone" + "\t");
            output.write("Elevation" + "\t");
            output.write("GTopo30" + "\n");
        
        
        while(scoreList.hasNext()){
            GeographicWord gw = (GeographicWord) scoreList.next();
            output.write(gw.getName() + "\t");
            output.write(scores.get(gw) + "\t");
            output.write(scores.get(gw) + "\t");
            output.write(gw.getGeoScore() + "\t");
            output.write(gw.getFrequency() + "\t");
            output.write(gw.getZoneDocName() + "\t");
            output.write(gw.getAsciiName() + "\t");
            output.write(gw.getLatitude() + "\t");
            output.write(gw.getLongitude() + "\t");
            output.write(gw.getCountryCode() + "\t");
            output.write(gw.getPopulation() + "\t");
            output.write(gw.getFeatureClass() + "\t");
            output.write(gw.getFeatureCode() + "\t");
            output.write(gw.getAdmin1Code() + "\t");
            output.write(gw.getAdmin2Code() + "\t");
            output.write(gw.getAdmin3Code() + "\t");
            output.write(gw.getAdmin4Code() + "\t");
            output.write(gw.getGeonameid() + "\t");
            output.write(gw.getAlternateNames() + "\t");
            output.write(gw.getTimeZone() + "\t");
            output.write(gw.getElevation() + "\t");
            output.write(gw.getGtopo30() + "\n");        
        }
                
        output.close();
    }
    


}
