/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.search;

import java.io.File;
import java.io.IOException;
import java.util.Vector;
import geotag.words.GeoRefDoc;
import geotag.words.GeographicWord;
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
    public File contentIndexDir = new File("./contentIndex");
    public File geographicIndexDir = new File("./geographicIndex");
    
    /**
     * Costruttore della classe
     */
    public ContentSearcher(){
        
    }
    
    /**
     * Metodo responsabile della creazione del ranking testuale dei documenti reperiti
     * @param keyWords : termini appartenenti alla query testuale
     * @return un vettore di documenti georeferenziati reperiti dall'indice dei documenti
     * @throws java.io.IOException
     * @throws org.apache.lucene.queryParser.ParseException
     */
    public Vector<GeoRefDoc> createTextualRankig(String keyWords) throws IOException, ParseException{
        IndexSearcher searcher = new IndexSearcher(contentIndexDir.getName());
        StandardAnalyzer stdAnalyzer = new StandardAnalyzer();
        
        Vector<GeoRefDoc> docs = new Vector<GeoRefDoc>();
        
        QueryParser qp = new QueryParser("content", stdAnalyzer);
        WildcardQuery wildcardQuery = new WildcardQuery(new Term("content", keyWords));
        Query query = qp.parse(wildcardQuery.toString());
        
        //Ordinati in base al peso, al contenuto e al nome del file
        Sort sort = new Sort(new SortField[]{
           SortField.FIELD_SCORE,               //Punteggio
           new SortField("fileName")});         //Nome del documento

        Hits hits = searcher.search(query, sort);
        int trovati = hits.length();
        
        if (trovati == 0) {
            //System.out.println("Nessun risultato per \"" + keyWords + "\"");
        }
        else {
            for (int i = 0; i < trovati; i++) {
                Document doc = hits.doc(i);

                //Popolo documento
                GeoRefDoc newDoc = new GeoRefDoc();               
                newDoc.setNomeDoc(doc.get("fileName"));
                newDoc.setTextScore(hits.score(i)); 
                docs.add(newDoc);
            }
        }
    
        return docs;
    }

    /**
     * Metodo responsabile del reperimento delle GeoWords associate ad ogni documento.
     * Per far questo avviene l'accesso all'indice geografico.
     * @param results : vettore con i documenti
     * @return vettore con i documenti e le GeoWords associate
     */
    public Vector<GeoRefDoc> findGeoWords(Vector<GeoRefDoc> results) throws IOException, ParseException {
        IndexSearcher searcher = new IndexSearcher(geographicIndexDir.getName());
        KeywordAnalyzer kwAnalyzer = new KeywordAnalyzer();
                
        QueryParser qp = new QueryParser("fileName", kwAnalyzer);
        
        for(int i = 0; i < results.size(); i++){
            GeoRefDoc doc = results.elementAt(i);
            Vector<GeographicWord> geoWordVector = new Vector<GeographicWord>();
            
            TermQuery termQuery = new TermQuery(new Term("fileName", "\""+doc.getNomeDoc()+"\""));            
            Query query = qp.parse(termQuery.toString());
            
            Hits hits = searcher.search(query);
            int trovati = hits.length();
            
            if (trovati == 0) {
                //System.out.println("Nessun georeferimento per \"" + doc.getNomeDoc() + "\"");
            }
            else {
                doc.setHaveGeoRef(true);
                
                for (int j = 0; j < trovati; j++) {
                    Document indexDoc = hits.doc(j);
                    geoWordVector.add(populationGeoWord(indexDoc));
                }                
                doc.setGeoWord(geoWordVector);           
            }
        }
      
        return results;
    }
    
    
    /**
     * Metodo che si occupa di popolare i vari campi della GeoWord, reperendoli
     * dal documento ricevuto come parametro.
     * @param doc : documento reperito dall'indice geografico
     * @return una GeoWord con i nuovi valori
     */
    public GeographicWord populationGeoWord(Document doc){
        GeographicWord newGeoWord = new GeographicWord();
        newGeoWord.setGeonameid(Integer.valueOf(doc.get("geonameId")).intValue());
        newGeoWord.setName(doc.get("name"));
        newGeoWord.setZoneDocName(doc.get("docName"));
        newGeoWord.setAsciiName(doc.get("asciiName"));
        newGeoWord.setAlternateNames(doc.get("alternateNames"));
        newGeoWord.setLatitude(Float.valueOf(doc.get("latitude")).floatValue());
        newGeoWord.setLongitude(Float.valueOf(doc.get("longitude")).floatValue());
        newGeoWord.setFeatureClass(doc.get("featureClass"));
        newGeoWord.setFeatureCode(doc.get("featureCode"));
        newGeoWord.setCountryCode(doc.get("countryCode"));
        newGeoWord.setCc2(doc.get("cc2"));
        newGeoWord.setAdmin1Code(doc.get("admin1code"));
        newGeoWord.setAdmin2Code(doc.get("admin2code"));
        newGeoWord.setAdmin3Code(doc.get("admin3code"));
        newGeoWord.setAdmin4Code(doc.get("admin4code"));
        newGeoWord.setPopulation(Integer.valueOf(doc.get("population")).intValue());
        newGeoWord.setElevation(Integer.valueOf(doc.get("elevation")).intValue());
        newGeoWord.setGtopo30(Integer.valueOf(doc.get("gTopo30")).intValue());
        newGeoWord.setTimeZone(doc.get("timeZone"));
        //data...
        newGeoWord.setPosition(Integer.valueOf(doc.get("textPosition")).intValue());
        newGeoWord.setAdminZone(Boolean.valueOf(doc.get("adminZone")).booleanValue());
        newGeoWord.setGeoScore(Double.valueOf(doc.get("geoScore")).doubleValue());
        newGeoWord.setFrequency(Integer.valueOf(doc.get("frequency")).intValue());
        newGeoWord.setImportance(Double.valueOf(doc.get("importance")).doubleValue());
        newGeoWord.setGeoRefValue(Double.valueOf(doc.get("geoRefValue")).doubleValue());
        newGeoWord.setGeoRefValueNorm(Double.valueOf(doc.get("geoRefValueNorm")).doubleValue());
        
        return newGeoWord;
    }
            

}
