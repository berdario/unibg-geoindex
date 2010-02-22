package dbcreator.scansione;


public class Serializ implements jdbm.helper.Serializer{
	static final long  serialVersionUID=42L;
	String a;
	Object ob;
	public java.lang.Object deserialize(byte[] serialized){
		 //Deserialize the content of an object from a byte array.
		a=new String(serialized);
		ob=(Object)a;
		return ob;
	 }
     
public byte[] 	serialize(java.lang.Object obj){
	//Serialize the content of an object into a byte array.
	
return obj.toString().getBytes();
}
     
}
