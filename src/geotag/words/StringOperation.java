/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag.words;

/**
 * Classe che contiene varie operazioni eseguite sulle stringhe
 * @author Giorgio Ghisalberti
 */
public class StringOperation {
    
    /**
     * Costruttore della classe
     */
    public StringOperation(){
        
    }

    /**
     * Metodo che esegue dei controlli sulle stringhe, per vedere se ci sono dei valori 
     * non corretti. Ad esempio nel caso in cui troviamo degli apici è necessario
     * aggiungere il doppio apice, per evitare incomprensioni nella scrittura
     * dei dati sul DB
     * @param source : stringa da sistemare
     * @return la stringa corretta
     */
    public static String convertString(String source) {
        StringBuffer sb = new StringBuffer();
       
        for (int i = 0; i < source.length(); i++) {
            if (source.charAt(i) == '\"') 
                source = source.replace('\"', ' ');
            else
                sb.append(source.charAt(i));
             
            if (source.charAt(i) == '\\' && source.charAt(i + 1) == '\'') {                  
                sb.deleteCharAt(i);
            }
            
            if (source.charAt(i) == '\'') {
                sb.append('\'');
            }
                        
        }
        return sb.toString();
    }
    
    /**
     * Metodo che riceve in ingresso una stringa e la formatta come se
     * fosse un nome geografico corretto.
     * Ovvero setta a maiuscolo la prima lettera di ogni parola che
     * forma la stringa e setta le altre a minuscolo
     * @param source
     * @return
     */
    public static String correctString(String source) {
        char firstChar = ' ';
        String first = "";
        String second = "";
        String stringCor = "";
        String sourceFinal = "";
        String[] words = null;
        String[] wordsApostrophe = null;
        
        source=source.replaceAll("(^ +)|( +$)", "");//remove leading and trailing whitespaces
        words = source.split(" ");
        
        for(int i = 0; i < words.length; i++){
            
            wordsApostrophe = words[i].split("\'");
            if(wordsApostrophe.length == 1){
                stringCor = adjust(words[i]);
            }
            else{
                if(sourceFinal.isEmpty())
                    first = adjust(wordsApostrophe[0]) + "\'";
                else
                    first = wordsApostrophe[0].toLowerCase() + "\'";
                second = adjust(wordsApostrophe[1]);
                stringCor = first + second;
            }
            
            if(i == 0)
                sourceFinal = stringCor;
            else
                sourceFinal = sourceFinal + " " + stringCor;
        }
     
        return sourceFinal;
    }
    
    
    public static String adjust(String str){
        char firstChar = ' ';
        String first = "";
        String second = "";
        String stringCor = "";
        
        if(str.charAt(0) >= 'a' && str.charAt(0) <= 'z')
            firstChar = (char) (str.charAt(0) - 'a' + 'A');
        else
            firstChar = str.charAt(0);

        first = Character.toString(firstChar);
        second = str.substring(1);
        second = second.toLowerCase();
        stringCor = first.concat(second);
        
        return stringCor;
    }
    
    
    public static String convertUrl(String url) {
        String finale = "";
        String[] str = null;
       
        str = url.split(" ");
        
        if(str.length > 0){
            for(int i = 0; i < str.length; i++){
                if(i == 0)
                    finale = str[i];
                else
                    finale = finale + "%20" + str[i];
            }
        }
        

        return finale;
    }
}
