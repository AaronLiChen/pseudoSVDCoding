package cn.edu.ustc.aaron.encoder;

import java.util.*;
import cn.edu.ustc.aaron.common.*;
import Jama.*;

public class Encoder {
    
    public static void main(String[] args) {
        int width = 160;
        int height = 128;
        int totalFrames = 300;
        String rFilename = new String("E:\\JavaWorkSpace\\PsvdProject\\Sequences\\Campus\\trees.yuv");
        ArrayList<Picture> picList = new ArrayList<>(); // Z:\\sequences_3DV_CfP\\Balloons\\Balloons3.yuv E:\\JavaWorkSpace\\PsvdProject\\Sequences\\Campus\\trees.yuv

        ReadYCbCr rYCbCr = ReadYCbCr.getInstance();
        rYCbCr.readPic(rFilename, width, height, totalFrames, picList);

        // String wFilename = new String("TreesW.yuv");
        WriteYCbCr wYCbCr = WriteYCbCr.getInstance();
        // wYCbCr.writePic(wFilename, width, height, totalFrames, picList);

        int gopSize = 60;
        MatrixPermutation matrixPerBase = new MatrixPermutation(gopSize, width, height, true);
        // first, stack gopSize pictures into one matrix -- stackedPicMatBase
        Matrix[] stackedPicMatBase = matrixPerBase.matrixLineByLine(picList, 0);
        // second, svd this stackedPicMatBase matrix
        Svd svdResultBase = new Svd(stackedPicMatBase, true);
        // third, reshape U and V matrix
        Matrix[] stackedUVMatBase = matrixPerBase.matrixStackUV(svdResultBase.getMatU(), svdResultBase.getMatV());
        String wFilename = new String("U.txt");
        wYCbCr.writeTxt(wFilename, svdResultBase.getMatU()[1]);
        wFilename = new String("V.txt");
        wYCbCr.writeTxt(wFilename, svdResultBase.getMatV()[1]);
        wFilename = new String("stackedUV.txt");
        wYCbCr.writeTxt(wFilename, stackedUVMatBase[1]);

        MatrixPermutation matrixPer = new MatrixPermutation(gopSize, width, height, true);
        Matrix[] stackedPicMat = null, stackedOneColMat = null;
        for (int gopNo = 1; gopNo < totalFrames / gopSize; gopNo++) {
            stackedPicMat = matrixPer.matrixLineByLine(picList, gopNo * gopSize);
            stackedOneColMat = matrixPer.matrixStackToOneCol(stackedPicMat);
        }

        
    }

}