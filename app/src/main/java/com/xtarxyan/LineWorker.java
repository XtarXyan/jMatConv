package com.xtarxyan;

import java.io.*;
import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.ImageLineInt;
import ar.com.hjg.pngj.PngReader;
import ar.com.hjg.pngj.PngWriter;

public class LineWorker implements Runnable {

    ChannelType[] channels;
    IOWorker parentWorker;
    PngWriter writer;
    ImageInfo outputInfo;

    int threadOffset;
    int numThreads;
    int numRows;

    LineWorker(ChannelType[] channels, IOWorker parentWorker, int threadOffset, int numThreads)
    {
        this.channels = channels;
        this.writer = parentWorker.writer;
        this.parentWorker = parentWorker;
        this.threadOffset = threadOffset;
        this.numThreads = numThreads;
        this.outputInfo = parentWorker.outputInfo;
        this.numRows = parentWorker.outputInfo.rows;
    }

    public void run() {

        int loc1 = Converter.inputLocs.get(channels[0]);
        int loc2 = Converter.inputLocs.get(channels[1]);
        int loc3 = Converter.inputLocs.get(channels[2]);

        PngReader r1 = new PngReader(new File(Converter.origFilenames.get(loc1 / 3)));
        PngReader r2 = new PngReader(new File(Converter.origFilenames.get(loc2 / 3)));
        PngReader r3 = new PngReader(new File(Converter.origFilenames.get(loc3 / 3)));

        int numChannels1 = r1.imgInfo.channels;
        int numChannels2 = r2.imgInfo.channels;
        int numChannels3 = r3.imgInfo.channels;

        for (PngReader reader : new PngReader[] {r1, r2, r3})
        { 
            if (reader.imgInfo.channels < 3 || reader.imgInfo.bitDepth != 8)
            {
                reader.end();
                throw new RuntimeException("Error: You are trying to convert a non-RGB8/-RGBA8 image. Please ensure you only input RGB8/RGBA8 images");
            }
        }

        ImageLineInt[] lines = new ImageLineInt[3];
        Integer index = threadOffset;

        while(index < numRows)
        {
            lines = getLines(r1, r2, r3, index);
            int[] sl1 = lines[0].getScanline();
            int[] sl2= lines[1].getScanline();
            int[] sl3 = lines[2].getScanline();

            ImageLineInt writerLine = new ImageLineInt(outputInfo);
            int[] writerScanline = writerLine.getScanline();

            for (int j = 0; j < outputInfo.cols; j++) {
                writerScanline[j * 3] = ( channels[0] != ChannelType.skip ? sl1[(j * numChannels1 * r1.imgInfo.cols / outputInfo.cols) + loc1 % 3] : 0 );
                writerScanline[j * 3 + 1] = ( channels[1] != ChannelType.skip ? sl2[(j * numChannels2 * r2.imgInfo.cols / outputInfo.cols) + loc2 % 3] : 0 );
                writerScanline[j * 3 + 2] = ( channels[2] != ChannelType.skip ? sl3[(j * numChannels3 * r3.imgInfo.cols / outputInfo.cols) + loc3 % 3] : 0 );
            }

            parentWorker.pushLine(index, writerLine);

            index += numThreads;
        }

        r1.end();
        r2.end();
        r3.end();
    }

    private ImageLineInt[] getLines(PngReader reader1, PngReader reader2, PngReader reader3, int lineNr) {
       ImageLineInt[] lines = { 
            (ImageLineInt) reader1.readRow(lineNr * reader1.imgInfo.rows / parentWorker.outputInfo.rows),
            (ImageLineInt) reader2.readRow(lineNr * reader2.imgInfo.rows / parentWorker.outputInfo.rows),
            (ImageLineInt) reader3.readRow(lineNr * reader3.imgInfo.rows / parentWorker.outputInfo.rows)
        };

        return lines;
   }

}
