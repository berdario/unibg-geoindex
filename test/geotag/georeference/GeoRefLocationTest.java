/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.georeference;

import geotag.words.GeoRefDoc;
import geotag.words.GeographicWord;
import java.sql.Statement;
import java.util.Comparator;
import java.util.Vector;
import jdbm.RecordManager;
import jdbm.btree.BTree;
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
public class GeoRefLocationTest {

    public GeoRefLocationTest() {
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

    
    @Test
    public void testMergeLocation() {
        System.out.println("mergeLocation");
        Vector<GeoRefDoc> results = null;
        String location = "";
        GeoRefLocation instance = new GeoRefLocation();
        Vector expResult = null;
        Vector result = instance.mergeLocation(results, location);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}