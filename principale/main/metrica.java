package principale.main;
import java.lang.reflect.Array;

public class metrica {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String s="comune dalmine";
		String t="dalmine";
		double distance=metric(s,t);
		System.out.print(distance);
		
		
	}
	public static double metric(String s,String t){
		int h;
		s=s.toLowerCase();
		t=t.toLowerCase();
		if( s.length() < t.length() )
			h=s.length();
		else
			h=t.length();
			
		h=h/2;
		String cs=common_char(s,t,h);
		String ct=common_char(t,s,h);
		double distance;
		double T;
		if(cs.length()==0 || ct.length()==0)
			distance=0;
		else
		{
			T=traspositions(cs,ct);
			distance=(( cs.length()/(double)s.length() + ct.length()/(double)t.length() + (cs.length() - T)/(double)s.length() ))/3.0 ;
		}
		return distance;
	}
	private static double traspositions(String x, String y) {
		double tra=0;
		for(int i=0;i<x.length();i++)
			if(x.charAt(i)!=y.charAt(i))
				tra++;
		tra=tra/2;
		return tra;
	}
	/*
	private static String common_char(String x,String y,int h){
		char[] copy=y.toCharArray();
		char Char;
		String common = "";
		boolean founded;
		int j;
		for(int i=0;i<x.length();i++){
				Char=x.charAt(i);
				founded=false;
				for(j=Math.max(0,i-h);j<Math.min(i+h, copy.length);j++)
				{
				
					if(founded==true)
						j=Math.min(i+h, copy.length)+1;
					else
						if(copy[j]==Char)
						{
							founded=true;
							common=common+Char;
							copy[j]='�';
						}
				}
				
		}
	return common;
	}*/

	private static String common_char(String PrimaStringa, String SecondaStringa,int H)
    {
        String common = ""; 
        Object[] copy = new Object[SecondaStringa.length()];
        for(int k= 0; k<SecondaStringa.length(); k++)
            copy[k] = SecondaStringa.charAt(k);
        for (int i=0; i<PrimaStringa.length(); i++) {
            char carattere = PrimaStringa.charAt(i);
            boolean trovato = false;
            for (int j=Math.max(0,i-H); !trovato && j<=Math.min(i+H,copy.length-1); j++) {
                if (copy[j]==(Object) carattere) {
                    trovato = true;
                    common = common + carattere;
                    Array.set(copy,j,null);
                }
            }
            
        }
        
        return common;
    }

}