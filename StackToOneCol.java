package cn.edu.ustc.aaron.encoder;

import Jama.Matrix;
import java.util.*;

public class StackToOneCol extends MatrixCreationAndOperation {

    public StackToOneCol (int matHeight, int matWidth, boolean codeCbCr) {
        super(matHeight, matWidth, codeCbCr);
        super.allocMemory();
    }

    private void stackToOneCol (double[][] src, double [][] dst, int width, int height) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                dst[x * height + y][0] = src[y][x];
            }
        }
    }

    public void operateMatrix (Matrix[]... srcs) {
        Matrix[] src = srcs[0];
        stackToOneCol(src[0].getArray(), super.arrY, src[0].getColumnDimension(), src[0].getRowDimension());
        if (super.codeCbCr) {
            stackToOneCol(src[1].getArray(), super.arrCb, src[1].getColumnDimension(), src[1].getRowDimension());
            stackToOneCol(src[2].getArray(), super.arrCr, src[2].getColumnDimension(), src[2].getRowDimension());
        }
    }


}