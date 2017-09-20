import java.util.*;
import java.io.*;
import structures.Table;
import structures.InsPos;

class Buffer {
    public final static int BLOCKSIZE = 4096;
    public final static byte EMPTY = 0xFFFFFFFF;

    String fileName;     //这块buffer对应的文件名
    int blockNum;		//这块buffer是文件的第几个块
//    int LRU;
    public byte[] value = new byte[BLOCKSIZE + 1];  
//    boolean isValid;
    boolean isWritten;

    public Buffer() {
//        isValid = false;
        isWritten = false;
//        LRU = 0;
        blockNum = 0;
        fileName = "";
        for (int i = 0; i<=BLOCKSIZE; i++) { value[i] = EMPTY; }
    }

    public int getBlockNum() {
        return blockNum;
    }

    public String getFileName() {
        return fileName;
    }
    
    public byte getValue(int pos) {
        if (pos>=0 && pos<BLOCKSIZE) return value[pos];
        return -1;
    }
}

public class BufferManager {
	public final static int MAXBLOCKNUM = 1024;
	public LinkedList<Buffer> bufferlist = new LinkedList<Buffer>();
	
	public BufferManager(){
		Buffer fisrt = new Buffer();
		bufferlist.add(fisrt);
	}
//	public ~Buffer_manager(){
//		
//	}
	
    @Override
    protected void finalize() throws Throwable {
        for (int i = 0; i<bufferlist.size(); i++) WriteBack(bufferlist.get(i));
        super.finalize();
    }
	
	public void scanTable(Table m_tableName) throws IOException {
        String fileName = m_tableName.getName() + ".table";
        for (int blockcnt = 0; blockcnt < m_tableName.getBlockNum(); blockcnt++) {
        	int index = BlockIsInBuffer(fileName, blockcnt);
            if ( index == -1) {
//                int bfNum = getEmptyBufferExcept(fileName);
                readBlock(fileName, blockcnt);
            }
            else{
            	bufferlist.addFirst(bufferlist.remove(index));
            }
        }
    }

	public int BlockIsInBuffer(String m_fileName, int m_blockOff){
		for(Buffer tmp : bufferlist){
			if(tmp.getFileName().equals(m_fileName)  && tmp.getBlockNum() == m_blockOff)
				return bufferlist.indexOf(tmp);
		}
		return -1;
	}
	
    public void readBlock(String m_filename, int m_blockcnt) throws IOException {
//        bufferArray[bfNum].isValid = true;
//        bufferArray[bfNum].isWritten = false;
//        bufferArray[bfNum].fileName = fileName;
//        bufferArray[bfNum].blockOfs = blockOfs;
    	Buffer tmp = new Buffer();
//    	tmp.initialize();
    	tmp.fileName = m_filename;
    	tmp.blockNum = m_blockcnt;
        File file = new File("./memory/" + m_filename);
        if (!file.exists()) file.createNewFile();
        RandomAccessFile raf = new RandomAccessFile(file , "r");
        raf.seek(Buffer.BLOCKSIZE * m_blockcnt);
        raf.read(tmp.value, 0, Buffer.BLOCKSIZE);
        bufferlist.addFirst(tmp);
        LRU();
        raf.close();
    }
	
    public void LRU() throws IOException {
    	Buffer rm = new Buffer();
    	if(bufferlist.size() < BufferManager.MAXBLOCKNUM){
    		rm = bufferlist.removeLast();
    		WriteBack(rm);
    	}
    }
    
    void WriteBack(Buffer m_buffer) throws IOException {
    	if( !m_buffer.isWritten ) return;
    	File file = new File("./memory/" + m_buffer.getFileName());
        if (!file.exists())  file.createNewFile();
        RandomAccessFile raf = new RandomAccessFile( file, "rw");
        raf.seek(Buffer.BLOCKSIZE * m_buffer.getBlockNum());
        raf.write(m_buffer.value, 0, Buffer.BLOCKSIZE);
        raf.close();
//        bufferArray[bufferNum].initialize();
    }
    
    public boolean freeTable(String m_tableName) {
//        for (int i = 0; i<MAXBLOCKNUM; i++) {
//            if (bufferArray[i].fileName.equals(fileName)) {
//                bufferArray[i].isValid = false;
//                bufferArray[i].isWritten = false;
//            }
//        }
    	for(int i = 0; i < bufferlist.size(); i++){
    		if( bufferlist.get(i).getFileName().equals(m_tableName) )
    			bufferlist.remove(i);
    	}
    	File file = new File("./memory/" + m_tableName + ".table");
    	return file.delete();
    }
    
    public InsPos getInsertPosition(Table m_table) throws IOException {
        InsPos r_pos  = new InsPos();
        if(m_table.getBlockNum() == 0){
        	r_pos.setbufferNum(AddBlockToFile(m_table));
        	r_pos.setBlockOff(0);
//        	WriteBlock(r_pos.getBufferNum());
        	return r_pos;
        }
        else{
        	 String fileName = m_table.getName() + ".table";
             int tupleLength = m_table.getTupleLength() + 1;
             int blockLast = m_table.getBlockNum() - 1;
             int bfNum = BlockIsInBuffer(fileName, blockLast);
             if (bfNum == -1) {
//                 bfNum = getEmptyBuffer();
                 readBlock(fileName, blockLast);
                 bfNum = 0;
             }
             final int tupleNum = Buffer.BLOCKSIZE / tupleLength;
             for (int cnt = 0; cnt < tupleNum; cnt++) {
                 int position = cnt * tupleLength;
                 byte isEmpty = bufferlist.get(bfNum).value[position];
                 if (isEmpty == Buffer.EMPTY) {
                     r_pos.setbufferNum(bfNum);
                     r_pos.setBlockOff(position);
//                     WriteBlock(r_pos.getBufferNum());
                     return r_pos;
                 }
             }
             r_pos.setbufferNum(AddBlockToFile(m_table));
             r_pos.setBlockOff(0);
//             WriteBlock(r_pos.getBufferNum());
             return r_pos;
        }
    }
    
    public int AddBlockToFile(Table m_table) throws IOException {
//        int bfNum = getEmptyBuffer();
//        bufferArray[bfNum].initialize();
//        bufferArray[bfNum].isValid = true;
//        bufferArray[bfNum].isWritten = true;
//        bufferArray[bfNum].fileName = fileInfo.getName() + ".table";
//        bufferArray[bfNum].blockOfs = fileInfo.getBlockNum();
    	Buffer tmp = new Buffer();
//    	tmp.initialize();
    	tmp.fileName = m_table.getName() + ".table";
    	tmp.blockNum = m_table.getBlockNum();
        tmp.isWritten = true;
        bufferlist.addFirst(tmp);
        LRU();
        m_table.addBlockNum();
        return bufferlist.indexOf(tmp);
    } 
    
    

}

