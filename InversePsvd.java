package cn.edu.ustc.aaron.decoder;

import java.util.*;
import cn.edu.ustc.aaron.common.*;
import Jama.*;

public class InversePsvd {
    private boolean codeCbCr;

    private Matrix[] stackedToOneColMat;
    private Matrix[] reshapedProductUVBaseMat;

    private Residue residue;
    private Diag diag;


    public InversePsvd (Matrix[] reshapedProductUVBaseMat, boolean codeCbCr) {
        this.reshapedProductUVBaseMat = reshapedProductUVBaseMat;
        this.codeCbCr = codeCbCr;

        allocMemory();
    }

    private void allocMemory () {
        int row = reshapedProductUVBaseMat[0].getRowDimension();
        int rowD = reshapedProductUVBaseMat[0].getColumnDimension();

        residue = new Residue(row, 1, codeCbCr);
        diag = new Diag(rowD, 1, codeCbCr);
    }


    private class CreateInitSuper extends MatrixCreationAndOperation {

        public CreateInitSuper (int matHeight, int matWidth, int matHeightC, boolean codeCbCr) {
            super(matHeight, matWidth, codeCbCr);
            super.matHeightC = matHeightC; // Bugs for Diag initialization.
            super.allocMemory();
        }
    
        public void operateMatrix(Matrix[]... srcs) {
            // init Matrix
            Matrix[] zeroMat = srcs[0];
            super.mat[0].setMatrix(0, super.matHeight-1, 0, super.matWidth-1, zeroMat[0]);
            if (codeCbCr) {
                super.mat[1].setMatrix(0, super.matHeightC-1, 0, super.matWidthC-1, zeroMat[0]);
                super.mat[2].setMatrix(0, super.matHeightC-1, 0, super.matWidthC-1, zeroMat[0]);
            }
        }
    }

    private class Residue extends CreateInitSuper {

        public Residue (int matHeight, int matWidth, boolean codeCbCr) {
            super(matHeight, matWidth, matHeight >> 2, codeCbCr);
        }
   
    }

    private class Diag extends CreateInitSuper {

        public Diag (int matHeight, int matWidth, boolean codeCbCr) {
            super(matHeight, matWidth, matHeight, codeCbCr);
        }
   
    }

    private void unpackDiag (Iterator diagLinkedListIt, int color) {
        Matrix diagMat = diag.getMatrix(color);
        for (int i = 0; i < diagMat.getRowDimension(); i++) {
            diagMat.set(i, 0, (double)(int)diagLinkedListIt.next());
        }
    }

     public Matrix[] invPsvd () {
        Matrix[] residueMat = residue.getMatrix();
        Matrix[] diagMat = diag.getMatrix();
        Matrix[] decStackedToOneColMat = new Matrix[3];

        decStackedToOneColMat[0] = reshapedProductUVBaseMat[0].times(diagMat[0]).plus(residueMat[0]).timesEquals(255.0);
        if (codeCbCr) {
            decStackedToOneColMat[1] = reshapedProductUVBaseMat[1].times(diagMat[1]).plus(residueMat[1]).timesEquals(255.0);
            decStackedToOneColMat[2] = reshapedProductUVBaseMat[2].times(diagMat[2]).plus(residueMat[2]).timesEquals(255.0);
        }
        return decStackedToOneColMat;
    }

    public Matrix[] invPsvd (Matrix[] residueMat, Iterator[] diagLinkedListIt) {
        Matrix[] diagMat = diag.getMatrix();
        Matrix[] decStackedToOneColMat = new Matrix[3];

        unpackDiag (diagLinkedListIt[0], 0);
        decStackedToOneColMat[0] = reshapedProductUVBaseMat[0].times(diagMat[0]).plus(residueMat[0]).timesEquals(255.0);
        if (codeCbCr) {
            unpackDiag (diagLinkedListIt[1], 1);
            decStackedToOneColMat[1] = reshapedProductUVBaseMat[1].times(diagMat[1]).plus(residueMat[1]).timesEquals(255.0);
            unpackDiag (diagLinkedListIt[2], 2);
            decStackedToOneColMat[2] = reshapedProductUVBaseMat[2].times(diagMat[2]).plus(residueMat[2]).timesEquals(255.0);
        }
        return decStackedToOneColMat;
    }
}