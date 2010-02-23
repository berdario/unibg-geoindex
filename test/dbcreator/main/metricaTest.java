/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dbcreator.main;

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
public class metricaTest {

    public metricaTest() {
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
     * Test of metric method, of class metrica.
     */
    @Test
    public void testMetric() {
        double result=metrica.metric("comune dalmine","dalmine");
        assertEquals(0.2857, result, 0.001);
    }

}