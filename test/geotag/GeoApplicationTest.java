/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag;

import geotag.words.GeoRefDoc;
import geotag.words.GeographicWord;
import geotag.words.Word;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Vector;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author dario
 */
public class GeoApplicationTest {

    String basepath,dbpath,slash,cachepath,indexpath;
    final String configfile="/home/dario/.config/geosearch/config";
    File index;
    private final ByteArrayOutputStream outContent=new ByteArrayOutputStream();


    @Before
    public void setUp() throws Exception {
        System.setOut(new PrintStream(outContent));
        slash = File.separator;
        File cfgfile = new File(configfile);
        assertTrue("il file di configurazione non esiste!", cfgfile.exists());

        PropertiesConfiguration config;
        try {
            config = new PropertiesConfiguration(configfile);
            basepath = config.getString("basepath");
            dbpath = basepath + config.getString("dbdirectory") + slash;
            cachepath = config.getString("cachepath");
            String[] indexDirs = config.getStringArray("indexdirs");
            boolean testDirExists = false;
            for (String i : indexDirs) {
                if (i.equals("/home/dario/Scrivania/prova2")) {
                    testDirExists = true;
                    break;
                }
            }
            assertTrue("nel file di configurazione non è specificata la directory dei file di prova da indicizzare!", testDirExists);


        } catch (ConfigurationException e) {
            e.printStackTrace();
        }

        //TODO trovare un modo per selezionare il path giusto (e magari leggere il file di configurazione) senza replicare il codice
        indexpath = basepath + "contentIndex";
        assertTrue("non c'è la directory dei dati del programma!", (new File(basepath)).exists());

        index = new File(indexpath);
        if (index.exists()) {
            FileUtils.moveDirectory(index, new File(indexpath + "2"));
        }

        testIndexing();
    }

    @After
    public void tearDown() throws Exception {
        System.setOut(null);
        File oldindex = new File(indexpath + "2");
        if (oldindex.exists()) {
            FileUtils.deleteDirectory(index);
            FileUtils.moveDirectory(oldindex, index);
        }
    }

    @Test
    public void testIndexing() {
        String[] args = {configfile, "--index"};
        GeoApplication app = new GeoApplication(configfile);
        app.createIndex();
        assertTrue("L'indice non è stato creato!", index.exists());
    }

    @Test
    public void testQuery() {

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
        GeoApplication app=new GeoApplication(configfile);
        Vector<GeoRefDoc> result=app.search("kcnjsdakhvb","nkadjnzlbd");
        assertTrue("trovati risultati inaspettati!", result.size()==0);
        result=app.search("SQL","Milano");
        boolean flag=false;
        for (GeoRefDoc doc : result){
            if (doc.id.equals("7cb05c1a428df429a30e549c3a45d30d")){
                flag=true;
                break;
            }
        }
        assertTrue("risultato atteso non trovato!", flag);
        result=app.search("kcnjsdakhvb","nkadjnzlbd");
        assertTrue("trovati risultati inaspettati!", result.size()==0);
    }
}