package cn.edu.ustc.aaron.common;

import cn.edu.ustc.aaron.encoder.*;
import cn.edu.ustc.aaron.decoder.*;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.PrintStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.HashMap;

public class PsvdTest {

    private String[] qps;
    private String seqName;
    private String srcSeqPath;
    private String frameRate;
    private String frameSkip;
    private String width;
    private String height;
    private String totalFrames;

    private String gopSize;
    private String codeCbCr;
    private static String arithmeticEncoding = "true";
    private static String arithmeticDecoding = "false";
    private static String diagBinFile = "Diag";
    private static String diagData = "true";
    private static String diagDataFile = "Diag.data";
    private String totalBytes;

    private HashMap<String, String> hmap;


    public PsvdTest (String[] qps, String srcSeqPath, String seqName, String frameRate, String totalFrames, String width, String height, String gopSize, String codeCbCr) {
        this.qps = qps;
        this.seqName = seqName;
        this.frameRate = frameRate;
        this.totalFrames = totalFrames;
        this.frameSkip = gopSize;
        this.width = width;
        this.height = height;
        this.gopSize = gopSize;
        this.codeCbCr = codeCbCr;

        this.srcSeqPath = srcSeqPath;
        hmap = new HashMap<>();
        hmap.put("OrgYuv", srcSeqPath+seqName+".yuv");
        hmap.put("Width", width);
        hmap.put("Height", height);
        hmap.put("FrameRate", frameRate);
        hmap.put("TotalFrames", totalFrames);
        hmap.put("GopSize", gopSize);
        hmap.put("CodeCbCr", codeCbCr);
        hmap.put("ArithmeticEncoding", arithmeticEncoding);
        hmap.put("ArithmeticDecoding", arithmeticDecoding);
        hmap.put("DiagBinFile", "common/bits/"+seqName+diagBinFile);
        hmap.put("DiagData", diagData);
        hmap.put("DiagDataFile", "common/bits/"+seqName+diagDataFile);
    }

    public static void main(String[] args) throws IOException {
        String[] qps = {"22", "27", "32", "37"};
        PsvdTest psvdTest = new PsvdTest(qps, args[0], args[1], args[2], args[3], args[4], args[5], args[6], new String("true"));
        //make directories
        psvdTest.makeDirectory("./common/bits");
        psvdTest.makeDirectory("./common/yuv");
        psvdTest.makeDirectory("./Cfg/h265");
        psvdTest.makeDirectory("./Cfg/psvd");

        psvdTest.makeDirectory("./Out/h265/log");
        psvdTest.makeDirectory("./Out/h265/bits");
        psvdTest.makeDirectory("./Out/h265/yuv");

        psvdTest.makeDirectory("./Out/psvd/xml");
        psvdTest.makeDirectory("./Out/psvd/log");
        psvdTest.makeDirectory("./Out/psvd/bits");
        psvdTest.makeDirectory("./Out/psvd/yuv");

        psvdTest.makeDirectory("./Done");
        // make cfg for h265 and psvd
        psvdTest.mkCfg();

        // prepare to make Xml
        DomXmlDocument dxd = new DomXmlDocument();

        // make encoder.xml
        psvdTest.hmap.put("ResYuv", "common/yuv/"+psvdTest.seqName+"Residue.yuv");
        dxd.createXmlFromTemplate("common/xml/encoder.xml", "./Out/psvd/xml/encoder.xml", psvdTest.hmap);
        // encode
        if (!(new File("./Done/encoder.done").exists())) {
            PrintStream encoderOutLog = null;
            try {
                // redirect encoderOutLog
                encoderOutLog = new PrintStream(new BufferedOutputStream(new FileOutputStream("Out/psvd/log/encoder.log")));
                System.setOut(encoderOutLog);
                // encode
                Encoder.encode("Out/psvd/xml/encoder.xml");
                DirMaker.createFile(new File("./Done/encoder.done"));
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                // close redirection
                encoderOutLog.close();
                System.setOut(System.out);
            }
        }

        // File fileY = new File(psvdTest.seqName+psvdTest.diagBinFile+"Y.bin");
        // File fileCb = new File(psvdTest.seqName+psvdTest.diagBinFile+"Cb.bin");
        // File fileCr = new File(psvdTest.seqName+psvdTest.diagBinFile+"Cr.bin");
        // long diagFileSize = fileY.length() + fileCb.length() + fileCb.length();
        File diagDataFile = new File(psvdTest.seqName+psvdTest.diagDataFile);
        long diagFileSize = diagDataFile.length();

        // run h265 TAppEncoder.exe
        for (String qp : qps) {
            if (!(new File("./Done/h265Encoder"+qp+".done").exists())) {
                Process h265EncoderProcess = runEXE("common/bin/TAppEncoder.exe -c common/cfg/encoder_randomaccess_main.cfg -c Cfg/h265/"+psvdTest.seqName+qp+".cfg");
                // h265 TAppEncoder.exe log redirection
                FileOutputStream h265Fos = new FileOutputStream("Out/h265/log/"+psvdTest.seqName+qp+".log");
                ProcessStreamRedirect h265Redirect = new ProcessStreamRedirect(h265EncoderProcess.getInputStream(), "OUTPUT", h265Fos);
                h265Redirect.start();
                DirMaker.createFile(new File("./Done/h265Encoder"+qp+".done"));
            }
        }

        for (String qp : qps) {
            if (!(new File("./Done/psvdResidueEncoder"+qp+".done").exists())) {
                // run psvd TAppEncoder.exe
                System.setOut(null);
                Process  psvdEncoderProcess = runEXE("common/bin/TAppEncoder.exe -c common/cfg/encoder_randomaccess_main.cfg -c Cfg/psvd/"+psvdTest.seqName+"Residue"+qp+".cfg");
                // psvd TAppEncoder.exe log redirection
                FileOutputStream psvdFos = new FileOutputStream("Out/psvd/log/"+psvdTest.seqName+"Residue"+qp+".log");
                ProcessStreamRedirect psvdRedirect = new ProcessStreamRedirect(psvdEncoderProcess.getInputStream(), "OUTPUT", psvdFos);
                psvdRedirect.start();
                try {
                    psvdEncoderProcess.waitFor();
                    DirMaker.createFile(new File("./Done/psvdResidueEncoder"+qp+".done"));
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
                System.setOut(System.out);
            }

            // make decoder.xml
            psvdTest.hmap.put("ResYuv", "Out/psvd/yuv/"+psvdTest.seqName+"Residue"+qp+"Rec.yuv");
            psvdTest.hmap.put("DecYuv", "Out/psvd/yuv/"+psvdTest.seqName+""+qp+"Dec.yuv");
            dxd.createXmlFromTemplate("common/xml/decoder.xml", "./Out/psvd/xml/decoder"+qp+".xml", psvdTest.hmap);
            // decode
            if (!(new File("./Done/decoder"+qp+".done").exists())) {
                PrintStream decoderOutLog = null;
                try {
                    // redirect decoderOutLog
                    decoderOutLog = new PrintStream(new BufferedOutputStream(new FileOutputStream("Out/psvd/log/decoder"+qp+".log")));
                    System.setOut(decoderOutLog);
                    // decode
                    Decoder.decode("Out/psvd/xml/decoder"+qp+".xml");
                    DirMaker.createFile(new File("./Done/decoder"+qp+".done"));
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                } finally {
                    // close redirection
                    decoderOutLog.close();
                    System.setOut(System.out);
                }
            }
            

            // make psnr.xml and calc psnr
            File resFile = new File("Out/psvd/bits/"+psvdTest.seqName+"Residue"+qp+".bin");
            long resFileSize = resFile.length();
            psvdTest.hmap.put("TotalBytes", String.valueOf(diagFileSize+resFileSize));
            dxd.createXmlFromTemplate("common/xml/psnr.xml", "./Out/psvd/xml/psnr"+qp+".xml", psvdTest.hmap);
            // redirect decoderOutLog
            PrintStream psnrOutLog = new PrintStream(new BufferedOutputStream(new FileOutputStream("Out/psvd/log/psnr"+qp+".log")));
            System.setOut(psnrOutLog);
            // calc psnr
            PsnrCalculation.calcPsnr("Out/psvd/xml/psnr"+qp+".xml");
            // close redirection
            psnrOutLog.close();
            System.setOut(System.out);
        }
        
    }

    private static Process runEXE (String cmd) {
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            return p;
        } catch (Exception e) {
            System.out.println("Error exe: "+e.getMessage());
        }
        return null;
    }

    private void mkCfg () {
        for (String qp : qps) {
            try {
                File h265Cfg = new File("./Cfg/h265/"+seqName+qp+".cfg");
                File psvdCfg = new File("./Cfg/psvd/"+seqName+"Residue"+qp+".cfg");
                DirMaker.createFile(h265Cfg);
                DirMaker.createFile(psvdCfg);
                writeParaToCfg(h265Cfg, this.srcSeqPath, "Out/h265/", seqName, frameRate, frameSkip, width, height, String.valueOf(Integer.parseInt(totalFrames) - Integer.parseInt(gopSize)), qp);
                writeParaToCfg(psvdCfg, "common/yuv/", "Out/psvd/", seqName+"Residue", frameRate, "0", width, height, totalFrames, qp);
            }
            catch (IOException e) {
                System.out.println("Can't create cfg files: "+e.getMessage());
            }
        }
    }

    private void writeParaToCfg (File file, String srcSeqPath, String dstSeqPath, String seqName, String frameRate, String frameSkip, String width, String height, String totalFrames, String qp) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write("InputFile : "+srcSeqPath+seqName+".yuv\n");
            writer.write("InputBitDepth : 8\n");
            writer.write("FrameRate : "+frameRate+"\n");
            writer.write("FrameSkip : "+frameSkip+"\n");
            writer.write("SourceWidth : "+width+"\n");
            writer.write("SourceHeight : "+height+"\n");
            writer.write("FramesToBeEncoded : "+totalFrames+"\n");
            writer.write("BitstreamFile : "+dstSeqPath+"bits/"+seqName+qp+".bin\n");
            writer.write("ReconFile : "+dstSeqPath+"yuv/"+seqName+qp+"Rec.yuv\n");
            writer.write("QP : "+qp+"\n");
            writer.write("Level : 3.1\n");

            writer.close();
        } catch (Exception e) {
            System.out.println("Write cfg error: "+e.getMessage());
        }
    }
    private void makeDirectory (String dirName) {
        DirMaker.makeDir(new File(dirName));
    }
}