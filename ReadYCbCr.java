package cn.edu.ustc.aaron.common;

import java.io.*;
import javax.swing.*;
import java.awt.image.*;
import javax.imageio.*;
import java.util.*;

public class ReadYCbCr
{
	private static ReadYCbCr readYCbCrSingleton;

	private DataInputStream dis;

	private DataInputStream disData;
	private byte[] oneLineY;
	private byte[] oneLineCbCr;
	
	private ReadYCbCr() {

	}

	public static ReadYCbCr getInstance() {
		if (readYCbCrSingleton == null) {
			readYCbCrSingleton = new ReadYCbCr();	
		}
		return readYCbCrSingleton;
	}

	public void readPic(int width, int height, int startFrame, int framesToBeCoded, ArrayList<Picture> picList) {
		int widthC = width >> 1;
		int heightC = height >> 1;
            
        for (int framesNo = startFrame; framesNo < startFrame + framesToBeCoded; framesNo++) {
        	Picture picTemp;
        	if (startFrame == 0) {
        		picTemp = new Picture(width, height);
        		//System.out.println(framesNo+"size:"+picList.size());
        		picList.add(picTemp); 
        	} else {
        		picTemp = picList.get(framesNo % picList.size());
        		//System.out.println(framesNo+"nosizeup,size:"+picList.size());
        	}
            readPlane(picTemp.getY(), oneLineY, width, height); 
            readPlane(picTemp.getCb(), oneLineCbCr, widthC, heightC);  
            readPlane(picTemp.getCr(), oneLineCbCr, widthC, heightC);  
        }
	} 
	
	public void startReading(String filename, int width, int widthC) {
		try
		{
			dis = new DataInputStream(new BufferedInputStream(
					new FileInputStream(filename)));
            
			oneLineY = new byte[width];
			oneLineCbCr = new byte[widthC];
		} 
		
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void startReading(DataInputStream disData, String filename, int width, int widthC) {
		try
		{
			disData = new DataInputStream(new BufferedInputStream(
					new FileInputStream(filename)));
			this.disData = disData;
            
			oneLineY = new byte[width];
			oneLineCbCr = new byte[widthC];
		} 
		
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
		
	 private boolean readPlane(int[] dst, byte[] tmp, int width, int height) {
		try
		{
			for (int y = 0; y < height; y++) 
			{
				dis.read(tmp);
				for (int x = 0; x < width; x++) 
				{
					dst[y * width + x] = unsignedByteToInt(tmp[x]);
				}	
			}

			return true;
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}

	}

	public void endReading(DataInputStream disData) {
		try {
			disData.close();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void endReading() {
		try {
			dis.close();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private static int unsignedByteToInt(byte b) {
		return (int) b & 0xFF;
	}

	private boolean readData(LinkedList<Integer> src, int total, int color) {
		try {
			int count = 0;
			while (count < total) {
				src.offer(disData.readInt());
				// System.out.println(src.peekLast());
				count++;
			}
			return true;
		}
		catch (Exception e)	{
			e.printStackTrace();
			return false;
		}
	}

	public boolean readData(String filename, List<LinkedList<Integer>> srcList, int total, boolean codeCbCr) {
		try {
			startReading(disData, filename, 0, 0);
			readData(srcList.get(0), total, 0);
			if (codeCbCr) {
				readData(srcList.get(1), total, 1);
				readData(srcList.get(2), total, 2);
			}
		}
		catch (Exception e)	{
			e.printStackTrace();
			return false;
		}
		finally {
			endReading(disData);
			return true;
		}
	}
    
}