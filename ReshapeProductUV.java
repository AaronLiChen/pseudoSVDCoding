package cn.edu.ustc.aaron.common;

import Jama.Matrix;
import java.util.*;

public class ReshapeProductUV extends MatrixCreationAndOperation {

    public ReshapeProductUV (int matHeight, int matWidth, boolean codeCbCr) {
        super(matHeight, matWidth, codeCbCr);
        super.allocMemory();
    }
    
    private void multiplyAndStackUV(double[][] srcU, double[][] srcV, double[][] dst, int colNo, int widthV, int heightU) {
        for (int x = 0; x < widthV; x++) {
                double srcElemV = srcV[colNo][x];
            for (int y = 0; y < heightU; y++) {
                // the colNoth column of U times the colNoth row of V and reshape the result matrix col by col to one column.
                dst[x * heightU + y][colNo] = srcU[y][colNo] * srcElemV;
            }
        }
    }

    public void operateMatrix(Matrix[]... srcs) {
        Matrix[] srcU = srcs[0];
        Matrix[] srcV = srcs[1];
        for (int colNo = 0; colNo < super.matWidth; colNo++) {
            multiplyAndStackUV(srcU[0].getArray(), srcV[0].transpose().getArray(), super.arrY, colNo, srcV[0].getColumnDimension(), srcU[0].getRowDimension()); 
            if (codeCbCr) {
                multiplyAndStackUV(srcU[1].getArray(), srcV[1].transpose().getArray(), super.arrCb, colNo, srcV[1].getColumnDimension(), srcU[1].getRowDimension());    
                multiplyAndStackUV(srcU[2].getArray(), srcV[2].transpose().getArray(), super.arrCr, colNo, srcV[2].getColumnDimension(), srcU[2].getRowDimension());    
            }
        }
    }
}