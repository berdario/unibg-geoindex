/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Vector;

import geotag.GeoApplication;
import geotag.words.GeographicWord;

/**
 * Classe responsabile della creazione di un file in formato HTML
 * che rappresenta una mappa di Google Maps di un particolare documento
 * @author Giorgio Ghisalberti
 */
public class GMaps {
    private String apiMapsKey = "ABQIAAAAEppQXP52PXhyP1JIW9Kn1RQj8CehLf8ZxfnoPL0ALVd_4dy80RQdWfJU7t-qHVGjwLY1qVpJQ_uIaQ";
    NumberFormat formatter = new DecimalFormat("#0.00");  //Formatto il numero a 2 cifre decimali
    private String path,slash;
    private String[] colors = { "red1", "red2", "red3", "red4", "red5",
                                "red6", "red7", "red8", "red9", "red10"};
    /**
     * Costruttore della classe
     */
    public GMaps(){
    	path = GeoApplication.getPath();
    	slash = File.separator;
    }
    
    /**
     * Metodo responsabile della creazione e scrittura del file in formato
     * HTML contenente la mappa delle GeoWord.
     * @param geoWordVector : elenco delle GeoWord da inserire nel file
     * @param nomeFile : nome da assegnare al file
     */
    public void create(Vector<GeographicWord> geoWordVector, String nomeFile){
        int gwTot = 0;
        double avgLat = 0.0;
        double avgLong = 0.0;
        double refLat = 0.0;
        double refLong = 0.0;
        double refGeoRefValue = 0.0;
        double maxDist = 0.0;
        int mapWidth = 750;
        int mapHeight = 450;
        int zoom = 0;
        GeographicWord gwMax = new GeographicWord();

        //Cerco GeoWord con geoRefValue maggiore
        for(int i = 0; i < geoWordVector.size(); i++){
            if(geoWordVector.elementAt(i).getGeoRefValue() > refGeoRefValue){
                refGeoRefValue = geoWordVector.elementAt(i).getGeoRefValue();
                gwMax = geoWordVector.elementAt(i);
            }
        }
        //Assegno il valore alle coordinate di riferimento
        for(int i = 0; i < geoWordVector.size(); i++){
            if(geoWordVector.elementAt(i).getGeoRefValue() == refGeoRefValue){
                refLat = geoWordVector.elementAt(i).getLatitude();
                refLong = geoWordVector.elementAt(i).getLongitude();
            }                
        }


        try{
            // Create file
            File indexDir = new File(GeoApplication.getPath()+"output"+slash+"GMaps");
            if(!indexDir.isDirectory()){
                // Create one directory
                if(!indexDir.mkdir()){
                    System.err.println("Problema nella creazione della directory " + indexDir);
                }
            }
             
            FileWriter fstream = new FileWriter(indexDir + "/" + nomeFile + "_map.html");
            BufferedWriter out = new BufferedWriter(fstream);
            
            out.write("<html>");
            out.write("<head>");
            out.write("<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"/>");
            out.write("<title>" + nomeFile + " - GMaps rappresentation </title>");
            out.write("<script src=\"http://maps.google.com/maps?file=api&amp;v=2&amp;key=" + apiMapsKey + "\"" + " type=\"text/javascript\"></script>");
            out.write(" <script type=\"text/javascript\">");
            out.write("function initialize() {");
            out.write("if (GBrowserIsCompatible()) {");
            out.write("var map = new GMap2(document.getElementById(\"map_canvas\"));");
            out.write("map.setCenter(new GLatLng(" + refLat + "," + refLong + "), 5);");            
            out.write("map.addControl(new GLargeMapControl());");
            out.write("map.addControl(new GMapTypeControl());");
            out.write("map.addControl(new GScaleControl());");
            out.write("map.enableScrollWheelZoom();");
                   
            
            //Funzione per creare Marker con scritta
            out.write("function createMarker(point, gName, lat, long, countryCode, population, geoScore, geoRefScore, color) {");
            out.write("var icon = new GIcon();");
            out.write("icon.image = \"icons/\" + color + \".png\";");
            out.write("icon.iconSize = new GSize(24, 24);");
            out.write("icon.iconAnchor = new GPoint(12, 24); ");
            out.write("var marker = new GMarker(point, icon); ");
            
            out.write("marker.value = gName;");
            out.write("GEvent.addListener(marker, \"click\", function() {");
            out.write("var myHtml = '<p><font face=\"Arial\"><font size=4><b>' + gName + '</b></font>,<font size=2> ' + countryCode + '<br/>Latitude: ' + lat + '<br/>Longitude: ' + long + '<br/>Population: ' + population + '<br/>GeoScore: ' + geoScore + '</font><br/><font size=2 color=\"#e10000\"><i>GeoReferenceValue: ' + geoRefScore + '</i></font></font></p>';");
            out.write("map.openInfoWindowHtml(point, myHtml );});");
            out.write("return marker;}");
            
            //Creazione Marker da GeoWords  
            for(int i = 0; i < geoWordVector.size(); i++){
                GeographicWord gw = geoWordVector.elementAt(i);
                    out.write("var point = new GLatLng(" + gw.getLatitude() + "," + gw.getLongitude() + ");");                    
                    out.write("map.addOverlay(createMarker(point, \"" + gw.getName() + "\", \"" + gw.getLatitude() + "\", \"" + gw.getLongitude() + "\", \"" + gw.getCountryCode() + "\", \"" + gw.getPopulation() + "\", \"" + formatter.format(gw.getGeoScore()) + "\", \"" + formatter.format(gw.getGeoRefValueNorm()) + "\", \"" + findColor(gw.getGeoRefValue()) + "\" ));");
            }

            
            out.write("}");
            out.write("}");
            out.write("</script>");
            out.write("</head>");
            out.write("<body onload=\"initialize()\" onunload=\"GUnload()\">");
            out.write("<h3><font face=\"Arial\">GMaps results for '" + nomeFile + "' file</font></h3>");
            out.write("<div id=\"map_canvas\" style=\"width: " + mapWidth + "px; height: " + mapHeight + "px\"></div>");
            out.write("</body>");
            out.write("</html>");
           
            //Close the output stream
            out.close();
        }catch (Exception e){
            System.err.println("Error: " + e.getMessage());
        }
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
                    
        return color;
    }
        
        
    
    
    
}
