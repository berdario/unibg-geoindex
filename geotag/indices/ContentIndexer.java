/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.indices;

import java.io.File;
import java.io.IOException;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

/**
 * Classe che ha il compito di creare l'indice del contenuto dei documenti selazionati
 * dall'utente.
 * Per la creazioen dell'indice mi affido alla libreria Lucene e per il parser
 * dei documenti alla libreria PDFBox.
 * @author Giorgio Ghisalberti
 */
public class ContentIndexer {
    public File indexDir = new File("./contentIndex"); // Directory di default
    public boolean create = false;  //"false" = indice deve essere aggiornato

    /**
     * Costruttore della classe.
     */
    public ContentIndexer(){     
    }

    /**
     * Metodo che controlla se il nome del file ricevuto come parametro
     * esiste già all'interno dell'indice del contenuto; in modo da
     * determinare se aggiornarlo o creare un nuovo elemento.
     * @param fileName : nome del file in esame
     * @return FALSE se il nome del file non esiste, TRUE se esiste
     * @throws java.io.IOException
     * @throws org.apache.lucene.queryParser.ParseException
    */ 
    public boolean control(String fileName) throws IOException, ParseException {
        //Controllo se esiste già l'indice
        if(indexDir.exists()){
            File[] nameFiles = indexDir.listFiles();  
            if(nameFiles.length > 0){   //Indice esiste
                IndexSearcher searcher = new IndexSearcher(indexDir.getName());
                KeywordAnalyzer kwAnalyzer = new KeywordAnalyzer();

                QueryParser qp = new QueryParser("fileName", kwAnalyzer);

                TermQuery termQuery = new TermQuery(new Term("fileName", "\""+ fileName +"\""));
                Query query = qp.parse(termQuery.toString());

                Hits hits = searcher.search(query);
                int trovati = hits.length();

                if (trovati == 0) 
                    return false;
                else 
                    return true;
            }
            else    //Indice non esiste
                return false;
        }
        else
            return false;
    }
    
    
    /**
     * Metodo principale della classe. Ha il compito di aggiornare l'indice del contenuto.
     * L'indice viene memorizzato nella cartella "contentIndex".
     * @param dir : path della directory contenente i file da indicizzare
     * @param fileName: nome del file in esame
     */
    public void indexing(String docContent, String fileName) throws IOException{ 
        IndexWriter index = null;
        
        //Controllo se esiste già l'indice
        if(indexDir.exists()){
            File[] nameFiles = indexDir.listFiles();  
            if(nameFiles.length > 0)
                create = false;
            else
                create = true;
        }
        
      
        index = new IndexWriter(indexDir.getName(), new StandardAnalyzer(), create);
        index.setUseCompoundFile(true);


        Document doc = new Document(); 
        doc.add(new Field("content", docContent, Field.Store.YES, Field.Index.TOKENIZED));
        doc.add(new Field("fileName", fileName, Field.Store.YES, Field.Index.UN_TOKENIZED));                
        index.addDocument(doc);

            
        index.optimize();
        index.close();
    }
    
    
}
