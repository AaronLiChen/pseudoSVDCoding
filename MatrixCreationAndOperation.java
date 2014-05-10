package cn.edu.ustc.aaron.common;

import Jama.Matrix;
import java.util.*;

public abstract class MatrixCreationAndOperation {
    protected int matWidth;
    protected int matHeight;
    protected int matWidthC;
    protected int matHeightC;
    protected boolean codeCbCr;

    protected double[][] arrY;
    protected double[][] arrCb;
    protected double[][] arrCr;
    protected Matrix[] mat;

    protected MatrixCreationAndOperation (int matHeight, int matWidth, boolean codeCbCr) {
        this.matHeight = matHeight;
        this.matWidth = matWidth;
        this.matHeightC = (matHeight >> 2);
        this.matWidthC = matWidth;
        this.codeCbCr = codeCbCr;

        mat = new Matrix[3];
    }

    protected void allocMemory () {
        arrY = new double [matHeight][matWidth];
        mat[0] = new Matrix(arrY, matHeight, matWidth);

        if (codeCbCr) {
            arrCb = new double [matHeightC][matWidthC];
            arrCr = new double [matHeightC][matWidthC];
            mat[1] = new Matrix(arrCb, matHeightC, matWidthC);
            mat[2] = new Matrix(arrCr, matHeightC, matWidthC);
        }
    }

    public Matrix[] getMatrix () {
        return mat;
    }

    public Matrix getMatrix (int color) {
        return mat[color];
    }

    public abstract void operateMatrix (Matrix[]... srcs);
}
