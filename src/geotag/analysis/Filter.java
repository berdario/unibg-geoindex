        /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.analysis;


import geotag.words.StringOperation;
import geotag.words.Word;
import java.util.ArrayList;

/**
 * Classe che ha il compito di gestire l'intera fase di filtro:
 *  - selezione dei token che iniziano con la maiuscola
 *  - creazione di nuovi token formati dalle parole che son separate da un apice
 *      e dopo l'apice hanno una lettera maiuscola
 *  - inserimento di tutte queste parole in un vettore di Word
 * @author Giorgio Ghisalberti
 */
public class Filter {
           
    /**
     * Costruttore della classe
     */
    public void WordFilter(){
        
    }
    
    /**
     * Metodo principale della classe che svolge tutte le regole principali di filtraggio.
     * @param wordVector : insieme di tutti i token reperiti dal testo del documento
     * @return l'insieme di tutte le nuove Word che superano la fase di filtro
     */
    public ArrayList<Word> filter(ArrayList<Word> wordVector ){
        ArrayList<Word> newWordVector = new ArrayList<Word>();
      
 
        for(int i = 0; i < wordVector.size(); i++){
            Word analyzingWord = wordVector.get(i);
            
            //Se TRUE correggo tutte le parole come se fossero parole da analizzare, ovvero iniziano con maiuscola
            if(analyzingWord.dateline){
                analyzingWord.setName(StringOperation.correctString(analyzingWord.getName()));
            }
            
            // Seleziono parole che iniziano con la lettera maiuscola
            if(upperCaseControl(analyzingWord)){
                newWordVector.add(analyzingWord);
            }// Seleziono le parole che contengono un apice e dopo di esso hanno una maiuscola
            else if(apostropheControl(analyzingWord)){   
                newWordVector.add(analyzingWord);
            }             
        }
        
        return newWordVector;
    }

    
    /**
     * Questo metodo riceve un oggetto Word ed analizza il nome per vedere se
     * contiene un apostrofo.
     * @param word : parola su cui eseguire il controllo
     * @return TRUE in 2 casi: se la parola NON ha l'apostrofo o se ha l'apostrofo
     *                         e la parola dopo l'apostrofo inizia con una lettera maiuscola 
     */
    public boolean apostropheControl(Word word){
        String wordName = word.getName();
        boolean result = false; // Setto la TRUE perché se la parola non contiene l'apostrofo deve 
                                // essere sottoposta alle altre fasi di analisi 
                
        for (int i = 0; i < wordName.length(); i++) {
            if (wordName.charAt(i) == '\''){ //<-- PROBLEMA con apostrofo diverso
                
                int ascii = (int) wordName.charAt(i+1);
                if(ascii > 64 &&  ascii < 91){
                    result = true;  // la Word ha un apostrofo e una lettera maiuscola dopo di esso
                }
                else
                    result = false; 
            }
        }           
        return result;
    }

    
    /**
     * Metodo che riceve un oggetto Word e restituisce TRUE nel caso in cui
     * il nome della Word inizia con una lettera maiuscola
     * @param word : parola da analizzare
     * @return TRUE se la word ricevuta come parametro inizia con una lettera maiuscola
     */
    public boolean upperCaseControl(Word word){
        String wordName = word.getName();
        boolean result = true;
        
        int ascii = (int) wordName.charAt(0);
        if(ascii > 64 &&  ascii < 91)
            result = true; // la parola inizia con una lettera maiuscola
        else
            result = false; 
        
        return result;
    }


}
