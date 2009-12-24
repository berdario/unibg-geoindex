/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.indices;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Vector;
import geotag.analysis.MyStandardAnalyzer;
import geotag.words.GeographicWord;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;

/**
 * Classe responsabile dell'aggiornamento dell'indice geografico
 * @author Giorgio Ghisalberti
 */
public class GeographicIndexer {
    File indexDir = new File("./geographicIndex");
    public boolean create = false;
    
    /**
     * Costruttore della classe
     */
    public GeographicIndexer(){  
    }
    
    /**
     * Metodo responsabile dell'aggiornamento dell'indice geografico.
     * @param geoWordVector : elenco delle GeoWord
     * @param fileName : nome del file in esame
     * @param swLanguage : indica il file di riferimento contenente le stopWords di interesse
     */
    public void indexing(Vector<GeographicWord> geoWordVector, String fileName, String swLanguage){
        try {            
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
            IndexWriter index = null;
            
            //Controllo se esiste già l'indice
            if(indexDir.exists()){
                File[] nameFiles = indexDir.listFiles();  
                if(nameFiles.length > 0)
                    create = false;
                else
                    create = true;
            }
                     
            index = new IndexWriter(indexDir.getName(), new MyStandardAnalyzer(swLanguage), create);
            index.setUseCompoundFile(true);
            
            //Creo un documento per ogni GeoWord e gli assegno i rispettivi parametri
            for(int i = 0; i < geoWordVector.size(); i++){
                GeographicWord gw = geoWordVector.elementAt(i);
                Document doc = new Document(); 
                doc.add(new Field("geonameId", Integer.toString(gw.getGeonameid()), Field.Store.YES, Field.Index.UN_TOKENIZED));
                doc.add(new Field("name", gw.getName(), Field.Store.YES, Field.Index.UN_TOKENIZED));
                doc.add(new Field("docName", gw.getZoneDocName(), Field.Store.YES, Field.Index.UN_TOKENIZED));
                doc.add(new Field("asciiName", gw.getAsciiName(), Field.Store.YES, Field.Index.UN_TOKENIZED));
                doc.add(new Field("alternateNames", gw.getAlternateNames(), Field.Store.YES, Field.Index.UN_TOKENIZED));
                doc.add(new Field("latitude", Float.toString(gw.getLatitude()), Field.Store.YES, Field.Index.UN_TOKENIZED));
                doc.add(new Field("longitude", Float.toString(gw.getLongitude()), Field.Store.YES, Field.Index.UN_TOKENIZED));
                doc.add(new Field("featureClass", gw.getFeatureClass(), Field.Store.YES, Field.Index.UN_TOKENIZED));
                doc.add(new Field("featureCode", gw.getFeatureCode(), Field.Store.YES, Field.Index.UN_TOKENIZED));
                doc.add(new Field("countryCode", gw.getCountryCode(), Field.Store.YES, Field.Index.UN_TOKENIZED));
                doc.add(new Field("cc2", gw.getCc2(), Field.Store.YES, Field.Index.UN_TOKENIZED));
                doc.add(new Field("admin1code", gw.getAdmin1Code(), Field.Store.YES, Field.Index.UN_TOKENIZED));
                doc.add(new Field("admin2code", gw.getAdmin2Code(), Field.Store.YES, Field.Index.UN_TOKENIZED));
                doc.add(new Field("admin3code", gw.getAdmin3Code(), Field.Store.YES, Field.Index.UN_TOKENIZED));
                doc.add(new Field("admin4code", gw.getAdmin4Code(), Field.Store.YES, Field.Index.UN_TOKENIZED));
                doc.add(new Field("population", Integer.toString(gw.getPopulation()), Field.Store.YES, Field.Index.UN_TOKENIZED));
                doc.add(new Field("elevation", Integer.toString(gw.getElevation()), Field.Store.YES, Field.Index.UN_TOKENIZED));
                doc.add(new Field("gTopo30", Integer.toString(gw.getGtopo30()), Field.Store.YES, Field.Index.UN_TOKENIZED));
                doc.add(new Field("timeZone", gw.getTimeZone(), Field.Store.YES, Field.Index.UN_TOKENIZED));               
                doc.add(new Field("modificationDate", dateFormatter.format(gw.getModificationDate()), Field.Store.YES, Field.Index.UN_TOKENIZED));
                
                doc.add(new Field("textPosition", Integer.toString(gw.getPosition()), Field.Store.YES, Field.Index.UN_TOKENIZED));
                doc.add(new Field("adminZone", Boolean.toString(gw.isAdminZone()), Field.Store.YES, Field.Index.UN_TOKENIZED));
                doc.add(new Field("geoScore", Double.toString(gw.getGeoScore()), Field.Store.YES, Field.Index.UN_TOKENIZED));
                doc.add(new Field("frequency", Integer.toString(gw.getFrequency()), Field.Store.YES, Field.Index.UN_TOKENIZED));
                doc.add(new Field("importance", Double.toString(gw.getImportance()), Field.Store.YES, Field.Index.UN_TOKENIZED));               
                doc.add(new Field("geoRefValue", Double.toString(gw.getGeoRefValue()), Field.Store.YES, Field.Index.UN_TOKENIZED));
                doc.add(new Field("geoRefValueNorm", Double.toString(gw.getGeoRefValueNorm()), Field.Store.YES, Field.Index.UN_TOKENIZED));
                
                doc.add(new Field("fileName", fileName.toString(), Field.Store.YES, Field.Index.UN_TOKENIZED));
                index.addDocument(doc);
            }
            
            index.optimize();
            index.close();
        } catch (IOException ex) {
            System.err.println("Errore nella fase di indicizzazione delle GeoWords");
        }
        
    }

}
