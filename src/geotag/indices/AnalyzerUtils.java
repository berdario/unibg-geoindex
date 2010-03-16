/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.indices;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Vector;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

/**
 * Classe che contiene tutti i metodi per l'analisi del testo dei documenti.
 * @author Giorgio Ghisalberti
 */
public class AnalyzerUtils {
    
    /**
     * Costruttore
     */
    public AnalyzerUtils(){
        
    }
    
    /**
     * Restitiusce i vari token che costituiscono il testo ricevuto come input,
     * basandosi sulle regole dell'analyzer
     * @param analyzer : tipo di analizzatore 
     * @param text : testo da analizzare
     * @return l'elenco dei token che costituiscono il testo
     * @throws java.io.IOException
     */
    public static Token[] tokensFromAnalysis (Analyzer analyzer, String text) throws IOException {
        TokenStream stream = analyzer.tokenStream("text", new StringReader(text));
        ArrayList tokenList = new ArrayList();
        
        while(true){
            Token token = stream.next();
            if(token == null)
                break;
            //System.out.println("[" + token + "]"); 
            tokenList.add(token);
        }
        
        return (Token[]) tokenList.toArray(new Token[0]);
    }
    
    /**
     * Metodo che stampa a video i token
     * @param analyzer : tipo di analizzatore
     * @param text : testo da analizzare
     * @throws java.io.IOException
     */
    public static void displayTokens(Analyzer analyzer, String text) throws IOException{
        Token[] tokens = tokensFromAnalysis(analyzer, text);
        
        for(int i = 0; i < tokens.length; i++){
            Token token = tokens[i];
            System.out.println("[" + token.termText() + "]");   // [ nome del token ]
        }
    }
    
    /**
     * Metodo che stampa a video i token comprensivi delle info aggiuntive, ovvero:
     * numero della posizione del carattere iniziale e finale, e tipo di parametro
     * @param analyzer : tipo di analizzatore
     * @param text : testo da analizzare
     * @throws java.io.IOException
     */
    public static void displayTokensWithFullDetails(Analyzer analyzer, String text) throws IOException{
        Token[] tokens = tokensFromAnalysis(analyzer, text);
                
        int position = 0;
        
        for(int i = 0; i < tokens.length; i++){
            Token token = tokens[i];
            int increment = token.getPositionIncrement();
            
            if(increment > 0)
                position = position + increment;
            
            System.out.println("[" + token.termText() + ":"     // nome del token
                    + token.startOffset() + "->"                // num posizione del carattere iniziale
                    + token.endOffset() + ":"                   // num posizione  del carattere finale
                    + token.type() + "] ");                     // tipo di parametro
                
        }
    }
    
    /**
     * Metodo ceh stampa a video i token e la rispettiva posizione
     * @param analyzer : tipo di analizzatore
     * @param text : testo da analizzare
     * @throws java.io.IOException
     */
    public static void displayTokensWithPositions(Analyzer analyzer, String text) throws IOException{
        Token[] tokens = tokensFromAnalysis(analyzer, text);
        int position = 0;
        
        for(int i = 0; i < tokens.length; i++){
            Token token = tokens[i];
            int increment = token.getPositionIncrement();
            
            if(increment > 0){
                position = position + increment;
                System.out.println();
                System.out.println(position + ": ");
            }
            
            System.out.println("[" + token.termText() + "]");                    // tipo di parametro
                
        }
    }
    
    /**
     * Restituisce un vettore di Stringhe contenente i nomi di tutti i token
     * estratti dal testo preso in input, sulla base delle regole imposte
     * dall'Analyzer.
     * @param analyzer : tipo di analizzatore
     * @param text : testo da analizzare
     * @return i token reperiti
     * @throws java.io.IOException
     */
    public ArrayList<String> getNameTokens(Analyzer analyzer, String text) throws IOException{
        Token[] tokens = tokensFromAnalysis(analyzer, text);
        ArrayList<String> tokensName = new ArrayList<String>();
        
        for(int i = 0; i < tokens.length; i++){
            Token token = tokens[i];
            tokensName.add(token.termText());           
        }
        
        return tokensName;
    }   
    
    /**
     * Metodo che reperisce la posizione di tutti i token e restitiusce un vettore
     * di interi contenente tutte le posizioni.
     * @param analyzer : tipo di analizzatore
     * @param text : testo da analizzare
     * @param position 
     * @return la posizione dei token
     * @throws java.io.IOException
     */
    public int[] getPositionTokens(Analyzer analyzer, String text, int position) throws IOException{
        Token[] tokens = tokensFromAnalysis(analyzer, text);       
        int[] tokensPosition = new int[tokens.length];        
        
        for(int i = 0; i < tokens.length; i++){
            Token token = tokens[i];
            int increment = token.getPositionIncrement();
            
            if(increment > 0){
                position = position + increment;
                tokensPosition[i] = position;
            }                
        }
        
        return tokensPosition;
    }  
    
    /**
     * Metodo che reperisce la frequenza di tutti i token
     * @param analyzer : tipo di analizzatore
     * @param text : testo da analizzare
     * @param frequency 
     * @return l'elenco delle frequenze
     * @throws java.io.IOException
     */
    public int[] getFrequencyTokens(Analyzer analyzer, String text, int frequency) throws IOException{
        Token[] tokens = tokensFromAnalysis(analyzer, text);       
        int[] tokensFrequency = new int[tokens.length];        
        
        Token[] tokensApp = tokens;
        
        for(int i = 0; i < tokens.length; i++){
            Token token = tokens[i];
            for(int j = 0; j < tokensApp.length; j++){
                Token tokenApp = tokensApp[j];
                
                if(token.termText().equals(tokenApp.termText()))
                    tokensFrequency[i]++;
                
            }        
        }
        
        return tokensFrequency;
    } 



}
