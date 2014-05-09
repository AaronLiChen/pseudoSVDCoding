package cn.edu.ustc.aaron.encoder;

import java.util.*;
import cn.edu.ustc.aaron.common.*;
import Jama.*;
import nayuki.arithcode.*;
import java.io.IOException;

public class Encoder {
    
    public static void main(String[] args) throws IOException {
        // timing
        long startMili=System.currentTimeMillis();
        int width = 160;
        int height = 128;
        int totalFrames = 1000;

        // prepare to read YCbCr
        String rFilename = new String("L:\\PsvdSequence\\Campus\\trees.yuv");
        ArrayList<Picture> picList = new ArrayList<>(); // Z:\\sequences_3DV_CfP\\Balloons\\Balloons3.yuv E:\\JavaWorkSpace\\PsvdProject\\Sequences\\Campus\\trees.yuv
        ReadYCbCr rYCbCr = ReadYCbCr.getInstance();
        // read YCbCr
        rYCbCr.readPic(rFilename, width, height, totalFrames, picList);

        // prepare to write YCbCr
        // WriteYCbCr wYCbCr = WriteYCbCr.getInstance();
        // String wFilename = new String("smallW.yuv");
        // wYCbCr.writePic(wFilename, width, height, totalFrames, picList);

        int gopSize = 5;
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
        // int testColor = 2;
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
        // prepare to write Residue
        WriteYCbCr wYCbCr = WriteYCbCr.getInstance();
        String wResidueFilename = new String("Dec.yuv");
        int residueWidth = width * height;
        wYCbCr.startWriting(wResidueFilename, residueWidth, residueWidth >> 2);
        // prepare to read Residue
        String rResidueFilename = new String("recResidue.yuv");
        ArrayList<Picture> picResidueList = new ArrayList<>();
        rYCbCr = ReadYCbCr.getInstance();
        // input residue pre-operation
        rYCbCr.readPic(rResidueFilename, width, height, totalFrames, picResidueList);
        StackGops stackedResidueGop = new StackGops(width * height, gopSize, codeCbCr);
        MatrixCreationAndOperation stackedResidueToOneColVector = new StackToOneCol(width * height * gopSize, 1, codeCbCr);
        Matrix mat127 = new Matrix(width * height * gopSize, 1, 127);
        Matrix mat127CbCr = new Matrix((width * height * gopSize >> 2), 1, 127);

        for (int gopNo = 1; gopNo < totalFrames / gopSize; gopNo++) {
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
            // wFilename = new String("inverseProductUVBase.txt");
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

            // write Residue
            // wYCbCr.writeMatColByCol(psvdOperation.getResidue(), gopSize, width * height, codeCbCr);

            stackedResidueGop.matrixLineByLine(picResidueList, (gopNo - 1) * gopSize);
            stackedResidueToOneColVector.operateMatrix(stackedResidueGop.getMatrix());
            stackedResidueToOneColVector.getMatrix(0).minusEquals(mat127).timesEquals(1.0/normlization);
            if (codeCbCr) {
                stackedResidueToOneColVector.getMatrix(1).minusEquals(mat127CbCr).timesEquals(1.0/normlization);
                stackedResidueToOneColVector.getMatrix(2).minusEquals(mat127CbCr).timesEquals(1.0/normlization);
            }
            wYCbCr.writeMatColByCol(psvdOperation.invPsvd(stackedResidueToOneColVector.getMatrix()), gopSize, width * height, codeCbCr);

            // test (getResidue() can be used only once!)
            // String wFilename = new String("ResidueRefined.txt");
            // wYCbCr.writeTxt(wFilename, psvdOperation.getResidue()[0]);
        }
        // end writing Residue
        wYCbCr.endWriting();

        // diag.mat Binarization
        // Binarization diagBinarization = new Binarization(psvdOperation.getDiag(), codeCbCr);
        // diagBinarization.binarizeDiag();

        // // test
        // // int testColor = 0;
        // // WriteYCbCr wYCbCr = WriteYCbCr.getInstance();
        // // String wFilename = new String("Binarization.txt");
        // // wYCbCr.writeTxt(wFilename, diagBinarization.getbinArr(testColor), 1, diagBinarization.getbinArr(testColor).length);

        // // arithmetic encoding
        // ArithmeticCompress arithmeticEncoder = new ArithmeticCompress(diagBinarization.getbinArr(), "binDiag", codeCbCr);

        // // arithmetic decoding
        // ArithmeticDecompress arithmeticDecoder = new ArithmeticDecompress("binDiag", codeCbCr);

        // // diag.mat invBinarization
        // Binarization invBinarization = new Binarization(arithmeticDecoder.getbinArr(), codeCbCr);
        // invBinarization.invBinarizeDiag();

        // test
        // Iterator diagListIt = psvdOperation.getDiag().get(0).iterator();
        // while(diagListIt.hasNext()) {
        //     int elem = (int) diagListIt.next();
        //     System.out.println(elem);
        // }

        // diagListIt = invBinarization.getDiag().get(0).iterator();
        // while(diagListIt.hasNext()) {
        //     int elem = (int) diagListIt.next();
        //     System.out.println(elem);
        // }

        System.out.println("Total time: " + ((System.currentTimeMillis() - startMili)/1000) + "s");
    }
}