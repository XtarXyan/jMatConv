package com.xtarxyan;

import ar.com.hjg.pngj.chunks.ChunkCopyBehaviour;
import ar.com.hjg.pngj.chunks.PngChunkTextVar;
import ar.com.hjg.pngj.IImageLine;
import ar.com.hjg.pngj.ImageLineHelper;
import ar.com.hjg.pngj.PngReader;
import ar.com.hjg.pngj.PngWriter;

import java.io.*;

import picocli.CommandLine;
import picocli.CommandLine.*;

@Command(name = "JMatConv", version = "1.0", mixinStandardHelpOptions = true)
public class JMatConv implements Runnable{

	public static void convert(String origFilename, String destFilename) {
		PngReader pngr = new PngReader(new File(origFilename));
		System.out.println(pngr.toString());
		int channels = pngr.imgInfo.channels;
		if (channels < 3 || pngr.imgInfo.bitDepth != 8)
        {
            pngr.end();
			throw new RuntimeException("This method is for RGB8/RGBA8 images");
        }
		PngWriter pngw = new PngWriter(new File(destFilename), pngr.imgInfo, true);
		pngw.copyChunksFrom(pngr.getChunksList(), ChunkCopyBehaviour.COPY_ALL_SAFE);
		while(pngr.hasMoreRows())  
        {
			IImageLine l1 = pngr.readRow();
            /*
			int[] scanline = ((ImageLineInt) l1).getScanline(); // to save typing
			for (int j = 0; j < pngr.imgInfo.cols; j++) {
				scanline[j * channels] /= 2;
				scanline[j * channels + 1] = ImageLineHelper.clampTo_0_255(scanline[j * channels + 1] + 20);
			}*/
			pngw.writeRow(l1);
		}
		pngr.end(); // it's recommended to end the reader first, in case there are trailing chunks to read
		pngw.end();
	}

    @Option(names = "-in")
    String inputFilename;

    @Option(names = "-out")
    String outputFilename;

    public void run() {
        convert(inputFilename, outputFilename);
    }
    
    public static void main(String[] args) {
        int exitCode = new CommandLine(new JMatConv()).execute(args);
        System.exit(exitCode);
    }
}
