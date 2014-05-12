package cn.edu.ustc.aaron.encoder;

import java.util.*;
import cn.edu.ustc.aaron.common.*;
import Jama.*;

/** Psvd Iterating Process. **/

public class Psvd {
    private boolean codeCbCr;
    private double tolerance;
    private double[] beta;
    private double betaOrg;
    private int[] iter;

    private Matrix[] stackedToOneColMat;
    private Matrix[] reshapedProductUVBaseMat;
    private Matrix[] inverseProductUVBaseMat;

    private TempDiff tempDiff;
    private Lambda lambda;
    private Residue residue;
    private Diag diag;

    private Matrix[] zeroMat;

    // prepare to store diagMat
    private List<LinkedList<Integer>> diagList;

    public Psvd (Matrix[] stackedToOneColMat, Matrix[] reshapedProductUVBaseMat, Matrix[] inverseProductUVBaseMat, boolean codeCbCr, double beta, double tol) {
        this.stackedToOneColMat = stackedToOneColMat;
        this.reshapedProductUVBaseMat = reshapedProductUVBaseMat;
        this.inverseProductUVBaseMat = inverseProductUVBaseMat;
        this.codeCbCr = codeCbCr;
        this.betaOrg = beta;
        this.tolerance = tol;

        allocMemory();
    }

    private void allocMemory () {
        int row = stackedToOneColMat[0].getRowDimension();
        int col = stackedToOneColMat[0].getColumnDimension();
        int rowD = reshapedProductUVBaseMat[0].getColumnDimension();

        tempDiff = new TempDiff(row, col, codeCbCr);
        lambda = new Lambda(row, col, codeCbCr);
        residue = new Residue(row, col, codeCbCr);
        diag = new Diag(rowD, 1, codeCbCr);
        iter = new int[3];
        beta = new double[3];
        beta[0] = beta[1] = beta[2] = betaOrg;

        zeroMat = new Matrix[1];
        zeroMat[0] = new Matrix(row, col, 0.0);

        diagList  = new ArrayList<>();
        for (int color = 0; color < 3; color++) {
            diagList.add(new LinkedList<Integer>());
        }

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

    private class TempDiff extends MatrixCreationAndOperation {

        public TempDiff (int matHeight, int matWidth, boolean codeCbCr) {
            super(matHeight, matWidth, codeCbCr);
        }
    
        public void operateMatrix(Matrix[]... srcs) {
            System.out.println("No init operation needed here!");
        }
    }

    private class Lambda extends CreateInitSuper {

        public Lambda (int matHeight, int matWidth, boolean codeCbCr) {
            super(matHeight, matWidth, matHeight >> 2, codeCbCr);
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

    public Matrix[] getResidue () {
        clipResidue();
        return residue.getMatrix();
    }
    public List<LinkedList<Integer>> getDiag () {
        return diagList;
    }

    private void storeAndRoundDiag (int color) {
        Matrix diagMat = diag.getMatrix(color);
        for (int i = 0; i < diagMat.getRowDimension(); i++) {
            diagList.get(color).offer((int)diagMat.get(i,0));       // int cast results in precision loss
        }
    }

    private void clipResidue (int color) {
        Matrix residueMat = residue.getMatrix(color);
        double [][] resMatArr = residueMat.getArray();
        for (int row = 0; row < residueMat.getRowDimension(); row++) {
            for (int col = 0; col < residueMat.getColumnDimension(); col++) {
                resMatArr[row][col] *= 255.0;
                if (resMatArr[row][col] > 128) {
                    resMatArr[row][col] = 128.0; 
                }
                if (resMatArr[row][col] < -128.0) {
                    resMatArr[row][col] = -127.0; 
                }
                resMatArr[row][col] += 127.0;
            }
        }
    }

    private void clipResidue () {
        clipResidue(0);
        if (codeCbCr) {
            clipResidue(1);
            clipResidue(2);
        }
    }

    private void solveR (int color) {
        Matrix[] tempDiffMat = tempDiff.getMatrix();
        Matrix[] lambdaMat = lambda.getMatrix();
        Matrix[] residueMat = residue.getMatrix();
        Matrix[] diagMat = diag.getMatrix();

        tempDiffMat[color] = stackedToOneColMat[color].minus(reshapedProductUVBaseMat[color].times(diagMat[color])).plus(lambdaMat[color].times(1.0/beta[color]));
        FindAndModify.findModifyLessThan(residueMat[color], tempDiffMat[color], beta[color]);
        FindAndModify.findModifyMoreThan(residueMat[color], tempDiffMat[color], beta[color]);

        // test
        // if (color == 0) {
        //     WriteYCbCr wYCbCr = WriteYCbCr.getInstance();
        //     String wFilename = new String("lambdaMat.txt");
        //     wYCbCr.writeTxt(wFilename, lambdaMat[color]);
        //     wFilename = new String("residueMat.txt");
        //     wYCbCr.writeTxt(wFilename, residueMat[color]);
        //     wFilename = new String("tempDiffMat.txt");
        //     wYCbCr.writeTxt(wFilename, tempDiffMat[color]);
        // }

    }

    private void solveD (int color) {
        Matrix[] lambdaMat = lambda.getMatrix();
        Matrix[] diagMat = diag.getMatrix();
        Matrix[] residueMat = residue.getMatrix();

        Matrix leftTerm = inverseProductUVBaseMat[color].times(reshapedProductUVBaseMat[color].transpose()).times(lambdaMat[color]).times(1.0/beta[color]);
        Matrix rightTerm = inverseProductUVBaseMat[color].times(reshapedProductUVBaseMat[color].transpose()).times(stackedToOneColMat[color].minus(residueMat[color]));
        diagMat[color] = leftTerm.plus(rightTerm);

        // test
        // if (color == 0) {
        //     WriteYCbCr wYCbCr = WriteYCbCr.getInstance();
        //     String wFilename = new String("diagMat.txt");
        //     wYCbCr.writeTxt(wFilename, diagMat[color]);
        // }
    }

    private void updatePara (int color) {
        Matrix[] lambdaMat = lambda.getMatrix();
        Matrix[] diagMat = diag.getMatrix();
        Matrix[] residueMat = residue.getMatrix();

        lambdaMat[color].minusEquals(reshapedProductUVBaseMat[color].times(diagMat[color]).plus(residueMat[color]).minus(stackedToOneColMat[color]).times(beta[color]));
        beta[color] *= 1.5;

        // test
        // if (color == 0) {
        //     WriteYCbCr wYCbCr = WriteYCbCr.getInstance();
        //     String wFilename = new String("lambdaMat.txt");
        //     wYCbCr.writeTxt(wFilename, lambdaMat[color]);

        //     System.out.println("beta = " + beta[color]);
        // }
    }

    private boolean judgeConverge (int color) {
        Matrix[] lambdaMat = lambda.getMatrix();
        Matrix[] diagMat = diag.getMatrix();
        Matrix[] residueMat = residue.getMatrix();

        double diff = reshapedProductUVBaseMat[color].times(diagMat[color]).plus(residueMat[color]).minus(stackedToOneColMat[color]).norm2()/stackedToOneColMat[color].norm2();

        // test
        // if (color == 0) {
        //     // System.out.println(iter[color]);
        //     // System.out.println(diff);
        //     System.out.println(residueMat[color].norm1());
        // }

        if (diff < tolerance) {
            System.out.println("Color: "+color);
            System.out.println("iterating times = "+iter[color]);
            System.out.println("Norm2Diff = "+diff);
            System.out.println("Norm1 = "+residueMat[color].norm1()+"\n\n");

            return true;
        }
        else {
            return false;
        }
    }

    private void initPara () {
        iter[0] = 0; iter[1] = 0; iter[2] = 0;
        beta[0] = beta[1] = beta[2] = betaOrg;

        lambda.operateMatrix(zeroMat);
        residue.operateMatrix(zeroMat);
        diag.operateMatrix(zeroMat);
    }

    public void psvdIteration () {
        initPara ();
        boolean convergedY = false;
        boolean convergedCb = false;
        boolean convergedCr = false;

        while (!convergedY) {
            iter[0]++;

            solveR(0);
            solveD(0);
            updatePara(0);
            convergedY = judgeConverge(0);

            // test 
            // if (iter[0] == 11) {
            //     convergedY = true;
            // }
        }
        storeAndRoundDiag(0);

        if (codeCbCr) {

            while (!convergedCb) {
                iter[1]++;

                solveR(1);
                solveD(1);
                updatePara(1);
                convergedCb = judgeConverge(1);
            }
            storeAndRoundDiag(1);

            while (!convergedCr) {
                iter[2]++;

                solveR(2);
                solveD(2);
                updatePara(2);
                convergedCr = judgeConverge(2);
            }
            storeAndRoundDiag(2);
        } 
    }

    // test
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

}