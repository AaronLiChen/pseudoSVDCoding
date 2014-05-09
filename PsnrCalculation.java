package cn.edu.ustc.aaron.common;

import java.util.*;
import cn.edu.ustc.aaron.encoder.*;
import Jama.Matrix;

public class PsnrCalculation {
    private int width;
    private int height;
    private int widthC;
    private int heightC;
    private int totalFrames;
    private int gopSize;
    private boolean codeCbCr;
    private double totalBytes;
    private int frameRate;

    private ArrayList<Picture> picOrgList;
    private ArrayList<Picture> picDecList;

    public PsnrCalculation (boolean codeCbCr, double totalBytes, int frameRate) {
        this.codeCbCr = codeCbCr;
        this.totalBytes = totalBytes;
        this.frameRate = frameRate;
    }

    public static void main(String[] args) {
        PsnrCalculation psnrCalc = new PsnrCalculation(true, 20, 438363);
        psnrCalc.readPic();
        psnrCalc.calcPsnr();
        psnrCalc.calcBitRate();
    }

    private void calcBitRate () {
        double time = (double)(totalFrames - gopSize) / (double)frameRate;
        double bitrate = 0.008 * totalBytes / time;
        System.out.println("BitRate: " + bitrate);
    }

    private void readPic () {
        width = 160;
        height = 128;
        widthC = width >> 1;
        heightC = height >> 1;
        totalFrames = 1000;
        gopSize = 5;
        codeCbCr = true;

        String rFilename = new String("L:\\PsvdSequence\\Campus\\trees.yuv");
        picOrgList = new ArrayList<>(); 
        ReadYCbCr rYCbCr = ReadYCbCr.getInstance();
        // read Org
        rYCbCr.readPic(rFilename, width, height, totalFrames, picOrgList);

        String rDecFilename = new String("Dec.yuv");
        picDecList = new ArrayList<>();
        rYCbCr = ReadYCbCr.getInstance();
        // read Dec
        rYCbCr.readPic(rDecFilename, width, height, totalFrames - gopSize, picDecList);
    }

    private void calcPsnr () {
        long ssdY = 0;
        long ssdCb = 0;
        long ssdCr = 0;
        double yPsnr = 0.0;
        double cbPsnr = 0.0;
        double crPsnr = 0.0;

        int maxvalY = 255;
        int maxvalC = 255;
        int iSize = width * height;
        double fRefValueY = (double) maxvalY * maxvalY * iSize;
        double fRefValueC = (double) maxvalC * maxvalC * iSize / 4.0;

        Picture orgPic = null;
        Picture decPic = null;
        for (int framesNo = gopSize; framesNo < totalFrames; framesNo++) {
            orgPic = picOrgList.get(framesNo);
            decPic = picDecList.get(framesNo - gopSize);
            ssdY = calcSSD(orgPic.getY(), decPic.getY(), width, height);
            yPsnr += ((ssdY != 0) ? 10.0 * log(fRefValueY / (double)ssdY, 10.0) : 99.99 );
            if (codeCbCr) {
                ssdCb = calcSSD(orgPic.getCb(), decPic.getCb(), widthC, heightC);
                ssdCr = calcSSD(orgPic.getCr(), decPic.getCr(), widthC, heightC);
                cbPsnr += ((ssdCb != 0) ? 10.0 * log(fRefValueC / (double)ssdCb, 10.0) : 99.99 );
                crPsnr += ((ssdCr != 0) ? 10.0 * log(fRefValueC / (double)ssdCr, 10.0) : 99.99 );
            }
        }

        System.out.println("Y-PSNR: "+ yPsnr/(totalFrames - gopSize));
        if (codeCbCr) {
            System.out.println("Cb-PSNR: "+ cbPsnr/(totalFrames - gopSize));
            System.out.println("Cr-PSNR: "+ crPsnr/(totalFrames - gopSize));
        }
    }

    private long calcSSD (int[] org, int[] dec, int width, int height) {
        long ssd = 0;
        for (int y = 0; y < height; y++) {
           for (int x = 0; x < width; x++) {
                int diff = org[y * width + x] - dec[y * width + x];
                ssd += diff * diff; 
            } 
        }
        return ssd;
    }

    private double log(double value, double base) {

        return Math.log(value) / Math.log(base);
    }
}