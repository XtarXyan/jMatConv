package com.xtarxyan;

import java.io.*;
import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.ImageLineInt;
import ar.com.hjg.pngj.PngWriter;

public class IOWorker implements Runnable {

    ChannelType channel1, channel2, channel3;
    File outputFile;
    int numThreads;

    ImageInfo outputInfo;

    PngWriter writer;
    ImageLineInt[] writerLines; 
    boolean[] jobsDone;

    int lineIdx = 0;

    IOWorker(File outputFile, ChannelType channel1, ChannelType channel2, ChannelType channel3, int num_threads) throws InterruptedException {
        this.outputFile = outputFile;
        this.channel1 = channel1;
        this.channel2 = channel2;
        this.channel3 = channel3;
        this.numThreads = num_threads;

        outputInfo = new ImageInfo(Converter.outWidth, Converter.outHeight, 8, false);
    }

    public void run() {
        writer = new PngWriter(outputFile, outputInfo, true);

        writerLines = new ImageLineInt[outputInfo.rows];
        jobsDone = new boolean[outputInfo.rows];

        for (int i = 0; i < numThreads; i++)
        {
            Thread thread = new Thread(new LineWorker(new ChannelType[] {channel1, channel2, channel3}, this, i, numThreads));
            thread.start();
        }

        int i = 0;

        try {
            while (i < outputInfo.rows)
            {
                if (jobsDone[i] != true) {
                    Thread.sleep(1);
                    continue;
                }
                writer.writeRow(writerLines[i]);
                i++;
            }

            System.out.println("Successfuly wrote output file " + outputFile.getCanonicalPath());
        }
        catch (InterruptedException e) {
            System.out.println("InterruptedException in thread " + Thread.currentThread().getName());
        }
        catch (IOException e) {
            System.out.println("IOException in file " + outputFile.getAbsolutePath());
        }

        
        writer.end();
   }

    public void pushLine(int index, ImageLineInt line) {
        writerLines[index] = line;
        jobsDone[index] = true;
    }

}

