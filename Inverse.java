package cn.edu.ustc.aaron.encoder;

import Jama.Matrix;
import java.util.*;

public class Inverse extends MatrixCreationAndOperation {

    public Inverse (int matHeight, int matWidth, boolean codeCbCr) {
        super(matHeight, matWidth, codeCbCr);
        super.matHeightC = matHeight;
    }
    
    public void operateMatrix(Matrix[]... srcs) {
        if (true) {
            Matrix[] coeffMat = srcs[0];
            double cf = coeffMat[0].get(0, 0);
            super.mat[0] = Matrix.identity(super.matHeight, super.matWidth).timesEquals(cf);
            if (super.codeCbCr) {
                super.mat[1] = Matrix.identity(super.matHeightC, super.matWidthC).timesEquals(cf);
                super.mat[2] = Matrix.identity(super.matHeightC, super.matWidthC).timesEquals(cf);
            }
        }
        else {
            Matrix[] coeffUV = srcs[0];
            super.mat[0] = coeffUV[0].transpose().times(coeffUV[0]).inverse();
            if (super.codeCbCr) {
                super.mat[1] = coeffUV[1].transpose().times(coeffUV[1]).inverse();
                super.mat[2] = coeffUV[2].transpose().times(coeffUV[2]).inverse();
            }
        }
    }
}