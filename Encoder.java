package cn.edu.ustc.aaron.encoder;

import java.util.*;
import cn.edu.ustc.aaron.common.*;
import Jama.*;

public class Encoder {
    
    public static void main(String[] args) {
        int width = 160;
        int height = 128;
        int totalFrames = 300;

        // prepare to read YCbCr
        String rFilename = new String("E:\\JavaWorkSpace\\PsvdProject\\Sequences\\Campus\\trees.yuv");
        ArrayList<Picture> picList = new ArrayList<>(); // Z:\\sequences_3DV_CfP\\Balloons\\Balloons3.yuv E:\\JavaWorkSpace\\PsvdProject\\Sequences\\Campus\\trees.yuv
        ReadYCbCr rYCbCr = ReadYCbCr.getInstance();
        // read YCbCr
        rYCbCr.readPic(rFilename, width, height, totalFrames, picList);

        // prepare to write YCbCr
        WriteYCbCr wYCbCr = WriteYCbCr.getInstance();

        int gopSize = 60;
        // first, stack gopSize pictures into one matrix -- stackedPicMatBase with (width*height) rows and gopSize cols
        StackGops stackedGopBase = new StackGops(width * height, gopSize, true);
        stackedGopBase.matrixLineByLine(picList, 0);
        // second, svd this stackedPicMatBase matrix
        Svd svdResultBase = new Svd(stackedGopBase.getPermutedMatrix(), true);
        // third, reshape U and V matrix -- reshapedProductUVBase with (width*height*gopSize) rows and gopSize cols
        MatrixPermutation reshapedProductUVBase = new ReshapeProductUV(width * height * gopSize, gopSize, true);
        reshapedProductUVBase.permuteMatrix(svdResultBase.getMatU(), svdResultBase.getMatV());
        // fourth, inverse reshapedProductUVBase
        MatrixPermutation inverseProductUVBase = new Inverse(gopSize, gopSize, true);
        inverseProductUVBase.permuteMatrix(reshapedProductUVBase.getPermutedMatrix());

        // prepare to read following YCbCr
        StackGops stackedGop = new StackGops(width * height, gopSize, true);
        // prepare to stack gop matrix to one-col vector with (width*height*gopSize) rows
        MatrixPermutation stackedToOneColVector = new StackToOneCol(width * height * gopSize, 1, true);
        for (int gopNo = 1; gopNo < 2; gopNo++) {
            // read following YCbCr
            stackedGop.matrixLineByLine(picList, gopNo * gopSize);
            // stack gop matrix to one-col vector
            stackedToOneColVector.permuteMatrix(stackedGop.getPermutedMatrix());
        }

        
    }

}