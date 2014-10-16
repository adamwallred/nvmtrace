package edu.gatech.util;

import java.util.*;
import java.io.*;

public class ExecCommand{
    
    public static Process execCommand(String[] command){
	Process proc = null;
	try{
	    proc = Runtime.getRuntime().exec(command);
	}catch(Exception e){
	    e.printStackTrace();
	    System.exit(-1);
	}
	
	return proc;
    }
        
    public static Process execAndWait(String[] command){
	Process proc = ExecCommand.execCommand(command);
	try{
	    proc.waitFor();
	    proc.getInputStream().close();
	    proc.getOutputStream().close();
	    proc.getErrorStream().close();
	}catch(Exception e){
	    e.printStackTrace();
	    System.exit(-1);
	}
	
	return proc;
    }
    
    private static String[] execAndRead(String[] command) throws Exception{
	Vector<String> lineVector = new Vector<String>();
	Process proc = ExecCommand.execCommand(command);
	BufferedReader br = 
	    new BufferedReader(new InputStreamReader(proc.getInputStream()));
	String line = br.readLine();
	while(line != null){
	    lineVector.add(line);
	    line = br.readLine();
	}
	
	proc.waitFor();
	
	proc.getInputStream().close();
	proc.getOutputStream().close();
	proc.getErrorStream().close();

	return (String[]) lineVector.toArray(new String[0]);
    }
    
    public static String[] execProvideOutput(String[] command){
	String[] output = null;
	try{
	    output = ExecCommand.execAndRead(command);
	}catch(Exception e){
	    e.printStackTrace();
	    System.exit(-1);
	}
	return output;
    }
    
    public static String[] cat(String path){
	String[] catCmd = {"cat", path};
	return ExecCommand.execProvideOutput(catCmd);
    }
    
    public static void mkdir(String path){
	String[] mkdirCmd = {"mkdir", path};
	ExecCommand.execAndWait(mkdirCmd);
    }
        
    public static void rmdir(String path){
	String[] rmdirCmd = {"rm", "-rf", path};
	ExecCommand.execAndWait(rmdirCmd);
    }
        
    public static void rm(String path){
	String[] rmCmd = {"rm", "-f", path};
	ExecCommand.execAndWait(rmCmd);
    }

    public static void cntrkrm(String src){
	String[] cntrkrmCmd = {"conntrack", "-D", "-s", src};
	ExecCommand.execProvideOutput(cntrkrmCmd);
    }
    
    public static void mv(String src, String dest){
	String[] mvCmd = {"mv", src, dest};
	ExecCommand.execAndWait(mvCmd);
    }

    public static void cp(String src, String dest){
	String[] cpCmd = {"cp", src, dest};
	ExecCommand.execAndWait(cpCmd);
    }
    
    public static void copydir(String src, String dest){
	String[] copydirCmd = {"cp", "-r", src, dest};
	ExecCommand.execAndWait(copydirCmd);
    }

    public static void dd(String ifile, String ofile, int cnt){
	String[] ddCmd = 
	    {"dd", "if=" + ifile, "of=" + ofile, "bs=1M", "count=" + cnt};
	ExecCommand.execAndWait(ddCmd);
    }
    
    public static void sleep(int seconds){
	if(seconds < 0)
	    return;
	
	try{
	    Thread.sleep(seconds * 1000);
	}catch(Exception e){
	    e.printStackTrace();
	    System.exit(-1);
	}
    }
    
    public static Process tcpdump(String iface, String file, String exp){
	String[] tcpdumpCmd = 
	    {"tcpdump", "-U", "-s", "0", "-i", iface, "-w", file, exp};
	return ExecCommand.execCommand(tcpdumpCmd);
    }
    
    public static void dmcreate(String name, String path){
	String[] dmcreateCmd = {"dmsetup", "create", name, path};
	ExecCommand.execAndWait(dmcreateCmd);
    }
    
    public static void dmremove(String name){
	String[] dmremoveCmd = {"dmsetup", "remove", name};
	ExecCommand.execAndWait(dmremoveCmd);
    }
    
    public static void kpartx(String opt, String device){
	String[] kpartxCmd = {"kpartx", opt, device};
	ExecCommand.execAndWait(kpartxCmd);
    }
    
    public static void mount(String device, String dir){
	String[] mountCmd = {"mount", device, dir};
	ExecCommand.execAndWait(mountCmd);
    }
    
    public static void umount(String dir){
	String[] umountCmd = {"umount", dir};
	ExecCommand.execAndWait(umountCmd);
    }
    
    public static String ipmipwrop(String host, String pswdpth, String op){
	String[] ipmitoolCmd =
	    {"ipmitool", "-I", "lanplus", "-U", "ADMIN",
	     "-H", host, "-f", pswdpth, "chassis", "power", op};
	
	while(true){
	    String[] response = ExecCommand.execProvideOutput(ipmitoolCmd);
	    if(response.length > 0){
		String normalized = response[0].toLowerCase();
		if(op.equals("status") || normalized.contains(op))
		    return normalized;
	    }
	    ExecCommand.sleep(1);
	}
    }

    public static void tcmblock(String hbadevpath, String device){
        String[] tcmblockcmd =
            {"tcm_node", "--iblock", hbadevpath, device};
        ExecCommand.execAndWait(tcmblockcmd);
    }

    public static void lioaddlun(String iqn, String lunName,
                                 String hbadevpath){
        String[] addluncmd =
            {"lio_node", "--addlun", iqn, "1", "0", lunName, hbadevpath};
        ExecCommand.execAndWait(addluncmd);
    }

    public static void lioaddnp(String iqn, String npIPPort){
        String[] addnpcmd = {"lio_node", "--addnp", iqn, "1", npIPPort};
        ExecCommand.execAndWait(addnpcmd);
    }

    public static void liodisauth(String iqn){
        String[] disauthcmd = {"lio_node", "--disableauth", iqn, "1"};
        ExecCommand.execAndWait(disauthcmd);
    }

    public static void lioaddnodeacl(String targetiqn, String initiatoriqn){
        String[] addnodeaclcmd =
            {"lio_node", "--addnodeacl", targetiqn, "1", initiatoriqn};
	ExecCommand.execAndWait(addnodeaclcmd);
    }

    public static void lioaddlunacl(String targetiqn, String initiatoriqn){
        String[] addlunaclcmd =
            {"lio_node", "--addlunacl", targetiqn, "1", initiatoriqn, "0",
              "0"};
        ExecCommand.execAndWait(addlunaclcmd);
    }

    public static void lioentpg(String iqn){
        String[] entpgcmd = {"lio_node", "--enabletpg", iqn, "1"};
        ExecCommand.execAndWait(entpgcmd);
    }

    public static void liodistpg(String iqn){
        String[] distpgcmd = {"lio_node", "--disabletpg", iqn, "1"};
	ExecCommand.execAndWait(distpgcmd);
    }

    public static void liodeliqn(String iqn){
        String[] deliqncmd = {"lio_node", "--deliqn", iqn};
        ExecCommand.execAndWait(deliqncmd);
    }

    public static void tcmdelhba(String hbadevpath){
	String[] hbadevpathtokens = hbadevpath.split("/");
        String[] delhbacmd = {"tcm_node", "--delhba", hbadevpathtokens[0]};
	ExecCommand.execAndWait(delhbacmd);
    }
}
