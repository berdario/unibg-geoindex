/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.search;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import geotag.GeoApplication;
import geotag.words.GeoRefDoc;
import geotag.words.GeographicWord;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
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
    	String path=GeoApplication.getPath();
    	this.contentIndexPath=path+"contentIndex";
    }
    
    /**
     * Metodo responsabile della creazione del ranking testuale dei documenti reperiti
     * @param keyWords : termini appartenenti alla query testuale
     * @return un vettore di documenti georeferenziati reperiti dall'indice dei documenti
     * @throws java.io.IOException
     * @throws org.apache.lucene.queryParser.ParseException
     */
    public Vector<GeoRefDoc> createTextualRankig(String keyWords){

        Vector<GeoRefDoc> docs = new Vector<GeoRefDoc>();

        try {
            //IndexSearcher searcher = new IndexSearcher(contentIndexDir.getName()); //questa roba è fottutamente perversa!
            IndexSearcher searcher = new IndexSearcher(contentIndexPath);
            StandardAnalyzer stdAnalyzer = new StandardAnalyzer();
            QueryParser qp = new QueryParser("content", stdAnalyzer);
            WildcardQuery wildcardQuery = new WildcardQuery(new Term("content", keyWords));
            Query query = qp.parse(wildcardQuery.toString());
            //Ordinati in base al peso, al contenuto e al nome del file
            Sort sort = new Sort(new SortField[]{SortField.FIELD_SCORE, new SortField("fileName")}); //Nome del documento
            Hits hits = searcher.search(query, sort);
            int trovati = hits.length();
            if (trovati == 0) {
                //System.out.println("Nessun risultato per \"" + keyWords + "\"");
            } else {
                for (int i = 0; i < trovati; i++) {
                    Document doc = hits.doc(i);
                    //Popolo documento
                    GeoRefDoc newDoc = new GeoRefDoc();
                    newDoc.setNomeDoc(doc.get("fileName"));
                    newDoc.docTitle = doc.get("title");
                    newDoc.docDescription = doc.get("description");
                    newDoc.docKeyWords = doc.get("keywords");
                    newDoc.docDateLine = doc.get("dateline");
                    newDoc.setTextScore(hits.score(i));
                    docs.add(newDoc);
                }
            }
            
        } catch (ParseException ex) {
            Logger.getLogger(ContentSearcher.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ContentSearcher.class.getName()).log(Level.SEVERE, null, ex);
        }

        return docs;
    }
            

}
