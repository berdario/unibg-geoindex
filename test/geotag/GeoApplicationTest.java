/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag;

import geotag.words.GeoRefDoc;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Dario Bertini
 */
public class GeoApplicationTest {

    String basepath,dbpath,slash,cachepath;
    static String indexpath;
    final static String configfile = "res/config";
    static File index;
    private final static ByteArrayOutputStream outContent=new ByteArrayOutputStream();

    static GeoApplication app;


    @BeforeClass
    public static void setUp() throws Exception {
        System.setOut(new PrintStream(outContent));

        File cfgfile = new File(configfile);
        
        assertTrue("il file di configurazione " + cfgfile.getCanonicalPath() + " non esiste!", cfgfile.exists());
        //TODO inserire il path del file di configurazione solo in una test suite

        app = new GeoApplication(configfile);

        String indexDir = Configuration.getIndexedDirsNames()[0];
        assertTrue("nel file di configurazione non è specificata la directory dei file di prova da indicizzare!", (new File(indexDir)).exists());

        indexpath = Configuration.getLucenePath();

        //assertTrue("non c'è la directory dei dati del programma!", (new File(basepath)).exists());

        index = new File(indexpath);
        if (index.exists()) {
            if (!FileUtils.deleteQuietly(index)){
                fail("Impossibile eliminare l'indice di prova preesistente");
            }
        }

    }

    @AfterClass
    public static void tearDown() throws Exception {
        System.setOut(null);
    }

    @Test
    public void testCreateIndex() throws IOException {
        
        app.createIndex();
        assertTrue("L'indice " + index.getCanonicalPath() + " non è stato creato!", index.exists());
    }

    @Test
    public void testQuery() {

        fail("test obsoleto, considerare di eliminarlo");
        /*TODO: fixare, questi for/println non hanno molto senso
         *prima veniva fatto il test sul main, l'idea sarebbe quella di portare il test sul metodo search interno
         *(come testsearch), a meno che testare il main() non abbia effettivamente un'utilità
         */

        String expectedOutput;
        String[] args = {"Rules", "Milano"};
        GeoApplication app = new GeoApplication(configfile);
        for (GeoRefDoc doc: app.search(args[0],args[1])){
            System.out.println(doc);
        }
        expectedOutput = "679f7a6192c3612d90f5376d455340cf";
        assertTrue("risultato non trovato!", outContent.toString().contains(expectedOutput));
        expectedOutput = "7cb05c1a428df429a30e549c3a45d30d";
        assertTrue("risultato non trovato!", outContent.toString().contains(expectedOutput));
        outContent.reset();

        args[0] = "SQL";
        for (GeoRefDoc doc: app.search(args[0],args[1])){
            System.out.println(doc);
        }
        expectedOutput = "7cb05c1a428df429a30e549c3a45d30d";
        assertTrue("risultato non trovato!", outContent.toString().contains(expectedOutput));
        outContent.reset();

        args[1] = "Torino";
        for (GeoRefDoc doc: app.search(args[0],args[1])){
            System.out.println(doc);
        }
        expectedOutput = "7cb05c1a428df429a30e549c3a45d30d";
        assertTrue("risultato non trovato!", outContent.toString().contains(expectedOutput));
        outContent.reset();

    }

    /*this tests the inner search function, not the whole search command
     */
    @Test
    public void testSearch(){
        ArrayList<GeoRefDoc> result=app.search("kcnjsdakhvb","nkadjnzlbd");
        assertTrue("trovati risultati inaspettati!", result.size()==0);

        result=app.search("SQL","Torino");
        boolean flag=false;
        for (GeoRefDoc doc : result){
            if (doc.url.endsWith("10.1.1.95.5729-8.pdf")){
                flag=true;
                break;
            }
        }
        assertTrue("risultato atteso non trovato!", flag);

        result=app.search("kcnjsdakhvb","nkadjnzlbd");
        assertTrue("trovati risultati inaspettati!", result.size()==0);

        result=app.search("SQL","Milano");
        flag=false;
        for (GeoRefDoc doc : result){
            if (doc.url.endsWith("10.1.1.95.5729-8.pdf")){
                flag=true;
                break;
            }
        }
        assertTrue("risultato atteso non trovato!", flag);
    }
}