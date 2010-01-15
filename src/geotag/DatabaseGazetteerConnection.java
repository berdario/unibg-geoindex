/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * Classe che esegue l'accesso al DataBase "geoname"
 * @author Giorgio Ghisalberti
 */
public class DatabaseGazetteerConnection {
    //Variabili globali
    
    private Connection myConnection; // A connection to the database  
    Statement        statement;       // Our statement to run queries with
    DatabaseMetaData dbmd;      // This is basically info the driver delivers
                                // about the DB it just connected to. I use
                                // it to get the DB version to confirm the
                                // connection in this example.
    
    /** Creates a new instance of MyDBConnection */
    public DatabaseGazetteerConnection(String driverClassName, String userName, String password, String dbConnString) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
        
            init(driverClassName, userName, password, dbConnString);
        
    }
    
    /**
     * Metodo che esegue l'inizializzazione dei parametri necessari per la connessione al DataBase
     * @throws java.lang.ClassNotFoundException
     * @throws java.sql.SQLException
     * @throws java.lang.InstantiationException
     * @throws java.lang.IllegalAccessException
     */
    public void init(String driverClassName, String userName, String password, String dbConnString) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException{
        //try{
        Class.forName(driverClassName);
            
        myConnection = DriverManager.getConnection(dbConnString, userName, password); //connect to the db
        dbmd = myConnection.getMetaData(); //get MetaData to confirm connection

        statement = myConnection.createStatement(); //create a statement that we can use later

    }

    /**
     * Metodo che restituisce lo Statment, necessario per eseguire le query sul DB
     * @return
     */
    public Statement getStatement() {
        return statement;
    }
    
    /**
     * Restituisce la connessione al DataBase per permettere alle altre classi di accedervi
     * @return 
     */
    public Connection getMyConnection(){
        return myConnection;
    }
    
    
    public void close(ResultSet rs){   
        if(rs !=null){
            try{
               rs.close();
            }
            catch(Exception e){}        
        }
    }
    
    
    public void close(java.sql.Statement stmt){
        
        if(stmt !=null){
            try{
               stmt.close();
            }
            catch(Exception e){}
        
        }
    }
    
    
    public void destroy(){
        if(myConnection !=null){

             try{
                   System.out.println("Disconnection to " + dbmd.getDatabaseProductName() + " " +
                       dbmd.getDatabaseProductVersion() + " successful.\n");
                   myConnection.close();
                }
                catch(Exception e){}


        }
    }

    

}
