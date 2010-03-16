/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.search;

import geotag.Configuration;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import geotag.words.GeoRefDoc;
import geotag.words.GeographicWord;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.FilterIndexReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TextFragment;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * Classe che esegue la ricerca dei documenti che soddisfano la query testuale
 * accedendo all'indice del contenuto
 * @author Giorgio Ghisalberti
 */
public class ContentSearcher {

    //TODO: dopo aver rimosso metodi obsoleti, questa classe rimane per un'unica funzione, valutare un refactoring

    public String contentIndexPath;
    
    /**
     * Costruttore della classe
     */
    public ContentSearcher(){
    	this.contentIndexPath=Configuration.getLucenePath();
    }
    
    /**
     * Metodo responsabile della creazione del ranking testuale dei documenti reperiti
     * @param keyWords : termini appartenenti alla query testuale
     * @return un vettore di documenti georeferenziati reperiti dall'indice dei documenti
     * @throws java.io.IOException
     * @throws org.apache.lucene.queryParser.ParseException
     */
    public Vector<GeoRefDoc> createTextualRanking(String keyWords){
        String text;
        Vector<GeoRefDoc> docs = new Vector<GeoRefDoc>();

        try {
            IndexSearcher searcher = new IndexSearcher(contentIndexPath);
            StandardAnalyzer stdAnalyzer = new StandardAnalyzer();
            QueryParser qp = new QueryParser("content", stdAnalyzer);
            WildcardQuery wildcardQuery = new WildcardQuery(new Term("content", keyWords));
            Query query = qp.parse(wildcardQuery.toString());
            //Ordinati in base al peso, al contenuto e al nome del file
            Sort sort = new Sort(new SortField[]{SortField.FIELD_SCORE, new SortField("id")}); //Nome del documento
            //FilterIndexReader reader = new FilterIndexReader(IndexReader.open(contentIndexPath));
            //query = query.rewrite(IndexReader.open(contentIndexPath)); //necessario per l'highlighting dello snippet
            Hits hits = searcher.search(query, sort);
            Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter(), new QueryScorer(query));
            int numRisultati = hits.length();
            if (numRisultati == 0) {
                //System.out.println("Nessun risultato per \"" + keyWords + "\"");
            } else {
                for (int i = 0; i < numRisultati; i++) {
                    Document doc = hits.doc(i);
                    //Popolo documento
                    GeoRefDoc newDoc = new GeoRefDoc();
                    newDoc.id = doc.get("id");
                    newDoc.title = doc.get("title");
                    newDoc.description = doc.get("description");
                    newDoc.keywords = doc.get("keywords");
                    newDoc.dateline = doc.get("dateline");
                    newDoc.url = doc.get("url");
                    newDoc.extension = doc.get("extension");
                    
                    text = doc.get("content");
                    TokenStream tokenStream = TokenSources.getAnyTokenStream(searcher.getIndexReader(), hits.id(i), "content", new StandardAnalyzer());
                    TextFragment[] frag = highlighter.getBestTextFragments(tokenStream, text, false, 3);
                    text = "";
                    for (int j = 0; j < frag.length; j++) {
                        if ((frag[j] != null) && (frag[j].getScore() > 0)) {
                            text += frag[j].toString();
                            if (j != frag.length){
                                text+=" ... ";
                            }
                        }
                    }
                    newDoc.htmlSnippet=text;

                    newDoc.setTextScore(hits.score(i));
                    docs.add(newDoc);
                }
            }
            
        } catch (ParseException ex) {
            Logger.getLogger(ContentSearcher.class.getName()).log(Level.SEVERE, "errore durante il parsing della query", ex);
        } catch (IOException ex) {
            Logger.getLogger(ContentSearcher.class.getName()).log(Level.SEVERE, "errore di lettura I/O", ex);
        }

        return docs;
    }
            

}
