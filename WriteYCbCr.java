package cn.edu.ustc.aaron.common;

import java.io.*;
import javax.swing.*;
import java.awt.image.*;
import java.util.*;
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
        
        Picture picTemp; 
        for (int framesNo = 0; framesNo < framesToBeWritten; framesNo++) {
            picTemp = picList.get(framesNo);
            writePlane(picTemp.getY(), oneLineY, width, height); 
            writePlane(picTemp.getCb(), oneLineCbCr, widthC, heightC);  
            writePlane(picTemp.getCr(), oneLineCbCr, widthC, heightC);    
        }

        endWriting();
	}

	public void writeMatColByCol(Matrix[] pictureMat, int gopSize, int frameSize, boolean codeCbCr) {
		int frameSizeC = frameSize >> 2;

		for (int frameNo = 0; frameNo < gopSize; frameNo++) {
			writePlaneColByCol(pictureMat[0].getArray(), oneLineY, frameNo * frameSize, frameSize); 
        	if (codeCbCr) {
        		writePlaneColByCol(pictureMat[1].getArray(), oneLineCbCr, frameNo * frameSizeC, frameSizeC);  
        		writePlaneColByCol(pictureMat[2].getArray(), oneLineCbCr, frameNo * frameSizeC, frameSizeC);  	
        	}
        	else {        	
        		writePlaneConstant(128, oneLineCbCr, frameSizeC);  
        		writePlaneConstant(128, oneLineCbCr, frameSizeC);
        	}
		}          
	}

	public void startWriting(String filename, int width, int widthC)
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

	private boolean writePlaneColByCol(double[][] src, byte[] tmp, int startPos, int height) {
		try	{
			for (int y = 0; y < height; y++)
			{
				tmp[y] = (byte)(src[y+startPos][0] + 0.5);
			}
			dos.write(tmp);
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	private boolean writePlaneConstant(int src, byte[] tmp, int height) {
		try	{
			for (int y = 0; y < height; y++)
			{
				tmp[y] = (byte)src;
			}
			dos.write(tmp);
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public void endWriting() {
		try	{
			dos.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void endWritingTxt() {
		try	{
			fw.close();
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
		finally {
			endWritingTxt();
		}
	}

	public boolean writeTxt(String filename, byte[] src, int width, int height) {
		startWritingTxt(filename);

		try {
			for (int y = 0; y < height; y++) 
			{
				for (int x = 0; x < width; x++) 
				{
					fw.write(String.valueOf(src[y * width + x] & 0xFF)+"\t");
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
		finally {
			endWritingTxt();
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
					fw.write(String.format("%1.9g\t", srcMat[y][x]));
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
		finally {
			endWritingTxt();
		}
	}

	private boolean writeData(LinkedList<Integer> src, int color) {
		Iterator srcIt = src.iterator();
		try {
			int elem;
			while (srcIt.hasNext()) {
				elem = (int)srcIt.next();
				dos.writeInt(elem);
				// System.out.println(elem);
			}
			return true;
		}
		catch (Exception e)	{
			e.printStackTrace();
			return false;
		}
	}

	public boolean writeData(String filename, List<LinkedList<Integer>> srcList, boolean codeCbCr) {
		try {
			startWriting(filename, 0, 0);
			writeData(srcList.get(0), 0);
			if (codeCbCr) {
				writeData(srcList.get(1), 1);
				writeData(srcList.get(2), 2);
			}
		}
		catch (Exception e)	{
			e.printStackTrace();
			return false;
		}
		finally {
			endWriting();
			return true;
		}
	}
}