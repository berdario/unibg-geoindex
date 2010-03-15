/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag;

import java.io.File;
import java.util.ArrayList;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

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

    PropertiesConfiguration config;
    private ArrayList<File> indexDirs;

    public Configuration(String configfile) {
        slash = System.getProperty("file.separator");

        if (configfile == null){
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
                configfile = System.getenv("APPDATA") + slash + "geosearch" + slash + "config";
            } else if (os.startsWith("Mac")) {
                configfile = homepath + "/Library/Application Support/geosearch/" + "config";
            } else {
                configfile = System.getProperty("user.dir") + "geosearch" + slash + "config";
            }
        }

        File cfgfile = new File(configfile);
        if (!cfgfile.exists()) {
            System.out.println("Missing configuration file, please run Dbcreator first");
            throw new ConfigFileNotFoundException();
        }
        
        try {
            config = new PropertiesConfiguration(configfile);
            path = config.getString("basepath");
            dbpath = path + config.getString("dbdirectory") + slash;
            cachepath = config.getString("cachepath");
            language = path + "stopWords" + slash + config.getString("languagefile");
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
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

        String[] indexDirPaths = config.getStringArray("indexdirs");
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

    class ConfigFileNotFoundException extends RuntimeException{
    }

}
