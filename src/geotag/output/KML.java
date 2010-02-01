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

import geotag.GeoApplication;
import geotag.words.GeographicWord;
import java.util.HashMap;
import java.util.Iterator;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Classe responsibile della creazione e scrittura di un file in formato KML
 * @author Giorgio Ghisalberti
 */
public class KML {
    NumberFormat formatter = new DecimalFormat("#0.00");  //Formatto il numero a 2 cifre decimali
    //Gamma di colori, varie tonalità di rosso
    private String path,slash;
    private String[] colors = { "7d000078", "7d0000a0", "7d0000c8", "7d0000eb", "7d0000ff",
                                "7d3c3cff", "7d6464ff", "7d8c8cff", "7db4b4ff", "7ddcdcff", "7dffffff"};
    
    /**
     * Costruttore della classe
     */
    public KML(){
    	path = GeoApplication.getPath();
        slash = File.separator;
    }
    
    /**
     * Metodo che crea un file in formato kml, contenente tutte
     * le GeoWord ricevute come parametro e le rispettive proprietà.
     * @param geoWordVector : elenco delle GeoWord da inserire nel file
     * @param nomeFile : nome da assegnare al ifle
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public void create(HashMap<GeographicWord, Double> scores, String nomeFile) throws FileNotFoundException, IOException{
        Iterator scoreList = scores.keySet().iterator();
        FileOutputStream fileOutputStream = null;
        String completeFileName = "";
        String markerColor = "";
        
        //Creo documento
        Document documento = new Document();        
        
        //Elemento radice
        Element gpx = new Element("kml");

        while(scoreList.hasNext()){
            GeographicWord gw = (GeographicWord) scoreList.next();
            
            Element place = new Element("Placemark");
            gpx.addContent(place);

            Element name = new Element("name");
            name.setText("<p><font size=2>"+ gw.getName() +"</font></p>");
            place.addContent(name);
            
            Element descr = new Element("description");
            String description = "<p><font size=1>CountryCode: " + gw.getCountryCode() + 
                                 "<br/>Latitude: " + formatter.format(gw.getLatitude()) + 
                                 "<br/>Longitude: " + formatter.format(gw.getLongitude()) + 
                                 "<br/>Population: " + gw.getPopulation() + 
                                 "<br/>GeoScore: " + formatter.format(gw.getGeoScore()) + 
                                 "<br/><font color=\"#e10000\"><i>GeoReferencingValue: " + formatter.format(scores.get(gw)) + "</i></font></font></p>";
            descr.setText(description);
            place.addContent(descr);
            
            
            Element point = new Element("Point");
            place.addContent(point);

            Element coord = new Element("coordinates");
            coord.setText(Float.toString(gw.getLongitude()) + "," + Float.toString(gw.getLatitude()) + "," + Float.toString(gw.getElevation()));
            point.addContent(coord); 
            
            //Assegno colore più scuro se geoRefValue è alto, più chiaro in caso contrario
            markerColor = findColor(scores.get(gw));
            
            Element style = new Element("Style");
            place.addContent(style);
            Element iconstyle = new Element("IconStyle");
            style.addContent(iconstyle);
            Element color = new Element("color");
            color.setText(markerColor);
            iconstyle.addContent(color);
            Element colormode = new Element("colorMode");
            colormode.setText("normal");
            iconstyle.addContent(colormode);
        }
               
        //Stampa del file
        XMLOutputter xmlOutputter = new XMLOutputter();
        xmlOutputter.setFormat(Format.getPrettyFormat());
        
        documento.setRootElement(gpx);
               
         // Create file
        File indexDir = new File(path+"output"+slash+"kml");
        if(!indexDir.isDirectory()){
            // Create one directory
            if(!indexDir.mkdir()){
                System.err.println("Problema nella creazione della directory " + indexDir);
            }
        }
        completeFileName = indexDir + "/" + nomeFile + ".kml";
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
