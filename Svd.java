package cn.edu.ustc.aaron.encoder;

import cn.edu.ustc.aaron.common.*;
import Jama.*;

public class Svd {
    SingularValueDecomposition[] svdResult;
    Matrix[] matU;
    Matrix[] matV;
    Matrix[] matS;

    public Svd (Matrix[] toBeSvdMat, boolean codeCbCr) {

        svdResult = new SingularValueDecomposition[3];
        matU = new Matrix[3];
        matV = new Matrix[3];
        matS = new Matrix[3];

        svdResult[0] = new SingularValueDecomposition(toBeSvdMat[0]);
        matU[0] = svdResult[0].getU();
        matV[0] = svdResult[0].getV();
        matS[0] = svdResult[0].getS();

        if (codeCbCr) {
            svdResult[1] = new SingularValueDecomposition(toBeSvdMat[1]);
            matU[1] = svdResult[1].getU();
            matV[1] = svdResult[1].getV();
            matS[1] = svdResult[1].getS();

            svdResult[2] = new SingularValueDecomposition(toBeSvdMat[2]);
            matU[2] = svdResult[2].getU();
            matV[2] = svdResult[2].getV();
            matS[2] = svdResult[2].getS();
        }
    }

    public SingularValueDecomposition[] getSvdResult () {
        return svdResult;
    }

    public Matrix[] getMatU () {
        return matU;
    }

    public Matrix[] getMatV () {
        return matV;
    }
    
    public Matrix[] getMatS () {
        return matS;
    }
}