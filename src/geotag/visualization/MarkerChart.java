/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.visualization;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;

import java.util.Vector;
import javax.swing.JPanel;
import geotag.words.GeoRefDoc;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYDrawableAnnotation;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;


/**
 * Classe che crea il diadramma contenente la visualizzazione dei risultati 
 * su doppi assi, uno relativo allo spatial score dei documenti e l'altro al text score.
 * @author Giorgio Ghisalberti
 */
public class MarkerChart extends ApplicationFrame {

    /**
     * Costruttore della classe.
     * @param title  the frame title.
     * @param results : elenco dei documenti reperiti
     * @param jPanel : pannello dell'interfaccia su cui viene realizzato il diagramma
     */
    public MarkerChart(final String title, Vector<GeoRefDoc> results, JPanel jPanel) {

        super("Geosearch results - '" + title + "'");
        final XYDataset data = createDataset(results);
        final JFreeChart chart = createChart(data, results, title);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(300, 300));
        setContentPane(chartPanel);
        jPanel.add(chartPanel);
        
        ChartMouseListener c = new ChartMouseListener() {

            public void chartMouseClicked(ChartMouseEvent arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void chartMouseMoved(ChartMouseEvent arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

    /**
     * Crea un diagramma su xon assi XY sul quale plotta i risultati della ricerca
     * @param data  the sample data.
     * @return A configured chart.
     */
    private JFreeChart createChart(final XYDataset data, Vector<GeoRefDoc> results, final String title) {

        final JFreeChart chart = ChartFactory.createScatterPlot(
            "",
            "Spatial Score",
            "Text Score", 
            data, 
            PlotOrientation.VERTICAL,
            false, //legend
            true, //tooltips
            false //url
        );

        final XYPlot plot = chart.getXYPlot();

        final NumberAxis domainAxis = new NumberAxis("Spatial Score");
        domainAxis.setRange(0.0, 1.0);
        domainAxis.setInverted(true);
        plot.setDomainAxis(domainAxis);
        
        final NumberAxis rangeAxis = new NumberAxis("Text Score");
        rangeAxis.setRange(0.0, 1.0);
        rangeAxis.setInverted(true);
        plot.setRangeAxis(rangeAxis);

        //Creo una Marker cerchiato per la location
        final CircleDrawer cd = new CircleDrawer(Color.blue, new BasicStroke(1.0f), null);
        final XYAnnotation bestBid = new XYDrawableAnnotation(1.0, 1.0, 11, 11, cd);
        plot.addAnnotation(bestBid);        
        final XYPointerAnnotation location = new XYPointerAnnotation(title, 1.0, 1.0, -Math.PI/6);
        location.setFont(new Font("SansSerif", Font.PLAIN, 9));
        location.setPaint(Color.BLUE);
        plot.addAnnotation(location);
        
        return chart;

    }

    /**
     * Returns a sample dataset.
     * @param results : vettore dei documenti reperiti
     * @return A sample dataset.
     */
    private XYSeriesCollection createDataset(Vector<GeoRefDoc> results) {        
        final XYSeriesCollection series = new XYSeriesCollection(createSupplierBids(results));        
        return series;

    }

    /**
     * Ritorna una serie di valori che verranno plottati sul diagramma.
     * Vengono creati a partire dallo spatial score e text score di ogn documento 
     * reperito
     * @param results : vettore dei documenti reperiti
     * @return una serie di dati con coordinate x e y
     */
    private XYSeries createSupplierBids(Vector<GeoRefDoc> results) {
        XYSeries values = new XYSeries("doc");
        
        values.add(1.0, 1.0);
        
        for(int i = 0; i < results.size(); i++){
            GeoRefDoc doc = results.elementAt(i);
            values.add(doc.getDistanceScore(), doc.getTextScore());
            values.setDescription(doc.id);
        }
        
        return values;
    }


}