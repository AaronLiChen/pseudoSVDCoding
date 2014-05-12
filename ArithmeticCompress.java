package nayuki.arithcode;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;


public class ArithmeticCompress {

	public ArithmeticCompress (byte[][] b, String outputFileName, boolean codeCbCr) throws IOException {

		arithmeticCompressProcess(b[0], outputFileName+"Y.bin");
		if (codeCbCr) {
			arithmeticCompressProcess(b[1], outputFileName+"Cb.bin");
			arithmeticCompressProcess(b[2], outputFileName+"Cr.bin");
		}
	}

	private void arithmeticCompressProcess (byte[] b, String outputFileName) throws IOException {
		File outputFile = new File(outputFileName);
		// byte[] b = new byte[]{0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 0 ,0 ,1, 1, 1, 1, 1, 1, 1, 1,0};

		
		// Read input file once to compute symbol frequencies
		FrequencyTable freq = getFrequencies(b);
		// freq.increment(2);  // EOF symbol gets a frequency of 1
		
		// Read input file again, compress with arithmetic coding, and write output file
		InputStream in = new ByteArrayInputStream(b);
		// InputStream in = new BufferedInputStream(new FileInputStream(inputFile));
		BitOutputStream out = new BitOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
		try {
			writeFrequencies(out, freq);
			System.out.println(freq.toString());
			compress(freq, in, out); // the AC is written after freq in outputBitstream
		} finally {
			out.close();
			// in.close();
		}
	}
	
	public static void main(String[] args) throws IOException {
		// Show what command line arguments to use
		// if (args.length == 0) {
		// 	System.err.println("Usage: java ArithmeticCompress InputFile OutputFile");
		// 	System.exit(1);
		// 	return;
		// }
		
		// Otherwise, compress
		// File inputFile = new File(args[0]);
		File outputFile = new File(args[0]);
		byte[] b = new byte[]{0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 0 ,0 ,1, 1, 1, 1, 1, 1, 1, 1,0};

		
		// Read input file once to compute symbol frequencies
		FrequencyTable freq = getFrequencies(b);
		// freq.increment(2);  // EOF symbol gets a frequency of 1
		
		// Read input file again, compress with arithmetic coding, and write output file
		InputStream in = new ByteArrayInputStream(b);
		// InputStream in = new BufferedInputStream(new FileInputStream(inputFile));
		BitOutputStream out = new BitOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
		try {
			writeFrequencies(out, freq);
			System.out.println(freq.toString());
			compress(freq, in, out); // the AC is written after freq in outputBitstream
		} finally {
			out.close();
			// in.close();
		}
	}

	private static FrequencyTable getFrequencies(byte[] b) {
		FrequencyTable freq = new SimpleFrequencyTable(new int[3]);
		// InputStream input = new BufferedInputStream(new FileInputStream(file));

		for (byte x : b) {
			freq.increment(x & 0xFF);
		}
		freq.increment(2);

		// try {
		// 	while (true) {
		// 		int b = input.read();
		// 		if (b == -1)
		// 			break;
		// 		freq.increment(b);
		// 	}
		// } finally {
		// 	input.close();
		// }
		return freq;
	}
	
	
	static void writeFrequencies(BitOutputStream out, FrequencyTable freq) throws IOException {
		for (int i = 0; i < 2; i++)
		{
			writeInt(out, 32, freq.get(i));
		}
	}
	
	
	static void compress(FrequencyTable freq, InputStream in, BitOutputStream out) throws IOException {
		ArithmeticEncoder enc = new ArithmeticEncoder(out);
		// int count = 0;
		while (true) {
			int b = in.read();
			if (b == -1)
				break;
			// System.out.print("\r"+(++count));
			enc.write(freq, b);

		}
		// System.out.print("\n");
		enc.write(freq, 2);  // EOF
		enc.finish();
	}
	
	
	private static void writeInt(BitOutputStream out, int numBits, int value) throws IOException {
		if (numBits < 0 || numBits > 32)
			throw new IllegalArgumentException();
		
		for (int i = 0; i < numBits; i++)
			out.write((byte)(value >>> i & 1));  // Little endian
	}
	
}
