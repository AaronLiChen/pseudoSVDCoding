package cn.edu.ustc.aaron.encoder;

import cn.edu.ustc.aaron.common.Picture;
import Jama.Matrix;
import java.util.*;

public abstract class MatrixPermutation {
    protected int matWidth;
    protected int matHeight;
    protected boolean codeCbCr;

    protected double[][] permutedArrY;
    protected double[][] permutedArrCb;
    protected double[][] permutedArrCr;
    protected Matrix[] permutedMat;

    protected MatrixPermutation (int matHeight, int matWidth, boolean codeCbCr) {
        this.matHeight = matHeight;
        this.matWidth = matWidth;
        this.codeCbCr = codeCbCr;
    }

    public void allocMemory () {
        permutedArrY = new double [matHeight][matWidth];
        permutedMat = new Matrix[3];
        permutedMat[0] = new Matrix(permutedArrY, matHeight, matWidth);

        if (codeCbCr) {
            permutedArrCb = new double [matHeight >> 2][matWidth];
            permutedArrCr = new double [matHeight >> 2][matWidth];
            permutedMat[1] = new Matrix(permutedArrCb, matHeight >> 2, matWidth);
            permutedMat[2] = new Matrix(permutedArrCr, matHeight >> 2, matWidth);    
        }
    }

    public Matrix[] getPermutedMatrix () {
        return permutedMat;
    }

    public Matrix getPermutedMatrix (int color) {
        return permutedMat[color];
    }

    public abstract void permuteMatrix (Matrix[]... srcs);
}
