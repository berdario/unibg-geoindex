package dbcreator.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;


import dbcreator.scansione.BUnzip2;
import dbcreator.scansione.ScanXML;
import geotag.Configuration;

public class Dbcreator {
    Configuration config;
    private static String path;
    public int step = 0;
	
    public Dbcreator(String configpath) {

        //set default paths
        String paths[] = Configuration.getDefaultPaths();
        if (configpath != null) {
            paths[0] = configpath;
        }

        File cfgfile = new File(paths[0]);
        if (!cfgfile.exists()) {
            Configuration.createConfiguration(paths[0], paths[1], paths[3]);
        }
        config = new Configuration(paths[0]);

        path = Configuration.getPath();
    }

    public boolean checkExistingDb() {
        boolean flag = true;

        File basepath = new File(path);
        if (!basepath.exists()) {
            basepath.mkdirs();
            checkNeededFiles();
            System.exit(1);
        } else {
            System.out.println("Verifica presenza strutture dati:");
            try {
                for (File f : Configuration.getRequiredFiles()) {
                    System.out.println(f.getCanonicalPath() + ": " + (f.exists() ? "OK" : "MANCA"));
                    if (!f.exists()) {
                        flag = false;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return flag;
    }

    public void checkNeededFiles() {
        boolean originFilesFlag = true;
        String warning = "";

        for (String f : Configuration.getNeededFiles()) {
            if (!(new File(path + f)).exists()) {
                if (originFilesFlag) {
                    originFilesFlag = false;
                    warning += "\nPlease insert into " + path + " the following files:\n";
                }
                warning += f + "\n";
            }
        }

        if (!warning.equals("")) {
            System.out.println(warning);
            System.exit(1);
        }
    }

	public void createDB(int step){
		
		switch (step) {
		case 0:
			caricaOSM();
			break;
		case 1:
			CaricaRtree();
			break;
		case 2:
			CaricaPopulation();
			break;
		case 3:
			CaricaGazetteer();
			break;
		case 4:
			CaricaAlternames();
			break;
		case 5:
			caricaAltro();
			break;
		}
	}

    public void createDB(boolean interactive) {
        checkNeededFiles();
        if (interactive) {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String[] step = {"OSM", "Rtree", "dati popolazione", "gazetteer", "alternames", "file restanti"};
            String input = null;
            for (int i = 0; i < step.length; i++) {
                while (true) {
                    System.out.println("caricare " + step[i] + "? [Y/n]");
                    try {
                        input = in.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (input.equalsIgnoreCase("y") || input.equals("")) {
                        createDB(i);
                        break;
                    } else if (input.equalsIgnoreCase("n")) {
                        break;
                    }
                }
            }

        } else {
            System.out.println("Caricamento OSM");
            caricaOSM();

            System.out.println("Fine caricamento OSM, inizio caricamento Rtree");
            CaricaRtree();

            System.out.println("Fine caricamento Rtree, inizio caricamento dati popolazione");
            CaricaPopulation();

            System.out.println("Fine caricamento dati popolazione, inizio caricamento Gazetteer");
            CaricaGazetteer();

            System.out.println("Fine caricamento Gsazetteer, inizio caricamento Alternames");
            CaricaAlternames();

            System.out.println("Fine caricamento Alternames, inizio caricamento dati restanti e ripulitura file temporanei");
            caricaAltro();

        }
        cleanUp();

        System.out.println("Caricamento terminato con successo");

    }

	private void caricaOSM(){
		
			String osmpath=path+Configuration.getNeededFiles()[Configuration.osmArrayFilePosition];
			//long Tempo1=System.currentTimeMillis();
			//System.out.println(path[0].substring(path[0].length()-3,path[0].length() ));
	    	if(osmpath.substring(osmpath.lastIndexOf('.')+1, osmpath.length()).equalsIgnoreCase("bz2")){
	    		BUnzip2 b=new BUnzip2(osmpath);
	    		Thread g=new Thread(b);
	    		g.setName("Scompatta");
	        	g.start();
	        	try{
	        	g.join();
	        	}
	        	catch (Exception e) {
					// TODO: handle exception
				}
	    	}

	    	if(osmpath.substring(osmpath.lastIndexOf('.')+1, osmpath.length()).equalsIgnoreCase("osm")){
	        	sistemaxml a=new sistemaxml();
	        	a.sistema(osmpath,osmpath+".out");

	    		ScanXML parsing=new ScanXML(osmpath+".out",0L,0L,true,true);
	    		
	    		
	    	}		
	    	//long Tempo2=System.currentTimeMillis();
		
	}
	
    private void CaricaRtree() {
        //TODO: fare attenzione: dopo alcune modifiche potrebbe darsi che l'rtree non venga sovrascritto se già esiste... testare

        RTreeLoad Rtree = new RTreeLoad();
        Rtree.carica();

    }

	private void CaricaPopulation(){
		
			
			readpopulation r=new readpopulation();
		    try{
		    	r.carica(path);
		    }
		    catch (Exception e) {
				// TODO: handle exception
			}
		
	}

	private void CaricaGazetteer(){
		
			
			readgazetteer r=new readgazetteer();
	        try{
	        	r.carica(path);
	        }
	        catch (Exception e) {
				// TODO: handle exception
			}
		
	}
	
	private void CaricaAlternames(){
		
			
			read_alternateNames r=new read_alternateNames();
	        try{
	        	r.carica(path);
			}catch (Exception e) {
				// TODO: handle exception
			}
		
	}
	
	private void caricaAltro(){
		try {
			admin1codeascii.carica(path);
			countryInfo.carica(path);
			featurecodes.carica(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

        private void cleanUp(){
            boolean outcome = FileUtils.deleteQuietly(new File(path+"node.db"));
            outcome &= FileUtils.deleteQuietly(new File(path+"node.lg"));
            outcome &= FileUtils.deleteQuietly(new File(path+"Way.db"));
            outcome &= FileUtils.deleteQuietly(new File(path+"Way.lg"));

            if (!outcome){
                System.err.println("errore nell'eliminare i file non necessari");
            }
        }
	
	public static int pid() {
		String id = ManagementFactory.getRuntimeMXBean().getName();
		String[] ids = id.split("@");
		return Integer.parseInt(ids[0]);
		}


		public static void listOpenFiles() throws IOException {
		int pid = pid();
		Runtime runtime = Runtime.getRuntime();
		Process process = runtime.exec("lsof -p " + pid);
		InputStream is = process.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String line;

		while ((line = br.readLine()) != null) {
		System.out.println(line);
		}
	}
}
