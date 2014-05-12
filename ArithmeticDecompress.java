package nayuki.arithcode;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;


public class ArithmeticDecompress {

	private byte[][] binArr;
	private List<LinkedList<Byte>> binList;
	private boolean codeCbCr;

	public ArithmeticDecompress (String inputFileName, boolean codeCbCr) throws IOException {
		this.codeCbCr = codeCbCr;

		binList = new ArrayList<>();
        for (int color = 0; color < 3; color++) {
            binList.add(new LinkedList<Byte>());
        }

        arithmeticDecompressProcess(binList.get(0), inputFileName+"Y.bin");
		if (codeCbCr) {
			arithmeticDecompressProcess(binList.get(1), inputFileName+"Cb.bin");
			arithmeticDecompressProcess(binList.get(2), inputFileName+"Cr.bin");
		}
		listToArr();
	}

	public byte[][] getbinArr () {
        return binArr;
    }
    public byte[] getbinArr (int color) {
        return binArr[color];
    }

	private void allocMemory () {
		binArr = new byte[3][];
		binArr[0] = new byte[binList.get(0).size()];
        if (codeCbCr) {
            binArr[1] = new byte[binList.get(1).size()];
            binArr[2] = new byte[binList.get(2).size()];
        }
	}

	private void listToArr (int color) {
		Iterator binLinkedListIt = binList.get(color).iterator();
		byte[] binArray = binArr[color];
		int binArrPos = 0;
		while (binLinkedListIt.hasNext()) {
			binArray[binArrPos++] = (byte)binLinkedListIt.next();
		}
	}

	private void listToArr () {
		allocMemory ();
		listToArr(0);
		if (codeCbCr) {
			listToArr(1);
			listToArr(2);
		}
	}

	private void arithmeticDecompressProcess (LinkedList<Byte> b, String inputFileName) throws IOException {

		// Otherwise, decompress
		File inputFile = new File(inputFileName);
		// File outputFile = new File(outputFileName);
		
		BitInputStream in = new BitInputStream(new BufferedInputStream(new FileInputStream(inputFile)));
		// OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));
		// Writer out = new BufferedWriter(new FileWriter(outputFile));
		try {
			FrequencyTable freq = readFrequencies(in);
			System.out.println(freq.toString());
			decompress(freq, in, b);
		} finally {
			// out.close();
			in.close();
		}
	}
	
	public static void main (String[] args) throws IOException {
		// Show what command line arguments to use
		if (args.length == 0) {
			System.err.println("Usage: java ArithmeticDecompress InputFile OutputFile");
			System.exit(1);
			return;
		}
		
		// Otherwise, decompress
		File inputFile = new File(args[0]);
		File outputFile = new File(args[1]);
		
		BitInputStream in = new BitInputStream(new BufferedInputStream(new FileInputStream(inputFile)));
		// OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));
		Writer out = new BufferedWriter(new FileWriter(outputFile));
		try {
			FrequencyTable freq = readFrequencies(in);
			// System.out.println(freq.toString());
			decompress(freq, in, out);
		} finally {
			out.close();
			in.close();
		}
	}
	
	
	static FrequencyTable readFrequencies(BitInputStream in) throws IOException {
		int[] freqs = new int[3];
		for (int i = 0; i < 2; i++)
			freqs[i] = readInt(in, 32);
		freqs[2] = 1;  // EOF symbol
		return new SimpleFrequencyTable(freqs);
	}
	
	
	static void decompress(FrequencyTable freq, BitInputStream in, Writer out) throws IOException {
		ArithmeticDecoder dec = new ArithmeticDecoder(in);
		while (true) {
			int symbol = dec.read(freq);
			if (symbol == 2)  // EOF symbol
				break;
			out.write(symbol);
		}
	}
	static void decompress(FrequencyTable freq, BitInputStream in, LinkedList<Byte> b) throws IOException {
		ArithmeticDecoder dec = new ArithmeticDecoder(in);
		// int count = 0;
		while (true) {
			int symbol = dec.read(freq);
			if (symbol == 2)  // EOF symbol
				break;
			// System.out.print("\r"+(++count));
			b.offer((byte)symbol);
		}
		// System.out.print("\n");
	}
	
	
	private static int readInt(BitInputStream in, int numBits) throws IOException {
		if (numBits < 0 || numBits > 32)
			throw new IllegalArgumentException();
		
		int result = 0;
		for (int i = 0; i < numBits; i++)
			result |= in.readNoEof() << i;  // Little endian
		return result;
	}
	
}
