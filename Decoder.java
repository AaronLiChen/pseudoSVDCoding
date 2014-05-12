package cn.edu.ustc.aaron.decoder;

import java.util.*;
import cn.edu.ustc.aaron.common.*;
import cn.edu.ustc.aaron.encoder.*;
import Jama.*;
import nayuki.arithcode.*;
import java.io.IOException;

public class Decoder {
        public static void main(String[] args) throws IOException {
        // timing
        long startMili = System.currentTimeMillis();
        // read paras
        DomXmlDocument paraXml = new DomXmlDocument();
        HashMap<String, String> paraMap = paraXml.parseXml(args[0]);
        int width = Integer.parseInt(paraMap.get("Width"));
        int height = Integer.parseInt(paraMap.get("Height"));
        int totalFrames = Integer.parseInt(paraMap.get("TotalFrames"));
        int gopSize = Integer.parseInt(paraMap.get("GopSize"));
        boolean codeCbCr = Boolean.parseBoolean(paraMap.get("CodeCbCr"));
        boolean acDecoding = Boolean.parseBoolean(paraMap.get("ArithmeticDecoding"));

        // prepare to read first gopSize org pics to recreate UV matrix
        String rFilename = paraMap.get("InputOrgPic");
        ArrayList<Picture> picList = new ArrayList<>();
        ReadYCbCr rYCbCr = ReadYCbCr.getInstance();
        rYCbCr.readPic(rFilename, width, height, gopSize, picList);

        /* start the encoding process */

        // 1st, stack gopSize pictures into 'stackedPicMatBase' matrix with (width*height) rows and gopSize cols
        StackGops stackedGopBase = new StackGops(width * height, gopSize, codeCbCr);
        stackedGopBase.matrixLineByLine(picList, 0);  // use the line by line method so that writing yuv at the end will be easy

        // 2nd, svd this stackedPicMatBase matrix
        Svd svdResultBase = new Svd(stackedGopBase.getMatrix(), codeCbCr);

        // 3rd, reshape U and V matrix into 'reshapedProductUVBase' with (width*height*gopSize) rows and gopSize cols
        MatrixCreationAndOperation reshapedProductUVBase = new ReshapeProductUV(width * height * gopSize, gopSize, codeCbCr);
        reshapedProductUVBase.operateMatrix(svdResultBase.getMatU(), svdResultBase.getMatV());
        // { NOMORLIAZATION
        double normalization = 255.0; // use this coz U and V are both divided by 255 for Psvd
        reshapedProductUVBase.getMatrix(0).timesEquals(1.0/normalization/normalization);
        if (codeCbCr) {
            reshapedProductUVBase.getMatrix(1).timesEquals(1.0/normalization/normalization);
            reshapedProductUVBase.getMatrix(2).timesEquals(1.0/normalization/normalization);
        }
        // }

        // 4th, inverse reshapedProductUVBase for Psvd's use
        MatrixCreationAndOperation inverseProductUVBase = new Inverse(gopSize, gopSize, codeCbCr);
        // inverseProductUVBase.operateMatrix(reshapedProductUVBase.getMatrix());
        // Since inverseProductUVBase is an identity matrix, here we directly generate it.
        // { NOMORLIAZATION
        Matrix[] coeffMat = new Matrix[1]; // use this because U and V are both divided by 255 for Psvd
        coeffMat[0] = new Matrix(1, 1, normalization*normalization*normalization*normalization);
        // }
        inverseProductUVBase.operateMatrix(coeffMat);

        // 5th, preparation for inverse pseudo svd
        InversePsvd invPsvdOperation = new InversePsvd(reshapedProductUVBase.getMatrix(), codeCbCr);
        // prepare to write dec.yuv
        WriteYCbCr wYCbCr = WriteYCbCr.getInstance();
        String wDecFilename = paraMap.get("OutputDecPic");
        int residueWidth = width * height;
        wYCbCr.startWriting(wDecFilename, residueWidth, residueWidth >> 2);
        // prepare to read Residue
        String rResidueFilename = paraMap.get("InputResPic");
        ArrayList<Picture> picResidueList = new ArrayList<>();
        rYCbCr = ReadYCbCr.getInstance();
        // input residue pre-operation
        rYCbCr.readPic(rResidueFilename, width, height, (totalFrames - gopSize), picResidueList);
        StackGops stackedResidueGop = new StackGops(width * height, gopSize, codeCbCr);
        MatrixCreationAndOperation stackedResidueToOneColVector = new StackToOneCol(width * height * gopSize, 1, codeCbCr);
        Matrix mat127 = new Matrix(width * height * gopSize, 1, 127);
        Matrix mat127CbCr = new Matrix((width * height * gopSize >> 2), 1, 127);

        List<LinkedList<Integer>> diagList;
        if (acDecoding) {
            // 6th arithmetic decoding
            ArithmeticDecompress arithmeticDecoder = new ArithmeticDecompress(paraMap.get("DiagBinFile"), codeCbCr);
            long arithmeticDecoderMili = System.currentTimeMillis();
            System.out.println("arithmeticDecoder time: " + ((arithmeticDecoderMili - startMili)/1000) + "s");

            // 7th diag.mat invBinarization
            Binarization invBinarization = new Binarization(arithmeticDecoder.getbinArr(), codeCbCr);
            diagList = invBinarization.invBinarizeDiag();
            
        }
        else {
            diagList  = new ArrayList<>();
            for (int color = 0; color < 3; color++) {
                diagList.add(new LinkedList<Integer>());
            }
            rYCbCr.readData(paraMap.get("DiagDataFile"), diagList, (totalFrames - gopSize), codeCbCr);

        }

        Iterator[] diagLinkedListIt = new Iterator[3];
        diagLinkedListIt[0] = diagList.get(0).iterator();
        if (codeCbCr) {
            diagLinkedListIt[1] = diagList.get(1).iterator();
            diagLinkedListIt[2] = diagList.get(2).iterator();
        }

        for (int gopNo = 1; gopNo < totalFrames / gopSize; gopNo++) {

            // psvd
            System.out.println("gopNo: "+gopNo);

            // write dec.yuv
            stackedResidueGop.matrixLineByLine(picResidueList, (gopNo - 1) * gopSize);
            stackedResidueToOneColVector.operateMatrix(stackedResidueGop.getMatrix());
            stackedResidueToOneColVector.getMatrix(0).minusEquals(mat127).timesEquals(1.0/normalization);
            if (codeCbCr) {
                stackedResidueToOneColVector.getMatrix(1).minusEquals(mat127CbCr).timesEquals(1.0/normalization);
                stackedResidueToOneColVector.getMatrix(2).minusEquals(mat127CbCr).timesEquals(1.0/normalization);
            }
            wYCbCr.writeMatColByCol(invPsvdOperation.invPsvd(stackedResidueToOneColVector.getMatrix(), diagLinkedListIt), gopSize, width * height, codeCbCr);
        }
        // end writing Residue
        wYCbCr.endWriting();

        // diag.mat Binarization
        // Binarization diagBinarization = new Binarization(psvdOperation.getDiag(), codeCbCr);
        // diagBinarization.binarizeDiag();

        // // arithmetic encoding
        // ArithmeticCompress arithmeticEncoder = new ArithmeticCompress(diagBinarization.getbinArr(), "binDiag", codeCbCr);

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