package cn.edu.ustc.aaron.encoder;

import cn.edu.ustc.aaron.common.Picture;
import Jama.Matrix;
import java.util.*;

public class StackGops extends MatrixPermutation {

    public StackGops (int matHeight,int matWidth,  boolean codeCbCr) {
        super(matHeight, matWidth, codeCbCr);
        super.allocMemory();
    }

    private void stackLineByLine (int[] src, double[][] dst, int frameNo, int width, int height) {
        for (int y = 0; y < height; y++) {
           for (int x = 0; x < width; x++) {
                // dst[][] is a (width * height) by gopSize matrix,
                // with line by line of src pics stacked as columns of new arrays.
               dst[y * width + x][frameNo] = (double)src[y * width + x];
            } 
        }
    }

    private void stackColByCol (int[] src, double[][] dst, int frameNo, int width, int height) {
        for (int x = 0; x < width; x++) {
           for (int y = 0; y < height; y++) {
                // dst[][] is a (width * height) by gopSize matrix,
                // with col by col of src pics stacked as columns of new arrays.
               dst[x * height + y][frameNo] = (double)src[y * width + x]; 
            } 
        }
    }

    public void matrixLineByLine (ArrayList<Picture> picList, int startFrame) {
        Picture srcPic = null;
        for (int frameNo = 0; frameNo < super.matWidth; frameNo++) {
            srcPic = picList.get(frameNo + startFrame);
            stackLineByLine(srcPic.getY(), super.permutedArrY, frameNo, srcPic.getWidth(), srcPic.getHeight());
            if (super.codeCbCr) {
                stackLineByLine(srcPic.getCb(), super.permutedArrCb, frameNo, srcPic.getWidthC(), srcPic.getHeightC());
                stackLineByLine(srcPic.getCr(), super.permutedArrCr, frameNo, srcPic.getWidthC(), srcPic.getHeightC());
            }
         }
    }

    public void matrixColByCol (ArrayList<Picture> picList, int startFrame) {
        Picture srcPic = null;
        for (int frameNo = 0; frameNo < super.matWidth; frameNo++) {
            stackColByCol(srcPic.getY(), super.permutedArrY, frameNo, srcPic.getWidth(), srcPic.getHeight());
            if (super.codeCbCr) {
                stackColByCol(srcPic.getCb(), super.permutedArrCb, frameNo, srcPic.getWidthC(), srcPic.getHeightC());
                stackColByCol(srcPic.getCr(), super.permutedArrCr, frameNo, srcPic.getWidthC(), srcPic.getHeightC());
            }
         }
    }

    public void permuteMatrix (Matrix[]... srcs) {
        System.out.println("Please use method 'matrixLineByLine' or 'matrixColByCol'!");
    }
}