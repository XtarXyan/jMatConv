package com.xtarxyan;

import java.io.*;
import ar.com.hjg.pngj.chunks.ChunksList;
import ar.com.hjg.pngj.IImageLine;
import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.ImageLineInt;
import ar.com.hjg.pngj.PngReader;
import ar.com.hjg.pngj.PngWriter;

public class WriterWorker implements Runnable {

    ChannelType channel1, channel2, channel3;
    File outputFile;

    WriterWorker(File outputFile, ChannelType channel1, ChannelType channel2, ChannelType channel3) {
        this.outputFile = outputFile;
        this.channel1 = channel1;
        this.channel2 = channel2;
        this.channel3 = channel3;
    }

    public void run() {
        ImageInfo outputInfo = new ImageInfo(Converter.outWidth, Converter.outHeight, 8, false);
        PngWriter writer = new PngWriter(outputFile, outputInfo, true);

        PngReader r1 = Converter.readers.get(Converter.inputLocs.get(channel1) / 3);
        PngReader r2 = Converter.readers.get(Converter.inputLocs.get(channel2) / 3);
        PngReader r3 = Converter.readers.get(Converter.inputLocs.get(channel3) / 3);

        for (int i = 0; i < outputInfo.rows; i++) {

            ImageLineInt l1 = (ImageLineInt) r1.readRow(i * r1.imgInfo.rows / outputInfo.rows);
            int[] sl1 = l1.getScanline();
            ImageLineInt l2 = (ImageLineInt) r2.readRow(i * r2.imgInfo.rows / outputInfo.rows);
            int[] sl2 = l2.getScanline();
            ImageLineInt l3 = (ImageLineInt) r3.readRow(i * r3.imgInfo.rows / outputInfo.rows);
            int[] sl3 = l3.getScanline();

            ImageLineInt writerLine = new ImageLineInt(outputInfo);
            int[] writerScanline = writerLine.getScanline();

            for (int j = 0; j < outputInfo.cols; j++) {
                writerScanline[j * 3] = ( channel1 != ChannelType.skip ? sl1[(j * 3) * r1.imgInfo.cols / outputInfo.cols] : 0 );
                writerScanline[j * 3 + 1] = ( channel2 != ChannelType.skip ? sl2[(j * 3 + 1) * r2.imgInfo.cols / outputInfo.cols] : 0 );
                writerScanline[j * 3 + 2] = ( channel3 != ChannelType.skip ? sl3[(j * 3 + 2) * r3.imgInfo.cols / outputInfo.cols] : 0 );
            }
            writer.writeRow(writerLine);
        }

        r1.end();
        r2.end();
        r3.end();
        writer.end();
    }

}

