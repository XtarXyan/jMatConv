package com.xtarxyan;

import java.io.*;
import java.util.LinkedList;

import picocli.CommandLine;
import picocli.CommandLine.*;

@Command(name = "JMatConv", version = "1.0", mixinStandardHelpOptions = true)
public class JMatConv implements Runnable{

    @Option(names = "--in", required = true, arity = "2..*")
    static LinkedList<String> inputArgs;

    @Option(names = "--out", required = true, arity = "2..*")
    static LinkedList<String> outputArgs;

    @Option(names = "--out-size", arity = "2", split = ":")
    static Integer[] outSize;

    public void run() {
        Converter.convert(inputArgs, outputArgs, outSize);
    }
    
    public static void main(String[] args) {
        int exitCode = new CommandLine(new JMatConv()).execute(args);
        System.exit(exitCode);
    }
}

