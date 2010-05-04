/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;
import jdbm.RecordManagerOptions;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Dario Bertini <berdario@gmail.com>
 */
public class Configuration {
    private static String path = null;
    private static String dbpath = null;
    private static String slash = null;
    private static String cachepath = null;
    private static String language = null;
    private static String lucenepath = null;
    private static String indexpath = null;
    private static String outputpath = null;

    private static String[] indexDirPaths = null;

    private ArrayList<File> indexDirs;

    static File requiredFiles[];
    static String neededFiles[];

    final public static int osmArrayFilePosition = 10;

    private static Properties defaultRecordManagerOptions = new Properties();

    PropertiesConfiguration config;

    public Configuration(String configfile) {
        slash = System.getProperty("file.separator");
        String[] defaultPaths = getDefaultPaths();

        if (configfile == null) {
            // set default config path
            configfile = defaultPaths[0];
        }

        File cfgfile = new File(configfile);
        if (!cfgfile.exists()) {
            throw new ConfigFileNotFoundException();
        }
        
        try {
            config = new PropertiesConfiguration(configfile);

            path = config.getString("basepath",defaultPaths[1]);
            dbpath = path + config.getString("dbdirectory") + slash;
            cachepath = config.getString("cachepath",defaultPaths[3]);
            language = path + "stopWords" + slash + config.getString("languagefile");

            indexDirPaths = config.getStringArray("indexdirs");

            String[] requiredFileNames = config.getStringArray("requiredfiles");
            String[] requiredDbFileNames = config.getStringArray("requireddbfiles");
            File[] innerRequiredFiles = new File[requiredDbFileNames.length + requiredFileNames.length];

            int i = 0;
            for (String rf : requiredFileNames) {
                innerRequiredFiles[i] = new File(path + rf);
                i++;
            }

            for (String rf : requiredDbFileNames) {
                innerRequiredFiles[i] = new File(dbpath + rf);
                i++;
            }
            requiredFiles = innerRequiredFiles;
            neededFiles = config.getStringArray("neededfiles");
            lucenepath = config.getString("luceneindex_directory");
            indexpath = config.getString("geoindex_directory");
            outputpath = path + config.getString("outputdir") + slash;

        } catch (ConfigurationException e) {
            e.printStackTrace();
        }

        defaultRecordManagerOptions.setProperty(RecordManagerOptions.DISABLE_TRANSACTIONS, "");

    }

    public static void createConfiguration(String configfile, String path, String cachepath) {
        String input = null, inputCachepath = "";

        File cfgfile = new File(configfile);
        File cfgpath = cfgfile.getParentFile();
        if (!cfgpath.exists()) {
            cfgpath.mkdirs();
        }
        
        try {
            System.out.println("Missing configuration file, do you want to create one? [Y/n]");
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                input = in.readLine();
                if (input.equalsIgnoreCase("y") || input.equals("")) {
                    System.out.println("Please insert data path [" + path + "]:");
                    input = in.readLine();
                    if (!input.equals("")) {
                        cachepath = input + "cache/";
                        System.out.println("Please insert cache path [" + cachepath + "]:");
                        inputCachepath = in.readLine();
                    }
                    break;
                } else if (input.equalsIgnoreCase("n")) {
                    System.exit(0);
                }
            }

            cfgfile.createNewFile();

            PropertiesConfiguration config = new PropertiesConfiguration(cfgfile);
            
            if (!input.equals("")) {
                path = input;
                if (!inputCachepath.equals("")){
                    cachepath = inputCachepath;
                }
            }

            File basedir = new File(path);
            File[] basedirFiles = basedir.listFiles();

            ArrayList<File> maps = new ArrayList<File>();
            for (File f : basedirFiles){
                if (f.getName().endsWith(".osm.bz2")){
                    maps.add(f);
                }
            }

            File map = new File("dfjsbjsdhvkjnbsjhcajkdfnvhakfjgnvfhjb"); //mi serve un oggetto file "valido", non posso usare null

            if (maps.size() == 0) {
                System.out.println("manca un file OpenStreetMap, inserirne uno in " + path);
                FileUtils.forceDelete(cfgfile);
                System.exit(1);
            } else if (maps.size() > 1) {
                input = null;
                while (!map.exists() || !map.getName().endsWith(".osm.bz2")) {
                    if (input != null) {
                        System.out.println("-nome file fornito non valido-");
                    }
                    System.out.println("trovati i seguenti file OpenStreetMap:");
                    for (File m : maps){
                        System.out.println(m.getName());
                    }
                    System.out.println("\nsceglierne uno [" + maps.get(0).getName() + "]:");
                    input = in.readLine();
                    if (!input.equals("")) {
                        map = new File(path + input);
                    } else {
                        map = maps.get(0);
                    }
                }
            } else {
                map = maps.get(0);
            }

            String fileNames[] = {"datiscritti.dat", "datiscritti.idx", "albero_Btree_osm.db",
            "albero_Btree_population.db", "albero_Btree_population2.db",
            "albero_Btree_Gazetteer.db", "albero_Btree_Intermedio.db",
            "albero_alternatenames.db", "albero_alternatenamesId.db",
            "albero_admin1codeascii.db", "albero_countryInfo.db",
            "albero_featurecodes.db"};

            String neededFileNames[] = {"stopWords", "stopWords" + slash + "englishSW.txt", "admin1CodesASCII.txt",
        "allCountries.zip", "alternateNames.txt", "iso-languagecodes.txt", "countryInfo.txt",
        "featureCodes.txt", "geoStopwords.txt", "IT.txt", map.getName(), "street.txt"};
            //Attenzione: la posizione di map.getName() dev'essere rispecchiata in osmArrayFilePosition


            config.setProperty("basepath", path);
            config.setProperty("cachepath", cachepath);

            config.setProperty("dbdirectory", "db");
            config.setProperty("luceneindex_directory", path + "index" + slash + "contentIndex" + slash);
            config.setProperty("geoindex_directory", path + "index" + slash);
            config.setProperty("requiredfiles", "coordinate.txt");
            config.setProperty("requireddbfiles", fileNames);
            config.setProperty("neededfiles", neededFileNames);
            config.setProperty("languagefile", "englishSW.txt");
            config.setProperty("outputdir", "output");

            config.save();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * @return Array with 0: config file path, 1: default base path, 2: default db path,
     * 3: default cache path
     */
    public static String[] getDefaultPaths(){
        String configfile;
        String os = System.getProperty("os.name");
        String homepath = System.getProperty("user.home");
        slash = System.getProperty("file.separator");
        if (os.startsWith("Linux")) {
            path = System.getenv("XDG_DATA_HOME");
            if (path == null) {
                path = homepath + "/.local/share/";
            }
            path += "geosearch/";

            configfile = System.getenv("XDG_CONFIG_HOME");
            if (configfile == null) {
                configfile = homepath + "/.config/";
            }
            configfile += "geosearch/config";

            cachepath = System.getenv("XDG_CACHE_HOME");
            if (cachepath == null) {
                cachepath = homepath + "/.cache/";
            }
            cachepath += "geosearch/";

        } else if (os.startsWith("Windows")) {
            path = System.getenv("APPDATA") + slash + "geosearch" + slash;
            configfile = path + "config";
            cachepath = path + "cache" + slash;
        } else if (os.startsWith("Mac")) {
            path = homepath + "/Library/Application Support/geosearch/";
            configfile = path + "config";
            cachepath = path + "cache/";
        } else {
            path = System.getProperty("user.dir") + "geosearch" + slash;
            configfile = path + "config";
            cachepath = path + "cache" + slash;
        }
        dbpath = path + "db" + slash;
        
        String[] result = {configfile, path, dbpath, cachepath};
        
        return result;
    }

    public ArrayList<File> updateIndexConfig(String inputpath) {
        try {
            ArrayList<File> innerIndexDirs = getIndexedDirs();

            if (inputpath != null) {
                File inputfile = new File(inputpath);
                if (!innerIndexDirs.contains(inputfile)) {
                    innerIndexDirs.add(new File(inputpath));
                    config.setProperty("indexdirs", innerIndexDirs);
                    config.save();
                }
            }

            indexDirs = innerIndexDirs;
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        return indexDirs;
    }

    private ArrayList<File> getIndexedDirs() {

        ArrayList<File> innerIndexDirs = new ArrayList<File>();

        for (String indf : indexDirPaths) {
            innerIndexDirs.add(new File(indf));
        }
        return innerIndexDirs;
    }

    public static String getPath(){
        return path;
    }

    public static String getDbPath(){
        return dbpath;
    }

    public static String getCachePath(){
        return cachepath;
    }

    public static String getSeparator(){
        return slash;
    }

    public static String getSWLanguage(){
        return language;
    }

    public static File[] getRequiredFiles(){
        return requiredFiles;
    }

    public static String[] getNeededFiles(){
        return neededFiles;
    }

    public static String getLucenePath(){
        return lucenepath;
    }

    public static String getIndexPath(){
        return indexpath;
    }

    public static String[] getIndexedDirsNames(){
        return indexDirPaths;
    }

    public static String getOutputPath(){
        return outputpath;
    }

    public static Properties getDefaultRecordManagerOptions() {
        return defaultRecordManagerOptions;
    }

    class ConfigFileNotFoundException extends RuntimeException{
    }

}
