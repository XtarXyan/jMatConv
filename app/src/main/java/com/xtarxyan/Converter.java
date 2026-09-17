package com.xtarxyan;

import ar.com.hjg.pngj.PngjException;
import ar.com.hjg.pngj.PngReader;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.HashMap;

public class Converter {

    public static HashMap<ChannelType, Integer> inputLocs = new HashMap<ChannelType, Integer>();
    public static ArrayList<ChannelType> channelOutputs = new ArrayList<ChannelType>();
    
    public static ArrayList<String> origFilenames;
    public static ArrayList<String> destFilenames;

    public static Integer outWidth, outHeight;

    public static void convert(LinkedList<String> inputArgs, LinkedList<String> outputArgs, Integer[] outSize, int num_threads) {
        loadArgs(inputArgs, outputArgs, outSize);

        ArrayList<Thread> threadList = new ArrayList<Thread>();

        Iterator<String> itF = destFilenames.iterator();
        Iterator<ChannelType> itCT = channelOutputs.iterator();

        try {
            while (itCT.hasNext())
            {
                ChannelType c1 = itCT.next();
                ChannelType c2 = (itCT.hasNext() ? itCT.next() : ChannelType.skip);
                ChannelType c3 = (itCT.hasNext() ? itCT.next() : ChannelType.skip);

                threadList.add(new Thread(new IOWorker(new File(itF.next()), c1, c2, c3, num_threads)));
            }

            System.out.println("Starting " + threadList.size() + " jobs with " + num_threads + " threads per job.");

            for (Thread thread : threadList)
            {
                thread.start();
            }
            for (Thread thread : threadList)
            {
                thread.join();
            }

            System.out.println("Done.");
        }
        catch (InterruptedException e)
        {
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

        origFilenames = new ArrayList<String>(inputArgs);
        destFilenames = new ArrayList<String>(outputArgs);

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

        inputLocs.put(ChannelType.skip, 0);
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
    
