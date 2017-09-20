package structures;

/**
 * Created by Administrator on 2015/10/6.
 */
public class InsPos {
    int bufferNum;  //缓存中块位置
    int blockOff;   //块内偏移量

    public InsPos() {bufferNum = 0; blockOff = 0;}

    public int getBufferNum() {
        return bufferNum;
    }

    public int getBlockOff() {
        return blockOff;
    }

    public void setbufferNum(int buffernum) {
        this.bufferNum = buffernum;
    }

    public void setBlockOff(int blockOff) {
        this.blockOff = blockOff;
    }
}
