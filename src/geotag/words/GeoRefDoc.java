/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.words;

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
    private Vector<GeographicWord> geoWord = null;
    private boolean geoRef = false;
    private int id = 0;
    private double sortScore = 0.0;
    
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
     * @return il vettore delle GeoWord
     */
    public Vector<GeographicWord> getGeoWord() {
        return geoWord;
    }

    /**
     * Riceve il vettore delle GeoWord che corrispondono alle entità geografiche
     * associate al documento
     * @param geoWord : vettore delle GeoWord
     */
    public void setGeoWord(Vector<GeographicWord> geoWord) {
        this.geoWord = geoWord;
    }
    public void addGeoWord(GeographicWord geoWord) {
    	if(this.geoWord==null)
    		this.geoWord=new Vector<GeographicWord>();
   		this.geoWord.add(geoWord);
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getSortScore() {
        return sortScore;
    }

    public void setSortScore(double sortScore) {
        this.sortScore = sortScore;
    }

}
