/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.indices;

import geotag.GeoApplication;

import geotag.words.GeoRefDoc;
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
    public File indexDir; // Directory di default
    public boolean create = false;  //"false" = indice deve essere aggiornato
    IndexWriter index = null;
    IndexSearcher searcher = null;
    
    /**
     * Costruttore della classe.
     * @throws IOException 
     */
    public ContentIndexer() {   
		indexDir = new File(GeoApplication.getPath() + File.separator
				+ "contentIndex");

		try {

			// Controllo se esiste già l'indice TODO togliere dopo aver
			// aggiornato lucene
			if (!indexDir.exists()) {
				indexDir.mkdirs();
			}
			
			File[] nameFiles = indexDir.listFiles();
			if (nameFiles.length > 0){
				create = false;
			} else {
				create = true;
			}

			String temp = indexDir.getAbsolutePath();
			index = new IndexWriter(indexDir, new StandardAnalyzer(), create);

			// Controllo se esiste già l'indice
			//if (indexDir.exists()) {
				nameFiles = indexDir.listFiles();
				if (nameFiles.length > 0) { // Indice esiste
					searcher = new IndexSearcher(indexDir.getAbsolutePath());
				}
			//}
			
		} catch (IOException e) {
			System.out.println("pare che il problema si verifichi durante un'apertura dell'indice");
			//closeIndex();
			e.printStackTrace();
		}
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
    public boolean control(String fileName) {

		try {
			KeywordAnalyzer kwAnalyzer = new KeywordAnalyzer();

			QueryParser qp = new QueryParser("fileName", kwAnalyzer);

			TermQuery termQuery = new TermQuery(new Term("fileName", "\""
					+ fileName + "\""));
			Query query = qp.parse(termQuery.toString());

			Hits hits = searcher.search(query);
			int trovati = hits.length();

			if (trovati != 0){
				return true;
			}
		} catch (ParseException e) {
			closeIndex();
			e.printStackTrace();
		} catch (IOException e) {
			closeIndex();
			e.printStackTrace();
		}
		return false;

    }
    
    
    /**
     * Metodo principale della classe. Ha il compito di aggiornare l'indice del contenuto.
     * L'indice viene memorizzato nella cartella "contentIndex".
     * @param docContent : testo del documento
     * @param geoDoc : documento contente altri valori da indicizzare (titolo, descrizione, keyword...)
     * @param fileName: nome del file in esame
     */
    public void indexing(String docContent, GeoRefDoc geoDoc, String fileName){
        
        try {
			index.setUseCompoundFile(true);


			Document doc = new Document(); 
			doc.add(new Field("content", docContent, Field.Store.YES, Field.Index.TOKENIZED));
			doc.add(new Field("fileName", fileName, Field.Store.YES, Field.Index.UN_TOKENIZED));
                        if (geoDoc.docTitle != null){
                            doc.add(new Field("title", geoDoc.docTitle, Field.Store.YES, Field.Index.UN_TOKENIZED));
                        }
                        if (geoDoc.docDescription!= null){
                            doc.add(new Field("description", geoDoc.docDescription, Field.Store.YES, Field.Index.UN_TOKENIZED));
                        }
                        if (geoDoc.docDateLine != null){
                            doc.add(new Field("dateline", geoDoc.docDateLine, Field.Store.YES, Field.Index.UN_TOKENIZED));
                        }
                        if (geoDoc.docKeyWords != null){
                            doc.add(new Field("keywords", geoDoc.docKeyWords, Field.Store.YES, Field.Index.UN_TOKENIZED));
                        }
			index.addDocument(doc);
		} catch (IOException e) {
			closeIndex();
			e.printStackTrace();
		}

    }
    
    public void closeIndex() {
		if (index != null) {
			try {
				index.optimize();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					index.close();
				} catch (IOException e) {
					System.out.println("OHSHI... non sono neanche riuscito a chiudere l'indice");
					e.printStackTrace();
				}
			}
		}
    }
    
}
