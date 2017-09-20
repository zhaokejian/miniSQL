import structures.*;

import java.io.*;
import java.util.ArrayList;

public class CatalogManager {
	ArrayList<Table> tableList = new ArrayList<Table>();
	
//	public ~CatalogManager(){
//		
//	}
	
    public CatalogManager() throws IOException, ClassNotFoundException {
        tableList.clear();
        Table table;
        File file = new File("./catalog/tableinfo.info");
        FileInputStream fis = new FileInputStream(file);

        try {
            ObjectInputStream ois = new ObjectInputStream(fis);
            while ((table = (Table)ois.readObject()) != null) {
                tableList.add(table);
            }
            ois.close();
        } catch (EOFException e) {
//            System.out.println("Tableinfo.info is empty!");
//            tableList.add(new Table());
        }

        fis.close();
    }
	
    @Override
    protected void finalize() throws Throwable {
        File file = new File("./catalog/tableinfo.info");
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write("");
        fileWriter.close();

        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        for (Table table : tableList) oos.writeObject(table);
        oos.flush();
        oos.close();
        fos.close();
        
        super.finalize();
    }
	
	
	
	public int findTable(String m_tableName){
		for(int i = 0; i < tableList.size(); i++){
			if(tableList.get(i).getName().equals(m_tableName))
				return i;
		}
		return -1;
	}
	
	public boolean createTable(SQLstatement m_query) throws IOException{
		Table table = new Table();
        int tupleLength = 0;
        boolean Primflag = false;

        table.setAttributes(m_query.getAttributes());
        table.setAttrNum(m_query.getAttributes().size());
        table.setName(m_query.getTableName());
        for (Attribute a : m_query.getAttributes()) {
            if (a.isPrimKey()) {
            	if( !Primflag){
            		Primflag = true;
            		table.setPrimKey(a.getName());
            	}
            	else {
            		System.out.println("It is not allowed to have more than one primary keys in "+m_query.getTableName());
            		return false;
            		}
            }
            if (a.isUnique()){
            	ArrayList<String > str = new ArrayList<String>();
            	table.uniques.put(a.getName(), str);
            }
            
            tupleLength += a.getLength();
        }
        table.setTupleLength(tupleLength);
        tableList.add(table);
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File("./catalog/tableinfo.info")));
        out.writeObject(table);
        out.close();
//        为主键建索引
//        if( Primflag ){
//        	
//        }
        File file = new File("./memory/" + m_query.getTableName() + ".table");
        file.createNewFile();
        return true;
	}
	
	public void dropTable(String m_tableName) throws IOException{
		 ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File("./catalog/tableinfo.info")));
		 out.reset();
		 int i;
		 for(i = 0; i < tableList.size(); i++){
			 if(tableList.get(i).getName().equals(m_tableName)){
				 break;
			 }
		 }
		 tableList.remove(i);
         File file = new File("./memory/" + m_tableName + ".table");
         if (file.exists())  file.delete();
        for (Table table : tableList){
                 out.writeObject(table);
        }
        out.close();
    }
	
	
	//index = true 检查这个属性能不能被用来创立为index；index = false 检查这个属性是否存在
	public boolean checkAttribute(Table m_table, String m_attrname, boolean index)
	{
		if(m_attrname.equals("*"))
			return true;
		boolean flag = false;//检查属性是否存在
		for(Attribute a: m_table.getAttributes()){
			if(a.getName().equals(m_attrname)){
				if(index){
					if( (!a.isPrimKey()) && (!a.isUnique()) ){
						System.out.println(a.getName() + "can't be the index key.");
						return false;
					}	
				}
				else{
					flag = true;
					break;
				}
			}
		}
		if(!flag){
			System.out.println(m_attrname + " not in the " + m_table.getName());//属性不存在
			return false;
		}
		else return true;//属性存在
	}
	
	
}
