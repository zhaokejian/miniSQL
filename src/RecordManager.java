import structures.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

public class RecordManager {
	BufferManager bufferManager = new BufferManager();

    @Override
    protected void finalize() throws Throwable {
        bufferManager.finalize();
        super.finalize();
    }
    
    public ArrayList<Hashtable<String,String>> select_index(Table table, ArrayList<Condition> conditions, ArrayList<Integer> fileofs) throws IOException{
    	bufferManager.scanTable(table);
    	ArrayList<Hashtable<String,String>> res = new ArrayList<>();
    	int bufferIndex = 0;
    	
    	
    	for(Integer offset : fileofs){
    		int x = 0, y = 0;
    		x = (int) Math.floor(offset / Buffer.BLOCKSIZE);
    		y = offset % Buffer.BLOCKSIZE;
    		
    		while(bufferManager.bufferlist.get(bufferIndex).getBlockNum()!=x &&!bufferManager.bufferlist.get(bufferIndex).fileName.equals(table.getName() + ".table") && bufferIndex < bufferManager.bufferlist.size())	
        		bufferIndex++;
    		
    		byte visibleBit = bufferManager.bufferlist.get(bufferIndex).value[y];
    		Hashtable<String,String> attrTable = new Hashtable<>();//��select����tuple�õ�hashtable
    		int valid = 1;
    		
    		
 			if(visibleBit == 1){
  				int of = 1;//����visiblebit
    			//��attributeΪ��λɨ	
    			for(int j = 0;j < table.getAttrNum(); j++){
    				Attribute tmpAttr = table.getAttributes().get(j);
    				String tmpName = tmpAttr.getName();
    				int tmpLength = tmpAttr.getLength();
    				DATA_TYPE tmpType = tmpAttr.getType();
    						
    				byte[] tmpValue = new byte[tmpLength];
    						
    				System.arraycopy(bufferManager.bufferlist.get(bufferIndex+x).value, y + of, tmpValue, 0, tmpLength);//������value������
        			String tmpValueStr = toString(tmpValue, tmpType, tmpLength).trim();//ͳһ���string
        			attrTable.put(tmpName, tmpValueStr);//����������Ϣ
    				of += tmpLength;
    				}		
    			if(conditions != null)//�������޶�
					for(Condition condition : conditions){
						if(!IsSatisfy(condition, attrTable.get(condition.getAttrName()))){
							valid = 0;//�����Ͼ�break
							break;
						}
					}
    			if(valid == 1)//����tuple����Ҫ������
    				res.add(attrTable);
 			}	
    }	
    return res;
}
    
    //select���
    public ArrayList<Hashtable<String,String>> select(Table table,ArrayList<Condition> conditions) throws IOException{
    	bufferManager.scanTable(table);
    	int readLength = table.getTupleLength() + 1;
    	
    	ArrayList<Hashtable<String,String>> res = new ArrayList<>();
    	int x=-1, flag=0;
    	//ɨ���е�buffer
    	for(int bufferIndex = 0; bufferIndex < bufferManager.bufferlist.size(); bufferIndex++){
    		//���buffer������Ҫ��table
    		if(bufferManager.bufferlist.get(bufferIndex).fileName.equals(table.getName() + ".table")){
    			if(flag == 0 & x == -1) {
    				x = bufferIndex;//��¼��table�ĵ�һ����λ��
    				flag = 1;
    			}
    			//����Ҫ��tuple
    			for(int blockIndex = 0; blockIndex <= Buffer.BLOCKSIZE - readLength; blockIndex += readLength){
    				//��ȡvisiblebit
    				byte visibleBit = bufferManager.bufferlist.get(bufferIndex).value[blockIndex];
    				Hashtable<String,String> attrTable = new Hashtable<>();//��select����tuple�õ�hashtable
    				attrTable.put("fileofs", Integer.toString((bufferManager.bufferlist.get(bufferIndex).blockNum - x) * Buffer.BLOCKSIZE + blockIndex));
    				
    				if(visibleBit == 1){
    					int offset = 1;//����visiblebit
    					int valid = 1;
    					//��attributeΪ��λɨ	
    					for(int i = 0;i < table.getAttrNum(); i++){
    						Attribute tmpAttr = table.getAttributes().get(i);
    						String tmpName = tmpAttr.getName();
    						int tmpLength = tmpAttr.getLength();
    						DATA_TYPE tmpType = tmpAttr.getType();
    						
    						byte[] tmpValue = new byte[tmpLength];
    						
    						for(Attribute a : table.getAttributes()){
    							if(a.getName().equals(tmpAttr.getName())){
    								System.arraycopy(bufferManager.bufferlist.get(bufferIndex).value, blockIndex + offset, tmpValue, 0, tmpLength);//������value������
        							String tmpValueStr = toString(tmpValue, tmpType, tmpLength).trim();//ͳһ���string
        							attrTable.put(tmpName, tmpValueStr);//����������Ϣ
        							break;
    							}
    						}
    						
    						offset += tmpLength;
    						}
    					if(conditions != null)//�������޶�
							for(Condition condition : conditions){
								if(!IsSatisfy(condition, attrTable.get(condition.getAttrName()))){
									valid = 0;//�����Ͼ�break
									break;
								}
							}
    					if(valid == 1)//����tuple����Ҫ������
							res.add(attrTable);
    				}
    			}
    		}
    	}
    	return res;    	
    }
    
    public int insert(Table table, String values) throws IOException{
		bufferManager.scanTable(table);
		
		InsPos insPos;
		String[] value = values.split(",");
		int dataLength = table.getTupleLength() + 1;
		byte[] insData = new byte[dataLength];
		for(int i = 1; i < insData.length; i++)
			insData[i] = -1;
		insData[0] = 1;//visiblebit
		
		for(int i = 0; i < value.length; i++)
    		value[i] = value[i].trim();//��ֹ�пո������
		
		if(value.length != table.getAttrNum())
			return -1;//��������
		
		int offset = 1;
    	for(int i = 0; i < table.getAttrNum(); i++ ){
    		DATA_TYPE type = table.getAttributes().get(i).getType();
    		if(table.getAttributes().get(i).isPrimKey()){
    			if( table.primaryKeys.contains(value[i]))
    				return -2;//primary key duplicate
    			else
    				table.primaryKeys.add(value[i]);
    		}
    		
    		if(table.getAttributes().get(i).isUnique()){
    			if(table.uniques.get(table.getAttributes().get(i).getName()).contains(value[i]))
    				return -3; //unique duplicate
    			else table.uniques.get(table.getAttributes().get(i).getName()).add(value[i]);
    		}
    			
    		switch(type){
    		case MYINT:
    			int tmpint = Integer.parseInt(value[i]);
    			System.arraycopy(ItoB(tmpint), 0, insData, offset, 4);
    			offset += 4;
    			break;
    		case MYFLOAT:
    			float tmpfloat = Float.parseFloat(value[i]);
    			System.arraycopy(FtoB(tmpfloat), 0, insData, offset, 4);
    			offset += 4;
    			break;
    		case MYCHAR:
    			if(value[i].charAt(0) != '\'' || value[i].charAt(value[i].length()-1) != '\'')
    				return -1;
    			value[i] = value[i].substring(1, value[i].length()-1);
    			byte[] tmpByte = value[i].getBytes();
    			System.arraycopy(tmpByte, 0, insData, offset, value[i].length());
    			offset += table.getAttributes().get(i).getLength();
    			break;
    		default:break;
    		}		
    		if(table.getAttributes().get(i).isUnique()){
    			if(table.uniques.get(table.getAttributes().get(i).getName()).contains(value[i]))
    				return -3; //unique duplicate
    			else table.uniques.get(table.getAttributes().get(i).getName()).add(value[i]);
    		}
    	}
    	
    	
    	
    	insPos = bufferManager.getInsertPosition(table);
    	for(int i = 0; i < dataLength; i++){
    		bufferManager.bufferlist.get(insPos.getBufferNum()).value[insPos.getBlockOff() + i] = insData[i];
    	}
    	table.addRecordNum();	
    	return bufferManager.bufferlist.get(insPos.getBufferNum()).blockNum*Buffer.BLOCKSIZE + insPos.getBlockOff();	
    }
    
    public int delete(Table table, ArrayList<Condition> conditions) throws IOException{
    	bufferManager.scanTable(table);
    	int readLength = table.getTupleLength() + 1;
    	int cnt = 0;
    	
    	//ɨ����buffer
    	for(int bufferIndex = 0; bufferIndex <bufferManager.bufferlist.size(); bufferIndex++){
    		//���buffer������Ҫ��table
    		if(bufferManager.bufferlist.get(bufferIndex).fileName.equals(table.getName() + ".table")){
    			//����Ҫ��tuple
    			for(int blockIndex = 0; blockIndex <= Buffer.BLOCKSIZE - readLength; blockIndex += readLength){
    				//��ȡvisiblebit
    				byte visibleBit = bufferManager.bufferlist.get(bufferIndex).value[blockIndex];
    				Hashtable<String,String> attrTable = new Hashtable<>();//��Ҫɾ����tuple�õ�hashtable
    				
    				if(visibleBit == 1){
    					int offset = 1;//����visiblebit
    					int valid = 1;
    					//��attributeΪ��λɨ	
    					for(int i = 0;i < table.getAttrNum(); i++){
    						Attribute tmpAttr = table.getAttributes().get(i);
    						String tmpName = tmpAttr.getName();
    						int tmpLength = tmpAttr.getLength();
    						DATA_TYPE tmpType = tmpAttr.getType();
    						byte[] tmpValue = new byte[tmpLength];
    	
    						System.arraycopy(bufferManager.bufferlist.get(bufferIndex).value, blockIndex + offset, tmpValue, 0, tmpLength);//������value������
    						String tmpValueStr = toString(tmpValue, tmpType, tmpLength).trim();//ͳһ���string
    						attrTable.put(tmpName, tmpValueStr);//����������Ϣ
    						
    						offset += tmpLength;
    						}
    					if(conditions != null)//�������޶�
							for(Condition condition : conditions){
								if(!IsSatisfy(condition, attrTable.get(condition.getAttrName()))){
									valid = 0;//�����Ͼ�break
									break;
								}
							}
    					if(valid == 1){//����tupleɾ��
    						bufferManager.bufferlist.get(bufferIndex).value[blockIndex] = Buffer.EMPTY;
    						cnt++;
    					}
    				}
    			}
    		}
    	}
    	return cnt;
    }
    
    public boolean droptable(String tableName){
    	return bufferManager.freeTable(tableName + ".table");
    }
    
    public boolean IsSatisfy(Condition condition, String value){
    	String conValue = condition.getValue();
    	
    	switch(condition.getRelationType()){
    	case EQUAL:
    		return value.equals(conValue);
    	case GREATER:
    		return (value.compareTo(conValue)>0);
    	case GREATER_EQUAL:
    		return (value.compareTo(conValue)>=0);
    	case NOT_EQUAL:
    		return !value.equals(conValue);
    	case SMALLER:
    		return (value.compareTo(conValue)<0);
    	case SMALLER_EQUAL:
    		return (value.compareTo(conValue)<=0);
    	default: return true;	
    	}
    }
    
    public String toString(byte[] value, DATA_TYPE type, int length) {
        String str;
        switch (type) {
            case MYINT:
                return Integer.toString(BtoI(value));
            case MYFLOAT:
                return Float.toString(BtoF(value));
            case MYCHAR:
                for (int i=0; i<value.length; i++) if (value[i] == -1) value[i] = 0;
                str =  new String(value);
                for (int i=0; i<value.length; i++) if (value[i] == 0) value[i] = -1;
                return str;
            default:return "";
        }
    }
    
    public byte[] ItoB(int i) {
        byte[] res = new byte[4];
        res[0] = (byte)((i>>24) & 0xFF);
        res[1] = (byte)((i>>16) & 0xFF);
        res[2] = (byte)((i>>8) & 0xFF);
        res[3] = (byte)(i & 0xFF);
        return res;
    }

    public byte[] FtoB(float f) {
        int num = Float.floatToIntBits(f);
        return ItoB(num);
    }

    public int BtoI(byte[] bytes) {
        int res;
        res = (bytes[0]<<24) | (bytes[1]<<16) | (bytes[2]<<8) | bytes[3];
        return res;
    }

    public float BtoF(byte[] bytes) {
        return Float.intBitsToFloat(BtoI(bytes));
    }
}


