/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.analysis;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WordlistLoader;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

/**
 * Classe che descrive le caratteristiche dell'Analizzatore utilizzato pre la ricerca dei token
 * secondo le regole di filtraggio stabilite.
 * @author Giorgio Ghisalberti
 */
public class MyStandardAnalyzer extends Analyzer{
    private Set stopWords;
    
 
    /**
     * Costruttore della classe
     * @param swLanguage : indica il nome del file dal quale reperire le stopWords selezionate
     *                     Di default vengono prese quelle inglesi
     * @throws java.io.IOException
     */
    public MyStandardAnalyzer(String swLanguage) throws IOException{
        File stopWordsFile = new File(swLanguage);
        stopWords = WordlistLoader.getWordSet(stopWordsFile);
    }
    
    /**
     * Metodo che permette di estrarre i token in maniera simile a quanto fa
     * lo StandardAnalyzer di Lucene. In questo caso però si tiene conto del
     * "Case" delle parole, ovvero non vengono trasformate tutte in minuscolo.
     * "StopFilter" è necessario in quanto permette di eliminare le stopWords, mentre
     * "StandardTokenizer" permette di considerare come un'unico token le parole
     * separate dall'apostrofo.
     * @param fieldName
     * @param reader
     * @return
     */
    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
        return new StandardFilter(new StopFilter((new StandardTokenizer(reader)), stopWords, true));
    }
    
    

}
