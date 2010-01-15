/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.parser;

import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Dario Bertini <berdario@gmail.com>
 */
public class DocumentWrapperTest {

    public DocumentWrapperTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of openPDFDocument method, of class DocumentWrapper.
     */
    @Test
    public void testOpenPDFDocument() {
        System.out.println("openPDFDocument");
        String documentName = "";
        DocumentWrapper instance = null;
        instance.openPDFDocument(documentName);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of openHTMLDocument method, of class DocumentWrapper.
     */
    @Test
    public void testOpenHTMLDocument() {
        System.out.println("openHTMLDocument");
        String docURL = "";
        DocumentWrapper instance = null;
        instance.openHTMLDocument(docURL);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of openXMLDocument method, of class DocumentWrapper.
     */
    @Test
    public void testOpenXMLDocument() {
        System.out.println("openXMLDocument");
        File fileName = null;
        DocumentWrapper instance = null;
        instance.openXMLDocument(fileName);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}