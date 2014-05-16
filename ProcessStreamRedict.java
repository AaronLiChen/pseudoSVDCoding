package cn.edu.ustc.aaron.common;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

class ProcessStreamRedict extends Thread  {
    InputStream is;
    String      type;
    OutputStream os;
    ProcessStreamRedict(InputStream is, String type) {
        this(is, type, null);
    }
    ProcessStreamRedict(InputStream is, String type, OutputStream redirect) {
        this.is = is;
        this.type = type;
        this.os = redirect;
    }
    public void run() {
        try {
            PrintWriter pw = null;
            if (os != null)
                pw = new PrintWriter(os);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String line = null;
            while ((line = br.readLine()) != null) {
                if (pw != null)
                    pw.println(line);
                System.out.println(type + ">" + line);
            }
            if (pw != null)
                pw.flush();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }    
}