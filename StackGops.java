package cn.edu.ustc.aaron.encoder;

import cn.edu.ustc.aaron.common.Picture;
import Jama.Matrix;
import java.util.*;

public class StackGops extends MatrixCreationAndOperation {

    public StackGops (int matHeight, int matWidth, boolean codeCbCr) {
        super(matHeight, matWidth, codeCbCr);
        super.allocMemory();
    }

    // we tend to use stackLineByLine coz it is easy for the following Residue to be written as the yuv format.
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
            stackLineByLine(srcPic.getY(), super.arrY, frameNo, srcPic.getWidth(), srcPic.getHeight());
            if (super.codeCbCr) {
                stackLineByLine(srcPic.getCb(), super.arrCb, frameNo, srcPic.getWidthC(), srcPic.getHeightC());
                stackLineByLine(srcPic.getCr(), super.arrCr, frameNo, srcPic.getWidthC(), srcPic.getHeightC());
            }
         }
    }

    public void matrixColByCol (ArrayList<Picture> picList, int startFrame) {
        Picture srcPic = null;
        for (int frameNo = 0; frameNo < super.matWidth; frameNo++) {
            stackColByCol(srcPic.getY(), super.arrY, frameNo, srcPic.getWidth(), srcPic.getHeight());
            if (super.codeCbCr) {
                stackColByCol(srcPic.getCb(), super.arrCb, frameNo, srcPic.getWidthC(), srcPic.getHeightC());
                stackColByCol(srcPic.getCr(), super.arrCr, frameNo, srcPic.getWidthC(), srcPic.getHeightC());
            }
         }
    }

    public void operateMatrix (Matrix[]... srcs) {
        System.out.println("Please use method 'matrixLineByLine' or 'matrixColByCol'!");
    }
}