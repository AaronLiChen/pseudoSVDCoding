package cn.edu.ustc.aaron.encoder;

import cn.edu.ustc.aaron.common.Picture;
import Jama.Matrix;
import java.util.*;

public class MatrixPermutation {
    int gopSize;
    int width;
    int height;
    int widthC;
    int heightC;
    boolean codeCbCr;

    double[][] arrayY;
    double[][] arrayCb;
    double[][] arrayCr;
    Matrix[] stackedPicMat;

    double[][] oneColArrY;
    double[][] oneColArrCb;
    double[][] oneColArrCr;
    Matrix[] stackedOneColMat; 

    double[][] stackedUVArrY;
    double[][] stackedUVArrCb;
    double[][] stackedUVArrCr;
    Matrix[] stackedUVMat;

    public MatrixPermutation (int gopSize, int width, int height, boolean codeCbCr) {
        this.gopSize = gopSize;
        this.width = width;
        this.height = height;
        this.widthC = width >> 1;
        this.heightC = height >> 1;
        this.codeCbCr = codeCbCr;

        arrayY = new double [width * height][gopSize];
        stackedPicMat = new Matrix[3];
        stackedPicMat[0] = new Matrix(arrayY, width * height, gopSize);
        

        oneColArrY = new double [width * height * gopSize][1];
        stackedOneColMat = new Matrix[3];
        stackedOneColMat[0] = new Matrix(oneColArrY, width * height * gopSize, 1);
        

        stackedUVArrY = new double [width * height * gopSize][gopSize];
        stackedUVMat = new Matrix[3];
        stackedUVMat[0] = new Matrix(stackedUVArrY, width * height * gopSize, gopSize);

        if (codeCbCr) {
            arrayCb = new double [widthC * heightC][gopSize];
            arrayCr = new double [widthC * heightC][gopSize];
            stackedPicMat[1] = new Matrix(arrayCb, widthC * heightC, gopSize);
            stackedPicMat[2] = new Matrix(arrayCr, widthC * heightC, gopSize);

            oneColArrCb = new double [widthC * heightC * gopSize][1];
            oneColArrCr = new double [widthC * heightC * gopSize][1];
            stackedOneColMat[1] = new Matrix(oneColArrCb, widthC * heightC * gopSize, 1);
            stackedOneColMat[2] = new Matrix(oneColArrCr, widthC * heightC * gopSize, 1);

            stackedUVArrCb = new double [widthC * heightC * gopSize][gopSize];
            stackedUVArrCr = new double [widthC * heightC * gopSize][gopSize];

            stackedUVMat[1] = new Matrix(stackedUVArrCb, widthC * heightC * gopSize, gopSize);
            stackedUVMat[2] = new Matrix(stackedUVArrCr, widthC * heightC * gopSize, gopSize);
        }
    }

    public Matrix[] getStackedPicMat() {
        return stackedPicMat;
    }

    public Matrix getStackedPicMat(int color) {
        return stackedPicMat[color];
    }

    public Matrix[] getStackedUVMat() {
        return stackedUVMat;
    }

    public Matrix getStackedUVMat(int color) {
        return stackedUVMat[color];
    }
   
    private void stackLineByLine(int[] src, double[][] dst, int frameNo, int width, int height) {
        for (int y = 0; y < height; y++) {
           for (int x = 0; x < width; x++) {
                // dst[][] is a (width * height) by gopSize matrix,
                // with line by line of src pics stacked as columns of new arrays.
               dst[y * width + x][frameNo] = (double)src[y * width + x];
            } 
        }
    }

    private void stackColByCol(int[] src, double[][] dst, int frameNo, int width, int height) {
        for (int x = 0; x < width; x++) {
           for (int y = 0; y < height; y++) {
                // dst[][] is a (width * height) by gopSize matrix,
                // with col by col of src pics stacked as columns of new arrays.
               dst[x * height + y][frameNo] = (double)src[y * width + x]; 
            } 
        }
    }

    private void stackToOneCol(double[][] src, double [][] dst, int width, int height) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                dst[x * height + y][0] = src[y][x];
            }
        }
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

    public Matrix[] matrixLineByLine(ArrayList<Picture> picList, int startFrame) {
        for (int frameNo = 0; frameNo < gopSize; frameNo++) {
            stackLineByLine(picList.get(frameNo + startFrame).getY(), arrayY, frameNo, width, height);
            if (codeCbCr) {
                stackLineByLine(picList.get(frameNo + startFrame).getCb(), arrayCb, frameNo, widthC, heightC);
                stackLineByLine(picList.get(frameNo + startFrame).getCr(), arrayCr, frameNo, widthC, heightC);
            }
         } 

        return stackedPicMat;
    }

    public Matrix[] matrixColByCol(ArrayList<Picture> picList, int startFrame) {
        for (int frameNo = 0; frameNo < gopSize; frameNo++) {
            stackColByCol(picList.get(frameNo + startFrame).getY(), arrayY, frameNo, width, height);
            if (codeCbCr) {
                stackColByCol(picList.get(frameNo + startFrame).getCb(), arrayCb, frameNo, widthC, heightC);
                stackColByCol(picList.get(frameNo + startFrame).getCr(), arrayCr, frameNo, widthC, heightC);
            }
         } 
 
        return stackedPicMat;
    }

    public Matrix[] matrixStackToOneCol(Matrix[] src) {
        stackToOneCol(src[0].getArray(), oneColArrY, src[0].getColumnDimension(), src[0].getRowDimension());
        if (codeCbCr) {
            stackToOneCol(src[1].getArray(), oneColArrCb, src[1].getColumnDimension(), src[1].getRowDimension());
            stackToOneCol(src[2].getArray(), oneColArrCr, src[2].getColumnDimension(), src[2].getRowDimension());
        }

        return stackedOneColMat;
    }

    public Matrix[] matrixStackUV(Matrix[] srcU, Matrix[] srcV) {
        for (int colNo = 0; colNo < gopSize; colNo++) {
            multiplyAndStackUV(srcU[0].getArray(), srcV[0].getArray(), stackedUVArrY, colNo, srcV[0].getColumnDimension(), srcU[0].getRowDimension()); 
            if (codeCbCr) {
                multiplyAndStackUV(srcU[1].getArray(), srcV[1].getArray(), stackedUVArrCb, colNo, srcV[1].getColumnDimension(), srcU[1].getRowDimension());    
                multiplyAndStackUV(srcU[2].getArray(), srcV[2].getArray(), stackedUVArrCr, colNo, srcV[2].getColumnDimension(), srcU[2].getRowDimension());    
            }
        }
        return stackedUVMat;
    }
}