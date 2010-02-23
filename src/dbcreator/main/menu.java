/*
 * Created by JFormDesigner on Sun Apr 26 20:52:18 CEST 2009
 */

package dbcreator.main;

import java.awt.*;
import java.awt.event.*;
import java.io.File;

import javax.swing.*;
import com.sun.java.swing.plaf.windows.*;

/**
 * @author marco marco
 */
public class menu extends JFrame {
	
	//Thread a,b;
	//avviocaricamento_osm avvio;
	//avviocaricamento_Rtree avvio2;
	//public menu(Thread a,Thread b,avviocaricamento_osm avvio,avviocaricamento_Rtree avvio2) {
	private String path;
	public menu() {
            initComponents();
		//this.a=a;
		//this.b=b;
		//this.avvio=avvio;
		//this.avvio2=avvio2;
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension frameSize = getSize();

            if (frameSize.height > screenSize.height) {
                frameSize.height = screenSize.height;
            }
            if (frameSize.width > screenSize.width) {
                frameSize.width = screenSize.width;
            }
            setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setVisible(true);
	}
	public void verifica(){
		System.out.print(path);
        File verificaosm=new File(path+"coordinate.txt");
        
        File verificar_tree1=new File(path+"db"+File.separator+"datiscritti.dat");
        File verificar_tree2=new File(path+"db"+File.separator+"datiscritti.idx");
        File verificar_tree3=new File(path+"db"+File.separator+"albero_Btree_osm.db");
        
        File verificapop1=new File(path+"db"+File.separator+"albero_Btree_population.db");
        File verificapop2=new File(path+"db"+File.separator+"albero_Btree_population2.db");
        
        
        File verificagaz1=new File(path+"db"+File.separator+"albero_Btree_Gazetteer.db");
        File verificagaz2=new File(path+"db"+File.separator+"albero_Btree_Intermedio.db");
        
        File verificaalter1=new File(path+"db"+File.separator+"albero_alternatenames.db");
        File verificaalter2=new File(path+"db"+File.separator+"albero_alternatenamesId.db");
    
        File verificaresto1=new File(path+"db"+File.separator+"albero_admin1codeascii.db");    
        File verificaresto2=new File(path+"db"+File.separator+"albero_countryInfo.db");
        File verificaresto3=new File(path+"db"+File.separator+"albero_featurecodes.db");
        
        
        Color verde=new Color(0,255,0);
        Color rosso=new Color(255,0,0);
        if(verificaosm.exists()){
        	label1.setText("OK");
        	label1.setForeground(verde);
        }
        else{
        	label1.setText("Manca");
        	label1.setForeground(rosso);
        }
        
        if(verificar_tree1.exists() && verificar_tree2.exists() && verificar_tree3.exists()){
        	label2.setText("OK");
        	label2.setForeground(verde);
        }
        else{
        	label2.setText("Manca");
        	label2.setForeground(rosso);
        }

        if(verificagaz1.exists() && verificagaz2.exists()){
        	label3.setText("OK");
        	label3.setForeground(verde);
        }
        else{
        	label3.setText("Manca");
        	label3.setForeground(rosso);
        }

        if(verificaalter1.exists() && verificaalter2.exists()){
        	label4.setText("OK");
        	label4.setForeground(verde);
        }
        else{
        	label4.setText("Manca");
        	label4.setForeground(rosso);
        }
        
        if(verificaresto1.exists() && verificaresto2.exists() && verificaresto3.exists()){
        	label5.setText("OK");
        	label5.setForeground(verde);
        }
        else{
        	label5.setText("Manca");
        	label5.setForeground(rosso);
        }

        if(verificapop1.exists() && verificapop2.exists()){
        	label6.setText("OK");
        	label6.setForeground(verde);
        }
        else{
        	label6.setText("Manca");
        	label6.setForeground(rosso);
        }
	}

	private void carica_RtreeActionPerformed(ActionEvent e) {
		
		//avvio2.setpath(path);
		
		avviocaricamento_Rtree caricamentoRtree = new avviocaricamento_Rtree();
        Thread t = new Thread(caricamentoRtree);
        caricamentoRtree.setpath(path);
		t.start();		
	}

	private void carica_osmActionPerformed(ActionEvent e) {
		// TODO add your code here
		String paths[]=new String[1];
		paths[0]=cartellafile.getText();
		
		avviocaricamento_osm caricamento_osm = new avviocaricamento_osm();
        Thread t2 = new Thread(caricamento_osm);
		
		caricamento_osm.setpath(paths);
		t2.start();
		
	}
	public void sorgentefile(String a){
		carica_osm.setEnabled(true);
        carica_Rtree.setEnabled(true);
        carica_alter.setEnabled(true);
        carica_gaz.setEnabled(true);
        caricadb.setEnabled(true);
        gazpop.setEnabled(true);
        
		cartellafile.setText(a);
		File appoggio=new File(cartellafile.getText());
        path=appoggio.getParent()+File.separator;
        verifica();
     }

	private void caricafileActionPerformed(ActionEvent e) {
		// TODO add your code here
		dialogo frame = new dialogo(this);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize2 = frame.getSize();
        if (frameSize2.height > screenSize.height) {
            frameSize2.height = screenSize.height;
        }
        if (frameSize2.width > screenSize.width) {
            frameSize2.width = screenSize.width;
        }
        frame.setLocation( ( screenSize.width - frameSize2.width ) / 2, ( screenSize.height - frameSize2.height ) / 2 );
        frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
        frame.setVisible(true);
        
        
	}

	
	
	private void carica_gazActionPerformed(ActionEvent e) {
		// TODO add your code here
		try{
			//avvio2.setpath(path);
			
			avviocaricamento_gazetteer caricamentogazetteer = new avviocaricamento_gazetteer();
	        Thread t = new Thread(caricamentogazetteer);
	        caricamentogazetteer.setpath(path);
			t.start();
		}
		catch (Exception ee) {
			// TODO: handle exception
		}
	}

	private void carica_alterActionPerformed(ActionEvent e) {
		// TODO add your code here
		try{
			//avvio2.setpath(path);
			
			avviocaricamento_alternateNames caricamentoalter = new avviocaricamento_alternateNames();
	        Thread t = new Thread(caricamentoalter);
	        caricamentoalter.setpath(path);
			t.start();
		}
		catch (Exception ee) {
			// TODO: handle exception
		}
	}

	
	private void caricadbActionPerformed(ActionEvent e) {
		// TODO add your code here
		try{
		admin1codeascii.carica(path);
		countryInfo.carica(path);
		featurecodes.carica(path);
		verifica();
		}
		catch (Exception ee) {
			// TODO: handle exception
		}
	}

	private void gazpopActionPerformed(ActionEvent e) {
		// TODO add your code here
		try{
			//avvio2.setpath(path);
			
			avviocaricamento_gazetteerpop caricamentogazetteerpop = new avviocaricamento_gazetteerpop();
	        Thread t = new Thread(caricamentogazetteerpop);
	        caricamentogazetteerpop.setpath(path);
			t.start();
		}
		catch (Exception ee) {
			// TODO: handle exception
		}
	}


	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner Evaluation license - Marc marco
		carica_Rtree = new JButton();
		carica_osm = new JButton();
		cartellafile = new JTextField();
		caricafile = new JButton();
		carica_gaz = new JButton();
		carica_alter = new JButton();
		caricadb = new JButton();
		label1 = new JLabel();
		label2 = new JLabel();
		label3 = new JLabel();
		label4 = new JLabel();
		label5 = new JLabel();
		gazpop = new JButton();
		label6 = new JLabel();

		//======== this ========
		setTitle("Menu");
		Container contentPane = getContentPane();

		//---- carica_Rtree ----
		carica_Rtree.setText("Caricamento r-tree");
		carica_Rtree.setEnabled(false);
		carica_Rtree.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				carica_RtreeActionPerformed(e);
			}
		});

		//---- carica_osm ----
		carica_osm.setText("Caricamento OSM");
		carica_osm.setEnabled(false);
		carica_osm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				carica_osmActionPerformed(e);
			}
		});

		//---- caricafile ----
		caricafile.setText("caricafile");
		caricafile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				caricafileActionPerformed(e);
			}
		});

		//---- carica_gaz ----
		carica_gaz.setText("Caricamento Gazetteer");
		carica_gaz.setEnabled(false);
		carica_gaz.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				carica_gazActionPerformed(e);
			}
		});

		//---- carica_alter ----
		carica_alter.setText("Caricamento AlternateNames");
		carica_alter.setEnabled(false);
		carica_alter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				carica_alterActionPerformed(e);
			}
		});

		//---- caricadb ----
		caricadb.setText("Carica DataBase restanti");
		caricadb.setEnabled(false);
		caricadb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				caricadbActionPerformed(e);
			}
		});

		//---- label1 ----
		label1.setText("Manca");
		label1.setFont(label1.getFont().deriveFont(label1.getFont().getSize() + 10f));
		label1.setForeground(Color.red);

		//---- label2 ----
		label2.setText("Manca");
		label2.setFont(label2.getFont().deriveFont(label2.getFont().getSize() + 10f));
		label2.setForeground(Color.red);

		//---- label3 ----
		label3.setText("Manca");
		label3.setForeground(Color.red);
		label3.setFont(label3.getFont().deriveFont(label3.getFont().getSize() + 10f));

		//---- label4 ----
		label4.setText("Manca");
		label4.setForeground(Color.red);
		label4.setFont(label4.getFont().deriveFont(label4.getFont().getSize() + 10f));

		//---- label5 ----
		label5.setText("Manca");
		label5.setForeground(Color.red);
		label5.setFont(label5.getFont().deriveFont(label5.getFont().getSize() + 10f));

		//---- gazpop ----
		gazpop.setText("Caricamento popolazione");
		gazpop.setEnabled(false);
		gazpop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gazpopActionPerformed(e);
			}
		});

		//---- label6 ----
		label6.setText("Manca");
		label6.setFont(label6.getFont().deriveFont(label6.getFont().getSize() + 10f));
		label6.setForeground(Color.red);

		GroupLayout contentPaneLayout = new GroupLayout(contentPane);
		contentPane.setLayout(contentPaneLayout);
		contentPaneLayout.setHorizontalGroup(
			contentPaneLayout.createParallelGroup()
				.addGroup(contentPaneLayout.createSequentialGroup()
					.addGap(38, 38, 38)
					.addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
						.addComponent(carica_gaz, GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)
						.addComponent(gazpop, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)
						.addComponent(carica_alter, GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)
						.addComponent(carica_osm, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)
						.addComponent(carica_Rtree, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)
						.addComponent(cartellafile, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)
						.addComponent(caricadb, GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE))
					.addGap(18, 18, 18)
					.addGroup(contentPaneLayout.createParallelGroup()
						.addComponent(label3, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
						.addComponent(caricafile, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
							.addComponent(label1, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(label6, GroupLayout.Alignment.LEADING))
						.addComponent(label5, GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE)
						.addComponent(label2)
						.addComponent(label4, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE))
					.addContainerGap())
		);
		contentPaneLayout.setVerticalGroup(
			contentPaneLayout.createParallelGroup()
				.addGroup(contentPaneLayout.createSequentialGroup()
					.addGap(19, 19, 19)
					.addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(cartellafile, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
						.addComponent(caricafile, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					.addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
						.addComponent(carica_osm, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
						.addComponent(label1, GroupLayout.DEFAULT_SIZE, 51, Short.MAX_VALUE))
					.addGap(18, 18, 18)
					.addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(carica_Rtree, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
						.addComponent(label2))
					.addGap(18, 18, 18)
					.addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(gazpop, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
						.addComponent(label6, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
					.addGap(16, 16, 16)
					.addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(label3, GroupLayout.PREFERRED_SIZE, 47, GroupLayout.PREFERRED_SIZE)
						.addComponent(carica_gaz, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE))
					.addGap(18, 18, 18)
					.addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(carica_alter, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
						.addComponent(label4))
					.addGap(15, 15, 15)
					.addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(caricadb, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
						.addComponent(label5))
					.addContainerGap())
		);
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - Marc marco
	private JButton carica_Rtree;
	private JButton carica_osm;
	private JTextField cartellafile;
	private JButton caricafile;
	private JButton carica_gaz;
	private JButton carica_alter;
	private JButton caricadb;
	private JLabel label1;
	private JLabel label2;
	private JLabel label3;
	private JLabel label4;
	private JLabel label5;
	private JButton gazpop;
	private JLabel label6;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
