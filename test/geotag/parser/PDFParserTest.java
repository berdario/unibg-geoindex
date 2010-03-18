/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.parser;

import geotag.Configuration;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 *
 * @author Dario Bertini
 */
public class PDFParserTest {

    public PDFParserTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        //TODO inserire il path del file di configurazione solo in una test suite
        new Configuration("res/config");
    }

    /*
    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }*/

    @Test
    public void testGetTitle() {
        try {
            PDFParser instance = new PDFParser(Configuration.getIndexedDirsNames()[0]+"Professional Oracle Programming (2005).pdf");
            String expResult = "Professional Oracle Programming";
            String result = instance.getTitle();
            assertEquals(expResult, result);
        } catch (IOException ex) {
            Logger.getLogger(PDFParserTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CryptographyException ex) {
            Logger.getLogger(PDFParserTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidPasswordException ex) {
            Logger.getLogger(PDFParserTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}