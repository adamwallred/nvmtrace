package edu.gatech.nvmtrace;

import java.util.*;

public class NVMSThread extends Thread{
    
    private Vector<NVMCThread> nvmCThreads;

    public NVMSThread(){
	this.nvmCThreads = new Vector<NVMCThread>();
    }
    
    public void addNVMCThread(NVMCThread nvmCThread){
	this.nvmCThreads.add(nvmCThread);
    }

    public void run(){
	for(int i = 0; i < this.nvmCThreads.size(); i++)
	    this.nvmCThreads.elementAt(i).requestStop();
	
	try{
	    for(int i = 0; i < this.nvmCThreads.size(); i++)
		this.nvmCThreads.elementAt(i).join();
	}catch(Exception e){
	    e.printStackTrace();
	}
    }
}
