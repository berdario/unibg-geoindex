package dbcreator.ricercapernome;

public class Serializ implements jdbm.helper.Serializer{
	static final long  serialVersionUID=42L;
	public java.lang.Object deserialize(byte[] serialized){
		 //Deserialize the content of an object from a byte array.
		String a=new String(serialized);
		Object ob;
		ob=(Object)a;
		return ob;
	 }
     
public byte[] 	serialize(java.lang.Object obj){
	//Serialize the content of an object into a byte array.
	
return obj.toString().getBytes();
}
     
}
