package principale.ricercapernome;

public class mbr {
private double x1;
private double y1;
private double x2;
private double y2;
public mbr(double a,double b,double c,double d)
{
x1=a;
y1=b;
x2=c;
y2=d;
}
public mbr()
{

}
public void show()
{
	System.out.println(x1+" "+y1+" "+x2+" "+y2+" ");
}
public String memo()
{
	String a=new String();
	a=x2+" "+y2+" "+x1+" "+y1;
	return a;
}
public double getx1()
{
	return x1;
}
public double gety1()
{
	return y1;
}
public double getx2()
{
	return x2;
}
public double gety2()
{
	return y2;
}


}
