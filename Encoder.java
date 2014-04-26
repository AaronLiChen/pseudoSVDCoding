package cn.edu.ustc.aaron.encoder;

import java.util.*;
import cn.edu.ustc.aaron.common.*;
import Jama.*;

public class Encoder {
    
    public static void main(String[] args) {
        int width = 8;
        int height = 4;
        int totalFrames = 10;

        // prepare to read YCbCr
        String rFilename = new String("E:\\JavaWorkSpace\\PsvdProject\\Sequences\\Campus\\small.yuv");
        ArrayList<Picture> picList = new ArrayList<>(); // Z:\\sequences_3DV_CfP\\Balloons\\Balloons3.yuv E:\\JavaWorkSpace\\PsvdProject\\Sequences\\Campus\\trees.yuv
        ReadYCbCr rYCbCr = ReadYCbCr.getInstance();
        // read YCbCr
        rYCbCr.readPic(rFilename, width, height, totalFrames, picList);

        // prepare to write YCbCr
        // WriteYCbCr wYCbCr = WriteYCbCr.getInstance();
        // String wFilename = new String("smallW.yuv");
        // wYCbCr.writePic(wFilename, width, height, totalFrames, picList);

        int gopSize = 2;
        boolean codeCbCr = true;
        // first, stack gopSize pictures into one matrix -- stackedPicMatBase with (width*height) rows and gopSize cols
        StackGops stackedGopBase = new StackGops(width * height, gopSize, codeCbCr);
        stackedGopBase.matrixLineByLine(picList, 0);
        // second, svd this stackedPicMatBase matrix
        Svd svdResultBase = new Svd(stackedGopBase.getMatrix(), codeCbCr);
        // third, reshape U and V matrix -- reshapedProductUVBase with (width*height*gopSize) rows and gopSize cols
        MatrixCreationAndOperation reshapedProductUVBase = new ReshapeProductUV(width * height * gopSize, gopSize, codeCbCr);
        reshapedProductUVBase.operateMatrix(svdResultBase.getMatU(), svdResultBase.getMatV());
        // { NOMORLIAZATION
        double normlization = 255.0; // use this coz U and V are both divided by 255 for Psvd
        reshapedProductUVBase.getMatrix(0).timesEquals(1.0/normlization/normlization);
        if (codeCbCr) {
            reshapedProductUVBase.getMatrix(1).timesEquals(1.0/normlization/normlization);
            reshapedProductUVBase.getMatrix(2).timesEquals(1.0/normlization/normlization);
        }
        // }
        // fourth, inverse reshapedProductUVBase
        MatrixCreationAndOperation inverseProductUVBase = new Inverse(gopSize, gopSize, codeCbCr);
        // inverseProductUVBase.operateMatrix(reshapedProductUVBase.getMatrix());
        // Since inverseProductUVBase is an identity matrix, here we directly generate it.
        // { NOMORLIAZATION
        Matrix[] coeffMat = new Matrix[1]; // use this coz U and V are both divided by 255 for Psvd
        coeffMat[0] = new Matrix(1, 1, normlization*normlization*normlization*normlization);
        // }
        inverseProductUVBase.operateMatrix(coeffMat);

        // test
        // int testColor = 0;
        // WriteYCbCr wYCbCr = WriteYCbCr.getInstance();
        // String wFilename = new String("stackedGopBase.txt");
        // wYCbCr.writeTxt(wFilename, stackedGopBase.getMatrix()[testColor]);
        // wFilename = new String("U.txt");
        // wYCbCr.writeTxt(wFilename, svdResultBase.getMatU()[testColor]);
        // wFilename = new String("V.txt");
        // wYCbCr.writeTxt(wFilename, svdResultBase.getMatV()[testColor]);
        // wFilename = new String("D.txt");
        // wYCbCr.writeTxt(wFilename, svdResultBase.getMatS()[testColor]);

        // prepare to read the following gops' YCbCr
        StackGops stackedGop = new StackGops(width * height, gopSize, codeCbCr);
        // prepare to stack gop matrix to one-col vector with (width*height*gopSize) rows
        MatrixCreationAndOperation stackedToOneColVector = new StackToOneCol(width * height * gopSize, 1, codeCbCr);
        // prepare to psvd
        Psvd psvdOperation = new Psvd(stackedToOneColVector.getMatrix(), reshapedProductUVBase.getMatrix(), inverseProductUVBase.getMatrix(), codeCbCr, 1.0, 1.0e-7);
        for (int gopNo = 1; gopNo < 2; gopNo++) {
            // read following YCbCr
            stackedGop.matrixLineByLine(picList, gopNo * gopSize);
            // stack gop matrix to one-col vector
            stackedToOneColVector.operateMatrix(stackedGop.getMatrix());
            // { NOMORLIAZATION
            stackedToOneColVector.getMatrix(0).timesEquals(1.0/normlization);
            if (codeCbCr) {
                stackedToOneColVector.getMatrix(1).timesEquals(1.0/normlization);
                stackedToOneColVector.getMatrix(2).timesEquals(1.0/normlization);
            }
            // }

            // test
            // int testColor = 0;
            // WriteYCbCr wYCbCr = WriteYCbCr.getInstance();
            // String wFilename = new String("inverseProductUVBase.txt");
            // wYCbCr.writeTxt(wFilename, inverseProductUVBase.getMatrix()[testColor]);
            // wFilename = new String("stackedGop.txt");
            // wYCbCr.writeTxt(wFilename, stackedGop.getMatrix()[testColor]);
            // wFilename = new String("stackedToOneColVector.txt");
            // wYCbCr.writeTxt(wFilename, stackedToOneColVector.getMatrix()[testColor]);
            // wFilename = new String("reshapedProductUVBase.txt");
            // wYCbCr.writeTxt(wFilename, reshapedProductUVBase.getMatrix()[testColor]);

            // psvd
            System.out.println("gopNo: "+gopNo);
            psvdOperation.psvdIteration();

        }

        
    }

}