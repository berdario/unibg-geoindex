/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.words;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

/**
 * Classe che rappresenta il documento reperito come risultato della query al seguito
 * dell'interrogazione all'indice del contenuto.
 * Ogni documento possiede un vettore di GeoWords, che corrispondono alle
 * entità geografiche ad esso associate, trovate nella fase di GeoTagging.
 * @author Giorgio Ghisalberti
 */
public class GeoRefDoc {
    private String nomeDoc = "";
    private double textScore = 0.0;
    private double distanceScore = 0.0;
    private HashMap<GeographicWord, Double> scores = null;//il secondo campo è il vecchio geoRefScore normalizzato
    private boolean geoRef = false;//TODO probabilmente inutile: basta controllare se geoWord è null
    private double sortScore = 0.0;
    public String docTitle, docDescription, docDateLine, docKeyWords;
    
    /**
     * Costruttore della classe
     */
    public GeoRefDoc(){
        
    }


    /**
     * Restituisce il peso geografico associato al documento
     * @return distanceScore
     */
    public double getDistanceScore() {
        return distanceScore;
    }

    /**
     * Setta il peso geografico associato al documento
     * @param distanceScore : valore da associare al distanceScore
     */
    public void setDistanceScore(double distanceScore) {
        this.distanceScore = distanceScore;
    }

    /**
     * Restituisce il vettore delle GeoWord associate al documento
     * @return HashMap delle GeoWord con i geoRefValue
     */
    public HashMap<GeographicWord, Double> getScores() {
        return scores;
    }

    /**
     * Riceve il vettore delle GeoWord che corrispondono alle entità geografiche
     * associate al documento
     * @param geoWord : HashMap delle GeoWord con i geoRefValue
     */
    public void setScores(HashMap<GeographicWord, Double> geoWord) {
        this.scores = geoWord;
    }
    
    public void addGeoWord(GeographicWord geoWord) {
    	if(this.scores == null){
    		this.scores=new HashMap<GeographicWord, Double>();
        }
   	this.scores.put(geoWord, 0.0);
    }

    public void addGeoWord(GeographicWord geoWord, double geoRefValue){
        if(this.scores == null){
            this.scores=new HashMap<GeographicWord, Double>();
        }
        this.scores.put(geoWord, geoRefValue);
    }

    /**
     * Restituisce il nome del documento
     * @return nome del documento
     */
    public String getNomeDoc() {
        return nomeDoc;
    }

    /**
     * Setta il nome del documento
     * @param nomeDoc : stringa che verrà associata al nome del documento
     */
    public void setNomeDoc(String nomeDoc) {
        this.nomeDoc = nomeDoc;
    }

    /**
     * Restituisce il peso testuale associato al documento
     * @return textScore 
     */
    public double getTextScore() {
        return textScore;
    }

    /**
     * Riceve il valore da assegnare al peso testuale del documento
     * @param textScore : valore da associare al textScore 
     */
    public void setTextScore(double textScore) {
        this.textScore = textScore;
    }

    public boolean isGeoRef() {
        return geoRef;
    }

    public void setHaveGeoRef(boolean haveGeoRef) {
        this.geoRef = haveGeoRef;
    }

    public double getSortScore() {
        return sortScore;
    }

    public void setSortScore(double sortScore) {
        this.sortScore = sortScore;
    }

}
