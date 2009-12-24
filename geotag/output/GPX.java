/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.output;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Vector;
import geotag.words.GeographicWord;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Classe responsabile della creazione di un file in formato GPX
 * @author giorgio
 */
public class GPX {
    NumberFormat formatter = new DecimalFormat("#0.00");  //Formatto il numero a 2 cifre decimali
    //Gamma di colori, varie tonalità di rosso
    private String[] colors = { "780000", "A00000", "C80000", "EB0000", "FF0000",
                                "FF3C3C", "FF6464", "FF8C8C", "FFB4B4", "FFDCDC", "FFFFFF"};
    
    /**
     * Costruttore della classe
     */
    public GPX(){
        
    }
    
    /**
     * Metodo che crea un file in formato GPX, contenente tutte
     * le GeoWord ricevute come parametro e le rispettive proprietà.
     * @param geoWordVector : elenco delle GeoWord da inserire nel file
     * @param nomeFile : nome da assegnare al file
     * @param maxLat : la latitudine più grande
     * @param minLat : la latitudine più piccola
     * @param maxLong : la longitudine più grande
     * @param minLong : la longitudine più piccola
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public void create(Vector<GeographicWord> geoWordVector, String nomeFile, double maxLat, double minLat, double maxLong, double minLong) throws FileNotFoundException, IOException{
        FileOutputStream fileOutputStream = null;
        String completeFileName = "";
        String markerColor = "";
        
        //Creo documento
        Document documento = new Document();        
        
        //Elemento radice
        Element gpx = new Element("gpx");
        gpx.setAttribute("version", "1.0");
        gpx.setAttribute("creator", "GG");
                
        Element nameDoc = new Element("name");
        nameDoc.setText(nomeFile);
        gpx.addContent(nameDoc);
        
        Element bounds = new Element("bounds");
        bounds.setAttribute("minlat", formatter.format(minLat));
        bounds.setAttribute("minLon", formatter.format(minLong));
        bounds.setAttribute("maxlat", formatter.format(maxLat));
        bounds.setAttribute("maxlon", formatter.format(maxLong));
        gpx.addContent(bounds);
      
        
        //WPT: punti di interesse
        for(int i = 0; i < geoWordVector.size(); i++){
            GeographicWord gw = geoWordVector.elementAt(i);               
            
                Element wpt = new Element("wpt");
                wpt.setAttribute("lat", formatter.format(gw.getLatitude()));
                wpt.setAttribute("lon", formatter.format(gw.getLongitude()));                
                
                if(gw.getElevation() > 0){
                    Element elevation = new Element("ele");
                    elevation.setText(Integer.toString(gw.getElevation()));
                    wpt.addContent(elevation);
                }
                
                Element name = new Element("name");
                name.setText(gw.getName());
                wpt.addContent(name);
                
                Element comment = new Element("cmt");
                comment.setText("Country code: " + gw.getCountryCode() + 
                                " - Population: " + gw.getPopulation() +
                                " - GeoScore: " + formatter.format(gw.getGeoScore()));
                wpt.addContent(comment);
                
                Element description = new Element("desc");
                String text = "GeoRefValue: " + formatter.format(gw.getGeoRefValueNorm());
                description.setText(text);
                wpt.addContent(description);
                
                //Assegno colore più scuro se geoRefValue è alto, più chiaro in caso contrario
                markerColor = findColor(gw.getGeoRefValueNorm());
                
                Element color = new Element("color");
                color.setText(markerColor);
                wpt.addContent(color);
                
                gpx.addContent(wpt);
            
        }
       
        //Stampa del file
        XMLOutputter xmlOutputter = new XMLOutputter();
        xmlOutputter.setFormat(Format.getPrettyFormat());
        
        documento.setRootElement(gpx);
               
         // Create file
        File indexDir = new File("./output/gpx");
        if(!indexDir.isDirectory()){
            // Create one directory
            if(!indexDir.mkdir()){
                System.err.println("Problema nella creazione della directory " + indexDir);
            }
        }
        completeFileName = indexDir + "/" + nomeFile + ".gpx";
        fileOutputStream = new FileOutputStream(new File(completeFileName));
        xmlOutputter.output(documento, fileOutputStream);

        fileOutputStream.close();
    }
    
    
    
    /**
     * In base al valore assunto dal geoRefValue assegno un colore al marker
     * @param geoRefValue : valore del GeoReferenceValue
     * @return il colore corrispondente
     */
    private String findColor(double geoRefValue) {
        String color = "";
        
        if(geoRefValue <= 1.0 && geoRefValue > 0.9)
            color = colors[0];
        else if(geoRefValue <= 9.0 && geoRefValue > 0.8)
            color = colors[1];
        else if(geoRefValue <= 8.0 && geoRefValue > 0.7)
            color = colors[2];
        else if(geoRefValue <= 7.0 && geoRefValue > 0.6)
            color = colors[3];
        else if(geoRefValue <= 6.0 && geoRefValue > 0.5)
            color = colors[4];
        else if(geoRefValue <= 5.0 && geoRefValue > 0.4)
            color = colors[5];
        else if(geoRefValue <= 4.0 && geoRefValue > 0.3)
            color = colors[6];
        else if(geoRefValue <= 3.0 && geoRefValue > 0.2)
            color = colors[7];
        else if(geoRefValue <= 2.0 && geoRefValue > 0.1)
            color = colors[8];
        else if(geoRefValue <= 1.0 && geoRefValue > 0.0)
            color = colors[9];
        else
            color = colors[10];
                    
        return color;
    }
    
    

    
}
