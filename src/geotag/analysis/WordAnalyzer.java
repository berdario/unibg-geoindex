/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.analysis;


import geotag.GeoApplication;
import java.io.IOException;
import java.util.Vector;
import geotag.indices.AnalyzerUtils;
import geotag.parser.DocumentWrapper;
import geotag.words.Word;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Classe che ha il compito di analizzare il documento e da questo estrarre
 * i token, basandosi sull'analizzatore da me creato: MyStandardAnalyzer.
 * @author giorgio
 */
public class WordAnalyzer {
    public String[] line = null; //insieme delle linee del PDF letto
    MyStandardAnalyzer myStdAnalyzer;
    ArrayList<String> tokensName = new ArrayList<String>();   //Vettore usato per memorizzare il nome dei token
    int[] tokensPosition;
    AnalyzerUtils au = new AnalyzerUtils();

    public WordAnalyzer(String swLanguage) {
        try {
            myStdAnalyzer = new MyStandardAnalyzer(swLanguage);
        } catch (IOException ex) {
            System.err.println("Errore nell'analisi del testo letto. Tokenizzazione fallita.");
        }
        tokensPosition = new int[tokensName.size()];  //Vettore usato per memorizzare la posizione dei token

    }

    /**
     * Metodo che restituisce l'elenco delle Word reperite dal testo ricevuto in input.
     * Le Word sono costituite dal nome e dalla loro posizione nel testo.
     * @param contenuto
     * @return
     */
    public ArrayList<Word> getWordVector(DocumentWrapper doc) throws IOException {
        ArrayList<Word> words = new ArrayList<Word>();

        words.addAll(skim(doc.title, 1, false));
        words.addAll(skim(doc.keywords, 1, false));
        words.addAll(skim(doc.description, 0.5, false));
        words.addAll(skim(doc.dateline, 0.5, true));

        return words;
    }

    ArrayList<Word> skim(String element, double importance, boolean dateline) {

        line = element.split(" ");

        if (importance == 0.0) {//è il contenuto del documento
            GeoApplication.logger.log(Level.INFO, "Num words: " + line.length);
        }

        //Scorro tutte le linee
        int position = 0;

        ArrayList<Word> words = new ArrayList<Word>();

        for (int i = 0; i < line.length; i++) {
            try {
                // Ottengo tutti i nomi e le posizioni dei token nella linea "line"
                tokensName = au.getNameTokens(myStdAnalyzer, line[i]);
                tokensPosition = au.getPositionTokens(myStdAnalyzer, line[i], position);
                tokensPosition = au.getPositionTokens(myStdAnalyzer, line[i], position);

                //TODO: attenzione: anche durante l'analisi di titolo e keyword viene calcolata la posizione...
                //questo potrebbe non essere il comportamento desiderato

                position = position + tokensPosition.length;

                for (int k = 0; k < tokensName.size(); k++) {
                    Word newWord = new Word();
                    //assegno a Word il nome
                    newWord.setName(tokensName.get(k));
                    //assegno a Word la posizione
                    newWord.setPosition(tokensPosition[k]);

                    newWord.dateline = dateline;
                    newWord.importance = importance;

                    // aggiungo la WORD al vettore
                    words.add(newWord);
                }
            } catch (IOException ex) {
                System.err.println("Errore nell'analisi del testo letto. Tokenizzazione fallita.");
            }
        }
        return words;
    }
}
