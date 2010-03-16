/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.words;

import java.util.ArrayList;

/**
 * Classe che contiene tutte le operazioni svolte sulle GeoWord, come l'eliminaizone 
 * delle GeoWord con ID uguale, ecc.
 * @author Giorgio Ghisalberti
 */
public class GeoWordsOperation {
    
    /**
     * Costruttore della classe
     */
    public GeoWordsOperation(){
        
    }
    
    /**
     * Metodo che da un elenco di WORD elimina quelle con geonameId uguale
     * @param finalWordVector : vettore con le GeoWord
     * @return vettore con le GeoWord tutte diverse
     */
    public static ArrayList<GeographicWord> eraseEquals(ArrayList<GeographicWord> finalWordVector){
        ArrayList<GeographicWord> newWordVector = new ArrayList<GeographicWord>();
        ArrayList<GeographicWord> appWordVector = new ArrayList<GeographicWord>();
        appWordVector = finalWordVector; //vettore di appoggio utile per l'analisi dei termini uguali
        ArrayList<GeographicWord> nomeGiaEsistente = new ArrayList<GeographicWord>(); //vettore con i nomi doppi
        
        for(int i = 0; i < finalWordVector.size(); i++){
            String wordName = finalWordVector.get(i).getName();
            int geonameId = finalWordVector.get(i).getGeonameid();
            
            if(uguale(geonameId, appWordVector)){ //Se nel vettore in analisi c'è un elemento con lo stesso geonameid               
                if(!uguale(geonameId, nomeGiaEsistente)){ //se il nome è già nel vettore con i nomi doppi
                                                          // lo inserisco nel vettore dei nomi doppi e in quello finale
                    nomeGiaEsistente.add(finalWordVector.get(i));
                    int currentFrequency = finalWordVector.get(i).getFrequency();
                    
                    newWordVector.add(finalWordVector.get(i));
                }
            }
            else //Se NON c'è un elemento con lo stesso nome
                newWordVector.add(finalWordVector.get(i));
        }
        
        return newWordVector;
    }

   /**
     * Restituisce TRUE se name è uguale ad un termine nel vettore
     * FALSE se non c'è nessun termine uguale
     * @param name
     * @param vettore
     * @return
     */
    public static boolean uguale(int geonameid, ArrayList<GeographicWord> vettore){
        boolean result = false;
        
        for(int i = 0; i < vettore.size(); i++){
            String wordName = vettore.get(i).getName();
            
            if(geonameid == vettore.get(i).getGeonameid())
                result = true;
        }
                
        return result;
    }

}
