package cn.edu.ustc.aaron.common;

public class Picture {
    int[] bufY;
    int[] bufCb;
    int[] bufCr;

    int width;
    int height;
    int widthC;
    int heightC;

    public Picture (int width, int height) {
        this.width = width;
        this.height = height;
        this.widthC = width >> 1;
        this.heightC = height >> 1;

        bufY = new int[width * height];
        bufCb = new int[widthC * heightC];
        bufCr = new int[widthC * heightC];
    }

    public int getWidth () {
        return width;
    }

    public int getHeight () {
        return height;
    }

    public int getWidthC () {
        return widthC;
    }

    public int getHeightC () {
        return heightC;
    }

    public int[] getY () {
        return bufY;
    }

    public int getY (int i) {
        return bufY[i];
    }

    public int[] getCb () {
        return bufCb;
    }

    public int getCb (int i) {
        return bufCb[i];
    }

    public int[] getCr () {
        return bufCr;
    }

    public int getV (int i) {
        return bufCr[i];
    }

}