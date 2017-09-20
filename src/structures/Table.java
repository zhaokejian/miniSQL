package structures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

public class Table implements Serializable{

    private static final long serialVersionUID = 19940713L;

    String name;
    int blockNum;
    int recordNum;
    int attrNum;
    int tupleLength;
    String primKey;
    ArrayList<Attribute> attributes;
    public Hashtable<String,ArrayList<String>> uniques; 
    public ArrayList<String> primaryKeys; 
    
    public Table() {
        blockNum = 0;
        recordNum = 0;
        attrNum = 0;
        tupleLength = 0;
        primaryKeys = new ArrayList<String>();
        uniques = new Hashtable<String,ArrayList<String>>();
    }

    public Attribute getAttributeWithName(String name){
    	Attribute a = new Attribute();
    	for(Attribute attr: attributes){
    		if(attr.name.equals(name))
    			a = attr;
    	}
    	return a;
    }
    
    public ArrayList<Attribute> getAttributes() {
        return attributes;
    }

    public int getAttrNum() {
        return attributes.size();
    }

    public int getBlockNum() {
        return blockNum;
    }

    public int getRecordNum() {
        return recordNum;
    }

    public int getTupleLength() {
        return tupleLength;
    }

    public String getName() {
        return name;
    }

    public String getPrimKey() {
        return primKey;
    }

    public void setBlockNum(int blockNum) {
        this.blockNum = blockNum;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAttributes(ArrayList<Attribute> attributes) {
        this.attributes = attributes;
    }

    public void setAttrNum(int attrNum) {
        this.attrNum = attrNum;
    }

    public void setPrimKey(String primKey) {
        this.primKey = primKey;
    }

    public void setRecordNum(int recordNum) {
        this.recordNum = recordNum;
    }

    public void setTupleLength(int tupleLength) {
        this.tupleLength = tupleLength;
    }

    public void addBlockNum() {
        this.blockNum++;
    }

    public void addRecordNum() {
        this.recordNum++;
    }
    
    public void subRecordNum() {
        this.recordNum--;
    }
    
}
