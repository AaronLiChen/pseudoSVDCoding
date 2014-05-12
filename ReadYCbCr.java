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

	public void readPic(String filename, int width, int height, int framesToBeCoded, ArrayList<Picture> picList) {
		int widthC = width >> 1;
		int heightC = height >> 1;
        startReading(filename, width, widthC);
            
        for (int framesNo = 0; framesNo < framesToBeCoded; framesNo++) {
            Picture picTemp = new Picture(width, height);
            readPlane(picTemp.getY(), oneLineY, width, height); 
            readPlane(picTemp.getCb(), oneLineCbCr, widthC, heightC);  
            readPlane(picTemp.getCr(), oneLineCbCr, widthC, heightC);  
            picList.add(picTemp); 
        }

        endReading();
	} 
	
	private void startReading(String filename, int width, int widthC) {
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

	private void endReading() {
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
				src.offer(dis.readInt());
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
			startReading(filename, 0, 0);
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
			endReading();
			return true;
		}
	}
    
}