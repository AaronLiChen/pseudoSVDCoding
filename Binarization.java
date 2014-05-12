package cn.edu.ustc.aaron.common;

import java.util.*;
import Jama.*;

public class Binarization {
    private byte[][] binArr;
    private int[] totalLength;
    private List<LinkedList<Integer>> diagList;

    private boolean codeCbCr;

    public Binarization (List<LinkedList<Integer>> diagList, boolean codeCbCr) {
        this.codeCbCr = codeCbCr;
        this.diagList = diagList;

        totalLength = new int[3];
        calcTotalLength();
        binArr = new byte[3][];
        binArr[0] = new byte[totalLength[0]];
        if (codeCbCr) {
            binArr[1] = new byte[totalLength[1]];
            binArr[2] = new byte[totalLength[2]];
        }

    }

    public Binarization (byte[][] binArr, boolean codeCbCr) {
        this.codeCbCr = codeCbCr;
        this.binArr = binArr;
        this.diagList = new ArrayList<>();
        for (int color = 0; color < 3; color++) {
            diagList.add(new LinkedList<Integer>());
        }

    }

    private void calcTotalLength() {
        calcTotalLength(0);
        if (codeCbCr) {
            calcTotalLength(1);
            calcTotalLength(2);
        }
    }

    private void calcTotalLength(int color) {
        totalLength[color] = 0;
        Iterator diagListIt = diagList.get(color).iterator();
        while(diagListIt.hasNext()) {
            int elem = (int) diagListIt.next();
            totalLength[color] += Math.abs(elem);  // unary binarization
        }
        totalLength[color] += 2 * diagList.get(color).size();  // plus one sign bit and one segmentation bit
        System.out.println("Binarization totalLength[" + color + "]: " + totalLength[color]);
    }

    private void binarizeDiag(int color) {
        int binPosElemFront = 0;
        int binPosElemEnd = 0;
        byte[] binArray = binArr[color];
        Iterator diagListIt = diagList.get(color).iterator();
        while(diagListIt.hasNext()) {
            int elem = (int) diagListIt.next();
            if (elem < 0) {  // a sign bit at the front
                binArray[binPosElemFront] = (byte)1;
            }
            else {
                binArray[binPosElemFront] = (byte)0;
            }
            binPosElemEnd = binPosElemFront + Math.abs(elem);
            for (int binPos = binPosElemFront + 1; binPos <= binPosElemEnd; binPos++) {
                binArray[binPos] = (byte)1;
            }
            binArray[binPosElemEnd + 1] = (byte)0; // a segmentation bit at the end
            binPosElemFront = binPosElemEnd + 2;
        }
    }

    public void binarizeDiag() {
        binarizeDiag(0);
        if (codeCbCr) {
            binarizeDiag(1);
            binarizeDiag(2);
        }
    }

    private void invBinarizeDiag(int color) {
        int binPosElemFront = 0;
        byte[] binArray = binArr[color];
        while (binPosElemFront < binArray.length) {
            int diffPos = 1;  // skip the sign bit
            while (binArray[binPosElemFront+diffPos] == (byte)1) {   // calc the abs value
                diffPos++;
            }
            if (binArray[binPosElemFront] == (byte)1) {    // decode the sign bit
                diagList.get(color).offer((-1) * (diffPos - 1));
            }
            else if (binArray[binPosElemFront] == (byte)0) {
                diagList.get(color).offer(diffPos - 1);
            }
            else {
                System.out.println("Unsuported sign bit in decoding arithmetic coding.");
            }
            binPosElemFront += diffPos + 1; // skip the segmentation bit
        }
    }

    public List<LinkedList<Integer>> invBinarizeDiag() {
        invBinarizeDiag(0);
        if (codeCbCr) {
            invBinarizeDiag(1);
            invBinarizeDiag(2);
        }
        return diagList;
    }

    public byte[][] getbinArr () {
        return binArr;
    }
    public byte[] getbinArr (int color) {
        return binArr[color];
    }

    public List<LinkedList<Integer>> getDiag () {
        return diagList;
    }
}