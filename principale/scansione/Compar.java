package principale.scansione;

import java.util.Comparator;
public class Compar implements Comparator {
  
   long a,b;
   
   public int compare(Object o1, Object o2) {
     
	   
	   a=Long.parseLong(o1.toString());
	   b=Long.parseLong(o2.toString());
	   
	   if(a>b)
	   {
		   return 1;
	   }
	   
	   if(a==b)
	   {
		   return 0;
	   }
	   
	   return -1;
	         
   }
   public boolean equals(Object obj)  {
	  
	   return true;
   }
}
