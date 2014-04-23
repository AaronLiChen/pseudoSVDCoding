package cn.edu.ustc.aaron.encoder;

import java.io.*;
import javax.swing.*;
import java.awt.image.*;
import java.util.*;
import cn.edu.ustc.aaron.common.Picture;
import Jama.Matrix;

public class WriteYCbCr
{
	private static WriteYCbCr writeYCbCrSingleton;
	private DataOutputStream dos;
	private FileWriter fw;
	private byte[] oneLineY;
	private byte[] oneLineCbCr;

	private WriteYCbCr() {

	}
	
	public static WriteYCbCr getInstance() {
		if (writeYCbCrSingleton == null) {
			writeYCbCrSingleton = new WriteYCbCr();	
		}
		return writeYCbCrSingleton;
	}

	public void writePic(String filename, int width, int height, int framesToBeWritten, ArrayList<Picture> picList) {
		int widthC = width >> 1;
		int heightC = height >> 1;
        startWriting(filename, width, widthC);
        // Z:\\sequences_3DV_CfP\\Balloons\\Balloons3.yuv E:\\JavaWorkSpace\\PsvdProject\\Sequences\\Campus\\trees.yuv
        
        Picture picTemp; 
        for (int framesNo = 0; framesNo < framesToBeWritten; framesNo++) {
            picTemp = picList.get(framesNo);
            writePlane(picTemp.getY(), oneLineY, width, height); 
            writePlane(picTemp.getCb(), oneLineCbCr, widthC, heightC);  
            writePlane(picTemp.getCr(), oneLineCbCr, widthC, heightC);    
        }

        endWriting();
	}

	private void startWriting(String filename, int width, int widthC)
	{
		try
		{
			dos = new DataOutputStream(new BufferedOutputStream(
					new FileOutputStream(filename)));
			
			oneLineY = new byte[width];
			oneLineCbCr = new byte[widthC];
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private boolean writePlane(int[] src, byte[] tmp, int width, int height) {
		try	{
			for (int y = 0; y < height; y++) 
			{
				for (int x = 0; x < width; x++) 
				{
					tmp[x] = (byte)src[y * width + x];
				}
				dos.write(tmp);
			}

			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	private void endWriting() {
		try	{
			dos.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void startWritingTxt(String filename) {
		try {
			fw = new FileWriter(filename);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean writeTxt(String filename, int[] src, int width, int height) {
		startWritingTxt(filename);

		try {
			for (int y = 0; y < height; y++) 
			{
				for (int x = 0; x < width; x++) 
				{
					fw.write(String.valueOf(src[y * width + x])+"\t");
				}
				fw.write("\r\n");
			}

			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public boolean writeTxt(String filename, Matrix src) {
		startWritingTxt(filename);
		double[][] srcMat = src.getArray();
		int width = src.getColumnDimension();
		int height = src.getRowDimension(); 
		try {
			for (int y = 0; y < height; y++) 
			{
				for (int x = 0; x < width; x++) 
				{
					fw.write(String.format("%1.4g\t", srcMat[y][x]));
				}
				fw.write("\r\n");
			}

			fw.flush();
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
}