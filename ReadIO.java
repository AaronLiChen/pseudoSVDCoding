package cn.edu.ustc.aaron.encoder;

import javax.imageio.ImageIO;
import javax.imageio.IIOException;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.ImageReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.awt.image.BufferedImage;

public class ReadIO {

    // public void readBmp() {
    //     File f;
    //     try {
    //         f = new File("E:\\JavaWorkSpace\\PsvdProject\\bmp\\00005.bmp");
    //     } catch (IIOException e) {
    //        e.printStackTrace(); 
    //     }
    //     BufferedImage bi = ImageIO.read(f);
    //     File wf = new File("E:\\JavaWorkSpace\\PsvdProject\\w05.bmp");
    //     ImageIO.write(bi, "bmp", wf);
    // }

    public static void main(String[] args) {

       File f = new File("E:\\JavaWorkSpace\\PsvdProject\\bmp\\00005.bmp");
       if (f == null) {
        System.out.println("f==null");
       }
       else
       {
        System.out.println(f);
       }
       try{
           BufferedImage bi = ImageIO.read(f); 
           System.out.println(bi.getHeight()+", width: " + bi.getWidth());
           
       } catch (IOException e) {
            System.out.println("can't open file" + f);
       }
       try{
            BufferedImage bi = ImageIO.read(f);
            File wf = new File("E:\\JavaWorkSpace\\PsvdProject\\bmp\\5w.jpg");
            ImageIO.write(bi, "jpg", wf);
       } catch (IOException e) {
            System.out.println("can't write file" + f);
       }


       // File f = new File("bmp\\00005.bmp");
       // ImageInputStream iis = null;
       // try {
       //      iis = ImageIO.createImageInputStream(f);
       // } catch (IIOException iioe1) {
       //      System.out.println("Unable to create an input stream.");
       //      return;
       // }
       // Iterator readers = ImageIO.getImageReadersByFormatName("bmp");
       // ImageReader reader = (ImageReader) readers.next(); 
       // reader.setInput(iis, true);
       // try {
       //      reader.read(0, param);
       // } catch (IIOException iioe2) {
       //      System.out.println("An error occured during reading: " + iioe2.getMessage());
       //      Throwable t = iioe2.getCause();
       //      if ((t != null) && (t instanceof IOException)) {
       //          System. out.println("Cause by IOException: " + t.getMessage());
       //      }
       // }
    }
}