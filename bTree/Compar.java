package bTree;

import java.util.Comparator;


public class Compar implements Comparator {
  
   
   
   public int compare(Object o1, Object o2) {
     
	   String a=o1.toString();
	   String b=o2.toString();
	   
	   int d=1;
	   
	   if(a.compareToIgnoreCase(b)>0)
	   {
		   return d;
	   }
	   int c=0;
	   if(a.compareToIgnoreCase(b)==0)
	   {
		   return c;
	   }
	   int r=-1;
	   
	   return r;
	         
   }
   public boolean equals(Object obj)  {
	  
	   return true;
   }
}
