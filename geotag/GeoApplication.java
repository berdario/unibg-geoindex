/*
 * GeoApplication.java
 *
 * Created on August 27, 2008, 9:49 AM
 */

package geotag;

import geotag.analysis.Filter;
import geotag.analysis.GeoCandidateIdentification;
import geotag.analysis.Score;
import geotag.analysis.WordAnalyzer;
import geotag.georeference.GeoRef;
import geotag.georeference.GeoRefLocation;
import geotag.indices.ContentIndexer;
import geotag.indices.GeographicIndexer;
import geotag.output.CreateOutput;
import geotag.parser.HTMLParser;
import geotag.parser.PDFParser;
import geotag.parser.XMLParser;
import geotag.search.ContentSearcher;
import geotag.search.DistanceSearcher;
import geotag.visualization.CountryItemListener;
import geotag.visualization.GeoWordsTable;
import geotag.visualization.MarkerChart;
import geotag.visualization.ResultsTable;
import geotag.words.GeoRefDoc;
import geotag.words.GeographicWord;
import geotag.words.StringOperation;
import geotag.words.Word;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.CharBuffer;
import java.nio.charset.CodingErrorAction;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.queryParser.ParseException;
import org.jdom.JDOMException;
import org.pdfbox.exceptions.CryptographyException;
import org.pdfbox.exceptions.InvalidPasswordException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
//import org.apache.commons.configuration.Configuration;
//import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author  giorgio
 */
public final class GeoApplication implements Runnable{
	
	static Vector<GeoRefDoc> results = new Vector<GeoRefDoc>();
    static Vector<GeoRefDoc> resultsmerge = new Vector<GeoRefDoc>();
    
    private String swLanguage;
    
    NumberFormat formatter = new DecimalFormat("#0.00");  //Formatto il numero a 2 cifre decimali
    
    //Campi dei documenti in esame (HTML e XML)
    private String title = "";
    private String description = "";
    private String dateline = "";
    private String keywords = "";
    
    double goodGeoScore = 0.65; //0.65   
    double uniqueGWScore = 0.2;
    
    private static String path,dbpath,cachepath,configfile,slash;
    private ArrayList<File> indexDirs;
    
	public class GeoApplicationGui extends javax.swing.JFrame{
	
	    //COSTANTI
	    String DRIVER_CLASS_NAME = "org.postgresql.Driver";         
	    String DB_NAME = "geonamesDB";
	    String USER_NAME = "postgres";
	    String PASSWORD = "10choch"; 
	    String DB_CONN_STRING = "jdbc:postgresql://localhost:5432/" + DB_NAME;
	    
	    //Variabili
	    private String fileName = File.separator + "Desktop";
	    File curDir = null; //Directory selezionata
	    
	    
	    
	    String selectedDocName = "";
	    
	    
	    
	    /** Creates new form GeoApplication */
	    public GeoApplicationGui() {
	         //definizione look and feel
	        try {
	            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	        } catch (IllegalAccessException ex) {
	            ex.printStackTrace();
	        } catch (ClassNotFoundException ex) {
	            ex.printStackTrace();
	        } catch (UnsupportedLookAndFeelException ex) {
	            ex.printStackTrace();
	        } catch (InstantiationException ex) {
	            ex.printStackTrace();
	        }
	        this.repaint();
	        //caricamento componenti
	        initComponents();
	        //setto le dimensioni del pannello rispetto a quelle dello schermo
	        this.setBounds(0, 0, (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth(), (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 30);
	        //titolo che compare nella barra superiore del frame
	        this.setTitle("GeoSearch");
	        
	        //Rendo INvisibili tabella e altri elementi
	        resultsSplitPane.setVisible(false);
	        problemLabel.setVisible(false);
	
	    }
	    
	    /** This method is called from within the constructor to
	     * initialize the form.
	     * WARNING: Do NOT modify this code. The content of this method is
	     * always regenerated by the Form Editor.
	     */
	    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
	    private void initComponents() {
	
	        tabbedPane = new javax.swing.JTabbedPane();
	        geoSearchPanel = new javax.swing.JPanel();
	        queryPanel = new javax.swing.JPanel();
	        queryTextField = new javax.swing.JTextField();
	        searchButton1 = new javax.swing.JButton();
	        locationPanel = new javax.swing.JPanel();
	        locationTextField = new javax.swing.JTextField();
	        jCheckBox = new javax.swing.JCheckBox();
	        countryComboBox = new javax.swing.JComboBox();
	        locationComboBox = new javax.swing.JComboBox();
	        searchButton2 = new javax.swing.JButton();
	        searchButton = new javax.swing.JButton();
	        resultsSplitPane = new javax.swing.JSplitPane();
	        resultsPanel = new javax.swing.JPanel();
	        visualizationSplitPane = new javax.swing.JSplitPane();
	        graphicPanel = new javax.swing.JPanel();
	        chartPanel = new javax.swing.JPanel();
	        jLabel5 = new javax.swing.JLabel();
	        listPanel = new javax.swing.JPanel();
	        resultsScrollPane = new javax.swing.JScrollPane();
	        resultsTable = new javax.swing.JTable();
	        jSlider = new javax.swing.JSlider();
	        querySliderLabel = new javax.swing.JLabel();
	        locationSliderLabel = new javax.swing.JLabel();
	        gMapsPanel = new javax.swing.JPanel();
	        gMapsButton = new javax.swing.JButton();
	        gMapsLabel = new javax.swing.JLabel();
	        jLabel7 = new javax.swing.JLabel();
	        openDocButton = new javax.swing.JButton();
	        GNEPanel = new javax.swing.JPanel();
	        GNEScrollPane = new javax.swing.JScrollPane();
	        GNETable = new javax.swing.JTable();
	        jLabel8 = new javax.swing.JLabel();
	        jLabel6 = new javax.swing.JLabel();
	        problemLabel = new javax.swing.JLabel();
	        jLabel9 = new javax.swing.JLabel();
	        geoTaggingPanel = new javax.swing.JPanel();
	        geoTagSplitPane = new javax.swing.JSplitPane();
	        geoTagScrollPane = new javax.swing.JScrollPane();
	        operationsTextArea = new javax.swing.JTextArea();
	        geoTagPanel = new javax.swing.JPanel();
	        fileChooserButton = new javax.swing.JButton();
	        startGeoRefButton = new javax.swing.JButton();
	        jLabel1 = new javax.swing.JLabel();
	        jLabel2 = new javax.swing.JLabel();
	        errorLabel = new javax.swing.JLabel();
	
	        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
	        setName("Form"); // NOI18N
	
	        tabbedPane.setName("tabbedPane"); // NOI18N
	
	        geoSearchPanel.setName("geoSearchPanel"); // NOI18N
	
	        queryPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
	        queryPanel.setName("queryPanel"); // NOI18N
	
	        
	        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(geotag.LocationBasedRetrievalApp.class).getContext().getResourceMap(GeoApplicationGui.class);
	        queryTextField.setFont(resourceMap.getFont("queryTextField.font")); // NOI18N
	        queryTextField.setText(resourceMap.getString("queryTextField.text")); // NOI18N
	        queryTextField.setToolTipText(resourceMap.getString("queryTextField.toolTipText")); // NOI18N
	        queryTextField.setName("queryTextField"); // NOI18N
	        queryTextField.addMouseListener(new java.awt.event.MouseAdapter() {
	            public void mouseClicked(java.awt.event.MouseEvent evt) {
	                queryTextFieldMouseClicked(evt);
	            }
	        });
	        queryTextField.addFocusListener(new java.awt.event.FocusAdapter() {
	            public void focusGained(java.awt.event.FocusEvent evt) {
	                queryTextFieldFocusGained(evt);
	            }
	        });
	
	        searchButton1.setIcon(resourceMap.getIcon("searchButton1.icon")); // NOI18N
	        searchButton1.setToolTipText(resourceMap.getString("searchButton1.toolTipText")); // NOI18N
	        searchButton1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
	        searchButton1.setBorderPainted(false);
	        searchButton1.setDefaultCapable(false);
	        searchButton1.setFocusPainted(false);
	        searchButton1.setFocusable(false);
	        searchButton1.setName("searchButton1"); // NOI18N
	        searchButton1.setRequestFocusEnabled(false);
	        searchButton1.setRolloverEnabled(false);
	        searchButton1.addMouseListener(new java.awt.event.MouseAdapter() {
	            public void mousePressed(java.awt.event.MouseEvent evt) {
	                searchButton1MousePressed(evt);
	            }
	        });
	
	        javax.swing.GroupLayout queryPanelLayout = new javax.swing.GroupLayout(queryPanel);
	        queryPanel.setLayout(queryPanelLayout);
	        queryPanelLayout.setHorizontalGroup(
	            queryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(queryPanelLayout.createSequentialGroup()
	                .addContainerGap()
	                .addComponent(searchButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
	                .addComponent(queryTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 619, Short.MAX_VALUE)
	                .addContainerGap())
	        );
	        queryPanelLayout.setVerticalGroup(
	            queryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(queryPanelLayout.createSequentialGroup()
	                .addContainerGap()
	                .addGroup(queryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
	                    .addComponent(searchButton1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                    .addComponent(queryTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE))
	                .addContainerGap())
	        );
	
	        locationPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
	        locationPanel.setName("locationPanel"); // NOI18N
	
	        locationTextField.setFont(resourceMap.getFont("locationTextField.font")); // NOI18N
	        locationTextField.setText(resourceMap.getString("locationTextField.text")); // NOI18N
	        locationTextField.setToolTipText(resourceMap.getString("locationTextField.toolTipText")); // NOI18N
	        locationTextField.setName("locationTextField"); // NOI18N
	        locationTextField.addMouseListener(new java.awt.event.MouseAdapter() {
	            public void mouseClicked(java.awt.event.MouseEvent evt) {
	                locationTextFieldMouseClicked(evt);
	            }
	        });
	        locationTextField.addFocusListener(new java.awt.event.FocusAdapter() {
	            public void focusGained(java.awt.event.FocusEvent evt) {
	                locationTextFieldFocusGained(evt);
	            }
	        });
	
	        jCheckBox.setText(resourceMap.getString("jCheckBox.text")); // NOI18N
	        jCheckBox.setToolTipText(resourceMap.getString("jCheckBox.toolTipText")); // NOI18N
	        jCheckBox.setName("jCheckBox"); // NOI18N
	        jCheckBox.addMouseListener(new java.awt.event.MouseAdapter() {
	            public void mouseClicked(java.awt.event.MouseEvent evt) {
	                jCheckBoxMouseClicked(evt);
	            }
	        });
	
	        countryComboBox.setFont(resourceMap.getFont("countryComboBox.font")); // NOI18N
	        countryComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "select a country" }));
	        countryComboBox.setToolTipText(resourceMap.getString("countryComboBox.toolTipText")); // NOI18N
	        countryComboBox.setEnabled(false);
	        countryComboBox.setName("countryComboBox"); // NOI18N
	        countryComboBox.addItemListener(new java.awt.event.ItemListener() {
	            public void itemStateChanged(java.awt.event.ItemEvent evt) {
	                countryComboBoxItemStateChanged(evt);
	            }
	        });
	
	        locationComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "select a location" }));
	        locationComboBox.setToolTipText(resourceMap.getString("locationComboBox.toolTipText")); // NOI18N
	        locationComboBox.setEnabled(false);
	        locationComboBox.setName("locationComboBox"); // NOI18N
	
	        searchButton2.setIcon(resourceMap.getIcon("searchButton2.icon")); // NOI18N
	        searchButton2.setToolTipText(resourceMap.getString("searchButton2.toolTipText")); // NOI18N
	        searchButton2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
	        searchButton2.setBorderPainted(false);
	        searchButton2.setFocusPainted(false);
	        searchButton2.setFocusable(false);
	        searchButton2.setName("searchButton2"); // NOI18N
	        searchButton2.setRequestFocusEnabled(false);
	        searchButton2.setRolloverEnabled(false);
	        searchButton2.addMouseListener(new java.awt.event.MouseAdapter() {
	            public void mousePressed(java.awt.event.MouseEvent evt) {
	                searchButton2MousePressed(evt);
	            }
	        });
	
	        javax.swing.GroupLayout locationPanelLayout = new javax.swing.GroupLayout(locationPanel);
	        locationPanel.setLayout(locationPanelLayout);
	        locationPanelLayout.setHorizontalGroup(
	            locationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(locationPanelLayout.createSequentialGroup()
	                .addContainerGap()
	                .addGroup(locationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addGroup(locationPanelLayout.createSequentialGroup()
	                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                        .addComponent(searchButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
	                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                        .addComponent(locationTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 473, Short.MAX_VALUE)
	                        .addContainerGap())
	                    .addGroup(locationPanelLayout.createSequentialGroup()
	                        .addComponent(countryComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
	                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 82, Short.MAX_VALUE)
	                        .addComponent(locationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)
	                        .addGap(20, 20, 20))
	                    .addGroup(locationPanelLayout.createSequentialGroup()
	                        .addComponent(jCheckBox)
	                        .addContainerGap(361, Short.MAX_VALUE))))
	        );
	        locationPanelLayout.setVerticalGroup(
	            locationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(locationPanelLayout.createSequentialGroup()
	                .addContainerGap()
	                .addGroup(locationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
	                    .addComponent(locationTextField)
	                    .addComponent(searchButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
	                .addGap(18, 18, 18)
	                .addComponent(jCheckBox)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addGroup(locationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
	                    .addGroup(locationPanelLayout.createSequentialGroup()
	                        .addComponent(countryComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
	                    .addComponent(locationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
	                .addContainerGap(21, Short.MAX_VALUE))
	        );
	
	        searchButton.setIcon(resourceMap.getIcon("searchButton.icon")); // NOI18N
	        searchButton.setText(resourceMap.getString("searchButton.text")); // NOI18N
	        searchButton.setToolTipText(resourceMap.getString("searchButton.toolTipText")); // NOI18N
	        searchButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
	        searchButton.setName("searchButton"); // NOI18N
	        searchButton.addMouseListener(new java.awt.event.MouseAdapter() {
	            public void mousePressed(java.awt.event.MouseEvent evt) {
	                searchButtonMousePressed(evt);
	            }
	        });
	
	        resultsSplitPane.setDividerLocation(400);
	        resultsSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
	        resultsSplitPane.setName("resultsSplitPane"); // NOI18N
	
	        resultsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
	        resultsPanel.setName("resultsPanel"); // NOI18N
	
	        visualizationSplitPane.setDividerLocation(400);
	        visualizationSplitPane.setName("visualizationSplitPane"); // NOI18N
	
	        graphicPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
	        graphicPanel.setName("graphicPanel"); // NOI18N
	
	        chartPanel.setName("chartPanel"); // NOI18N
	
	        javax.swing.GroupLayout chartPanelLayout = new javax.swing.GroupLayout(chartPanel);
	        chartPanel.setLayout(chartPanelLayout);
	        chartPanelLayout.setHorizontalGroup(
	            chartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGap(0, 372, Short.MAX_VALUE)
	        );
	        chartPanelLayout.setVerticalGroup(
	            chartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGap(0, 341, Short.MAX_VALUE)
	        );
	
	        jLabel5.setFont(resourceMap.getFont("jLabel5.font")); // NOI18N
	        jLabel5.setForeground(resourceMap.getColor("jLabel5.foreground")); // NOI18N
	        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
	        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
	        jLabel5.setName("jLabel5"); // NOI18N
	
	        javax.swing.GroupLayout graphicPanelLayout = new javax.swing.GroupLayout(graphicPanel);
	        graphicPanel.setLayout(graphicPanelLayout);
	        graphicPanelLayout.setHorizontalGroup(
	            graphicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(graphicPanelLayout.createSequentialGroup()
	                .addGap(12, 12, 12)
	                .addComponent(chartPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                .addContainerGap())
	            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
	        );
	        graphicPanelLayout.setVerticalGroup(
	            graphicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(graphicPanelLayout.createSequentialGroup()
	                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
	                .addComponent(chartPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                .addContainerGap())
	        );
	
	        visualizationSplitPane.setLeftComponent(graphicPanel);
	
	        listPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
	        listPanel.setName("listPanel"); // NOI18N
	
	        resultsScrollPane.setName("resultsScrollPane"); // NOI18N
	
	        resultsTable.setFont(resourceMap.getFont("resultsTable.font")); // NOI18N
	        resultsTable.setModel(new javax.swing.table.DefaultTableModel(
	            new Object [][] {
	
	            },
	            new String [] {
	                "Rank", "Document name", "Text score", "Distance score"
	            }
	        ) {
	            boolean[] canEdit = new boolean [] {
	                false, false, false, false
	            };
	
	            public boolean isCellEditable(int rowIndex, int columnIndex) {
	                return canEdit [columnIndex];
	            }
	        });
	        try{
	        	resultsTable.setGridColor(resourceMap.getColor("resultsTable.gridColor")); // NOI18N
	        } catch (IllegalArgumentException e){}
	        resultsTable.setName("resultsTable"); // NOI18N
	        resultsTable.setRowHeight(20);
	        resultsTable.addMouseListener(new java.awt.event.MouseAdapter() {
	            public void mouseClicked(java.awt.event.MouseEvent evt) {
	                resultsTableMouseClicked(evt);
	            }
	        });
	        resultsScrollPane.setViewportView(resultsTable);
	
	        jSlider.setName("jSlider"); // NOI18N
	
	        querySliderLabel.setText(resourceMap.getString("querySliderLabel.text")); // NOI18N
	        querySliderLabel.setName("querySliderLabel"); // NOI18N
	
	        locationSliderLabel.setText(resourceMap.getString("locationSliderLabel.text")); // NOI18N
	        locationSliderLabel.setName("locationSliderLabel"); // NOI18N
	
	        gMapsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
	        gMapsPanel.setName("gMapsPanel"); // NOI18N
	
	        gMapsButton.setIcon(resourceMap.getIcon("gMapsButton.icon")); // NOI18N
	        gMapsButton.setText(resourceMap.getString("gMapsButton.text")); // NOI18N
	        gMapsButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
	        gMapsButton.setEnabled(false);
	        gMapsButton.setName("gMapsButton"); // NOI18N
	        gMapsButton.addMouseListener(new java.awt.event.MouseAdapter() {
	            public void mouseClicked(java.awt.event.MouseEvent evt) {
	                gMapsButtonMouseClicked(evt);
	            }
	        });
	
	        gMapsLabel.setFont(resourceMap.getFont("gMapsLabel.font")); // NOI18N
	        gMapsLabel.setText(resourceMap.getString("gMapsLabel.text")); // NOI18N
	        gMapsLabel.setEnabled(false);
	        gMapsLabel.setName("gMapsLabel"); // NOI18N
	
	        javax.swing.GroupLayout gMapsPanelLayout = new javax.swing.GroupLayout(gMapsPanel);
	        gMapsPanel.setLayout(gMapsPanelLayout);
	        gMapsPanelLayout.setHorizontalGroup(
	            gMapsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(gMapsPanelLayout.createSequentialGroup()
	                .addComponent(gMapsButton)
	                .addGap(94, 94, 94)
	                .addComponent(gMapsLabel)
	                .addContainerGap(132, Short.MAX_VALUE))
	        );
	        gMapsPanelLayout.setVerticalGroup(
	            gMapsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addComponent(gMapsButton)
	            .addGroup(gMapsPanelLayout.createSequentialGroup()
	                .addContainerGap()
	                .addComponent(gMapsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
	                .addContainerGap())
	        );
	
	        jLabel7.setFont(resourceMap.getFont("jLabel7.font")); // NOI18N
	        jLabel7.setForeground(resourceMap.getColor("jLabel7.foreground")); // NOI18N
	        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
	        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
	        jLabel7.setName("jLabel7"); // NOI18N
	
	        openDocButton.setBackground(resourceMap.getColor("openDocButton.background")); // NOI18N
	        openDocButton.setIcon(resourceMap.getIcon("openDocButton.icon")); // NOI18N
	        openDocButton.setText(resourceMap.getString("openDocButton.text")); // NOI18N
	        openDocButton.setToolTipText(resourceMap.getString("openDocButton.toolTipText")); // NOI18N
	        openDocButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
	        openDocButton.setName("openDocButton"); // NOI18N
	        openDocButton.addMouseListener(new java.awt.event.MouseAdapter() {
	            public void mouseClicked(java.awt.event.MouseEvent evt) {
	                openDocButtonMouseClicked(evt);
	            }
	            public void mousePressed(java.awt.event.MouseEvent evt) {
	                openDocButtonMousePressed(evt);
	            }
	        });
	
	        javax.swing.GroupLayout listPanelLayout = new javax.swing.GroupLayout(listPanel);
	        listPanel.setLayout(listPanelLayout);
	        listPanelLayout.setHorizontalGroup(
	            listPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, listPanelLayout.createSequentialGroup()
	                .addGap(144, 144, 144)
	                .addComponent(querySliderLabel)
	                .addGap(18, 18, 18)
	                .addComponent(jSlider, javax.swing.GroupLayout.DEFAULT_SIZE, 484, Short.MAX_VALUE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(locationSliderLabel)
	                .addGap(120, 120, 120))
	            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 870, Short.MAX_VALUE)
	            .addGroup(listPanelLayout.createSequentialGroup()
	                .addContainerGap()
	                .addComponent(resultsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 846, Short.MAX_VALUE)
	                .addContainerGap())
	            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, listPanelLayout.createSequentialGroup()
	                .addContainerGap()
	                .addComponent(gMapsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(openDocButton, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addContainerGap())
	        );
	        listPanelLayout.setVerticalGroup(
	            listPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(listPanelLayout.createSequentialGroup()
	                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addGroup(listPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
	                    .addComponent(jSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addComponent(querySliderLabel)
	                    .addComponent(locationSliderLabel))
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(resultsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addGroup(listPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addComponent(openDocButton, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
	                    .addComponent(gMapsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
	                .addContainerGap())
	        );
	
	        visualizationSplitPane.setRightComponent(listPanel);
	
	        javax.swing.GroupLayout resultsPanelLayout = new javax.swing.GroupLayout(resultsPanel);
	        resultsPanel.setLayout(resultsPanelLayout);
	        resultsPanelLayout.setHorizontalGroup(
	            resultsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addComponent(visualizationSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 1280, Short.MAX_VALUE)
	        );
	        resultsPanelLayout.setVerticalGroup(
	            resultsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addComponent(visualizationSplitPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
	        );
	
	        resultsSplitPane.setTopComponent(resultsPanel);
	
	        GNEPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
	        GNEPanel.setName("GNEPanel"); // NOI18N
	
	        GNEScrollPane.setBackground(resourceMap.getColor("GNEScrollPane.background")); // NOI18N
	        GNEScrollPane.setName("GNEScrollPane"); // NOI18N
	
	        GNETable.setBackground(resourceMap.getColor("GNETable.background")); // NOI18N
	        GNETable.setBorder(javax.swing.BorderFactory.createEtchedBorder());
	        GNETable.setModel(new javax.swing.table.DefaultTableModel(
	            new Object [][] {
	
	            },
	            new String [] {
	                "Location name", "GeoScore", "GeoReferenceValue", "Country code", "Admin 1 code", "Admin 2 code", "Latitude", "Longitude", "Population", "Elevation"
	            }
	        ) {
	            boolean[] canEdit = new boolean [] {
	                false, true, false, true, true, true, false, false, false, false
	            };
	
	            public boolean isCellEditable(int rowIndex, int columnIndex) {
	                return canEdit [columnIndex];
	            }
	        });
	        GNETable.setEnabled(false);
	        GNETable.setName("GNETable"); // NOI18N
	        GNEScrollPane.setViewportView(GNETable);
	
	        jLabel8.setFont(resourceMap.getFont("jLabel8.font")); // NOI18N
	        jLabel8.setForeground(resourceMap.getColor("jLabel8.foreground")); // NOI18N
	        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
	        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
	        jLabel8.setName("jLabel8"); // NOI18N
	
	        javax.swing.GroupLayout GNEPanelLayout = new javax.swing.GroupLayout(GNEPanel);
	        GNEPanel.setLayout(GNEPanelLayout);
	        GNEPanelLayout.setHorizontalGroup(
	            GNEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(GNEPanelLayout.createSequentialGroup()
	                .addGap(12, 12, 12)
	                .addComponent(GNEScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 1256, Short.MAX_VALUE)
	                .addContainerGap())
	            .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, 1280, Short.MAX_VALUE)
	        );
	        GNEPanelLayout.setVerticalGroup(
	            GNEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(GNEPanelLayout.createSequentialGroup()
	                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
	                .addComponent(GNEScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE)
	                .addContainerGap())
	        );
	
	        resultsSplitPane.setRightComponent(GNEPanel);
	
	        jLabel6.setFont(resourceMap.getFont("jLabel6.font")); // NOI18N
	        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
	        jLabel6.setName("jLabel6"); // NOI18N
	
	        problemLabel.setFont(resourceMap.getFont("problemLabel.font")); // NOI18N
	        problemLabel.setForeground(resourceMap.getColor("problemLabel.foreground")); // NOI18N
	        problemLabel.setText(resourceMap.getString("problemLabel.text")); // NOI18N
	        problemLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
	        problemLabel.setName("problemLabel"); // NOI18N
	
	        jLabel9.setFont(resourceMap.getFont("jLabel9.font")); // NOI18N
	        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
	        jLabel9.setName("jLabel9"); // NOI18N
	
	        javax.swing.GroupLayout geoSearchPanelLayout = new javax.swing.GroupLayout(geoSearchPanel);
	        geoSearchPanel.setLayout(geoSearchPanelLayout);
	        geoSearchPanelLayout.setHorizontalGroup(
	            geoSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, geoSearchPanelLayout.createSequentialGroup()
	                .addContainerGap()
	                .addGroup(geoSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
	                    .addComponent(resultsSplitPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 1284, Short.MAX_VALUE)
	                    .addComponent(problemLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 1284, Short.MAX_VALUE)
	                    .addGroup(geoSearchPanelLayout.createSequentialGroup()
	                        .addGroup(geoSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
	                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, geoSearchPanelLayout.createSequentialGroup()
	                                .addGap(81, 81, 81)
	                                .addComponent(searchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
	                                .addGap(18, 18, 18)
	                                .addGroup(geoSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                                    .addComponent(jLabel9)
	                                    .addComponent(jLabel6)))
	                            .addComponent(queryPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
	                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                        .addComponent(locationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
	                .addContainerGap())
	        );
	        geoSearchPanelLayout.setVerticalGroup(
	            geoSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(geoSearchPanelLayout.createSequentialGroup()
	                .addContainerGap()
	                .addGroup(geoSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addGroup(geoSearchPanelLayout.createSequentialGroup()
	                        .addComponent(queryPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                        .addGroup(geoSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                            .addGroup(geoSearchPanelLayout.createSequentialGroup()
	                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
	                                .addComponent(searchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
	                            .addGroup(geoSearchPanelLayout.createSequentialGroup()
	                                .addGap(32, 32, 32)
	                                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))))
	                    .addGroup(geoSearchPanelLayout.createSequentialGroup()
	                        .addGap(133, 133, 133)
	                        .addComponent(jLabel9))
	                    .addComponent(locationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
	                .addGap(18, 18, 18)
	                .addComponent(resultsSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(problemLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addContainerGap())
	        );
	
	        tabbedPane.addTab(resourceMap.getString("geoSearchPanel.TabConstraints.tabTitle"), geoSearchPanel); // NOI18N
	
	        geoTaggingPanel.setName("geoTaggingPanel"); // NOI18N
	
	        geoTagSplitPane.setDividerLocation(150);
	        geoTagSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
	        geoTagSplitPane.setName("geoTagSplitPane"); // NOI18N
	
	        geoTagScrollPane.setName("geoTagScrollPane"); // NOI18N
	
	        operationsTextArea.setColumns(20);
	        operationsTextArea.setRows(5);
	        operationsTextArea.setName("operationsTextArea"); // NOI18N
	        geoTagScrollPane.setViewportView(operationsTextArea);
	
	        geoTagSplitPane.setBottomComponent(geoTagScrollPane);
	
	        geoTagPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
	        geoTagPanel.setName("geoTagPanel"); // NOI18N
	
	        fileChooserButton.setIcon(resourceMap.getIcon("fileChooserButton.icon")); // NOI18N
	        fileChooserButton.setText(resourceMap.getString("fileChooserButton.text")); // NOI18N
	        fileChooserButton.setName("fileChooserButton"); // NOI18N
	        fileChooserButton.addMouseListener(new java.awt.event.MouseAdapter() {
	            public void mousePressed(java.awt.event.MouseEvent evt) {
	                fileChooserButtonMousePressed(evt);
	            }
	        });
	
	        startGeoRefButton.setIcon(resourceMap.getIcon("startGeoRefButton.icon")); // NOI18N
	        startGeoRefButton.setText(resourceMap.getString("startGeoRefButton.text")); // NOI18N
	        startGeoRefButton.setEnabled(false);
	        startGeoRefButton.setName("startGeoRefButton"); // NOI18N
	        startGeoRefButton.addMouseListener(new java.awt.event.MouseAdapter() {
	            public void mousePressed(java.awt.event.MouseEvent evt) {
	                startGeoRefButtonMousePressed(evt);
	            }
	        });
	
	        jLabel1.setFont(resourceMap.getFont("jLabel1.font")); // NOI18N
	        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
	        jLabel1.setName("jLabel1"); // NOI18N
	
	        jLabel2.setFont(resourceMap.getFont("jLabel2.font")); // NOI18N
	        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
	        jLabel2.setName("jLabel2"); // NOI18N
	
	        javax.swing.GroupLayout geoTagPanelLayout = new javax.swing.GroupLayout(geoTagPanel);
	        geoTagPanel.setLayout(geoTagPanelLayout);
	        geoTagPanelLayout.setHorizontalGroup(
	            geoTagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, geoTagPanelLayout.createSequentialGroup()
	                .addContainerGap()
	                .addGroup(geoTagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addComponent(fileChooserButton, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addComponent(startGeoRefButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addGroup(geoTagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 348, Short.MAX_VALUE)
	                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 348, Short.MAX_VALUE))
	                .addGap(848, 848, 848))
	        );
	        geoTagPanelLayout.setVerticalGroup(
	            geoTagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(geoTagPanelLayout.createSequentialGroup()
	                .addContainerGap()
	                .addGroup(geoTagPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addGroup(geoTagPanelLayout.createSequentialGroup()
	                        .addComponent(fileChooserButton)
	                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
	                        .addComponent(startGeoRefButton, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
	                        .addGap(60, 60, 60))
	                    .addGroup(geoTagPanelLayout.createSequentialGroup()
	                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 66, Short.MAX_VALUE)
	                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
	                        .addGap(70, 70, 70))))
	        );
	
	        geoTagSplitPane.setLeftComponent(geoTagPanel);
	
	        errorLabel.setFont(resourceMap.getFont("errorLabel.font")); // NOI18N
	        errorLabel.setForeground(resourceMap.getColor("errorLabel.foreground")); // NOI18N
	        errorLabel.setText(resourceMap.getString("errorLabel.text")); // NOI18N
	        errorLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
	        errorLabel.setName("errorLabel"); // NOI18N
	
	        javax.swing.GroupLayout geoTaggingPanelLayout = new javax.swing.GroupLayout(geoTaggingPanel);
	        geoTaggingPanel.setLayout(geoTaggingPanelLayout);
	        geoTaggingPanelLayout.setHorizontalGroup(
	            geoTaggingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, geoTaggingPanelLayout.createSequentialGroup()
	                .addContainerGap()
	                .addGroup(geoTaggingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
	                    .addComponent(geoTagSplitPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 1284, Short.MAX_VALUE)
	                    .addComponent(errorLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 1284, Short.MAX_VALUE))
	                .addContainerGap())
	        );
	        geoTaggingPanelLayout.setVerticalGroup(
	            geoTaggingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, geoTaggingPanelLayout.createSequentialGroup()
	                .addContainerGap()
	                .addComponent(geoTagSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 949, Short.MAX_VALUE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
	                .addComponent(errorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addContainerGap())
	        );
	
	        tabbedPane.addTab(resourceMap.getString("geoTaggingPanel.TabConstraints.tabTitle"), geoTaggingPanel); // NOI18N
	
	        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
	        getContentPane().setLayout(layout);
	        layout.setHorizontalGroup(
	            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 1316, Short.MAX_VALUE)
	        );
	        layout.setVerticalGroup(
	            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addComponent(tabbedPane)
	        );
	
	        pack();
	    }// </editor-fold>//GEN-END:initComponents
	
	    private void fileChooserButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fileChooserButtonMousePressed
	        File currentDir = new File(fileName);
	        JFileChooser chooser = new JFileChooser();
	        chooser.setCurrentDirectory(currentDir);
	                
	        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);       
	        chooser.setFileFilter(new MyFileExtensionFilter());
	                
	        JFrame fcFrame = new JFrame();
	        int result = chooser.showOpenDialog(fcFrame);
	        
	        if (result == JFileChooser.APPROVE_OPTION)
	        {
	            curDir = chooser.getSelectedFile(); //Directory selezionata      
	            try{
	                //Stampo il nome della directory
	                //dirNameDxLabel.setText(curDir.getName());
	                startGeoRefButton.setEnabled(true);
	            }
	            catch (Exception e){
	                e.printStackTrace();
	            }
	        }
	    }//GEN-LAST:event_fileChooserButtonMousePressed
	
	    private void startGeoRefButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_startGeoRefButtonMousePressed
	        startGeoRefButton.setEnabled(false);
	        startGeoRefButton.setFocusable(false);
	        
	        String errortext=createIndex(curDir);
	        
	        if (errortext!=""){
	        	errorLabel.setText(errortext);
	        }
	 
	        operationsTextArea.append("Stop analysis...\n");
	        operationsTextArea.append("**********\n");
	        
	        startGeoRefButton.setEnabled(true);
	        startGeoRefButton.setFocusable(true);
	    }//GEN-LAST:event_startGeoRefButtonMousePressed
	
	    private void jCheckBoxMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jCheckBoxMouseClicked
	        if(!jCheckBox.isSelected()){
	            countryComboBox.setEnabled(false);
	            locationComboBox.setEnabled(false);
	            locationTextField.setEnabled(true); 
	        }else{                  
	            //Il campo di testo per inserire la location viene settato a Disable
	            if(locationTextField.isEnabled()){
	                locationTextField.setEnabled(false);            
	                //try {
	                    DatabaseGazetteerConnection dbConnection;
	                    
	                    Vector<String> countries = new Vector<String>();
	                    try {
	                        dbConnection = new DatabaseGazetteerConnection(DRIVER_CLASS_NAME, USER_NAME, PASSWORD, DB_CONN_STRING);
	                        Statement stmt = dbConnection.getStatement();
	                        countries = selectItems("countryinfo", "", stmt);
	                        stmt.close();
	                    } catch (InstantiationException ex) {
	                        Logger.getLogger(GeoApplicationGui.class.getName()).log(Level.SEVERE, null, ex);
	                    } catch (IllegalAccessException ex) {
	                        Logger.getLogger(GeoApplicationGui.class.getName()).log(Level.SEVERE, null, ex);
	                    } catch (ClassNotFoundException ex) {
	                        Logger.getLogger(GeoApplicationGui.class.getName()).log(Level.SEVERE, null, ex);
	                    } catch (SQLException ex) {
	                        Logger.getLogger(GeoApplicationGui.class.getName()).log(Level.SEVERE, null, ex);
	                    }
	                    
	
	                    //Ordino alfabeticamente...
	                    Collections.sort(countries);
	                    
	                    for(int i = 0; i < countries.size(); i++){
	                        countryComboBox.addItem(countries.elementAt(i));
	                    }                       
	                    countryComboBox.setEnabled(true);
	                /*} catch (SQLException ex) {
	                    errorLabel.setText("Error query commit");
	                } catch (InstantiationException ex) {
	                    errorLabel.setText("Error DataBase connection");
	                } catch (IllegalAccessException ex) {
	                    errorLabel.setText("Error DataBase access");
	                }*/
	            }
	            else
	                locationTextField.setEnabled(true);
	        }
	    }//GEN-LAST:event_jCheckBoxMouseClicked
	
	    private void searchButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchButtonMousePressed
	        resultsSplitPane.setVisible(true);
	        problemLabel.setVisible(true);
	        GNETable.setEnabled(false);
	        
	        GeographicWord geoLocation = new GeographicWord();        
	        String keyWords = "";
	        String location = "";
	        String country = "";
	        String zone = "";
	           
	        //Leggo i parametri
	        keyWords = queryTextField.getText();
	        location = locationTextField.getText();
	        
	        //obsoleto?
	        if(jCheckBox.isSelected()){
	            country = (String) countryComboBox.getSelectedItem();
	            location = (String) locationComboBox.getSelectedItem();
	        }
	                    
	        if(!keyWords.isEmpty()){
	            try {
	            	resultsmerge.clear();
	                // --> Creazione del RANKING testuale
	                ContentSearcher content = new ContentSearcher();
	                results = content.createTextualRankig(keyWords);
	                //Devo cercare nell'indice geografico le GeoWords di ogni documento
	                //results = content.findGeoWords(results);
	                
	                
	                // --> Georeferenziazione della LOCATION
	                if(!location.isEmpty()){
	                    //DatabaseGazetteerConnection dbConnection;
	                    
	                    try {
	                        //dbConnection = new DatabaseGazetteerConnection(DRIVER_CLASS_NAME, USER_NAME, PASSWORD, DB_CONN_STRING);
	                        Statement stmt = null;//dbConnection.getStatement();
	                        GeoRefLocation grLoc = new GeoRefLocation();
	                        if(jCheckBox.isSelected())
	                            geoLocation = grLoc.getGeoLocation(location, country, stmt);                        
	                        else
	                            geoLocation = grLoc.getGeoLocation(location, stmt);
	                        //stmt.close();
	                    }
	                    catch (Exception e) {
							// TODO: handle exception
	                    	System.out.print("Errore in Ricerca");
						}
		//                    catch (InstantiationException ex) {
		//                        Logger.getLogger(GeoApplication.class.getName()).log(Level.SEVERE, null, ex);
		//                    } catch (IllegalAccessException ex) {
		//                        Logger.getLogger(GeoApplication.class.getName()).log(Level.SEVERE, null, ex);
		//                    } catch (ClassNotFoundException ex) {
		//                        Logger.getLogger(GeoApplication.class.getName()).log(Level.SEVERE, null, ex);
		//                    } catch (SQLException ex) {
		//                        Logger.getLogger(GeoApplication.class.getName()).log(Level.SEVERE, null, ex);
		//                    }
	                    
	                }
	                
	                if(geoLocation.isLocation()){
	                    //Rendo visibili tabella e altri elementi
	                    if(resultsTable.isVisible() == false)
	                        resultsTable.setVisible(true);
	                    if(jSlider.isVisible() == false)
	                        jSlider.setVisible(true);
	                    if(querySliderLabel.isVisible() == false)
	                        querySliderLabel.setVisible(true);
	                    if(locationSliderLabel.isVisible() == false)
	                        locationSliderLabel.setVisible(true);
	                    if(gMapsPanel.isVisible() == false)
	                        gMapsPanel.setVisible(true);
	
	                    
	                    results=search(keyWords, location);
	                    
	                    // --> Creazione del RANKING geografico
	                    // Se il documento non è georeferenziato ritorna ZERO come distanceScore
	                    //	VECCHIO CODICE
	                    //DistanceSearcher distance = new DistanceSearcher();
	                    //distance.createDistanceRanking(results, geoLocation);
	                    
	                    // --> Visualizzazione dei risultati su doppi assi (JFreeChart API)
	                    final MarkerChart chart = new MarkerChart(geoLocation.getName(), results, chartPanel);
	                    chart.pack();
	                        
	                    // --> Visualizzazione dei risultati in una tabella
	                    final ResultsTable table = new ResultsTable();
	                    table.createTable(resultsTable, results, 50);
	                    
	                    // --> Calcolo del RANKING distribuito ed aggiorno la tabella dei risultati
	                    jSlider.setEnabled(true);
	                    jSlider.addChangeListener(new ChangeListener() {
	                        public void stateChanged(ChangeEvent e) {
	                            table.createTable(resultsTable, results, ((JSlider) e.getSource()).getValue());
	                        }
	                    });
	                    /*
	                    resultsTable.addMouseListener(new MouseAdapter() {
	                        @Override
	                        public void mouseClicked(MouseEvent evt) {
	                            if(evt.getButton() == 3) {
	                                System.out.println("Right Click........");
	                                // Mouse left click should be forced here to select the row.
	
	                            }
	                            if(evt.getButton() == 1) {
	                                System.out.println("Mouse LEFT CLICK FORCED.....");
	                            }
	                        }
	                    });
	                    */
	                }
	                else
	                    problemLabel.setText("\'Location\' field isn't a geo-location");
	            /*
	            } catch (java.text.ParseException ex) {
	                problemLabel.setText("Error query parse");
	            } catch (SQLException ex) {
	                problemLabel.setText("Error query commit");            
	            } catch (InstantiationException ex) {
	                problemLabel.setText("Error DataBase connection");
	            } catch (IllegalAccessException ex) {
	                problemLabel.setText("Error DataBase access");
	                */
	            } 
	        catch (IOException ex) {
	                problemLabel.setText("Error index search");
	            } catch (ParseException ex) {
	                problemLabel.setText("Error in text query, no results for " + keyWords);
	            }
	        }else
	            problemLabel.setText("I campi query o location sono vuoti. Inserire dei valori.");
	        
	    }//GEN-LAST:event_searchButtonMousePressed
	
	    private void gMapsButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_gMapsButtonMouseClicked
	        File gMapsDir = new File("./output/GMaps"); 
	
	        String documentWithoutExtension = selectedDocName.substring(0, selectedDocName.lastIndexOf('.'));
	        String gMapsDoc = documentWithoutExtension + "_map.html";
	
	        //Correggo link
	        gMapsDoc = StringOperation.convertUrl(gMapsDoc);
	
	        if(gMapsDir.isDirectory()){
	            File docFile = new File("./output/GMaps" + gMapsDoc); 
	            if (!docFile.isDirectory()){
	                openPage(gMapsDoc);
	            }
	        }
	
	    }//GEN-LAST:event_gMapsButtonMouseClicked
	
	    private void countryComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_countryComboBoxItemStateChanged
	        //try {
	            locationComboBox.setEnabled(true);
	
	            CountryItemListener actionListener;
	            try {
	                actionListener = new CountryItemListener(countryComboBox, locationComboBox, new DatabaseGazetteerConnection(DRIVER_CLASS_NAME, USER_NAME, PASSWORD, DB_CONN_STRING));
	                countryComboBox.addItemListener(actionListener);
	            } catch (InstantiationException ex) {
	                Logger.getLogger(GeoApplicationGui.class.getName()).log(Level.SEVERE, null, ex);
	            } catch (IllegalAccessException ex) {
	                Logger.getLogger(GeoApplicationGui.class.getName()).log(Level.SEVERE, null, ex);
	            } catch (ClassNotFoundException ex) {
	                Logger.getLogger(GeoApplicationGui.class.getName()).log(Level.SEVERE, null, ex);
	            } catch (SQLException ex) {
	                Logger.getLogger(GeoApplicationGui.class.getName()).log(Level.SEVERE, null, ex);
	            }
	            
	
	        /*} catch (SQLException ex) {
	            problemLabel.setText("Contry selected isn't correct");
	        } catch (InstantiationException ex) {
	            problemLabel.setText("Contry selected isn't correct");
	        } catch (IllegalAccessException ex) {
	            problemLabel.setText("Contry selected isn't correct");
	        }*/
	    }//GEN-LAST:event_countryComboBoxItemStateChanged
	
	    private void searchButton1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchButton1MousePressed
	        // TODO add your handling code here:
	    }//GEN-LAST:event_searchButton1MousePressed
	
	    private void searchButton2MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchButton2MousePressed
	        // TODO add your handling code here:
	    }//GEN-LAST:event_searchButton2MousePressed
	
	    private void resultsTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_resultsTableMouseClicked
	        resultsTable.addMouseListener(new java.awt.event.MouseAdapter() {
	            public void mousePressed(java.awt.event.MouseEvent evt) {
	                int count=evt.getClickCount();
	                if(count==2){
	                    //System.out.println("HO sentito il doppio click");
	                    //CATTURARE EVENTO                    
	                    try{
	                        for(int i=0; i<resultsTable.getSelectedRowCount(); i++){
	                            selectedDocName = (String) resultsTable.getValueAt(resultsTable.getSelectedRows()[i],1);
	                            
	                            gMapsButton.setEnabled(true);
	                            GNETable.setEnabled(true);
	                            
	                            //Popolo la tabella
	                            GeoWordsTable table = new GeoWordsTable(GNETable, results, selectedDocName);
	                            
	                        }
	                    }catch(Exception ex){
	                        ex.printStackTrace();
	                    }
	                }
	            }
	
	        }
	        );
	    }//GEN-LAST:event_resultsTableMouseClicked
	
	    private void queryTextFieldMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_queryTextFieldMouseClicked
	        queryTextField.setText("");
	    }//GEN-LAST:event_queryTextFieldMouseClicked
	
	    private void locationTextFieldMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_locationTextFieldMouseClicked
	        locationTextField.setText("");
	    }//GEN-LAST:event_locationTextFieldMouseClicked
	
	    private void queryTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_queryTextFieldFocusGained
	        queryTextField.setText("");
	    }//GEN-LAST:event_queryTextFieldFocusGained
	
	    private void locationTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_locationTextFieldFocusGained
	        locationTextField.setText("");
	    }//GEN-LAST:event_locationTextFieldFocusGained
	
	    private void openDocButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_openDocButtonMousePressed
	        // TODO add your handling code here:
	    }//GEN-LAST:event_openDocButtonMousePressed
	
	    private void openDocButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_openDocButtonMouseClicked
	        // APERTURA DOCUMENTO
	        openDoc(selectedDocName);
	    }//GEN-LAST:event_openDocButtonMouseClicked
	
	    
	    public void openDoc(String docName){
	        String url = "./docs/" + docName;        
	        String errMsg = "Error attempting to open document";
	        String osName = System.getProperty("os.name");
	        
	        if(docName != null)
	            if(docName.contains(".")){
	                String docExtension = docName.substring(docName.lastIndexOf('.') + 1, docName.length());                        
	                String docNameWithoutExtension = docName.substring(0, docName.lastIndexOf('.'));
	            }else
	                problemLabel.setText("Error open document " + docName);
	        else
	            problemLabel.setText("No document is selected");
	        
	        
	        try {
	            //if(docExtension.equalsIgnoreCase("html") || docExtension.equalsIgnoreCase("htm")
	            //    || docExtension.equalsIgnoreCase("xml")){                    
	                if (osName.startsWith("Mac OS")) {
	                    Class fileMgr = Class.forName("com.apple.eio.FileManager");
	                    Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] {String.class});
	                    openURL.invoke(null, new Object[] {url});
	                } else if (osName.startsWith("Windows"))
	                    Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
	                else {  //assume Unix or Linux
	                    String[] browsers = { "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
	                    String browser = null;
	                    for (int count = 0; count < browsers.length && browser == null; count++)
	                        if (Runtime.getRuntime().exec( new String[] {"which", browsers[count]}).waitFor() == 0)
	                            browser = browsers[count];//TODO è meglio usare gnome-open, xdg-open e l'equivalente kde
	                    if (browser == null) throw new Exception("Could not find web browser");
	                    else Runtime.getRuntime().exec(browser + " " + url);
	                }
	            //}
	            /*else if(docExtension.equalsIgnoreCase("pdf")){
	                if (osName.startsWith("Mac OS")) {
	                    Runtime.getRuntime().exec("open " + url);
	                } else if (osName.startsWith("Windows"))
	                    Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
	                else {  //assume Unix or Linux
	                    String docContent = openPDFDocument(url);
	                    
	                    
	                }
	            }   */            
	        } catch (Exception e) { 
	            problemLabel.setText(errMsg + ":\n" + e.getLocalizedMessage());
	        }      
	    }
	    
	    /**
	     * Metodo che apre attraverso un web browser la mappa del file selezionato
	     * @param gMapsDoc : nome del file (HTML) da aprire
	     */
	    public void openPage(String gMapsDoc) {
	        String url = "./output/GMaps/" + gMapsDoc;
	        String errMsg = "Error attempting to launch web browser";
	        String osName = System.getProperty("os.name");
	        try {
	            if (osName.startsWith("Mac OS")) {
	                Class fileMgr = Class.forName("com.apple.eio.FileManager");
	                Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] {String.class});
	                openURL.invoke(null, new Object[] {url});
	            } else if (osName.startsWith("Windows"))
	                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
	            else {  //assume Unix or Linux
	                String[] browsers = { "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
	                String browser = null;
	                for (int count = 0; count < browsers.length && browser == null; count++)
	                    if (Runtime.getRuntime().exec( new String[] {"which", browsers[count]}).waitFor() == 0)
	                        browser = browsers[count];
	                if (browser == null) throw new Exception("Could not find web browser");
	                else Runtime.getRuntime().exec(browser + " " + url);
	            }
	        } catch (Exception e) { 
	            problemLabel.setText(errMsg + ":\n" + e.getLocalizedMessage());
	        }
	    }
	    
	    /**
	     * Metodo che accede al DataBase per reperire tutte le Nazioni
	     * @param table
	     * @param countryCode
	     * @param stmt
	     * @return
	     * @throws java.sql.SQLException
	     */
	    private Vector<String> selectItems(String table, String countryCode, Statement stmt) throws SQLException{
	        Vector<String> countries = new Vector<String>();
	        String query = "";
	        
	        if(table.equals("countryinfo"))
	            query = "SELECT name FROM " + table;               
	        else if(table.equals("geoname")){
	            query = "SELECT asciiname FROM " + table + " WHERE countrycode='" + countryCode + "'";
	        }
	        
	        ResultSet result = stmt.executeQuery(query);
	
	        while (result.next()) {
	            countries.add(result.getString(1));
	        }
	        
	        return countries; 
	    }
	    
	    
	    
	    
	    
	    // Variables declaration - do not modify//GEN-BEGIN:variables
	    private javax.swing.JPanel GNEPanel;
	    private javax.swing.JScrollPane GNEScrollPane;
	    private javax.swing.JTable GNETable;
	    private javax.swing.JPanel chartPanel;
	    private javax.swing.JComboBox countryComboBox;
	    private javax.swing.JLabel errorLabel;
	    private javax.swing.JButton fileChooserButton;
	    private javax.swing.JButton gMapsButton;
	    private javax.swing.JLabel gMapsLabel;
	    private javax.swing.JPanel gMapsPanel;
	    private javax.swing.JPanel geoSearchPanel;
	    private javax.swing.JPanel geoTagPanel;
	    private javax.swing.JScrollPane geoTagScrollPane;
	    private javax.swing.JSplitPane geoTagSplitPane;
	    private javax.swing.JPanel geoTaggingPanel;
	    private javax.swing.JPanel graphicPanel;
	    private javax.swing.JCheckBox jCheckBox;
	    private javax.swing.JLabel jLabel1;
	    private javax.swing.JLabel jLabel2;
	    private javax.swing.JLabel jLabel5;
	    private javax.swing.JLabel jLabel6;
	    private javax.swing.JLabel jLabel7;
	    private javax.swing.JLabel jLabel8;
	    private javax.swing.JLabel jLabel9;
	    private javax.swing.JSlider jSlider;
	    private javax.swing.JPanel listPanel;
	    private javax.swing.JComboBox locationComboBox;
	    private javax.swing.JPanel locationPanel;
	    private javax.swing.JLabel locationSliderLabel;
	    private javax.swing.JTextField locationTextField;
	    private javax.swing.JButton openDocButton;
	    private javax.swing.JTextArea operationsTextArea;
	    private javax.swing.JLabel problemLabel;
	    private javax.swing.JPanel queryPanel;
	    private javax.swing.JLabel querySliderLabel;
	    private javax.swing.JTextField queryTextField;
	    private javax.swing.JPanel resultsPanel;
	    private javax.swing.JScrollPane resultsScrollPane;
	    private javax.swing.JSplitPane resultsSplitPane;
	    private javax.swing.JTable resultsTable;
	    private javax.swing.JButton searchButton;
	    private javax.swing.JButton searchButton1;
	    private javax.swing.JButton searchButton2;
	    private javax.swing.JButton startGeoRefButton;
	    private javax.swing.JTabbedPane tabbedPane;
	    private javax.swing.JSplitPane visualizationSplitPane;
	    // End of variables declaration//GEN-END:variables
	    
	}
	
	//public class GeoApplicationCmd{
		private void GeoApplicationCmd() {
			boolean flag=true;
			//InputStreamReader cin = new InputStreamReader(System.in);
			BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
			//CharBuffer target=CharBuffer.allocate(200);//sostituisco charbuffer con bufferedreader
			String buffer;
			String[] pair;
			System.out.print("LocationBasedSearch:\n" +
					"------------------------------------------------------------" +
					"--------------------\n" +
					"Interactive prompt: to quit type \"quit\" or press Ctrl-D\n");
			flag=true;
			do{
	    		try {
	    			System.out.print(">");
	    			buffer=in.readLine();
					
					if( buffer==null || buffer.equals("quit") ){
						flag=false;
					} else if ((pair=buffer.split(",")).length==2){
						cmdSearch(pair[0],pair[1]);
					} else if ((pair=buffer.split(" ")).length==2){
						cmdSearch(pair[0],pair[1]);
					}
					/*if (target.toString().equals("help")){
						
					}*/else if (!buffer.equals("")){
						System.out.print("unknown command\n");
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			} while (flag);
		}
		//TODO questa modifica non mi piace molto
		
		private void cmdSearch(String keywords, String location){
			Vector<GeoRefDoc> results=search(keywords,location);
			int i=0;
			for (GeoRefDoc doc : results) {
				i++;
				System.out.println(i+"° documento:\n"+doc.getNomeDoc()+
						"\ntextscore: "+doc.getTextScore()+
						"\nsortscore: "+doc.getSortScore()+
						"\ndistancescore: "+doc.getDistanceScore()+
						"\nid: "+doc.getId()+"\n");
			}
		}
		
	//}
	
	private String createIndex(File curDir){
		String errortext="";
        String documentContent = "";   
        String documentExtension = "";
        String documentName = "";
        double impValue = 0.0;  //E' un valore che indica l'importanza di un termine nel doc
        boolean stop = false;
        String hash;
        ContentIndexer contInd = new ContentIndexer();
        
   
        if(curDir.isDirectory()){
            File[] nameFiles = curDir.listFiles();           
            for (int i = 0; i < nameFiles.length; i++){
                if (!nameFiles[i].isDirectory()){
                        Vector<Word> wordVector = new Vector<Word>();
                        Vector<Word> filterWordVector = new Vector<Word>();
                        Vector<GeographicWord> geoWordVector = new Vector<GeographicWord>();
                        Vector<GeographicWord> finalGeoWordVector = new Vector<GeographicWord>();
                        Vector<Word> finalWordVector = new Vector<Word>();
                        Vector<Word> finalFilterWordVector = new Vector<Word>();
                        boolean alreadyIndexed = false;
                        boolean upperDateLine = false;
                        
                        // Gestione di ogni file contenuto nella directory
                        String name = nameFiles[i].getName();
                        documentExtension = nameFiles[i].getName().substring(nameFiles[i].getName().lastIndexOf('.') + 1, nameFiles[i].getName().length());
                        
                        documentName = nameFiles[i].getName().substring(0, nameFiles[i].getName().lastIndexOf('.'));
                        documentContent = openDocument(nameFiles[i].toString(), documentExtension, nameFiles[i]);

                        /* commentato: nel caso lo si può riaggiungere, ma per evitare un if(gui) sarebbe necessario reimplementarlo con una sottoclasse e quindi ripensare l'architettura di geoapplication
                         * comunque pare che non essendo parte di un thread questo non possa comparire nella gui così facilmente
                         * 
                        operationsTextArea.append("**********\n");
                        operationsTextArea.append("File name: " + nameFiles[i].getName() + "\n");
                        operationsTextArea.append("Start analysis...\n");
                        */
                        
                        //Memorizzo ORA di inizio dell'elaborazione
                        Date start = new Date();
                                
                        try {
                        	hash = DigestUtils.md5Hex(nameFiles[i].toURI().toString());
                        	
                        	//Controllo se il file è già stato indicizzato                              

                        	
                            alreadyIndexed = contInd.control(hash);
                            
                            System.out.println("\n  File name: " + documentName);
                            
                            //DatabaseGazetteerConnection dbConnection = new DatabaseGazetteerConnection(DRIVER_CLASS_NAME, USER_NAME, PASSWORD, DB_CONN_STRING);
                            Statement stmt = null;//dbConnection.getStatement();
                            
                            if(!alreadyIndexed){
                            	
                            	//TODO verificare il comportamento con file con lo stesso nome
                            	FileUtils.copyFile(nameFiles[i], new File(cachepath + hash +"."+ documentExtension));
                            	
                                do{   
                                    stop = false;
                                    // Estrazione delle Word candidate                                    
                                    WordAnalyzer generator = new WordAnalyzer(); 
                                    wordVector = generator.getWordVector(documentContent, swLanguage); 

                                    // Fase di FILTRO
                                    Filter myFilter = new Filter();
                                    filterWordVector = myFilter.filtering(wordVector, documentContent, upperDateLine);

                                    // Fase di GEO-VALUTAZIONE                                                                                   
                                    GeoCandidateIdentification geoAnalysis = new GeoCandidateIdentification();                            
                                    geoWordVector = geoAnalysis.analyzing(filterWordVector, wordVector, documentContent, impValue, stmt, upperDateLine);                    

                                    //Tra elementi con uguale geonameid ne prendo solo 1 con peso e importanza maggiore 
                                    finalGeoWordVector = importanceControl(geoWordVector, finalGeoWordVector); 
                                    finalWordVector = update(finalWordVector, wordVector);
                                    finalFilterWordVector = update(finalFilterWordVector, filterWordVector);
                                    
                                    //Indicizzo il contenuto del file 
                                    //PRIMA ERA COMMENTATO
                                     contInd.indexing(documentContent, hash);
                                                                        
                                    /*
                                    Se gli altri campi, oltre al contenuto del documento sono vuoti
                                    vado avanti, altrimenti eseguo indicizzazione ed analisi anche di quelli
                                     */
                                    if (!title.isEmpty()) {
                                        documentContent = title;
                                        title = "";
                                        impValue = 1;
                                        upperDateLine = false;
                                    } else if (description != null && !description.isEmpty()) {
                                        documentContent = description;
                                        description = "";
                                        upperDateLine = false;
                                        impValue = 0.5;
                                    } else if (dateline != null && !dateline.isEmpty()) {
                                        documentContent = dateline;
                                        dateline = "";
                                        upperDateLine = true;
                                        impValue = 0.5;
                                    } else if (keywords != null && !keywords.isEmpty()) {
                                        documentContent = keywords;
                                        keywords = "";
                                        upperDateLine = false;
                                        impValue = 1;
                                    } else {
                                        stop = true;
                                    }
                                 }while(!stop);
                                
                                
                                    // Fase di SCORING   
                                    Score newScore = new Score();
                                    finalGeoWordVector = newScore.updateGeoScore(finalGeoWordVector, finalWordVector, swLanguage, finalFilterWordVector, stmt);
                                    //stmt.close();
                                    
                                    //Elimino le GeoWord con peso sotto lo 0.6
                                    finalGeoWordVector = selectGeoWords(finalGeoWordVector);

                                    //GEOREFERENZIAZIONE
                                    GeoRef geoReferencing = new GeoRef();
                                    finalGeoWordVector = geoReferencing.calculateGeoRefValue(finalGeoWordVector);
                                    finalGeoWordVector = geoReferencing.delete(finalGeoWordVector);	//Controllo incrociato tra GeoScore e GeoRef

                                    /*for(int r=0; r < finalGeoWordVector.size(); r++){
                                        System.out.println(finalGeoWordVector.elementAt(r).getName() + " - "
                                                + formatter.format(finalGeoWordVector.elementAt(r).getGeoScore()) + " - "
                                                + formatter.format(finalGeoWordVector.elementAt(r).getGeoRefValue()));
                                    }*/

                                    //Aggiornamento dell'indice geografico
                                    //	VECCHIO CODICE
                                    //GeographicIndexer geoIndex = new GeographicIndexer(); //INDICIZZAZIONE delle GeoWords
                                    //geoIndex.indexing(finalGeoWordVector, nameFiles[i].getName(), swLanguage);

                                    //INIZIO MODIFICA
                                    for(int ind = 0; ind < finalGeoWordVector.size(); ind++){
                                        GeographicWord gw = finalGeoWordVector.elementAt(ind);                                    
                                        // cerco nell'r-tree
                                        System.out.print(finalGeoWordVector.elementAt(ind).getName() + " - geoscore: "
                                                + formatter.format(finalGeoWordVector.elementAt(ind).getGeoScore()) + " - georefvalue: "
                                                + formatter.format(finalGeoWordVector.elementAt(ind).getGeoRefValue())+" - codici: ");
                                        
                                        vettori vettoricodici=new vettori();
                                        vettoricodici=query_rtree.query(path,gw.getmbr_x1(), gw.getmbr_y1(),gw.getmbr_x2(), gw.getmbr_y2());
                                        boolean trovato=false;
                                        for(int trovati=0;trovati<vettoricodici.codici.size();trovati++)
                                        {
                                        	System.out.print(vettoricodici.codici.elementAt(trovati)+" ");
                                        	File file = new File(dbpath+(Integer.parseInt(vettoricodici.codici.elementAt(trovati))/1000)+slash+vettoricodici.codici.elementAt(trovati));
                                        	if (file.exists()){

                                        		FileReader reader=new FileReader(file);
                                        		LineNumberReader lr = new LineNumberReader(reader);
                                        		String line = lr.readLine();
                                        		String data[];
                                        		while (line != null)
                                        		{
                                        			data=line.split("�#");
                                        			if(data[0].equalsIgnoreCase(hash))
                                        				trovato=true;
                                        			line = lr.readLine();
                                        		}
                                        		reader.close();
                                        		
                                        	}
                                        	
                                        	if(trovato==false){
                                        		FileWriter file2=new FileWriter(dbpath+(Integer.parseInt(vettoricodici.codici.elementAt(trovati))/1000)+slash+vettoricodici.codici.elementAt(trovati),true);
                                        		file2.write(hash+"�#"+gw.getGeoScore()+"\r\n");
                                        		file2.close();
                                        	}
                                        }
                                        System.out.println();
                                    }
                                    //FINE MODIFICA
                                    
                                    //Creazione dei file di output                 
                                    CreateOutput output = new CreateOutput(finalGeoWordVector, documentName);
                                }
                            else{ //erano errorlabel, da ripristinare?
                                errortext+="Il file " + nameFiles[i].getName() +" è già stato indicizzato\n";
                            }
                                
                        } catch (IOException ex) {
                            errortext+="Error reading file\n";
                            ex.printStackTrace();
                        }/* catch (ParseException ex) {
                            errortext+="Error DataBase operation\n";
                        } */
	//                        catch (SQLException ex) {
	//                           errorLabel.setText("Error DataBase operation");
	//                        } 
	//                        catch (InstantiationException ex) {
	//                            errorLabel.setText("Error DataBase connection");
	//                        } 
	//                        catch (IllegalAccessException ex) {
	//                            errorLabel.setText("Error DataBase access");
	//                        } 
	//                        catch (ClassNotFoundException ex) {
	//                            errorLabel.setText("Error !!");
	//                        }
                    
                        //ORA di fine dell'elaborazione
                        Date end = new Date();
                        System.out.print("Time: " + (end.getTime() - start.getTime()) / 1000 + " seconds");                    
                        
                }
            }
        }
        contInd.closeIndex();   
		return errortext;
		
	}
	
	private String createIndex() {
		String errortext="";
		for (File f:indexDirs){
			errortext+=createIndex(f);
			//ContentIndexer.closeIndex();
		}
		return errortext;
	}

	/**
	 * Metodo per la gestione dell'apertura dei file da indicizzare.
	 * Sono accettati 3 formati: PDF, HTML e XML
	 * @param documentName : nome del documento
	 * @param documentExtension : estensione del file
	 * @param fileName : nome completo del file
	 * @return il contenuto del documento
	 * @throws ParserException
	 */
	public String openDocument(String documentName, String documentExtension, File fileName) {        
	    String content = "";
	    String errortext="";
	    if (documentExtension.equalsIgnoreCase("pdf")) {           
	        content = openPDFDocument(documentName);            
	    } else if (documentExtension.equalsIgnoreCase("html")
	            || documentExtension.equalsIgnoreCase("htm")) {
	        content = openHTMLDocument(documentName);
	    } else if (documentExtension.equalsIgnoreCase("xml")) {
	        content = openXMLDocument(fileName);
	    } else {
	        errortext+="File format isn't supported\n";//era un errorlabel, da ripristinare per la gui? 
	    }
	               
	    return content;
	}

	/**
	 * Apertura del file in formato PDF .
	 * Per eseguire questa operazione mi sono affidato alle librerie PDFBox e FontBox
	 * @param documentName : nome del documento
	 * @return il contenuto del file PDF
	 */
	public String openPDFDocument(String documentName) { 
	    String docContent = "";
	    String errortext="";
	    PDFParser pdfFileParser = new PDFParser();
	    
	    try {
	        docContent = pdfFileParser.parsinfPDFFile(documentName);
	        
	    } catch (IOException ex) {
	        errortext+="Error onening PDF file\n";//era un errorlabel
	    } catch (CryptographyException ex) {
	        //statusMessageLabel.setText("Error");
	    } catch (InvalidPasswordException ex) {
	        errortext+="Error: password invalid\n";//era un errorlabel
	    }
	    
	    return docContent;
	}

	/**
	 * Apertura del documento il formato HTML:
	 * Estraggo il testo dal documento contenuto all'interno del tag <body>,
	 * escludendo i LINK perché potrebbero essere fuorvianti.
	 * In particolare memorizzo i campi "title", "description" e "keywords" per i quali, se 
	 * conterranno un elemento geografico, dovrò aumentare il peso di rilevanza.
	 * Per eseguire queste operazioni mi sono affidato alle librerie: HTMLParser, Jericho-HTML
	 * @param docURL : indirizzo del documento
	 * @return il contenuto del file HTML
	 * @throws org.htmlparser.util.ParserException
	 */
	public String openHTMLDocument(String docURL){                 	
	    String body = "";
	    String errortext="";
	    
	    try {
	        HTMLParser htmlFileParser = new HTMLParser(docURL);
	        body = htmlFileParser.parsingHTMLFile();
	          
	        //Document title
	        title = htmlFileParser.getTitle();
	        String t = title;
	        
	        //Document description
	        description = htmlFileParser.getMetaValue("description");
	        String s = description;
	        
	        //Document keywords
	        keywords = htmlFileParser.getMetaValue("keywords");
	        String k = keywords;
	
	    } catch (IOException ex) {
	        errortext+="Error opening HTML file\n";//era un errorlabel
	    }
	
	    return body;
	}

	/**
	 * Apertura del file nel formato XML.
	 * Per eseguire questa operazione mi sono affidato alla libreria SAX e JDOM
	 * @param fileName : nome del file 
	 * @return il contenuto del file
	 */
	public String openXMLDocument(File fileName){         
	    String content = "";
	    String errortext="";
	    
	    try {              
	        XMLParser xmlFileParser = new XMLParser(fileName);
	        
	        content = xmlFileParser.parsingXMLfile("");
	
	        //Document title
	        title = xmlFileParser.parsingXMLfile("title");
	        String t = title;
	        
	        //Document dateline
	        dateline = xmlFileParser.parsingXMLfile("dateline");
	        String d = dateline;
	        String a = d;
	    } catch (JDOMException ex) {
	        errortext+="Error parsing XML file\n";//erano errorlabel
	    } catch (IOException ex) {
	        errortext+="Error opening XML file\n";
	    }
	
	    return content;
	}

	public Vector<Word>  update (Vector<Word> finalWordVector, Vector<Word> wordVector){
	    
	    if(finalWordVector.size() == 0){
	        for(int i = 0; i < wordVector.size(); i++){
	            finalWordVector.add(wordVector.elementAt(i));
	        }
	    }else{
	        for(int i = 0; i < finalWordVector.size(); i++){
	            Word gw1 = finalWordVector.elementAt(i);
	            
	            for(int j = 0; j < wordVector.size(); j++){
	                Word gw2 = wordVector.elementAt(j);
	                if(gw1.getGeonameid() == gw2.getGeonameid()) 
	                    gw1.setFrequency(gw1.getFrequency() + 1);
	                else
	                    finalWordVector.add(gw2);
	            }  
	        }
	
	    }
	    
	    return finalWordVector;
	}

	/**
	 * Metodo che controlla se nel vettore ci sono GeoWord con lo stesso geonameId
	 * In questo caso seleziona solo la GeoWord con importanza maggiore
	 * @param finalGeoWordVector : vettore delle GeoWord
	 * @return il vettore delle GeoWord senza zone doppie
	 */
	public Vector<GeographicWord>  importanceControl (Vector<GeographicWord> geoWordVector, Vector<GeographicWord> finalGeoWordVector){
	    Vector<GeographicWord> newGeoWordVector = new Vector<GeographicWord>();
	    boolean diverso = true;
	    
	    //Se è la prima volta che popolo il vettore
	    if(finalGeoWordVector.size() == 0){
	        for(int i = 0; i < geoWordVector.size(); i++){
	            newGeoWordVector.add(geoWordVector.elementAt(i));
	        }
	    }else{       
	        for(int i = 0; i < finalGeoWordVector.size(); i++){
	            GeographicWord gw1 = finalGeoWordVector.elementAt(i);
	            diverso = true;
	            
	            for(int j = 0; j < geoWordVector.size(); j++){
	                GeographicWord gw2 = geoWordVector.elementAt(j);
	                if(gw1.getGeonameid() == gw2.getGeonameid()){                                              
	                    
	                    //Nuova freq
	                    int newFreq = gw1.getFrequency() + gw2.getFrequency();                                                  
	                    
	                    //Nuova Importance                                
	                    double newImp = gw1.getImportance() + gw2.getImportance();                            
	                   
	                    GeographicWord newGW = new GeographicWord();
	                    newGW = gw1;
	                    newGW.setFrequency(newFreq);
	                    newGW.setImportance(newImp);
	                    
	                    newGeoWordVector.add(newGW);
	                    diverso = false;                        
	                }
	            }
	
	            if(diverso)
	                newGeoWordVector.add(gw1);    
	        }
	        
	        //Inserisco le NUOVE geoWord trovate
	        for(int i = 0; i < geoWordVector.size(); i++){
	            GeographicWord gw3 = geoWordVector.elementAt(i);
	            boolean different = true;
	            
	            for(int j = 0; j < newGeoWordVector.size(); j++){
	                GeographicWord gw4 = newGeoWordVector.elementAt(j);
	                
	                if(gw3.getGeonameid() == gw4.getGeonameid())
	                    different = false;
	            }
	            
	            if(different){
	                gw3.setGeoScore(gw3.getGeoScore() + uniqueGWScore);
	                newGeoWordVector.add(gw3); 
	            }
	        }
	    }
	    return newGeoWordVector;
	}

	/**
	 * Metodo che seleziona le GeoWords da mantenere dopo la fase di pesatura e che quindi 
	 * verranno indicizzate e mostrate come output. 
	 * @param geoWordVector : elenco delel GeoWord
	 * @return elenco delle GeoWord ritenute valide per il documento
	 */
	public Vector<GeographicWord> selectGeoWords(Vector<GeographicWord> geoWordVector){
	    Vector<GeographicWord> newGeoWordVector = new Vector<GeographicWord>();
	    boolean control = false;
	    double geoScoreBoundary;
	    
	    
	    
	    for(int j = 0; j < geoWordVector.size(); j++){
	        GeographicWord gw = geoWordVector.elementAt(j);
	        double geoScore = gw.getGeoScore();
	        double geoReferenceValue = gw.getGeoRefValue();
	        int frequency = gw.getFrequency();
	        String geoName = gw.getName();
	        String zoneDocName = gw.getZoneDocName();
	        
	        /* Seleziono solo le Word che hanno un nome più lungo di una lettera
	         * e con peso > di un peso preso come limite minimo: goodGeoScore 
	        */ 
	        if(geoName != null && geoName.length()>1 && zoneDocName.length() > 1 && geoScore >= goodGeoScore){
	            //Alle zone con geoScore > 1 devo settarlo ad 1
	            if(geoScore > 1.0){
	                gw.setGeoScore(1.0);
	            }
	            
	            //Alle zone con frequenza uguale a 0 metto 1
	            if(frequency == 0){
	                gw.setFrequency(1);
	            }
	                
	            if(geoScore > goodGeoScore){   
	                //if(!(geoScore <= 0.7 && geoReferenceValue <= 0.2))
	                    newGeoWordVector.add(gw);
	            }
	                
	        }
	    }
	    
	    //Controllo se zona con peso > 0.6 è unica, in questo caso aumento di 0.1 il peso
	    if(newGeoWordVector.size() == 1){
	        GeographicWord gw = newGeoWordVector.elementAt(0);
	        double score = gw.getGeoScore();
	        if(score >= goodGeoScore && ((score + goodGeoScore) < 1.0)){
	            gw.setGeoScore(score + uniqueGWScore);
	        }
	    }
	
	
	    return newGeoWordVector;
	}

	public static Vector<GeoRefDoc> search(String keyWords, String location){
		
		try {
	    	
			ContentSearcher content = new ContentSearcher();
	        results = content.createTextualRankig(keyWords);
			
			GeoRefLocation grLoc = new GeoRefLocation();
	        GeographicWord geoLocation;
			geoLocation = grLoc.getGeoLocation(location, null);
			
			// cerco nell'r-tree con l'mbr del paese (in futuro se si vogliono trovare
	        // più risultati bisogna allargare l'mbr.
			
	        double allarga=0;
	        int cerca=0;
	        boolean paeselocalizzato=false;
			boolean documentotrovato=false;
			
	        grLoc = new GeoRefLocation();
	        int numeroresults=0;
	        vettori vettoricodici=query_rtree.query(path,geoLocation.getmbr_x1()+allarga, geoLocation.getmbr_y1()+allarga,geoLocation.getmbr_x2()-allarga, geoLocation.getmbr_y2()-allarga);
	        
	        for(int trovati=0;trovati<vettoricodici.codici.size();trovati++)
	        {
	        	String line = null;
	        	FileReader fileletto = null;
	        	LineNumberReader lr = null;
	        	
				try {
					fileletto = new FileReader(dbpath + (Integer.parseInt(vettoricodici.codici.elementAt(trovati)) / 1000) + slash + vettoricodici.codici.elementAt(trovati));
					// file.write(nameFiles[i].getName()+"�#"+gw.getGeoScore()+"\r\n");
					lr = new LineNumberReader(fileletto);
					line = lr.readLine();
				} catch (IOException e) {

				}
	    		
	    		paeselocalizzato=false;
	    		documentotrovato=false;
	    		
	    		while (line != null)
	    		{
	    			String data[];
	    			data=line.split("�#");
	    			GeoRefDoc documentoref=new GeoRefDoc();
	    			documentotrovato=false;
	    			numeroresults=0;
	    			//for(int numeroresults=0;numeroresults<results.size();numeroresults++)
	    			while(numeroresults<results.size() && documentotrovato==false)
	    			{
	    				if(results.elementAt(numeroresults).getNomeDoc().equalsIgnoreCase(data[0])){
	    					documentoref=results.elementAt(numeroresults);
	    					if(paeselocalizzato==false){
	    						try{
	    							geoLocation = grLoc.getGeoLocation(vettoricodici.nomi.elementAt(trovati), null);
	    						}
	    						catch (Exception e) {
	    							// 	TODO: handle exception
	    						}
	    						paeselocalizzato=true;
	    					}
	    					boolean trova=false;
	    					cerca=0;	
	    					//for(int cerca=0;cerca<resultsmerge.size();cerca++)
	    					while(cerca<resultsmerge.size() && trova==false)
	    					{
	    						if(resultsmerge.elementAt(cerca).getNomeDoc().equalsIgnoreCase(data[0])){
	    							resultsmerge.elementAt(cerca).addGeoWord(geoLocation);
	    							trova=true;
	    						}
	    						cerca++;
	    					}
	    					if(trova==false){
	    						documentoref.addGeoWord(geoLocation);
	    						resultsmerge.add(documentoref);
	    					}
	    				}
	    				numeroresults++;
	    			}
	    			
	    			line = lr.readLine();
	    		}
	          	
	    		if (fileletto!=null){
	    			fileletto.close();
	          	}
	        }
	        results=resultsmerge;
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (java.text.ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
	    return results;
	}
	
	public void run(){
		new GeoApplicationGui().setVisible(true);
	}
	
	public static String getPath(){
		return path;
	}
	
	public GeoApplication(String cfgpath){
		slash = System.getProperty("file.separator");
		
		if (cfgpath!=null){
			configfile=cfgpath;
		} else {
			// set default config path

			String os = System.getProperty("os.name");
			String homepath = System.getProperty("user.home");
			if (os.startsWith("Linux")) {

				configfile = System.getenv("XDG_CONFIG_HOME");
				if (configfile == null) {
					configfile = homepath + "/.config/";
				}
				configfile += "geosearch/config";

			} else if (os.startsWith("Windows")) {
				configfile = System.getenv("APPDATA") + slash + "geosearch"
						+ slash + "config";
			} else if (os.startsWith("Mac")) {
				configfile = homepath
						+ "/Library/Application Support/geosearch/" + "config";
			} else {
				configfile = System.getProperty("user.dir") + "geosearch"
						+ slash + "config";
			}
		}
		
		loadConfiguration();
	}
	
	public void loadConfiguration(){
		File cfgfile=new File(configfile);
		if (!cfgfile.exists()){
			System.out.println("Missing configuration file, please run Dbcreator first");
			System.exit(0);
		}
		PropertiesConfiguration config;
		try {
			config = new PropertiesConfiguration(configfile);
			path=config.getString("basepath");
			dbpath=path+config.getString("dbdirectory")+slash;
			cachepath=config.getString("cachepath");
			swLanguage=path+"stopWords"+slash+config.getString("languagefile");
		} catch (ConfigurationException e) {		
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private ArrayList<File> updateIndexConfig(String inputpath) {//da aggiungere controllo? permette di aggiungere in continuazione lo stesso path
		try {
			PropertiesConfiguration config = new PropertiesConfiguration(configfile);
			ArrayList<File> innerIndexDirs=updateIndexConfig();
			
			if (inputpath!=null){
				File inputfile=new File(inputpath);
				if (!innerIndexDirs.contains(inputfile)){
					innerIndexDirs.add(new File(inputpath));
					config.setProperty("indexdirs", innerIndexDirs);
					config.save();
				}
			}
			
			return innerIndexDirs;
		} catch (ConfigurationException e) {		
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	private ArrayList<File> updateIndexConfig() {
		try {
			PropertiesConfiguration config = new PropertiesConfiguration(configfile);
			String[] indexDirPaths=config.getStringArray("indexdirs");
			ArrayList<File> innerIndexDirs=new ArrayList<File>();
			
			for (String indf:indexDirPaths){
				innerIndexDirs.add(new File(indf));
			}
			return innerIndexDirs;
				
		} catch (ConfigurationException e) {		
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		GeoApplication mainApp;
		CommandLineParser parser = new PosixParser();
		Options options=new Options();
		options.addOption("g", "gui", false, "starts with the gui");
		options.addOption("h", "help", false, "print this message");
		//options.addOption("i", "index", false, "indexes the documents on the given path");
		options.addOption(OptionBuilder.withLongOpt("index").hasOptionalArg().withDescription("indexes the documents on the given path or reindex everything if without argument").create("i"));
		HelpFormatter usagehelp = new HelpFormatter();
		String cfgpath = null;
		try {
			CommandLine cmd = parser.parse(options, args);
			args = cmd.getArgs();

			if (cmd.hasOption("help")) {
				StackTraceElement[] stack = Thread.currentThread ().getStackTrace ();
				String programName = stack[stack.length - 1].getFileName();
				String usage=programName+" [configfile] --index [path]\n" +
				programName+" [configfile] keyword place";
				usagehelp.printHelp(usage, options);
			} else if (cmd.hasOption("gui")) {
				mainApp = new GeoApplication(cfgpath);
				java.awt.EventQueue.invokeLater(mainApp
				/*
				 * new Runnable() { public void run() { new
				 * GeoApplicationGui().setVisible(true); } }
				 */);
			} else {
				if (args.length==1 || args.length==3){
					cfgpath=args[0];
					if (args.length==3){
						String[] temp={args[1],args[2]};
						args=temp;
					}
				}
				if (cmd.hasOption("index")) {
					mainApp = new GeoApplication(cfgpath);
					String inputpath = cmd.getOptionValue("index");
					mainApp.indexDirs = mainApp.updateIndexConfig(inputpath);

					if (inputpath != null) {
						String errortext = mainApp.createIndex(new File(
								inputpath));
						System.out.println(errortext);
					} else {
						String errortext = mainApp.createIndex();
						System.out.println(errortext);
					}
				} else if (args.length == 2 || args.length == 3) {
					mainApp = new GeoApplication(cfgpath);
					Vector<GeoRefDoc> results = search(args[0], args[1]);
					for (GeoRefDoc doc : results) {
						System.out.println(doc.getNomeDoc() + "\ntextscore: "
								+ doc.getTextScore() + "\nsortscore: "
								+ doc.getSortScore() + "\ndistancescore: "
								+ doc.getDistanceScore() + "\nid: "
								+ doc.getId() + "\n");
					}
				} else if (args.length == 0 || args.length == 1) {
					mainApp = new GeoApplication(cfgpath);
					mainApp.GeoApplicationCmd();
				}
			}
		} catch (org.apache.commons.cli.ParseException e) {
			e.printStackTrace();
		}
	}

	


}