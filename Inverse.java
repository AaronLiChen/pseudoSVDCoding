package cn.edu.ustc.aaron.encoder;

import cn.edu.ustc.aaron.common.Picture;
import Jama.Matrix;
import java.util.*;

public class Inverse extends MatrixPermutation {

    public Inverse (int matHeight, int matWidth, boolean codeCbCr) {
        super(matHeight, matWidth, codeCbCr);
        allocMemory();
    }
    
    public void allocMemory () {
        super.permutedMat = new Matrix[3];
    }

    public void permuteMatrix(Matrix[]... srcs) {
        Matrix[] coeffUV = srcs[0];
        super.permutedMat[0] = coeffUV[0].transpose().times(coeffUV[0]).inverse();
        if (super.codeCbCr) {
            super.permutedMat[1] = coeffUV[1].transpose().times(coeffUV[1]).inverse();
            super.permutedMat[2] = coeffUV[2].transpose().times(coeffUV[2]).inverse();
        }

        WriteYCbCr wYCbCr = WriteYCbCr.getInstance();
        // test -- write YCbCr
        String wFilename = new String("Yinverse.txt");
        wYCbCr.writeTxt(wFilename, super.permutedMat[0]);
        wFilename = new String("Cbinverse.txt");
        wYCbCr.writeTxt(wFilename, super.permutedMat[1]);
        wFilename = new String("Crinverse.txt");
        wYCbCr.writeTxt(wFilename, super.permutedMat[2]);
    }
}