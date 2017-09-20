package structures;

import java.io.Serializable;

public class Index implements Serializable{
	
	private static final long serialVersionUID = 19950436L;
	
	String tableName;
	Attribute srcAttr;
	
	public Index(){
		tableName = "";
		srcAttr = null;
	}
	
	public String GettableName(){
		return tableName;
	}
	public Attribute GetsrcAttr(){
		return srcAttr;
	}
	
	public void SettableName(String m_tableName){
		this.tableName = m_tableName;
	}
	public void SetsrcAttr(Attribute m_attr){
		this.srcAttr = m_attr;
	}
	
}
