import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.JComboBox.KeySelectionManager;

import structures.*;

public class API{
	SQLstatement sqlStatement;
	RecordManager rm;
	CatalogManager cm;
	IndexManager im;
	Table table;
	ArrayList<Attribute> attributes = new ArrayList<>();
    ArrayList<Condition> conditions = new ArrayList<>();
    String content;
    
    ArrayList<Hashtable<String,String>> resList;

	public API() throws IOException, ClassNotFoundException{
		cm = new CatalogManager();
		rm = new RecordManager();
		im = new IndexManager();
	}
	
	public void setSQL(SQLstatement sqlstatement) {
		this.sqlStatement = sqlstatement;	
	}
	
	public void execSQL() throws Throwable{
		if(sqlStatement.getType() == STATEMENT_TYPE.CREATE_TABLE)
	{
		if (cm.findTable(sqlStatement.getTableName()) != -1){
			System.out.println("Fail. "+sqlStatement.getTableName()+" has already existed.");
//			return false;
		}
		else{
				if(cm.createTable(sqlStatement)){
					System.out.println(sqlStatement.getTableName()+" created successfully.");
					Table t = cm.tableList.get(cm.findTable(sqlStatement.getTableName()));
					for(Attribute a : t.getAttributes()){
						if(a.isPrimKey()){
							ArrayList<String> temp = new ArrayList<String>();
//							temp.add("hello");
							ArrayList<Integer> temp2 = new ArrayList<Integer>();
//							temp2.add(1);
							im.create_index(sqlStatement.getTableName(), a.getName(),"",temp, temp2, a.getLength());
						}
					}
				}
				else{
					System.out.println("Fail. More than one primary keys have been defined.");
				}
			}
	}
else if(sqlStatement.getType() == STATEMENT_TYPE.DROP_TABLE)
	{
		int cnt = cm.findTable(sqlStatement.getTableName());
		table = cm.tableList.get(cnt);
		if (cnt != -1){
			// 删index
			
			for(Attribute a: cm.tableList.get(cnt).getAttributes()){
				if(!a.getIndexName().equals("")){
					if(a.getIndexName() != "" ){
						im.drop_index(a.getIndexName());
					}
					if(a.isPrimKey()){
					im.drop_primary_index(sqlStatement.getTableName(), a.getName());
					}
				}
			}
			
				cm.dropTable(sqlStatement.getTableName());
				System.out.println(sqlStatement.getTableName() + " dropped successfully.");

		}
		else{
			System.out.println(sqlStatement.getTableName()+" not exist");
		}
	}
//		case DELETE:
		else if(sqlStatement.getType() == STATEMENT_TYPE.DELETE){
			int n;
			int cnt = cm.findTable(sqlStatement.getTableName());
			if(cnt == -1)
				System.out.println(sqlStatement.getTableName()+" not exist");
			else{
				table = cm.tableList.get(cnt);
				n = rm.delete(table, null);
				System.out.println(n + " records deleted");
				
				ArrayList<Attribute> attrWithIndex = new ArrayList<Attribute>();
				for(Attribute attrtmp : table.getAttributes()){
					if(!attrtmp.getIndexName().equals("") || attrtmp.isPrimKey())
						attrWithIndex.add(attrtmp);
				}
				
				for(Attribute attrtmp : attrWithIndex){
					im.delete_all(table.getName(), attrtmp.getName());
				}
				
			}
		}
		//case DELETE_WHERE:
		else if(sqlStatement.getType() == STATEMENT_TYPE.DELETE_WHERE){
			int n;
			int cnt = cm.findTable(sqlStatement.getTableName());
			if(cnt == -1)
				System.out.println(sqlStatement.getTableName()+" not exist");
			else{
				table = cm.tableList.get(cnt);
				conditions = sqlStatement.getConditions();
				n = rm.delete(table, conditions);
				System.out.println(n + " records deleted");
			
				ArrayList<Condition> conditionWithIndex = new ArrayList<Condition>();
				Attribute attrtmp = new Attribute();
				
				for(Condition condition : sqlStatement.getConditions()){
					attrtmp = table.getAttributeWithName(condition.getAttrName());
					if(!attrtmp.getIndexName().equals("") || attrtmp.isPrimKey()){
						conditionWithIndex.add(condition);
					}
				}
			
				ArrayList<ArrayList<Integer>> fileofs = new ArrayList<ArrayList<Integer>>();
				ArrayList<Integer> fileoffsets = new ArrayList<Integer>();
				
				for(Condition contmp : conditionWithIndex){
					int type;
					switch(contmp.getRelationType()){
					case EQUAL: type = 0;break;
					case SMALLER: type = 1;break;
					case SMALLER_EQUAL: type = 2;break;
					case GREATER :type = 3; break;
					case GREATER_EQUAL: type = 4;break;
					default:type = -1;break;
					}
					im.delete_from_index(sqlStatement.getTableName(),contmp.getAttrName() ,contmp.getValue(),type);
				}
				
			}
			
		}
		//case INSERT:
		else if(sqlStatement.getType() == STATEMENT_TYPE.INSERT){
			int cnt = cm.findTable(sqlStatement.getTableName());
			
			int res;
			if(cnt == -1)
				System.out.println(sqlStatement.getTableName()+" not exist");
			else{
				table = cm.tableList.get(cnt);
				content = sqlStatement.getContent();
				res = rm.insert(table, content);
				
				switch(res){
				case -1: System.out.println("Syntax error!");break;
				case -2: System.out.println("Duplicate primary key!");break;
				case -3: System.out.println("Duplicate unique attribute");break;
				default:break;
				}
				
				String[] value = content.split(",");
				for(int i = 0; i < value.length; i++){
		    		value[i] = value[i].trim();
		    		if(value[i].charAt(0) == '\'' )
		    			value[i] = value[i].substring(1, value[i].length()-1);
				}
				Hashtable<Attribute,String> attrcontent = new Hashtable<Attribute,String>();
				for(int i = 0; i < table.getAttrNum(); i++){
					attrcontent.put(table.getAttributes().get(i), value[i]);
				}
				
				ArrayList<Attribute> attrWithIndex = new ArrayList<Attribute>();
				for(Attribute attrtmp : table.getAttributes()){
					if(!attrtmp.getIndexName().equals("") || attrtmp.isPrimKey())
						attrWithIndex.add(attrtmp);
				}
				
				for(Attribute attrtmp : attrWithIndex){
					im.insert_in_index(table.getName(), attrtmp.getName(), attrcontent.get(attrtmp), res);
				}
			}
			
		}
		//case SELECT:
		else if(sqlStatement.getType() == STATEMENT_TYPE.SELECT){
			ArrayList<Hashtable<String,String>> res = new ArrayList<Hashtable<String,String>>();
			
			int cnt = cm.findTable(sqlStatement.getTableName());
			if(cnt == -1)
				System.out.println(sqlStatement.getTableName()+" not exist");
			else{
				table = cm.tableList.get(cnt);
				res = rm.select(table, null);
				
			for(int i = 0; i < table.getAttrNum(); i++)
					System.out.print(table.getAttributes().get(i).getName() + "\t");
				System.out.println("");
				
				for(Hashtable<String,String> record : res){//每一个tuple
					for(int j = 0; j < table.getAttrNum(); j++)
						System.out.print(record.get(table.getAttributes().get(j).getName()) + "\t");
					System.out.println("");
				}
				System.out.println(res.size() + " records in total.");

			}
		}
//		case SELECT_WHERE:
		else if(sqlStatement.getType() == STATEMENT_TYPE.SELECT_WHERE){
			ArrayList<Hashtable<String,String>> res = new ArrayList<Hashtable<String,String>>();
			Attribute attrtmp = new Attribute();
			
			int cnt = cm.findTable(sqlStatement.getTableName());
			if(cnt == -1)
				System.out.println(sqlStatement.getTableName()+" not exist");
			else{
				
				table = cm.tableList.get(cnt);
				ArrayList<Condition> conditionWithoutIndex = sqlStatement.getConditions();
				ArrayList<Condition> conditionWithIndex = new ArrayList<Condition>();
				
				ArrayList<Condition> condi= new ArrayList<Condition>();
				condi = sqlStatement.getConditions();
				for(int i = 0; i < condi.size(); i++){
					attrtmp = table.getAttributeWithName(condi.get(i).getAttrName());
					if(!attrtmp.getIndexName().equals("") || attrtmp.isPrimKey()){
						conditionWithIndex.add(condi.get(i));
						conditionWithoutIndex.remove(condi.get(i));
					}
				}
				
				//attrIndex这个list里存着的是有index的attr
				//调用index manager 传出 一个integer的list,最后得到一个list的list
				ArrayList<ArrayList<Integer>> fileofs = new ArrayList<ArrayList<Integer>>();
				ArrayList<Integer> fileoffsets = new ArrayList<Integer>();
				
				for(Condition contmp : conditionWithIndex){
					int type;
					switch(contmp.getRelationType()){
					case EQUAL: type = 0;break;
					case SMALLER: type = 1;break;
					case SMALLER_EQUAL: type = 2;break;
					case GREATER :type = 3; break;
					case GREATER_EQUAL: type = 4;break;
					default:type = -1;break;
					}
					fileoffsets = im.select_offsets(table.getName(),contmp.getAttrName(),contmp.getValue(),type);
					fileofs.add(fileoffsets);
				}
				
				ArrayList<Integer> offsets = new ArrayList<Integer>();
				offsets = fileofs.get(0);
				
				for(Integer offset : offsets)
					for(ArrayList<Integer> ofs : fileofs)
						if(!ofs.contains(offset))
							offsets.remove(offset);
				
				if(offsets.isEmpty()){
					conditions = sqlStatement.getConditions();
					res = rm.select(table, conditions);
				}
				else{
					conditions = conditionWithoutIndex;
					res = rm.select_index(table, conditions, offsets);
				}
				
				for(int i = 0; i < table.getAttrNum(); i++)
					System.out.print(table.getAttributes().get(i).getName() + "\t");
				System.out.println("");
				for(Hashtable<String,String> record : res){//每一个tuple
					for(int j = 0; j < table.getAttrNum(); j++)
						System.out.print(record.get(table.getAttributes().get(j).getName()) + "\t");
					System.out.println("");
				}
				System.out.println(res.size() + " records in total.");
				 
			}
		}
		//case create index
				else if(sqlStatement.getType() == STATEMENT_TYPE.CREATE_INDEX){
			//========create_index(String tablename, String attri, ArrayList<String> keys, ArrayList<Integer> off, int length)
					//length为属性字节长度
					int indx = cm.findTable(sqlStatement.getTableName());
			
					boolean flag = false;
					int mark = 0;
					if(indx == -1){
						System.out.println("Fail. The table "+sqlStatement.getTableName()+" not exist..");
						return;
					}
					table = cm.tableList.get(indx);
					
					for(int i=0; i < cm.tableList.get(indx).getAttributes().size();i++){
						if(cm.tableList.get(indx).getAttributes().get(i).getName().equals(sqlStatement.getContent())){
							mark = i;
							flag = true;
						}
					}
					if(!flag){
						System.out.println("Fail. The attribute "+sqlStatement.getContent()+" not exist.");
						return;
					}
					ArrayList<Hashtable<String,String>> attrtmp = new ArrayList<Hashtable<String,String>>();
					attrtmp = rm.select(table, null);
					ArrayList<String> keys = new ArrayList<String>();
					ArrayList<Integer> offsets = new ArrayList<Integer>();
					Attribute tmpattr = new Attribute();
					tmpattr = table.getAttributeWithName(sqlStatement.getContent());
					String tmpkey;
					int tmpofs;
					
					for(int i = 0; i<attrtmp.size(); i++){
						tmpofs = Integer.parseInt(attrtmp.get(i).get("fileofs"));
						tmpkey = attrtmp.get(i).get(sqlStatement.getContent());
						keys.add(tmpkey);
						offsets.add(tmpofs);
					}
					
					if(table.getAttributeWithName(sqlStatement.getContent()).isUnique()){		
					if(!im.create_index(sqlStatement.getTableName(), sqlStatement.getContent(),sqlStatement.getIndexName(), 
							 keys, offsets,tmpattr.getLength())){
						System.out.println("error! create_index");
					}
					else{
						cm.tableList.get(indx).getAttributes().get(mark).setIndexName(sqlStatement.getIndexName());
					}
					}
					else System.out.println("Not unique!");
				}
				
				else if(sqlStatement.getType() == STATEMENT_TYPE.DROP_INDEX){
			//=========drop_index(String tablename, String attri)
					String[] str = im.getname_fromindex(sqlStatement.getIndexName()).split("\\_");
					if(!im.drop_index(sqlStatement.getIndexName())){
						System.out.println("error! drop_index");
					}
					else{	
						String tablenm = str[0];
						String attrnm = str[1];
						int indx = cm.findTable(tablenm);
						for(Attribute a: cm.tableList.get(indx).getAttributes()){
							if(a.getName().equals(attrnm))
								a.setIndexName("");
						}
						
					}
				}

		else if(sqlStatement.getType() == STATEMENT_TYPE.QUIT){
			cm.finalize();
			rm.finalize();
			System.exit(0);
			}
	}
}
	

