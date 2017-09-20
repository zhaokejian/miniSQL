package structures;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.ArrayList;

class Node implements Serializable{

    private static final long serialVersionUID = 19950436L;

    int ORDER;
    int isEmpty;//鏍囪缁撶偣鏄惁涓虹┖
    int isLeaf;//鏍囪鏄惁涓哄彾鑺傜偣
    ArrayList<String> keys = new ArrayList<>();//閿��
    ArrayList<Integer> children = new ArrayList<>();//瀛愬潡鐨勬爣鍙凤紙鎸囬拡锛�
    int father_block;//鐖跺潡鐨勬爣鍙�
    int childNum;//鎸囬拡鏁帮紝涓巆hildren瀵瑰簲

    Node(int order) {//鍒涘缓Node鏃朵竴瀹氭湁闃舵暟
       this.ORDER=order;
       childNum = 0;
       isEmpty = 1;//涓虹┖
       isLeaf = -1;//涓嶇‘瀹氭槸鍚︿负鍙惰妭鐐�
       father_block = -1;
    }

    public void setChildNum(int childNum) {
        this.childNum = childNum;
    }

    public void setFather(Integer father) {
        this.father_block = father;
    }
    
    public void setEmpty(int empty){
    	this.isEmpty=empty;
    }
    
    public void setLeaf(int leaf){
    	this.isLeaf=leaf;
    }   
}


public class BPlusTree implements Serializable{	
	private static final long serialVersionUID = 19950436L;
	
	int ORDER;//闃舵暟
    int leafmin;//鍙惰妭鐐规渶灏忛敭鍊兼暟鐩�
    int leafmax;//鍙惰妭鐐规渶澶ч敭鍊兼暟鐩�
    int nodemin;//闈炲彾鑺傜偣鏈�灏忛敭鍊兼暟鐩�
    
	ArrayList<Node> nodes = new ArrayList<>();
	int root_block;//鏍硅妭鐐瑰潡鍙�
	int empty_block;//绗竴涓┖缁撶偣鍧楀彿
	int Empty;//浼樺厛绾ф渶楂橈紝鐩存帴鏍囪鏍戞槸鍚︿负绌�
	
	
    public BPlusTree(String filename){
    	int indexl, indexr;
    	String temp;
    	try{
    		FileReader reader = new FileReader(filename);
    		BufferedReader br = new BufferedReader(reader);
       
    		String templine = null;  
    		templine = br.readLine();
    		if(templine.charAt(0) == '!'){
				indexl = 1;
				indexr = templine.indexOf('\t', indexl);
				temp = templine.substring(indexl, indexr);
				ORDER = Integer.parseInt(temp);   				
				leafmin = (int) Math.ceil((ORDER-1)/2.0);
		    	leafmax = ORDER-1;
		    	nodemin = (int) Math.ceil(ORDER/2.0);
		    	
				indexl = indexr+1;
				indexr = templine.indexOf('\t', indexl);
				temp = templine.substring(indexl, indexr);
				root_block = Integer.parseInt(temp);
				
				indexl = indexr+1;
				indexr = templine.indexOf('\t', indexl);
				temp = templine.substring(indexl, indexr);   						
				empty_block = Integer.parseInt(temp);
				
				indexl = indexr+1;
				indexr = templine.indexOf('\t', indexl);
				temp = templine.substring(indexl, indexr);
				Empty = Integer.parseInt(temp);  
				
				while((templine = br.readLine()) != null){
					if(templine.charAt(0) == '#'){
	    				read_tonode(templine);
	    			}
					else{
						System.out.println("error_BPlusTree(String filename)_2");
		    			return;
					}
				}
			}   		
    		else {   			
    			System.out.println("error_BPlusTree(String filename)_1");
    			return;
    		}   			
    	}catch(Exception e) {
			e.printStackTrace();
		}
    	

    }
    
    public BPlusTree(int order){//缁欏畾闃舵暟鏂板缓B+鏍戯紝褰㈡垚涓�涓牴鑺傜偣寰呮坊鍔犲�肩殑涓棿鐘舵�侊紝娌℃湁绌虹粨鐐归摼琛�
    	nodes.clear();
    	this.ORDER = order;
    	this.leafmin = (int) Math.ceil((ORDER-1)/2.0);
    	this.leafmax = ORDER-1;
    	this.nodemin = (int) Math.ceil(ORDER/2.0);
    	this.root_block = -1;
    	this.empty_block = -1;//娌℃湁绌虹粨鐐�
    	this.Empty = 1;
    }
     
   
    
    public ArrayList<Integer> select(String value, int type){
    	ArrayList<Integer> offsets = new ArrayList<>();
    	
    	if(Empty == 1)  return offsets;//鏍戜负绌烘椂鐩存帴杩斿洖绌烘暟缁�
    	
    	Node leaf = find(value);//鎵惧埌瀵瑰簲鐨勫彾鑺傜偣
    	int number = leaf.childNum - 1;//瀵瑰簲鍙惰妭鐐归敭鍊兼暟
    	int leaf_block = nodes.indexOf(leaf);//瀵瑰簲鍙惰妭鐐瑰潡鍙�
    	   	
    	int i,j;
    	int n;//姣忎釜鍙惰妭鐐圭殑閿�间釜鏁�=childNum-1
    	Node tmp;
    	int flag = -2;
    	if(type == 0){
    		for(i=0; i<leaf.childNum-1; i++){//閬嶅巻閿��
    			if(leaf.keys.get(i).compareTo(value) == 0){//鎵惧埌閿�硷紝杩斿洖offset
    				offsets.add(leaf.children.get(i));
    				return offsets;
    			}
    		}
    		return offsets;
    	}
    	if(type == 1 || type == 2){// <鎴�<=
    		int mostleft_block = find_mostleft_leaf();
     		
    		if(type == 1){//<   			
    			for(i = 0; i<number; i++){//閬嶅巻瀵瑰簲鍙惰妭鐐圭殑閿��
    				if(leaf.keys.get(i).compareTo(value)>=0){//i涓虹涓�涓笉绗﹀悎鏉′欢鐨勯敭鍊肩殑鍧愭爣
    					flag = i-1;//flag澶勪负鏈�澶х鍚堣姹傜殑閿�肩殑鍧愭爣
    					break;
    				}
    			}
    			if(i == number) flag = i-1;//瀵瑰簲鍙惰妭鐐逛腑鐨勯敭鍊煎潎绗﹀悎瑕佹眰
    		}
    		else if(type == 2){//<=
    			for(i = 0; i<number; i++){
    				if(leaf.keys.get(i).compareTo(value)>0){
    					flag = i-1;
    					break;
    				}
    			}
    			if(i == number) flag = i-1;
    		}
    		
    			//瀵筬lag鍒嗙被鍒ゆ柇
    			if(flag == -1){//鏈�澶х鍚堣姹傜殑閿�煎湪鍓嶄竴涓彾鑺傜偣
    				i = mostleft_block; //浜岄噸寰幆锛屼粠鏈�宸﹁竟鍙惰妭鐐瑰埌value瀵瑰簲缁撶偣鐨勫墠涓�涓粨鐐�
    				while(i!=leaf_block){
    					tmp = nodes.get(i);//褰撳墠鍙惰妭鐐�
    					n = tmp.childNum - 1;
    					for(j=0; j<n; j++){//閬嶅巻鍙惰妭鐐逛腑鐨勯敭鍊�
    						offsets.add(tmp.children.get(j));//鍦╫ffsets鏁扮粍涓坊鍔犲��
    					}
    					i = tmp.children.get(n);//i涓轰笅涓�涓彾鑺傜偣鐨勫潡鍙�
    				}
    				//缁撴潫寰幆锛岃繑鍥烇紝鍙兘鏈繘鍏ュ惊鍧忥紝offsets涓虹┖
    				return offsets;
    			}
    			else{//鍏朵粬鎯呭喌flag鍧囪〃绀虹鍚堣姹傞敭鍊肩殑鍧愭爣
    				i = mostleft_block;
    				while(i!=leaf_block){
    					tmp = nodes.get(i);
    					n = tmp.childNum - 1;
    					for(j=0; j<n; j++){
    						offsets.add(tmp.children.get(j));
    					}
    					i = tmp.children.get(n);//i涓轰笅涓�涓彾鑺傜偣鐨勫潡鍙�
    				}
    				//閬嶅巻瀵瑰簲leaf缁撶偣鍒癴lag
    				for(j=0; j<=flag; j++){//flag涓哄潗鏍囷紝鍔�=鍙�
    					offsets.add(leaf.children.get(j));
    				}
    				
    				return offsets;
    			}    		
    	}
    	else {// >鎴�>=
    		if(type == 3){
    			for(i = 0; i<number; i++){
    				if(leaf.keys.get(i).compareTo(value)>0){//i涓虹涓�涓鍚堣姹傜殑鍊肩殑鍧愭爣
    					flag = i;
    					break;
    				}
    			}
    			if(i == number) flag = -1;//绗﹀悎瑕佹眰鐨勫�奸敭鍊煎湪涓嬩竴涓彾鑺傜偣
    		}
    		else if(type == 4){
    			for(i = 0; i<number; i++){
    				if(leaf.keys.get(i).compareTo(value)>=0){
    					flag = i;
    					break;
    				}
    			}
    			if(i == number) flag = -1;
    		}
    		
    		//鍒ゆ柇flag
    		if(flag == -1){//鏈�灏忕鍚堣姹傜殑閿�煎湪涓嬩竴涓粨鐐�
    			i = leaf.children.get(number);//涓嬩竴涓彾鑺傜偣鍧楀彿
    			while(i!=-1){
    				tmp = nodes.get(i);
					n = tmp.childNum - 1;
					for(j=0; j<n; j++){
						offsets.add(tmp.children.get(j));
					}
					i = tmp.children.get(n);//i涓轰笅涓�涓彾鑺傜偣鐨勫潡鍙�
    			}
    			//缁撴潫寰幆锛岃繑鍥烇紝鍙兘鏈繘鍏ュ惊鍧忥紝offsets涓虹┖
				return offsets;
    		}
    		else{//浠庡搴斿彾鑺傜偣鐨刦lag寮�濮�
    			for(j=flag; j<number; j++){
    				offsets.add(leaf.children.get(j));
    			}
    			i = leaf.children.get(number);
    			while(i!=-1){
    				tmp = nodes.get(i);
					n = tmp.childNum - 1;
					for(j=0; j<n; j++){
						offsets.add(tmp.children.get(j));
					}
					i = tmp.children.get(n);
    			}
				return offsets;
    		}
    	}
    }
    
    public ArrayList<String> select_key(String value, int type){
    	ArrayList<String> keys = new ArrayList<>();
    	
    	if(Empty == 1)  return keys;//鏍戜负绌烘椂鐩存帴杩斿洖绌烘暟缁�
    	
    	Node leaf = find(value);//鎵惧埌瀵瑰簲鐨勫彾鑺傜偣
    	int number = leaf.childNum - 1;//瀵瑰簲鍙惰妭鐐归敭鍊兼暟
    	int leaf_block = nodes.indexOf(leaf);//瀵瑰簲鍙惰妭鐐瑰潡鍙�
    	   	
    	int i,j;
    	int n;//姣忎釜鍙惰妭鐐圭殑閿�间釜鏁�=childNum-1
    	Node tmp;
    	int flag = -2;
    	if(type == 0){
    		for(i=0; i<leaf.childNum-1; i++){//閬嶅巻閿��
    			if(leaf.keys.get(i).compareTo(value) == 0){//鎵惧埌閿�硷紝杩斿洖offset
    				keys.add(leaf.keys.get(i));
    				return keys;
    			}
    		}
    		return keys;
    	}
    	if(type == 1 || type == 2){// <鎴�<=
    		int mostleft_block = find_mostleft_leaf();
     		
    		if(type == 1){//<   			
    			for(i = 0; i<number; i++){//閬嶅巻瀵瑰簲鍙惰妭鐐圭殑閿��
    				if(leaf.keys.get(i).compareTo(value)>=0){//i涓虹涓�涓笉绗﹀悎鏉′欢鐨勯敭鍊肩殑鍧愭爣
    					flag = i-1;//flag澶勪负鏈�澶х鍚堣姹傜殑閿�肩殑鍧愭爣
    					break;
    				}
    			}
    			if(i == number) flag = i-1;//瀵瑰簲鍙惰妭鐐逛腑鐨勯敭鍊煎潎绗﹀悎瑕佹眰
    		}
    		else if(type == 2){//<=
    			for(i = 0; i<number; i++){
    				if(leaf.keys.get(i).compareTo(value)>0){
    					flag = i-1;
    					break;
    				}
    			}
    			if(i == number) flag = i-1;
    		}
    		
    			//瀵筬lag鍒嗙被鍒ゆ柇
    			if(flag == -1){//鏈�澶х鍚堣姹傜殑閿�煎湪鍓嶄竴涓彾鑺傜偣
    				i = mostleft_block; //浜岄噸寰幆锛屼粠鏈�宸﹁竟鍙惰妭鐐瑰埌value瀵瑰簲缁撶偣鐨勫墠涓�涓粨鐐�
    				while(i!=leaf_block){
    					tmp = nodes.get(i);//褰撳墠鍙惰妭鐐�
    					n = tmp.childNum - 1;
    					for(j=0; j<n; j++){//閬嶅巻鍙惰妭鐐逛腑鐨勯敭鍊�
    						keys.add(tmp.keys.get(j));//鍦╫ffsets鏁扮粍涓坊鍔犲��
    					}
    					i = tmp.children.get(n);//i涓轰笅涓�涓彾鑺傜偣鐨勫潡鍙�
    				}
    				//缁撴潫寰幆锛岃繑鍥烇紝鍙兘鏈繘鍏ュ惊鍧忥紝offsets涓虹┖
    				return keys;
    			}
    			else{//鍏朵粬鎯呭喌flag鍧囪〃绀虹鍚堣姹傞敭鍊肩殑鍧愭爣
    				i = mostleft_block;
    				while(i!=leaf_block){
    					tmp = nodes.get(i);
    					n = tmp.childNum - 1;
    					for(j=0; j<n; j++){
    						keys.add(tmp.keys.get(j));
    					}
    					i = tmp.children.get(n);//i涓轰笅涓�涓彾鑺傜偣鐨勫潡鍙�
    				}
    				//閬嶅巻瀵瑰簲leaf缁撶偣鍒癴lag
    				for(j=0; j<=flag; j++){//flag涓哄潗鏍囷紝鍔�=鍙�
    					keys.add(leaf.keys.get(j));
    				}
    				
    				return keys;
    			}    		
    	}
    	else {// >鎴�>=
    		if(type == 3){
    			for(i = 0; i<number; i++){
    				if(leaf.keys.get(i).compareTo(value)>0){//i涓虹涓�涓鍚堣姹傜殑鍊肩殑鍧愭爣
    					flag = i;
    					break;
    				}
    			}
    			if(i == number) flag = -1;//绗﹀悎瑕佹眰鐨勫�奸敭鍊煎湪涓嬩竴涓彾鑺傜偣
    		}
    		else if(type == 4){
    			for(i = 0; i<number; i++){
    				if(leaf.keys.get(i).compareTo(value)>=0){
    					flag = i;
    					break;
    				}
    			}
    			if(i == number) flag = -1;
    		}
    		
    		//鍒ゆ柇flag
    		if(flag == -1){//鏈�灏忕鍚堣姹傜殑閿�煎湪涓嬩竴涓粨鐐�
    			i = leaf.children.get(number);//涓嬩竴涓彾鑺傜偣鍧楀彿
    			while(i!=-1){
    				tmp = nodes.get(i);
					n = tmp.childNum - 1;
					for(j=0; j<n; j++){
						keys.add(tmp.keys.get(j));
					}
					i = tmp.children.get(n);//i涓轰笅涓�涓彾鑺傜偣鐨勫潡鍙�
    			}
    			//缁撴潫寰幆锛岃繑鍥烇紝鍙兘鏈繘鍏ュ惊鍧忥紝offsets涓虹┖
				return keys;
    		}
    		else{//浠庡搴斿彾鑺傜偣鐨刦lag寮�濮�
    			for(j=flag; j<number; j++){
    				keys.add(leaf.keys.get(j));
    			}
    			i = leaf.children.get(number);
    			while(i!=-1){
    				tmp = nodes.get(i);
					n = tmp.childNum - 1;
					for(j=0; j<n; j++){
						keys.add(tmp.keys.get(j));
					}
					i = tmp.children.get(n);
    			}
				return keys;
    		}
    	}
    }
        
    public boolean insert_one(String value, int off){
    	//鑻ヤ负绌�,鍒涘缓鏍硅妭鐐癸紝涓旀牴鑺傜偣鏍囪涓哄彾鑺傜偣
    	if(Empty == 1){
    		Node root = create_node();
    		root.setEmpty(0);
    		root.setLeaf(1);//鏂板缓鏍戠殑鏍硅妭鐐逛篃鏄彾鑺傜偣
        	root.setFather(-1);//鏍硅妭鐐圭殑鐖跺潡鍙风疆涓�-1
        	root.setChildNum(2);//纭繚閿�间负0锛屾柟渚挎彃鍏�
        	root_block = nodes.indexOf(root);
        	
        	root.keys.add(value);
        	root.children.add(off);
        	root.children.add(-1);
        	
        	Empty = 0;//鏍戦潪绌�
        	return true;
    	}
    	
    	Node leaf = find(value);
    	int leafblock = nodes.indexOf(leaf);
    	int number = leaf.childNum-1;//瀵瑰簲鍙惰妭鐐归敭鍊兼暟
    	int i;
		int flag=0;//flag涓哄彾鑺傜偣涓彃鍏ヤ綅缃殑鍧愭爣
		for(i=0; i<number; i++){
			if(leaf.keys.get(i).compareTo(value) >= 0){//"="涔熼�夊彇褰撳墠i鍧愭爣
				if(leaf.keys.get(i).compareTo(value)==0) {
					System.out.println("閲嶅鎻掑叆鐩稿悓鍊硷紒");//閿欒淇℃伅
					return false;
				}
				flag = i;
				break;
			}
		}
		if(i == number) flag = i;//鍔犲湪鏈�鍚�
		
    	if(number <= (leafmax-1)){//鍙惰妭鐐规湁鎻掑叆绌洪棿   锛堥敭鍊煎垽鏂級		 
    		return insert_nondiv_leaf(leaf, value, off, flag);
    	}
    	else{//浠庡彾鑺傜偣寮�濮嬪垎瑁�
    		//ArrayList temp鎵挎帴
    		ArrayList<String> tempkeys = insert_to_temp_keys(number+1, flag, leaf, value);
    		ArrayList<Integer> tempoff = insert_to_temp_child(number+1, flag, leaf, off);

    		//鏂板缓鍙惰妭鐐瑰垎瑁傚苟杩炴帴
    		Node newleaf = create_node();
    		int newblock = nodes.indexOf(newleaf);
    		
    	//涓哄垎瑁傚悗鐨勪袱涓彾鑺傜偣璧嬪��
    		int fstN = nodemin;//绗竴涓敭鍊兼暟ORDER/2鍚戜笂鍙栨暣
    		int sndN = number+1 - fstN;
    		  		
    		//鍏堟洿鏂扮浜屼釜缁撶偣,鐖惰妭鐐瑰緟瀹�
    		newleaf.setLeaf(1);
    		newleaf.setEmpty(0);
    		newleaf.setChildNum(sndN+1);
    		copy_from_temp_keys(sndN, fstN, newleaf, tempkeys);
    		copy_from_temp_child(sndN, fstN, newleaf, tempoff);
    		newleaf.children.add(sndN, leaf.children.get(number));//鏂扮粨鐐圭殑涓嬩竴涓粨鐐逛负鍘熷彾鑺傜偣鐨勪笅涓�涓�
    		
    		//鏇存柊绗竴涓粨鐐癸紝鐖惰妭鐐逛笉鍙�
    		leaf.setChildNum(fstN+1);
    		leaf.keys.clear();
    		leaf.children.clear();
    		copy_from_temp_keys(fstN, 0, leaf, tempkeys);
    		copy_from_temp_child(fstN, 0, leaf, tempoff);
    		leaf.children.add(fstN, newblock);//閾炬帴鍒版柊鑺傜偣
    		
    	//灏嗘柊鑺傜偣鍚戜笂杩炴帴鍒版暣妫垫爲
    		if(root_block == leafblock){//鍙惰妭鐐逛负鏍�
    			Node rootnode = create_node();
    			int rootblock = nodes.indexOf(rootnode);
    			root_block = rootblock;
    			
    			rootnode.setChildNum(2);
    			rootnode.setEmpty(0);
    			rootnode.setLeaf(0);
    			rootnode.setFather(-1);
    			
    			rootnode.children.add(leafblock);
    			rootnode.children.add(newblock);
    			//鍚戜笅鎵惧埌鏈�宸﹁竟鐨勫彾鑺傜偣鑾峰彇鏍硅妭鐐圭殑閿��
    			Node root_mostleft = find_mostleft(newleaf);
    			rootnode.keys.add(root_mostleft.keys.get(0));
    			
    			leaf.setFather(rootblock);
    			newleaf.setFather(rootblock);
    			return true;   			
    		}
    		else return (insert_in_parent(newleaf, nodes.get(leaf.father_block)));
    	}
    }
    
    private boolean insert_in_parent(Node newnode, Node fathernode){
    	//鎵惧埌鍦ㄧ埗鑺傜偣涓殑鎻掑叆浣嶇疆
    	int i,flag=0;
    	Node mostleft = find_mostleft(newnode);
    	String value = mostleft.keys.get(0);//鑾峰緱瑕佹彃鍏ョ埗鑺傜偣鐨勯敭鍊硷紝涓簄ewnode涓嬫渶宸﹁竟鐨勫彾鑺傜偣鐨勭涓�涓敭鍊�
    	int childblock = nodes.indexOf(newnode);
    	int fatherblock = nodes.indexOf(fathernode);
    	
    	for(i=0; i<(fathernode.childNum-1); i++){//閬嶅巻鍒伴敭鍊兼暟
    		if(fathernode.keys.get(i).compareTo(value)>0){//涓嶅彲鑳芥湁鐩哥瓑鐨勬儏鍐�
    			flag = i;
    			break;
    		}
    	}
    	if(i == (fathernode.childNum-1)) flag = i;//鎻掑叆鍒版渶鍚�
    	
    //鏄惁鏈夌┖闂存彃鍏�
    	if(fathernode.childNum<=(ORDER-1)){//鐖惰妭鐐规湁鎻掑叆绌洪棿锛屾寚閽堟暟鍒ゆ柇
    		newnode.setFather(fatherblock);
    		return (insert_nondiv_node(fathernode, value, childblock, flag));//閫掑綊鍑哄彛1
    	}
    	else{    		
    		//閫掑綊鍒嗚鐖惰妭鐐�
    			//ArrayList temp鎵挎帴,childNum杩樻湭+1
    			ArrayList<String> tempkeys = insert_to_temp_keys(ORDER, flag, fathernode, value);
    			ArrayList<Integer> tempchild = insert_to_temp_child(ORDER+1, flag+1, fathernode, childblock);
        		
        		//鏂板缓鑺傜偣鍒嗚骞朵笌瀛愬潡杩炴帴
        		Node newfathernode = create_node();
        		int newfatherblock = nodes.indexOf(newfathernode);
        		
        	//涓哄垎瑁傚悗鐨勪袱涓妭鐐硅祴鍊�
        		int fst_childN = nodemin;//绗竴涓猚hild鏁癘RDER/2鍚戜笂鍙栨暣
        		int snd_childN = ORDER+1 - fst_childN;
        		int fst_keyN = fst_childN - 1;
        		int snd_keyN = snd_childN - 1;
        		
        		if(flag+2 <= fst_childN) newnode.setFather(fatherblock);
        		else newnode.setFather(newfatherblock);
        		
        		//鍏堟洿鏂扮浜屼釜缁撶偣,鐖惰妭鐐瑰緟瀹�
        		newfathernode.setLeaf(0);
        		newfathernode.setEmpty(0);
        		newfathernode.setChildNum(snd_childN);
        		copy_from_temp_child(snd_childN, fst_childN, newfathernode, tempchild);
        		//浠巘emp涓璫opy閿�硷紝浣嗘湭蹇呭叏閮ㄩ噰鐢紝绗竴涓敭鍊煎湪temp涓潗鏍囦笌绗簩涓粨鐐逛腑绗竴涓寚閽堝潗鏍囧搴�
        		copy_from_temp_keys(snd_keyN, fst_childN, newfathernode, tempkeys);//纭畾鏄鐨�
        		//鏇存柊鏂拌妭鐐逛互涓嬬殑鑺傜偣鐨刦atherblock
        		for(i=0; i<snd_childN; i++){
        			nodes.get(newfathernode.children.get(i)).setFather(newfatherblock);
        		}
        		
        		//鏇存柊鍘熺埗鑺傜偣,瀹冪殑鐖惰妭鐐逛笉鍙�
        		fathernode.setChildNum(fst_childN);
        		fathernode.keys.clear();
        		fathernode.children.clear();
        		fathernode.setEmpty(0);
        		fathernode.setLeaf(0);      		
        		copy_from_temp_child(fst_childN, 0, fathernode, tempchild);
        		copy_from_temp_keys(fst_keyN, 0, fathernode, tempkeys);
        		
        		if(fatherblock==root_block){//闇�瑕佹柊寤烘牴鑺傜偣锛岄�掑綊鍑哄彛2
        			//鏂板缓鏍硅妭鐐�
        			Node rootnode = create_node();
        			int rootblock = nodes.indexOf(rootnode);
        			root_block = rootblock;
        			rootnode.setFather(-1);
        			rootnode.setChildNum(2);
        			rootnode.setEmpty(0);
        			rootnode.setLeaf(0);
        			
        			rootnode.children.add(fatherblock);
        			rootnode.children.add(newfatherblock);
        			//鍚戜笅鎵惧埌鏈�宸﹁竟鐨勫彾鑺傜偣鑾峰彇鏍硅妭鐐圭殑閿��
        			Node root_mostleft = find_mostleft(newfathernode);
        			rootnode.keys.add(root_mostleft.keys.get(0));
        			
        			fathernode.setFather(rootblock);
        			newfathernode.setFather(rootblock);
        			return true;
        		}
        		//閫掑綊鐨勫皢鍒嗚寰楀埌鐨勬柊鑺傜偣鎻掑叆鏁存５鏍�
        		else return insert_in_parent(newfathernode, nodes.get(fathernode.father_block));   		
    	}
    }
    
    public boolean delete_one(String value){
    	Node leaf = find(value);
    	int leafblock = nodes.indexOf(leaf);
    	int fatherblock = leaf.father_block;
    	int number = leaf.childNum-1;//瀵瑰簲鍙惰妭鐐归敭鍊兼暟
    	int i;
		int flag=0;//flag涓哄彾鑺傜偣涓垹闄ら敭鍊肩殑鍧愭爣
		for(i=0; i<number; i++){
			if(leaf.keys.get(i).compareTo(value) == 0){
				flag = i;
				break;
			}
		}
		if(i == number){
			System.out.println("BPlusTree:delete_one:鏈壘鍒板搴斿��");
			return false;
		}
	//鍒嗘儏鍐靛垽鏂紝鍙拡瀵瑰彾鑺傜偣
		//鍙惰妭鐐逛负鏍硅妭鐐�
		if(leafblock == root_block){
			if(number >= 2){//澶熷ぇ
				return delete_noncoa_leaf(leaf, flag);
			}   						
			else{//BPTree涓虹┖锛孍mpty=1鏍囪鍒犻櫎鏁存５鏍�
				Empty = 1;
				clear_node(leaf);
				root_block = -1;
				return true;
			}
		}
		//鍙惰妭鐐逛笉鏄牴鑺傜偣
		else{
			if(delete_noncoa_leaf(leaf, flag)){//鍒犻櫎瀵瑰簲閿�煎拰鎸囬拡锛屾寚閽堟暟宸茬粡鍑�1
				number = leaf.childNum-1;
				if(flag == 0)	update_fatherkey_fromchild(leaf, value);
				
				//璇ュ彾鑺傜偣鍙互瀛樺湪
				if(number >= leafmin){					
					//鑻ヤ笉婊¤冻if鏉′欢鏃犻渶鏇存敼鍏朵粬
					return true;					
				}
				
				//璇ュ彾鑺傜偣澶皬涓嶅彲瀛樺湪
				else{					
					if(number == 0){//璇ュ彾鑺傜偣涓凡缁忔病鏈夐敭鍊硷紝鍙洿鎺ュ垹闄�
						//杩炴帴鍓嶅悗鍙惰妭鐐�	
						int last_leaf_block = find_last_leaf(leaf);
						if(last_leaf_block!=-1){//瀛樺湪鍓嶄竴涓彾鑺傜偣
							Node last_leaf = nodes.get(last_leaf_block);
							last_leaf.children.set(last_leaf.childNum-1, leaf.children.get(0));
						}
						clear_node(leaf);
						//閫掑綊鍦板鐖惰妭鐐瑰垹闄ゆ寚鍚戣鍙惰妭鐐圭殑鎸囬拡
						return delete_entry(nodes.get(fatherblock), leafblock);
					}
					else{//澶嶆潅鎯呭喌锛岄渶瑕佸悎骞舵垨閲嶆柊鍒嗛厤
						//鍏堟壘鍓嶄竴涓厔寮熷彾鑺傜偣
						int last_sibling_block = find_last_sibling(leaf);
						
						//瀛樺湪鍓嶄竴涓厔寮�,鍒欒瘯鐫�涓庡墠涓�涓厔寮熷悎骞�
						if(last_sibling_block != -1){
							Node last_sibling = nodes.get(last_sibling_block);
							int last_sibling_number = last_sibling.childNum-1;
							
							//鍓嶄竴涓厔寮熷彲浠ュ悎骞�
							if((last_sibling_number + number) <= leafmax){
								//灏唋eaf涓墿浣欎俊鎭斁鍒板墠涓�涓厔寮�
								for(i=0; i<number; i++){//閿�奸亶鍘�
									last_sibling.keys.add(i+last_sibling_number, leaf.keys.get(i));
									last_sibling.children.add(i+last_sibling_number, leaf.children.get(i));
								}
								last_sibling.children.add(i, leaf.children.get(i));//娣诲姞鏈�鍚庝竴涓寚閽堥摼鎺ュ埌leaf鐨勪笅涓�涓彾鑺傜偣
													
								last_sibling.setChildNum(last_sibling.childNum+leaf.childNum-1);
								clear_node(leaf);
								
								return delete_entry(nodes.get(fatherblock), leafblock);
								//涓庡墠涓�涓厔寮熷悎骞舵垚鍔�
							}
							
							//鍓嶄竴涓厔寮熷お澶т笉鍙悎骞讹紝鎵惧悗涓�涓厔寮�
							else{
								int next_sibling_block = find_next_sibling(leaf);
								
								//瀛樺湪鍚庝竴涓厔寮�
								if(next_sibling_block != -1){
									Node next_sibling = nodes.get(next_sibling_block);
									int next_sibling_number = next_sibling.childNum-1;
									
									//鍚庝竴涓厔寮熷彲浠ュ悎骞�
									if((next_sibling_number + number) <= leafmax){
										coa_nextsibling_leaf(leaf, next_sibling_block);
										
										//杩炴帴鍓嶄竴涓彾鑺傜偣鍒皀ext_sibling_block
										last_sibling.children.set(last_sibling_number, next_sibling_block);
										update_fatherkey(nodes.get(leaf.father_block), next_sibling_block);
										clear_node(leaf);
										
										return delete_entry(nodes.get(fatherblock), leafblock);
										//涓庡悗涓�涓厔寮熷悎骞舵垚鍔�
									}
									
									//鍚庝竴涓厔寮熶笉鍙悎骞�
									else{//浠庡墠涓�涓厔寮熶腑鍊熶竴涓敭鍊硷紝鍘熷彾鑺傜偣浠ユ柊鐨勬柟寮忓瓨鍦�
										borrow_lastkey_leaf(leaf, last_sibling_block);
										
										//鍊熼敭鍊煎悗锛堝凡缁忕‘瀹氭湁宸﹀厔寮燂級鍙惰妭鐐圭殑绗竴涓敭鍊兼敼鍙橈紝闇�瑕佹洿鏂扮埗鑺傜偣鐨勭储寮�
										Node father = nodes.get(leaf.father_block);
										//閬嶅巻鎸囬拡锛屾壘鍒扮浉搴旀寚閽堟洿鏀瑰墠闈㈢殑绱㈠紩
										for(i=1; i<father.childNum; i++){//涓嶅彲鑳芥槸绗竴涓寚閽�
											if(father.children.get(i) == leafblock){
												father.keys.set(i-1, leaf.keys.get(0));
												break;
											}
										}
										return true;
									}
								}
								
								//娌℃湁鍚庝竴涓厔寮燂紝鐩存帴浠庡墠涓�涓厔寮熷�熺储寮�
								else{
									borrow_lastkey_leaf(leaf, last_sibling_block);
									
									//鍊熼敭鍊煎悗锛堝凡缁忕‘瀹氭湁宸﹀厔寮燂級鍙惰妭鐐圭殑绗竴涓敭鍊兼敼鍙橈紝闇�瑕佹洿鏂扮埗鑺傜偣鐨勭储寮�
									Node father = nodes.get(leaf.father_block);
									//閬嶅巻鎸囬拡锛屾壘鍒扮浉搴旀寚閽堟洿鏀瑰墠闈㈢殑绱㈠紩
									for(i=1; i<father.childNum; i++){//涓嶅彲鑳芥槸绗竴涓寚閽�
										if(father.children.get(i) == leafblock){
											father.keys.set(i-1, leaf.keys.get(0));
											break;
										}
									}
									return true;
								}
							}
							
						}
						
						else{//鍓嶄竴涓厔寮熶笉瀛樺湪锛屾壘鍚庝竴涓厔寮�
							int next_sibling_block = find_next_sibling(leaf);
							Node next_sibling = nodes.get(next_sibling_block);
							int next_sibling_number = next_sibling.childNum-1;
							
							//鍚庝竴涓厔寮熷彲浠ュ悎骞�
							if((next_sibling_number + number) <= leafmax){
								coa_nextsibling_leaf(leaf, next_sibling_block);	
								update_fatherkey(nodes.get(leaf.father_block), next_sibling_block);
								clear_node(leaf);
								return delete_entry(nodes.get(fatherblock), leafblock);
								//涓庡悗涓�涓厔寮熷悎骞舵垚鍔�
							}
							else{//鍚戝悗涓�涓厔寮熷�熺储寮�
								borrow_nextkey_leaf(leaf, next_sibling_block);
								
								//鍊熼敭鍊煎悗鍚庝竴涓厔寮熷彾鑺傜偣绗竴涓敭鍊兼敼鍙橈紝闇�瑕佹洿鏂扮埗鑺傜偣鐨勭储寮�
								Node father = nodes.get(leaf.father_block);
								//閬嶅巻鎸囬拡锛屾壘鍒扮浉搴旀寚閽堟洿鏀瑰墠闈㈢殑绱㈠紩
								for(i=1; i<father.childNum; i++){//涓嶅彲鑳芥槸绗竴涓寚閽�
									if(father.children.get(i) == next_sibling_block){
										father.keys.set(i-1, next_sibling.keys.get(0));
										break;
									}
								}
								return true;
							}
						}
					}
				}
			}
			else{
				System.out.println("error when delete_noncoa_leaf(leaf, flag) return false");
				return false;
			}
		}
		
    }
    
    public boolean delete_all(){
    	for(int i=0; i<nodes.size(); i++){
    		if(nodes.get(i).isEmpty == 0) clear_node(nodes.get(i));
    	}
    	Empty = 1;
    	root_block = -1;
    	return true;
    }
    
    //鍙傛暟涓哄緟鎿嶄綔鐨勮妭鐐瑰拰鑺傜偣涓鍒犻櫎鐨勬寚閽堬紝闇�瑕佸彟鍙栧搴旇鎸囬拡鐨勯敭鍊奸噸鏂板垽鏂�
    private boolean delete_entry(Node node, int child){
    	int nodeblock = nodes.indexOf(node);
    	int fatherblock = node.father_block;
    	int number = node.childNum-1;//瀵瑰簲鍙惰妭鐐归敭鍊兼暟
    	int i;
		int flag=0;//flag涓哄彾鑺傜偣涓垹闄ゆ寚閽堢殑鍧愭爣
		for(i=0; i<node.childNum; i++){
			if(node.children.get(i) == child){
				flag = i;
				break;
			}
		}
		if(i == node.childNum){
			System.out.println("BPlusTree:delete_entry:鏈壘鍒板搴旀寚閽�");
			return false;
		}
		
		//鍒犻櫎鐖惰妭鐐逛腑鐨勫搴旀寚閽堬紝鎸囬拡鍦ㄦ渶鍓嶉潰闇�瑕佸彟澶栨搷浣�
		if(flag == 0) {
			String key = node.keys.get(0);
			delete_noncoa_leaf(node, 0);//瀛愬コ鏁板凡缁忓噺1
			update_fatherkey_fromchild(node, key);
		}
		else{
			node.children.remove(flag);
			node.keys.remove(flag-1);
			
			node.setChildNum(number);//瀛愬コ鏁板噺1
		}
		
	//鍒嗘儏鍐靛垽鏂垹闄ゅ悗鐨勯潪鍙剁粨鐐硅兘鍚﹀瓨鍦�
		//褰撳墠缁撶偣鏄牴	
		if(root_block == nodeblock){
			if(node.childNum == 1){//鏍硅妭鐐瑰彧鏈変竴涓瀛愶紝璁╀粬鍞竴鐨勫瀛愭垚涓烘牴
				root_block = node.children.get(0);
				nodes.get(root_block).setFather(-1);
				clear_node(node);
				return true;//閫掑綊鍑哄彛
			}
			else return true;//閫掑綊鍑哄彛
		}
		//褰撳墠缁撶偣闈炴牴
		else{			
			if(node.childNum >= nodemin) return true;//閫掑綊鍑哄彛
			//缁撶偣涓嶅澶т笉鍙互瀛樺湪锛屼絾涓�瀹氫粛鐒跺瓨鏈夋湁鐢ㄤ俊鎭紝鍚堝苟鎴栭噸鏂板垎閰嶇殑澶嶆潅鎯呭喌
			else{
			//鍏堟壘鍓嶄竴涓厔寮�
				int last_sibling_block = find_last_sibling(node);
				
				//鏈夊墠涓�涓厔寮�
				if(last_sibling_block != -1){
					//璇曠潃涓庡墠涓�涓厔寮熷悎骞�
					Node last_sibling = nodes.get(last_sibling_block);

					//鍓嶄竴涓厔寮熷彲浠ュ悎骞�
					if((last_sibling.childNum + node.childNum) <= ORDER){
						last_sibling.keys.add("");//鍏堜互绌洪敭鍊艰ˉ瓒�
						last_sibling.keys.addAll(node.keys);
						last_sibling.children.addAll(node.children);

						
						update_fatherkey(last_sibling, node.children.get(0));//鏇存柊last_sibling鐨勯敭鍊�						
						
						//鏇存柊鍘熺粨鐐逛笅瀛愬コ鐨剆etfather
						set_childrens_father(node, last_sibling_block);						
						clear_node(node);
						
						return delete_entry(nodes.get(fatherblock), nodeblock);
					}
					//鍓嶄竴涓厔寮熷お澶т笉鍙悎骞讹紝鎵惧悗涓�涓厔寮�
					else{
						int next_sibling_block = find_next_sibling(node);
						
						//瀛樺湪鍚庝竴涓厔寮�
						if(next_sibling_block != -1){
							Node next_sibling = nodes.get(next_sibling_block);
							
							//鍚庝竴涓厔寮熷彲浠ュ悎骞�
							if((next_sibling.childNum + node.childNum) <= ORDER){
								coa_nextsibling_node(node, next_sibling_block);
								update_fatherkey(nodes.get(node.father_block), next_sibling_block);
								set_childrens_father(node, next_sibling_block);
								clear_node(node);
								return delete_entry(nodes.get(fatherblock), nodeblock);
							}
							
							//鍚庝竴涓厔寮熶笉鍙互鍚堝苟锛屽悜鍓嶄竴涓厔寮熷�熺储寮�
							else{
								borrow_last_key_node(node, last_sibling_block);
								update_fatherkey(nodes.get(node.father_block), nodeblock);
								set_childrens_father(node, nodeblock);
								return true;
							}
						}
						
						//涓嶅瓨鍦ㄥ悗涓�涓厔寮燂紝鐩存帴鍚戝墠涓�涓厔寮熷�熺储寮�
						else{
							borrow_last_key_node(node, last_sibling_block);
							update_fatherkey(nodes.get(node.father_block), nodeblock);
							set_childrens_father(node, nodeblock);
							return true;
						}
					}
				}
				
				//娌℃湁鍓嶄竴涓厔寮燂紝鐩存帴鎵惧悗涓�涓厔寮�
				else{
					int next_sibling_block = find_next_sibling(node);
					Node next_sibling = nodes.get(next_sibling_block);
					
					//鍚庝竴涓厔寮熷彲浠ュ悎骞�
					if((next_sibling.childNum + node.childNum) <= ORDER){
						coa_nextsibling_node(node, next_sibling_block);
						update_fatherkey(nodes.get(node.father_block), next_sibling_block);
						set_childrens_father(node, next_sibling_block);
						clear_node(node);
						return delete_entry(nodes.get(fatherblock), nodeblock);
					}
					//鍚庝竴涓厔寮熶笉鍙互鍚堝苟锛屽悜鍚庝竴涓厔寮熷�熺储寮�
					else{
						borrow_next_key_node(node, next_sibling_block);
						update_fatherkey(nodes.get(node.father_block), next_sibling_block);
						set_childrens_father(node, nodeblock);
						return true;
					}
				}
			}
		}
		
    }
           
    private boolean delete_noncoa_leaf(Node leaf, int flag){
    	int i;
    	leaf.setChildNum(leaf.childNum-1);//淇敼childnum
    	//flag鍚庣殑鏁翠綋鍓嶇Щ
    	for(i=flag; i<(leaf.childNum-1); i++){//浠ユ柊閿�兼暟閬嶅巻
    		leaf.keys.set(i, leaf.keys.get(i+1));//i+1鑳藉彇鍒版暟缁勭殑鏈�鍚庝竴涓�硷紙鍘熸潵鐨剆ize澶�1锛�
    		leaf.children.set(i, leaf.children.get(i+1));
    	}
    	leaf.children.set(i, leaf.children.get(i+1));//鎸囬拡鏁板1
    	leaf.keys.remove(i);//鍒犻櫎鏈�鍚庝竴涓�
    	
    	leaf.children.remove(i+1);
    	return true;
    }
    
    private void coa_nextsibling_node(Node node, int next_sibling_block){
    	Node next_sibling = nodes.get(next_sibling_block);
    	Node leaf_next_sibling = find_mostleft(next_sibling);
    	String fstkey = leaf_next_sibling.keys.get(0);
    	
    	//鍏堣祴鍊肩粰temp鏁扮粍
    	ArrayList<String> tempkeys = new ArrayList<>(ORDER-1);
    	ArrayList<Integer> tempchild = new ArrayList<>(ORDER);
    	tempkeys.addAll(node.keys);
    	tempchild.addAll(node.children);
    	tempkeys.add(fstkey);
    	tempkeys.addAll(next_sibling.keys);
    	tempchild.addAll(next_sibling.children);
    	
    	//浠巘emp鏁扮粍涓嫹璐�
    	next_sibling.keys.clear();
    	next_sibling.children.clear();
    	copy_from_temp_keys(node.childNum+next_sibling.childNum-1, 0, next_sibling, tempkeys);
    	copy_from_temp_child(node.childNum+next_sibling.childNum, 0, next_sibling, tempchild);
    	
    	next_sibling.setChildNum(next_sibling.childNum+node.childNum);
    }
    
    private void coa_nextsibling_leaf(Node leaf, int next_sibling_block){
    	Node next_sibling = nodes.get(next_sibling_block);
    	int next_sibling_number = next_sibling.childNum-1;
    	int leaf_number = leaf.childNum-1;
    	//鍏堣祴鍊肩粰temp鏁扮粍   	
    	ArrayList<String> tempkeys = new ArrayList<>(ORDER-1);
    	ArrayList<Integer> tempchild = new ArrayList<>(ORDER);
    	
    	tempkeys.addAll(leaf.keys);
    	tempchild.addAll(leaf.children);
    	tempkeys.addAll(next_sibling.keys);
    	tempchild.addAll(leaf.childNum-1, next_sibling.children);//瑕嗙洊鍓嶄竴鑺傜偣鐨勬渶鍚庝竴涓寚閽�

    	//浠巘emp鏁扮粍涓嫹璐�
    	next_sibling.keys.clear();
    	next_sibling.children.clear();
    	copy_from_temp_keys(leaf_number+next_sibling_number, 0, next_sibling, tempkeys);
    	copy_from_temp_child(leaf_number+next_sibling_number+1, 0, next_sibling, tempchild);
    	
    	next_sibling.setChildNum(next_sibling.childNum+leaf.childNum-1);
    }
    
    private void borrow_nextkey_leaf(Node leaf, int next_sibling_block){
    	Node next_sibling = nodes.get(next_sibling_block);
    	int next_sibling_number = next_sibling.childNum-1;
    	String key = next_sibling.keys.get(0);
    	int child = next_sibling.children.get(0);
    	int number = leaf.childNum-1;
    	int i;
    	//鍦╨eaf缁撶偣鏈熬鎻掑叆key鍜宑hild
    	insert_nondiv_leaf(leaf, key, child, number);//淇敼浜嗗瓙濂虫暟
    	
    	//next_sibling闆嗕綋鍓嶇Щ
    	for(i=0; i<next_sibling_number-1; i++){
    		next_sibling.keys.set(i, next_sibling.keys.get(i+1));
    		next_sibling.children.set(i, next_sibling.children.get(i+1));
    	}
    	next_sibling.children.set(i, next_sibling.children.get(i+1));
    	
    	next_sibling.keys.remove(next_sibling_number-1);
    	next_sibling.children.remove(next_sibling_number);
    	
    	next_sibling.setChildNum(next_sibling_number);
    }
    
    private void borrow_lastkey_leaf(Node leaf, int last_sibling_block){
    	Node last_sibling = nodes.get(last_sibling_block);
    	int last_sibling_number = last_sibling.childNum-1;
    	String key = last_sibling.keys.get(last_sibling_number-1);
    	int child = last_sibling.children.get(last_sibling_number-1);
    	
    	//鍦╨eaf缁撶偣鐨勬渶鍓嶉潰鎻掑叆key鍜宑hild
    	insert_nondiv_leaf(leaf, key, child, 0);//淇敼浜嗗瓙濂虫暟
    	
    	last_sibling.children.set(last_sibling_number-1, last_sibling.children.get(last_sibling_number));
    	last_sibling.children.remove(last_sibling_number);
    	last_sibling.keys.remove(last_sibling_number-1);
    	
    	last_sibling.setChildNum(last_sibling_number);
    }
    
    private void borrow_last_key_node(Node node, int last_sibling_block){
    	Node last_sibling = nodes.get(last_sibling_block);
    	String key = find_mostleft(node).keys.get(0);
    	int child = last_sibling.children.get(last_sibling.childNum-1);
    	
    	insert_nondiv_leaf(node, key, child, 0);
    	
    	last_sibling.children.remove(last_sibling.childNum-1);
    	last_sibling.keys.remove(last_sibling.childNum-2);
    	
    	last_sibling.setChildNum(last_sibling.childNum-1);
    }
    
    private void borrow_next_key_node(Node node, int next_sibling_block){
    	Node next_sibling = nodes.get(next_sibling_block);
    	String key = find_mostleft(next_sibling).keys.get(0);
    	int child = next_sibling.children.get(0);
    	
    	node.keys.add(key);
    	node.children.add(child);
    	node.setChildNum(node.childNum+1);
    	
    	next_sibling.setChildNum(next_sibling.childNum-1);
    	next_sibling.keys.remove(0);
    	next_sibling.children.remove(0);
//    	for(i=0; i<next_sibling.childNum-1; i++){
//    		next_sibling.children.set(i, next_sibling.children.get(i+1));
//    		next_sibling.keys.set(i, next_sibling.keys.get(i+1));
//    	}
//    	next_sibling.children.set(i, next_sibling.children.get(i+1));
//    	
//    	next_sibling.keys.remove(i);
//    	next_sibling.children.remove(i+1);
    }
 
    public boolean write_BPlusTree(String filename){
		try {  
			File file = new File(filename);
			if(!file.exists()){
				file.createNewFile();
			}
            FileWriter fileWriter = new FileWriter(file);            
            BufferedWriter bw = new BufferedWriter(fileWriter);
            
            String s = "!"+ Integer.toString(ORDER) + '\t' + Integer.toString(root_block) + "\t" 
            			+ Integer.toString(empty_block) + "\t" + Integer.toString(Empty) + "\t";
            bw.write(s); 
            bw.newLine();
            
            for(int i=0; i<nodes.size(); i++){
            	bw.write(node_toString(nodes.get(i)));
            	bw.newLine();
            }
            
            bw.close();
            fileWriter.close(); // 鍏抽棴鏁版嵁娴�   
        }catch(Exception e) {
            e.printStackTrace();
        }

		return true;
	}
    
    private String node_toString(Node node){
    	int i;
    	String nodeString = "#" + Integer.toString(node.isEmpty) + "\t" + Integer.toString(node.isLeaf) + "\t" 
    						+ Integer.toString(node.father_block) + "\t" + Integer.toString(node.childNum) + "\t";
    	for(i=0; i<node.childNum; i++){
    		nodeString += Integer.toString(node.children.get(i));
    		nodeString += "\t";
    	}

    	for(i=0; i<node.childNum-1; i++){
    		nodeString += node.keys.get(i);
    		nodeString += "\t";
    	}
    	return nodeString;
    }
    
    private void read_tonode(String templine){
    	int indexl, indexr, i;
    	String temp;
    	Node node = new Node(ORDER);
    	node.ORDER = ORDER;
    	
    	indexl = 1;
		indexr = templine.indexOf('\t', indexl);
		temp = templine.substring(indexl, indexr);
		node.isEmpty = Integer.parseInt(temp);
		
		indexl = indexr+1;
		indexr = templine.indexOf('\t', indexl);
		temp = templine.substring(indexl, indexr);
		node.isLeaf = Integer.parseInt(temp);
		
		indexl = indexr+1;
		indexr = templine.indexOf('\t', indexl);
		temp = templine.substring(indexl, indexr);
		node.father_block = Integer.parseInt(temp);
		
		indexl = indexr+1;
		indexr = templine.indexOf('\t', indexl);
		temp = templine.substring(indexl, indexr);
		node.childNum = Integer.parseInt(temp);
		
		for(i=0; i<node.childNum; i++){
			indexl = indexr+1;
			indexr = templine.indexOf('\t', indexl);
			temp = templine.substring(indexl, indexr);
			node.children.add(Integer.parseInt(temp));
		}
		for(i=0; i<node.childNum-1; i++){
			indexl = indexr+1;
			indexr = templine.indexOf('\t', indexl);
			temp = templine.substring(indexl, indexr);
			node.keys.add(temp);
		}
		nodes.add(node);
    }
    
  //鏃犻渶鍒嗚鍦版彃鍏ヤ竴涓彾鑺傜偣锛宖lag涓烘彃鍏ョ殑浣嶇疆锛屼笉鏀瑰彉鏈�鍚庢寚閽堝涓嬩竴涓潡鐨勮繛鎺�
    private boolean insert_nondiv_leaf(Node leaf, String value, int off, int flag){
    	leaf.setChildNum(leaf.childNum+1);
    	//flag鍚庣殑鍊兼暣浣撳悗绉�
    	leaf.children.add(flag, off);
    	leaf.keys.add(flag, value);

		return true;
    }
    
    private boolean insert_nondiv_node(Node node, String value, int childblock, int flag){
    	node.setChildNum(node.childNum+1);
    	
    	node.children.add(flag+1, childblock);
    	node.keys.add(flag, value);
    	
    	return true;
    }
    
    private Node find(String value){//鏌ユ壘鏌愪釜閿�兼墍鍦ㄧ殑鍙惰妭鐐�
    	return find(nodes.get(root_block), value);//浠庢牴鑺傜偣閫掑綊
    }
    
    private Node find(Node node, String value){
    	if(node.isLeaf == 1){//鍒ゆ柇鏄惁涓哄彾鑺傜偣
    		return node;
    	}
    	else{
    		int i;
    		for(i=0; i<(node.childNum-1); i++){//閬嶅巻閿�硷紝鏁扮洰姣旀寚閽堝皯1
    			if (node.keys.get(i).compareTo(value) > 0) //绛変簬杩涘叆鍙宠竟鎸囬拡
    				return find(nodes.get(node.children.get(i)), value);//杩涘叆纭畾閿�肩殑宸﹁竟鎸囬拡
        	}
    		return find(nodes.get(node.children.get(i)), value);//瀹屾垚寰幆鍧囨病鏈夋壘鍒扮鍚堟潯浠堕敭鍊硷紝杩涘叆鏈�鍙虫寚閽�
    	}   	
    }
    
    private int find_mostleft_leaf(){//鎵惧埌涓�妫垫爲鐨勬渶宸﹁竟鍙惰妭鐐瑰潡鍙�
    	Node leaf = find_mostleft(nodes.get(root_block));
    	return nodes.indexOf(leaf);
    }
    
    private Node find_mostleft(Node node){//鎵惧埌涓�涓妭鐐逛笅鐨勬渶宸﹁竟鍙惰妭鐐�
    	if(node.isLeaf == 1) return node;
    	else return find_mostleft(nodes.get(node.children.get(0)));
    }
    
    private int find_last_sibling(Node node){//杩斿洖鍧楀彿
    	if(node.father_block == -1) return -1;//璇ヨ妭鐐逛负鏍硅妭鐐癸紝娌℃湁鍏勫紵
    	else{
    		Node fathernode = nodes.get(node.father_block);
        	int nodeblock = nodes.indexOf(node); 
        	int childN = fathernode.childNum;
        	int i;
        	for(i=0; i<childN; i++){
        		if(fathernode.children.get(i) == nodeblock) break;
        	}
        	if(i==0) return -1;//璇ヨ妭鐐逛负绗竴涓瓙鍧楋紝娌℃湁鍓嶄竴涓厔寮�
        	else return fathernode.children.get(i-1);
    	}   	
    }
    
    private int find_next_sibling(Node node){
    	if(node.father_block == -1) return -1;
    	else{
    		Node fathernode = nodes.get(node.father_block);
        	int nodeblock = nodes.indexOf(node); 
        	int childN = fathernode.childNum;
        	int i;
        	for(i=0; i<childN-1; i++){
        		if(fathernode.children.get(i) == nodeblock){
        			break;
        		}
        	}
        	if(i==childN-1) return -1;//璇ヨ妭鐐逛负鏈�鍚庝釜瀛愬潡锛屾病鏈夊悗涓�涓厔寮�
        	else return fathernode.children.get(i+1);
    	}
    }
    
    private int find_last_leaf(Node leaf){
    	//浠庢渶宸﹁竟鍙惰妭鐐归亶鍘�
    	int mostleft_leaf_block = find_mostleft_leaf();
    	int leaf_block = nodes.indexOf(leaf);
    	if(mostleft_leaf_block == leaf_block) return -1;//娌℃湁鍓嶄竴涓彾鑺傜偣
    	Node temp = nodes.get(mostleft_leaf_block);
    	int nextblock = temp.children.get(temp.childNum-1);
    	while(nextblock != leaf_block){//鐩哥瓑鏃跺嵆涓鸿鎵剧殑鍓嶄竴涓彾鑺傜偣
    		temp = nodes.get(nextblock);
    		nextblock = temp.children.get(temp.childNum-1);
    	}
    	return nodes.indexOf(temp);
    }
    
    private Node create_node(){
    	Node newnode;
		if(empty_block==-1){//鏍戜腑娌℃湁绌哄潡锛屾柊寤�
			newnode = new Node(ORDER);
			nodes.add(newnode);//鍔犲叆nodes
		}
		else{//鍦ㄧ┖缁撶偣閾捐〃涓幏寰楃┖鍧�
			newnode = nodes.get(empty_block);
			empty_block = newnode.children.get(0);//鏇存柊绌虹粨鐐归摼琛紝涓嬩竴涓┖缁撶偣鍙�
			newnode.children.clear();
			newnode.keys.clear();
		}		
		return newnode;
    }
    
  //娓呯┖涓�涓妭鐐瑰苟灏嗗畠鍔犲叆绌洪摼琛紙nodes涓笉鍒犻櫎绌洪棿锛�
    private void clear_node(Node node){
    	node.children.clear();
		node.keys.clear();
		node.setEmpty(1);//鏍囪涓虹┖
		node.setChildNum(1);//绌洪摼琛ㄦ寚閽�
		node.setFather(-1);//鏃犲叧鍙橀噺缃负-1
		node.setLeaf(-1);
		
		if(empty_block == -1){//杩樻湭鍒涘缓绌洪摼琛紝node涓虹┖閾捐〃涓敮涓�鐨勮妭鐐�
			empty_block = nodes.indexOf(node);
			node.children.add(-1);
		}
		else{
			node.children.add(empty_block);
			empty_block = nodes.indexOf(node);
		}
    }
    
    //鎻掑叆鍒颁复鏃舵暟缁勶紝size涓烘暟缁勫ぇ灏�,flag涓烘彃鍏ョ殑鍧愭爣锛宯ode涓烘嫹璐濇簮鑺傜偣锛� value涓烘彃鍏ュ��
    private ArrayList<String> insert_to_temp_keys(int size, int flag, Node node, String value){
    	int i;
		ArrayList<String> tempkeys = new ArrayList<>(size);
		for(i=0; i<flag; i++){
			tempkeys.add(node.keys.get(i));
		}
		tempkeys.add(value);//鎻掑叆
		for(i=flag+1; i<size; i++){
			tempkeys.add(node.keys.get(i-1));
		}
		return tempkeys;
    }
    
    private ArrayList<Integer> insert_to_temp_child(int size, int flag, Node node, int m){
    	int i;
		ArrayList<Integer> tempoff = new ArrayList<>(size);
		for(i=0; i<flag; i++){
			tempoff.add(node.children.get(i));
		}
		tempoff.add(m);
		for(i=flag+1; i<size; i++){
			tempoff.add(node.children.get(i-1));
		}
		return tempoff;
    }
    
    //浠庝复鏃舵暟缁勪腑鎷疯礉锛宻ize涓烘嫹璐濆ぇ灏忥紝start涓簍emp鏁扮粍涓殑璧峰鍧愭爣
    private void copy_from_temp_keys(int size, int start, Node node, ArrayList<String> tempkeys){
    	int i;
    	for(i=0; i<size; i++){
			node.keys.add(i, tempkeys.get(i+start));
		}   	
    }
    
    private void copy_from_temp_child(int size, int start, Node node, ArrayList<Integer> tempchild){
    	int i;
    	for(i=0; i<size; i++){
			node.children.add(i, tempchild.get(i+start));
		}   	
    }
    
    private void set_childrens_father(Node deletenode, int newfather){
    	int i;
    	//鏇存柊鍘熺粨鐐逛笅瀛愬コ鐨刦ather
		for(i=0; i<deletenode.childNum; i++){
			nodes.get(deletenode.children.get(i)).setFather(newfather);
		}
    }
    
    private void update_fatherkey(Node father, int child){//缁欏畾瀛愬コ鐨勬寚閽堬紝鏇存柊璇ユ寚閽堝墠鐨勭储寮曞��
    	Node leaf = find_mostleft(nodes.get(child));
    	
    	int i;
    	for(i=1; i<father.childNum; i++){//涓嶅彲鑳芥槸绗竴涓寚閽�
			if(father.children.get(i) == child){
				father.keys.set(i-1, leaf.keys.get(0));
				break;
			}
		}
    	if(i == father.childNum) System.out.println("error_update_fatherkey!");
    }
    
    //鍙傛暟涓哄鑷存洿鏂伴敭鍊肩殑瀛愯妭鐐瑰拰鍘熼敭鍊�
    private void update_fatherkey_fromchild(Node childnode, String key){
    	Node temp = childnode;
    	int flag = 0;
    	while(temp.father_block != -1){//褰撳墠缁撶偣涓烘牴鑺傜偣鏃跺仠姝㈡煡鎵鹃渶瑕佹洿鏂伴敭鍊肩殑鐖惰妭鐐�
    		int i;
    		if(find_last_sibling(temp) != -1){//褰撳墠缁撶偣鏈夊墠涓�涓厔寮燂紝闇�瑕佹洿鏂伴敭鍊肩殑鑺傜偣涓鸿缁撶偣鐨勭埗浜�
    			Node father = nodes.get(temp.father_block);
    			for(i=0; i<father.childNum-1; i++){
    				if(father.keys.get(i).compareTo(key) == 0){//鎵惧埌瑕佹洿鏂扮殑閿��
    					Node leaf = find_mostleft(nodes.get(father.children.get(i+1)));
    					String newkey = leaf.keys.get(0);
    					father.keys.set(i, newkey);
    					flag = 1;
    					break;
    				}
    			}
    			if(i == father.childNum-1){    				
    				System.out.println("error_update_fatherkey_fromchild");
    				return;
    			}
    		}
    		else temp = nodes.get(temp.father_block);
    		if(flag == 1) break;
    	}
    }

}