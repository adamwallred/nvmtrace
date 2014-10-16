package edu.gatech.nvmtrace;

import java.io.*;

import edu.gatech.util.*;

public class NVMController{

    public static void usage(){
	System.out.println("Usage: nvmtrace.jar wspacefile");
        System.exit(-1);
    }
    
    public static void main(String[] args){
        if(args.length == 0 || !new File(args[0]).exists())
            NVMController.usage();
	
	String[] workspacePaths = ExecCommand.cat(args[0]);
        NVMCThread[] nvmCThreads = new NVMCThread[workspacePaths.length];
        for(int i = 0; i < workspacePaths.length; i++)
            nvmCThreads[i] = new NVMCThread(workspacePaths[i]);

	NVMSThread nvmSThread = new NVMSThread();
        for(int i = 0; i < nvmCThreads.length; i++)
            nvmSThread.addNVMCThread(nvmCThreads[i]);
	Runtime.getRuntime().addShutdownHook(nvmSThread);
        
        for(int i = 0; i < nvmCThreads.length; i++)
            nvmCThreads[i].start();
    }
}
