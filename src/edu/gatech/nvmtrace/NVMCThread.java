package edu.gatech.nvmtrace;

import java.io.*;
import java.text.*;
import java.util.*;

import edu.gatech.util.*;

public class NVMCThread extends Thread{
    
    public static final int nvmRunTime = 180;
    public static final int pollInterval = 16;
    public static final int resetInterval = 5;
    
    private boolean stop = false;
    private String basePath;
    
    private BufferedWriter log;
    private NVMThreadDB dbase;
    private NVMConfig config;
        
    public NVMCThread(String basePath){
	if(!basePath.endsWith("/"))
            this.basePath = basePath + "/";
        else
            this.basePath = basePath;
	
	try{
	    final String logPath =  this.basePath + "log.txt";
	    this.log = new BufferedWriter(new FileWriter(logPath, true));
	}catch(Exception e){
	    e.printStackTrace();
	    System.exit(-1);
	}
	
	this.dbase = new NVMThreadDB();
	this.config = new NVMConfig(this.dbase);
    }
    
    public String getMacAddr(){
	return ExecCommand.cat(this.basePath + "cfg/mac")[0];
    }

    public String getNetworkAddr(){
	return ExecCommand.cat(this.basePath + "cfg/ip")[0];
    }

    public String getIPMINetworkAddr(){
	return ExecCommand.cat(this.basePath + "cfg/ipmi")[0];
    }
    
    public String getDMTablePath(){
	return this.basePath + "cfg/dmtable";
    }
    
    public String getNVMDiskPath(){
	return this.basePath + "disk/";
    }
    
    public String getWorkspaceName(){
	return ExecCommand.cat(this.basePath + "cfg/name")[0];
    }
    
    public String getCoWDevice(){
	return "/dev/mapper/" + this.getWorkspaceName();
    }

    public String getTargetIQN(){
        return ExecCommand.cat(this.basePath + "cfg/iqn")[0];
    }

    public String getInititatorIQN(){
        return ExecCommand.cat(this.config.getInititatorIQNPath())[0];
    }

    public String getHBADevPath(){
        return ExecCommand.cat(this.basePath + "cfg/hbadevpath")[0];
    }

    public String getiSCSIIPPort(){
        return ExecCommand.cat(this.config.getiSCSIIPPortPath())[0];
    }
    
    private void logWrite(String message){
	final String logTimeFormat = "yyyyMMdd HH:mm:ss";
	
        String logTime = 
	    new SimpleDateFormat(logTimeFormat).format(new Date());
        try{
            this.log.write(logTime + " | " + message + "\n");
            this.log.flush();
        }catch(Exception e){
            e.printStackTrace();
            System.exit(-1);
        }
    }
    
    public void resetNVMPower(){
	String status =
	    ExecCommand.ipmipwrop(this.getIPMINetworkAddr(),
				  this.config.getIPMIPasswdPath(), "status");
	
	if(status.contains("off"))
	    ExecCommand.ipmipwrop(this.getIPMINetworkAddr(),
				  this.config.getIPMIPasswdPath(), "on");
	else
	    ExecCommand.ipmipwrop(this.getIPMINetworkAddr(),
				  this.config.getIPMIPasswdPath(), "reset");
	
	ExecCommand.sleep(NVMCThread.resetInterval);
    }
    
    public void prepareNVMDisk(String md5){
	ExecCommand.dmcreate(this.getWorkspaceName(),
			     this.getDMTablePath());
	
	ExecCommand.kpartx("-a", this.getCoWDevice());
	ExecCommand.mount(this.getCoWDevice() + "1", this.getNVMDiskPath());
	
	ExecCommand.cp(this.config.getInputPath() + md5,
		       this.getNVMDiskPath() + "artifact.exe");
    
	ExecCommand.umount(this.getNVMDiskPath());
	ExecCommand.kpartx("-d", this.getCoWDevice());
    }
    
    public Vector<Process> startNVMSession(String md5){
	Vector<Process> nvmSession = new Vector<Process>();
	
	this.resetNVMPower();
	this.prepareNVMDisk(md5);
	
	ExecCommand.tcmblock(this.getHBADevPath(), this.getCoWDevice());
        ExecCommand.lioaddlun(this.getTargetIQN(), this.getWorkspaceName(), 
                              this.getHBADevPath());
        ExecCommand.lioaddnp(this.getTargetIQN(), this.getiSCSIIPPort());
        ExecCommand.liodisauth(this.getTargetIQN());
        ExecCommand.lioaddnodeacl(this.getTargetIQN(), this.getInititatorIQN());
        ExecCommand.lioaddlunacl(this.getTargetIQN(), this.getInititatorIQN());
        ExecCommand.lioentpg(this.getTargetIQN());
	
	Process tcpdump = 
	    ExecCommand.tcpdump(this.config.getNetAddrTransInterface(),
				this.config.getLocalPcapPath(md5),
				this.config.
				  getTCPDumpExpression(this.getMacAddr()));
	nvmSession.add(tcpdump);
	
	return nvmSession;
    }
    
    public void stopNVMSession(String md5, Vector<Process> nvmSession){
	for(int i = 0; i < nvmSession.size(); i++){
	    nvmSession.elementAt(i).destroy();
	    try{
		nvmSession.elementAt(i).waitFor();
	    }catch(Exception e){}
	}
	ExecCommand.cntrkrm(this.getNetworkAddr());
        ExecCommand.liodistpg(this.getTargetIQN());
        ExecCommand.liodeliqn(this.getTargetIQN());
        ExecCommand.tcmdelhba(this.getHBADevPath());
    }
        
    public void analyzeSessionArtifacts(String md5){
        ExecCommand.kpartx("-a", this.getCoWDevice());
        ExecCommand.mount(this.getCoWDevice() + "1", this.getNVMDiskPath());
        
        String pcapPath = this.config.getLocalPcapPath(md5);
        String nvmDiskPath = this.getNVMDiskPath();
        
        /*
         * Here, use your favorite network traffic and disk
         * forensics libraries/tools to identify events and
         * extract intelligence. pcapPath points to the file
         * containing network traffic generated by the sample.
         * nvmDiskPath points to the mounted root of the NVM's
         * hard disk drive for sample's processing session.
         */
        
        ExecCommand.umount(this.getCoWDevice() + "1");
        ExecCommand.kpartx("-d", this.getCoWDevice());
    }

    public void clearSessionArtifacts(String md5){
	ExecCommand.dmremove(this.getWorkspaceName());
	ExecCommand.rmdir(this.config.getLocalOutputPath(md5));
    }

    public void requestStop(){
        this.stop = true;
    }
    
    public void run(){
	while(true){
	    if(this.stop){
		ExecCommand.ipmipwrop(this.getIPMINetworkAddr(),
				      this.config.getIPMIPasswdPath(), "off");
		return;
	    }
	    
	    String md5 = this.dbase.getNextSampleMD5();
	    if(md5 == null){
		ExecCommand.sleep(NVMCThread.pollInterval);
		continue;
	    }
	    
	    ExecCommand.mkdir(this.config.getLocalOutputPath(md5));
	    
	    this.logWrite("starting nvm for " + md5); 
	    
	    Vector<Process> vmSession = this.startNVMSession(md5);
	    ExecCommand.rm(this.config.getInputPath() + md5);
	    
	    ExecCommand.sleep(NVMCThread.nvmRunTime);
	    this.stopNVMSession(md5, vmSession);
	    	    
	    this.logWrite("stopped nvm for " + md5);
	    
	    this.analyzeSessionArtifacts(md5);
	    this.clearSessionArtifacts(md5);
	}
    }
}
