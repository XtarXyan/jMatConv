package com.xtarxyan;

import ar.com.hjg.pngj.chunks.ChunkCopyBehaviour;
import ar.com.hjg.pngj.chunks.PngChunkTextVar;
import ar.com.hjg.pngj.chunks.ChunksList;
import ar.com.hjg.pngj.ImageLineInt;
import ar.com.hjg.pngj.IImageLine;
import ar.com.hjg.pngj.ImageLineHelper;
import ar.com.hjg.pngj.PngjException;
import ar.com.hjg.pngj.PngReader;
import ar.com.hjg.pngj.PngWriter;

import java.io.*;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.HashMap;

public class Converter {

    public static HashMap<ChannelType, Integer> inputLocs = new HashMap<ChannelType, Integer>();
    public static ArrayList<ChannelType> channelOutputs = new ArrayList<ChannelType>();
    
    private static LinkedList<String> origFilenames;
    private static LinkedList<String> destFilenames;

    public static Integer outWidth, outHeight;

    public static ArrayList<PngReader> readers = new ArrayList<PngReader>();

    public static void convert(LinkedList<String> inputArgs, LinkedList<String> outputArgs, Integer[] outSize) {
        loadArgs(inputArgs, outputArgs, outSize);
        test();

        ArrayList<Thread> threadList = new ArrayList<Thread>();

        Iterator<String> itF = destFilenames.iterator();
        Iterator<ChannelType> itCT = channelOutputs.iterator();
        while (itCT.hasNext())
        {
            ChannelType c1 = itCT.next();
            ChannelType c2 = (itCT.hasNext() ? itCT.next() : ChannelType.skip);
            ChannelType c3 = (itCT.hasNext() ? itCT.next() : ChannelType.skip);

            threadList.add(new Thread(new WriterWorker(new File(itF.next()), c1, c2, c3)));
        }
        for (Thread thread : threadList)
        {
            thread.start();
        }
    }

    public static void convertBasic(String origFilename, String destFilename) throws PngjException {
        try {
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
                int[] scanline = ((ImageLineInt) l1).getScanline(); // to save typing
                for (int j = 0; j < pngr.imgInfo.cols; j++) {
                    scanline[j * channels] /= 2;
                    scanline[j * channels + 1] = ImageLineHelper.clampTo_0_255(scanline[j * channels + 1] + 20);
                }			pngw.writeRow(l1);
            }
            pngr.end(); // it's recommended to end the reader first, in case there are trailing chunks to read
            pngw.end();
        }
        catch (PngjException e) {
            System.out.println("Oops, something went wrong with the file IO! " + 
                    "I'm guessing you either put an image that is not a PNG into me such as a JPEG, or your file is corrupted. " + 
                    "Keep in mind I only work with PNG's, not any other formats.");
        }
        catch (Exception e) {
            System.out.println("Oops, something went wrong.\n");
        }
	}

    public static void test() throws PngjException {
        try {
            
            for (int i = 0; i < channelOutputs.size(); i++)
            {
                System.out.println(channelOutputs.get(i).toString() + " ");
            }
            return;
           }
        catch (PngjException e) {
            System.out.println("Oops, something went wrong with the file IO! " + 
                    "I'm guessing you either put an image that is not a PNG into me such as a JPEG, or your file is corrupted. " + 
                    "Keep in mind I only work with PNG's, not any other formats.");
        }
        catch (Exception e) {
            System.out.println("Oops, something went wrong.\n");
        }

    }

    public static void loadArgs(LinkedList<String> inputArgs, LinkedList<String> outputArgs, Integer[] outSize) {
        String channelInputString = inputArgs.pop();
        String channelOutputString = outputArgs.pop();

        origFilenames = inputArgs;
        destFilenames = outputArgs;

        outWidth = outSize[0];
        outHeight = outSize[1];

        int index = 0;
 
        for (char letter : channelInputString.toUpperCase().toCharArray())
        {
            ChannelType type = resolveChannel(letter);
            inputLocs.put(type, index++);
            if (type == ChannelType.base) {
                inputLocs.put(ChannelType.base2, index++);
                inputLocs.put(ChannelType.base3, index++);
            }
            if (type == ChannelType.normal) {
                inputLocs.put(ChannelType.normal2, index++);
                inputLocs.put(ChannelType.normal3, index++);
            }
        }

        if (index > origFilenames.size() * 3)
        {
            throw new RuntimeException("There are more specified input layers than the number of input files support!");
        }

        // Initialize readers
        Iterator<String> it = origFilenames.iterator();
        while (index > 0)
        {
            PngReader pngr = new PngReader(new File(it.next()));
            int channels = pngr.imgInfo.channels;
            if (channels < 3 || pngr.imgInfo.bitDepth != 8)
            {
                pngr.end();
                throw new RuntimeException("This method is for RGB8/RGBA8 images");
            }

            readers.add(pngr);
            index -= 3;
        }
        index = 0;

        for (char letter : channelOutputString.toUpperCase().toCharArray())
        {
            ChannelType type = resolveChannel(letter);

            if (!inputLocs.containsKey(type) && type != ChannelType.skip)
            {
                throw new RuntimeException("You are trying to convert to a layer type that is not specified in the input order, specifically a " + type.toString() + " layer. This is not supported.");
            }

            channelOutputs.add(type);
            index++;
            if (type == ChannelType.base) {
                channelOutputs.add(ChannelType.base2);
                channelOutputs.add(ChannelType.base3);
                index += 2;
            }
            if (type == ChannelType.normal) {
                channelOutputs.add(ChannelType.normal2);
                channelOutputs.add(ChannelType.normal3);
                index += 2;
            }

        }
 
        if (index > destFilenames.size() * 3)
        {
            throw new RuntimeException("There are more specified output layers than the number of output files support!");
        }

        
    }

    private static ChannelType resolveChannel(char c)
    {
        switch (c) {
            case 'X': {
                return ChannelType.skip;    
            }
            case 'B': {
                return ChannelType.base;    
            }
            case 'N': {
                return ChannelType.normal;    
            }
            case 'R': {
                return ChannelType.roughness;    
            }
            case 'M': {
                return ChannelType.metallic;    
            }
            case 'A': {
                return ChannelType.ambient;    
            }
            case 'O': {
                return ChannelType.opacity;    
            }
            case 'E': {
                return ChannelType.emission;    
            }
            default: {
                throw new RuntimeException("Invalid character!");
            }
        }

    }

    
}
    
