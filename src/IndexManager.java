import structures.*;
import java.io.File;
import java.util.ArrayList;

public class IndexManager {

	private int calc_ORDER(int length){
		int ORDER;
		ORDER = 4096/(4+length);
		return ORDER;
	}
	
	public boolean create_index(String tablename, String attri, String indexname, 
			ArrayList<String> keys, ArrayList<Integer> off, int length){
			if(keys.size() == off.size()){
				BPlusTree tree = new BPlusTree(calc_ORDER(length));
				int size = keys.size();
				int i;
				for(i=0; i<size; i++){
					tree.insert_one(keys.get(i), off.get(i));
				}
				String filename;
				filename = "./index/" + tablename + "_" + attri + "_" + indexname + ".index";
				return (tree.write_BPlusTree(filename));
			}			
			else {
				System.out.println("error! indexmanager:create_index_(keys.size() != off.size())");
				return false;
			}	
}
	
	public boolean insert_in_index(String tablename, String attri, String keys, int off){
		String indexname = get_indexname(tablename, attri);
		String filename = "./index/" + tablename + "_" + attri + "_" + indexname +".index";
		File file = new File(filename);
		if(file.exists()){
			BPlusTree tree = new BPlusTree(filename);
			if(tree.insert_one(keys, off)) return tree.write_BPlusTree(filename);
			else{
				System.out.println("error! indexmanager:BPlusTree.insert_one");
				return false;
			}
		}
		else{
			System.out.println("error! indexmanager:insert_in_index(index not found)");
			return false;
		}
	}
	
	public boolean delete_from_index(String tablename, String attri, String key, int type){
		String indexname = get_indexname(tablename, attri);
		String filename = "./index/" + tablename + "_" + attri + "_" + indexname + ".index";
		File file = new File(filename);
		if(file.exists()){
			BPlusTree tree = new BPlusTree(filename);
			ArrayList<String> keys = tree.select_key(key, type);
			for(int i=0; i<keys.size();i++){
				if(!tree.delete_one(keys.get(i))){
					System.out.println("error! indexmanager:BPlusTree.delete_one");
					return false;
				}
			}
			return tree.write_BPlusTree(filename);
		}
		else{
			System.out.println("error! indexmanager:delete_from_index(index not found)");
			return false;
		}
	}
	
	public boolean delete_all(String tablename, String attri){
		String indexname = get_indexname(tablename, attri);
		String filename = "./index/" + tablename + "_" + attri + "_" + indexname + ".index";
		File file = new File(filename);
		if(file.exists()){
			BPlusTree tree = new BPlusTree(filename);
			if(tree.delete_all()) return tree.write_BPlusTree(filename);
			else{
				System.out.println("error! indexmanager:BPlusTree.delete_all");
				return false;
			}
		}
		else{
			System.out.println("error! indexmanager:delete_all(index not found)");
			return false;
		}		
	}
	
	public ArrayList<Integer> select_offsets(String tablename, String attri, String key, int type){
		String indexname = get_indexname(tablename, attri);
		ArrayList<Integer> offsets = new ArrayList<Integer>();
		String filename = "./index/" + tablename + "_" + attri + "_" + indexname + ".index";
		File file = new File(filename);
		if(file.exists()){
			BPlusTree tree = new BPlusTree(filename);
			return tree.select(key, type);
		}
		else{
			System.out.println("error! indexmanager:delete_from_index(index not found)");
			return offsets;
		}
	}
	
	public boolean drop_index(String indexname){
		String name = getname_fromindex(indexname);
		String filename ="./index/" + name + "_" + indexname + ".index";
		File file = new File(filename);
		if(file.exists()) return file.delete();
		else{
			System.out.println("error! indexmanager:drop_index(index not found)");
			return false;
		}
	}
	
	public boolean drop_primary_index(String tablename, String attri){
		String filename ="./index/" + tablename + "_" + attri + "_" + ".index";
		File file = new File(filename);
		if(file.exists()) return file.delete();
		else{
			System.out.println("error! indexmanager:drop_primary_index(index not found)");
			return false;
		}
	}
	
	public String getname_fromindex(String indexname){
		File file = new File("./index/");     
        File[] array = file.listFiles();
        String name="", temp;
        int i,j;
        for(i=0;i<array.length;i++){
        	temp = array[i].getName();
        	j = temp.indexOf("_" + indexname + '.');
        	if(j >=0){
        		name = temp.substring(0, j);
        		break;
        	}
        }
        return name;
	}
	
	private String get_indexname(String tablename, String attri){
		File file = new File("./index/");     
        File[] array = file.listFiles();
        String name="", temp;
        int i,j;
        for(i=0;i<array.length;i++){
        	temp = array[i].getName();
        	j = temp.indexOf(tablename + "_" + attri + "_");
        	if(j >=0){
        		int m = temp.indexOf('_');
        		int n = temp.indexOf('_',m+1);
        		int k = temp.indexOf('.');
        		name = temp.substring(n+1, k);
        		break;
        	}
        }
        return name;
	}

}
