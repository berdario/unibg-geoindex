/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.analysis;


import java.io.File;
import java.io.IOException;
import java.util.Vector;
import geotag.indices.AnalyzerUtils;
import geotag.words.Word;


/**
 * Classe che ha il compito di analizzare il documento e da questo estrarre
 * i token, basandosi sull'analizzatore da me creato: MyStandardAnalyzer.
 * @author giorgio
 */
public class WordAnalyzer {
    public String[] line = null; //insieme delle linee del PDF letto
    File indexDir = new File("/indexContent"); // ora è di defalut, poi la farò sceglere all'utente
                                               // Sarà passata come parametro del costruttore
    public WordAnalyzer(){        
    }
      
    
    /**
     * Metodo che restituisce l'elenco delle Word reperite dal testo ricevuto in input.
     * Le Word sono costituite dal nome e dalla loro posizione nel testo.
     * @param contenuto
     * @return
     */
    public Vector<Word> getWordVector(String contenuto, String swLanguage) throws IOException{
        line = contenuto.split(" ");
        MyStandardAnalyzer myStdAnalyzer = new MyStandardAnalyzer(swLanguage);
        Vector<Word> wordVector = new Vector<Word>();        
        Vector<String> tokensName = new Vector<String>();   //Vettore usato per memorizzare il nome dei token        
        int[] tokensPosition = new int[tokensName.size()];  //Vettore usato per memorizzare la posizione dei token              
        AnalyzerUtils au = new AnalyzerUtils();

        //Scorro tutte le linee del documento
        int position = 0; 
        for(int i = 0; i < line.length; i++){
            try {
                // Ottengo tutti i nomi e le posizioni dei token nella linea "line"
                tokensName = au.getNameTokens(myStdAnalyzer, line[i]);
                tokensPosition = au.getPositionTokens(myStdAnalyzer, line[i], position);
                tokensPosition = au.getPositionTokens(myStdAnalyzer, line[i], position);
                
                position = position + tokensPosition.length;
                
                for(int k = 0; k < tokensName.size(); k++){ 
                    Word newWord = new Word();
                    
                    //assegno a Word il nome
                    newWord.setName(tokensName.elementAt(k)); 
                    //assegno a Word la posizione       
                    newWord.setPosition(tokensPosition[k]); 
                    
                    // aggiungo la WORD al vettore
                    wordVector.add(newWord);
                }                
              
            } catch (IOException ex) {
                System.err.println("Errore nell'analisi del testo letto. Tokenizzazione fallita.");
            }
        }

        System.out.println("Num words: " + line.length);
        
        return wordVector;
    }

    
    
}
