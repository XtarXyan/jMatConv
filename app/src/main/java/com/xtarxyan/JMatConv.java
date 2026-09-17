package com.xtarxyan;

import java.io.*;
import java.util.LinkedList;

import picocli.CommandLine;
import picocli.CommandLine.*;

@Command(name = "JMatConv", version = "1.0", mixinStandardHelpOptions = true)
public class JMatConv implements Runnable{

    @Option(names = "--in", required = true, arity = "2..*", description = "Input configuration (eg. BARMN) followed by list of input file names. The following configuration letters are accepted: \n B - Base map (3 channels) \n N - Normal map (3 channels) \n R - Roughness map (1 channel) \n M - Metallic map (1 channel) \n A - AO map (1 channel) \n O - Opacity map (1 channel) \n E - Emission map (1 channel) \n X- Skip (1 channel, will skip to the next channel. Useful for configurations where you have empty channels that should be discarded)")
    static LinkedList<String> inputArgs;

    @Option(names = "--out", required = true, arity = "2..*", description = "Output configuration (eg. BNRMA) followed by list of output file names. The following configuration letters are accepted: \n B - Base map (3 channels) \n N - Normal map (3 channels) \n R - Roughness map (1 channel) \n M - Metallic map (1 channel) \n A - AO map (1 channel) \n O - Opacity map (1 channel) \n E - Emission map (1 channel) \n X- Skip (1 channel, will skip to the next channel. Useful for configurations where you have empty channels that should be discarded)")
    static LinkedList<String> outputArgs;

    @Option(names = "--out-size", required = false, arity = "2", split = ":", description = "Width and height of the output image(s). All input channels will be scaled to conform to this resolution.")
    static Integer[] outSize = {256, 256};

    @Option(names = "--thread-count", description = "Number of worker threads for writing an output file.")
    static int NUM_THREADS = 8;

    public void run() {
        Converter.convert(inputArgs, outputArgs, outSize, NUM_THREADS);
    }
    
    public static void main(String[] args) {
        int exitCode = new CommandLine(new JMatConv()).execute(args);
        System.exit(exitCode);
    }
}

