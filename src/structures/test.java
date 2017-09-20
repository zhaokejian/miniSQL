package structures;

public class test {
	
	
	public static void main(String args[]){
		STATEMENT_TYPE s = STATEMENT_TYPE.CREATE_INDEX;
		switch(s){
		case CREATE_INDEX:
			System.out.println("yes");
			break;
		default:
			System.out.println("no");
			break;
		}
	}
}
