package cn.edu.ustc.aaron.encoder;

import java.util.*;
import Jama.*;

public class FindAndModify {
    public static double showSign (double value) {
        if (value == 0.0) {
            return 0.0;
        }
        else {
            return (value / Math.abs(value));
        }
    }
    public static void findModifyLessThan (Matrix modifyTarget, Matrix findTarget, double beta, boolean codeCbCr) {
        double [][] findTar = findTarget.getArray();
        double [][] modifyTar = modifyTarget.getArray();
        for (int row = 0; row < findTarget.getRowDimension(); row++) {
            for (int col = 0; col < findTarget.getColumnDimension(); col++) {
                if (findTar[row][col] < 1.0 / beta) {
                    modifyTar[row][col] = 0; 
                }
            }
        }
    }

    public static void findModifyMoreThan (Matrix modifyTarget, Matrix findTarget, double beta, boolean codeCbCr) {
        double [][] findTar = findTarget.getArray();
        double [][] modifyTar = modifyTarget.getArray();
        for (int row = 0; row < findTarget.getRowDimension(); row++) {
            for (int col = 0; col < findTarget.getColumnDimension(); col++) {
                if (findTar[row][col] > 1.0 / beta) {
                    modifyTar[row][col] = findTar[row][col] - 1.0 / beta * showSign(findTar[row][col]); 
                }
            }
        }
    }
}

