/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.words;

/**
 * Classe che rappresenta le parole estratte dal documento e sono caratterizzate 
 * da nome, frequenza, posizione, ecc.
 * @author Giorgio Ghisalberti
 */
public class Word {
    private int geonameid = 0;
    private String name = null;
    private int position = 0;
    private int frequency = 0;
    private String alternateNames = null;
    private int relevanceWeight = 0;
    private int posGap = 0;

    public void Word(){    
    }

    public int getGeonameid() {
        return geonameid;
    }

    public void setGeonameid(int geonameid) {
        this.geonameid = geonameid;
    }
       
    
    public String getAlternateNames() {
        return alternateNames;
    }

    public void setAlternateNames(String alternateNames) {
        this.alternateNames = alternateNames;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }


    public int getRelevanceWeight() {
        return relevanceWeight;
    }

    public void setRelevanceWeight(int relevanceWeight) {
        this.relevanceWeight = relevanceWeight;
    }

    public int getPosGap() {
        return posGap;
    }

    public void setPosGap(int posGap) {
        this.posGap = posGap;
    }


    
    

}
