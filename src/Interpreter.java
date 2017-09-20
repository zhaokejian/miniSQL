import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.*;
import java.util.ArrayList;


import java.util.Scanner;

import structures.*;

public class Interpreter {
	
	
	
	public static DATA_TYPE CheckDataType(String type){
		char c = type.charAt(0);
		switch(c){
		case 'i':return DATA_TYPE.MYINT;
		case 'f':return DATA_TYPE.MYFLOAT;
		case 'c':return DATA_TYPE.MYCHAR;
		default: return null;
		}
	}
	
	public static RELATION_TYPE CheckOpType(String type){
		String t = type;
		switch(t){
		case "=":return RELATION_TYPE.EQUAL;
		case "<>":return RELATION_TYPE.NOT_EQUAL;
		case "<":return RELATION_TYPE.SMALLER;
		case ">":return RELATION_TYPE.GREATER;
		case "<=":return RELATION_TYPE.SMALLER_EQUAL;
		case ">=":return RELATION_TYPE.GREATER_EQUAL;
		default: return null;
		}
	}
	
	public static void intepreter(int inSrc, String path) throws Throwable,IOException,ClassNotFoundException, FileNotFoundException{
		API api = new API();
		Scanner in = null;
		FileInputStream inputStream = null;
		SQLstatement sqlStatement = new SQLstatement();
		if(inSrc == 0){
			File file = new File(path);
			in = new Scanner(file);
		}
		else{
			in = new Scanner(System.in);
			System.out.print("Mysql>");
		}
        boolean ifError = false;
        while(in.hasNext()){
        	STATEMENT_TYPE type;
    	    String tableName;
    	    String indexName;
    	    ArrayList<Attribute> attributes = new ArrayList<>();
    	    ArrayList<Condition> conditions = new ArrayList<>();
    	    String content;
        	
        	String sql = in.nextLine();
        	String  tmpName, tmp[], priName;
        	int tmpLength = 4;
        	
    	    DATA_TYPE tmpType;
        	
        	while(!sql.contains(";"))
        		sql += in.nextLine();
        	
        	if(sql.startsWith("create table")){
        		String[] s = new String[2];
        		s[0] = sql.substring(0, sql.indexOf('(')).trim();
        		s[1] = sql.substring(sql.indexOf('(')+1, sql.lastIndexOf(')'));
        		tableName = s[0].split(" ")[2];
        		String attr[] = s[1].split(",");
        		
        		for(int i = 0; i < attr.length; i++){
        			Attribute attribute = new Attribute();
        			tmp = attr[i].trim().split(" ");
        			if(tmp.length == 5){//5���ֵ�
        				if((tmp[0]+tmp[1]).equals("primarykey")){//primary key��������
        					priName = tmp[3];
        					boolean flag = false;
        					for(Attribute attrs : attributes){
								if(attrs.getName().equals(priName)){
        							attrs.setIsPrimKey(true);
        							flag = true;
								}
        					}
        					if(flag == false)
        						ifError = true;
        				}
        			}
        			else if(tmp.length == 3){//����+����+unique
        					tmpName = tmp[0];
        					tmpType = CheckDataType(tmp[1]);
        					if(tmpType.equals(DATA_TYPE.MYCHAR))
        						tmpLength = Integer.parseInt(tmp[1].substring(5, tmp[1].length()-1));
        					else tmpLength = 4;
        					if(tmp[2].equals("unique")){
        						attribute.setName(tmpName);
        						attribute.setType(tmpType);
        						attribute.setLength(tmpLength);
        						attribute.setIsUnique(true);
        					}
        					else ifError = true;
        					attributes.add(attribute);
        			}
        			else if(tmp.length == 2){//���� + ����
        				tmpName = tmp[0];
        				tmpType = CheckDataType(tmp[1]);
        				if(tmpType.equals(DATA_TYPE.MYCHAR))
    						tmpLength = Integer.parseInt(tmp[1].substring(5, tmp[1].length()-1));
        				else tmpLength = 4;
        				attribute.setName(tmpName);
        				attribute.setType(tmpType);
        				attribute.setLength(tmpLength);
        				attributes.add(attribute);
        			}
        			else ifError = true;
        			
        			
        		}
        		if(!ifError){
	        		sqlStatement.setStateType(STATEMENT_TYPE.CREATE_TABLE);
	        		sqlStatement.setTableName(tableName);
	        		sqlStatement.setAttributes(attributes);
	        	}
        	}
        	else if(sql.startsWith("drop table")){
        		String s[] = sql.split(" ");
        		if(s.length != 3) ifError = true;
        		else{
	        		tableName = s[2].substring(0, s[2].length()-1);
	        		sqlStatement.setStateType(STATEMENT_TYPE.DROP_TABLE);
	        		sqlStatement.setTableName(tableName);
        		}
        	}
        	else if(sql.startsWith("create index")){
        		String s[] = sql.split(" ");
        		if(s.length != 8) ifError = true;
        		else{
	        		indexName = s[2];
	        		tableName = s[4];
	        		content = s[6];
	        		
	        		sqlStatement.setIndexName(indexName);
	        		sqlStatement.setTableName(tableName);
	        		sqlStatement.setStateType(STATEMENT_TYPE.CREATE_INDEX);
	        		sqlStatement.setContent(content);
        		}
        	}
        	else if(sql.startsWith("drop index")){
        		String s[] = sql.split(" ");
        		if(s.length != 3) ifError = true;
        		else{
	        		indexName = s[2].substring(0, s[2].length()-1);
	        		sqlStatement.setStateType(STATEMENT_TYPE.DROP_INDEX);
	        		sqlStatement.setIndexName(indexName);
        		}
        	}
        	else if(sql.startsWith("select * from")){
        		if(!sql.contains("where")){
        			String s[] = sql.split(" ");
        			if(s.length != 4) ifError = true;
        			else{
	        			tableName = s[3].substring(0, s[3].length()-1);
	        			sqlStatement.setTableName(tableName);
	            		sqlStatement.setStateType(STATEMENT_TYPE.SELECT);
        			}
        		}
        		else{
        			String s[] = sql.split(" ");
        			if(!s[4].equals("where")) ifError = true;
        			else{
	        			tableName = s[3];
	        			String cons = sql.substring(sql.indexOf("where") + 6, sql.length()-1);
	        			String con[] = cons.split(" and ");
	        			
	        			String attrName, value;
	        			RELATION_TYPE op;
	        			for(int i = 0;i < con.length; i++){
	        				Condition condition = new Condition();
	        				String c[] = con[i].split(" ");
	        				if(c.length != 3) ifError = true;
	        				attrName = c[0];
	        				op = CheckOpType(c[1]);
	        				value = c[2];
	        				if(value.charAt(0) == '\'')
	        					value = value.substring(1, value.length()-1);
	        				
	        				condition.setAttrName(attrName);
	        				condition.setRelationType(op);
	        				condition.setValue(value);
	        				
	        				conditions.add(condition);
	        			}
	        			sqlStatement.setStateType(STATEMENT_TYPE.SELECT_WHERE);
	        			sqlStatement.setConditions(conditions);
	        			sqlStatement.setTableName(tableName);
        			}
        		}
        	}	
        
        	else if(sql.startsWith("insert into")){
        		String s[] = sql.split(" ");
        		tableName = s[2];
        		if(!s[3].equals("values")) ifError = true;
        		else{
	        		content = sql.substring(sql.indexOf('(')+1, sql.indexOf(')')).trim();
	        		sqlStatement.setTableName(tableName);
	        		sqlStatement.setStateType(STATEMENT_TYPE.INSERT);
	        		sqlStatement.setContent(content);
        		}
        	}
        	else if(sql.startsWith("delete from")){
        		if(!sql.contains("where")){
        			String s[] = sql.split(" ");
        			if(s.length != 3) 
        				ifError = true;
        			else{
        				tableName = s[2].substring(0, s[2].length()-1);
        			
        				sqlStatement.setStateType(STATEMENT_TYPE.DELETE);
        				sqlStatement.setTableName(tableName);
        			}
        		}
        		else{
        			String s[] = sql.split(" ");
        			if(!s[3].equals("where")) ifError = true;
        			else{
	        			tableName = s[2];
	        			String cons = sql.substring(sql.indexOf("where") + 6, sql.length()-1);
	        			String con[] = cons.split(" and ");
	        			
	        			String attrName, value;
	        			RELATION_TYPE op;
	        			for(int i = 0;i < con.length; i++){
	        				Condition condition = new Condition();
	        				String c[] = con[i].split(" ");
	        				if(c.length != 3) ifError = true;
	        				attrName = c[0];
	        				op = CheckOpType(c[1]);
	        				value = c[2];
	        				if(value.charAt(0) == '\'')
	        					value = value.substring(1, value.length()-1);
	        				
	        				condition.setAttrName(attrName);
	        				condition.setRelationType(op);
	        				condition.setValue(value);
	        				
	        				conditions.add(condition);
	        			}
	        			sqlStatement.setStateType(STATEMENT_TYPE.DELETE_WHERE);
	        			sqlStatement.setConditions(conditions);
	        			sqlStatement.setTableName(tableName);
        			}
        		}
        	}
        	else if(sql.equals("quit;")){
        		System.out.println("GOODBYE");
        		sqlStatement.setStateType(STATEMENT_TYPE.QUIT);
        	}
        	else if(sql.contains("execfile")){
        		String s[] = sql.split(" ");
        		intepreter(0,s[1].substring(0, s[1].length()-1));
        	}
        	else 
        		ifError = true;
        		
         	
        	if(ifError)
            	System.out.println("Syntax error!");
        	else{
//        	API api = new API(sqlStatement);
        	api.setSQL(sqlStatement);
        	api.execSQL();
        	}
        	System.out.print("Mysql>");
        }
        
        in.close();
	}
	
	public static void main(String args[]) throws Throwable, ClassNotFoundException, IOException, FileNotFoundException{
		
	    
		System.out.println("*****************************************");
        System.out.println("***********Welcome to MiniSQL!***********");
        System.out.println("**************Vertion 1.0****************");
        System.out.println("**********Copyright@2015 436*************");
        System.out.println("*****************************************");
		
        intepreter(1,null);
        
	}
}
