package edu.gatech.nvmtrace;

public class NVMConfig{
    
    private NVMThreadDB dbase;

    public NVMConfig(NVMThreadDB dbase){
	this.dbase = dbase;
    }
    
    public String getStorageInterface(){
	return "nvm-storage";
    }

    public String getNetAddrTransInterface(){
	return "nvm-nat";
    }
    
    public String getEtcPath(){
	return "/opt/gtisc/etc/";
    }
    
    public String getIPMIPasswdPath(){
	return this.getEtcPath() + "nvm.ipmi.passwd";
    }
    
    public String getiSCSIIPPortPath(){
        return this.getEtcPath() + "nvm.iscsi.ipport";
    }
    
    public String getInititatorIQNPath(){
        return this.getEtcPath() + "nvm.iscsi.initiatoriqn";
    }
        
    public String getInputPath(){
        return "/opt/gtisc/nvmtrace/input/";
    }
    
    private String getOutputPrefix(){
	return "/opt/gtisc/nvmtrace/output/";
    }
    
    public String getOutputParentPath(String md5){
	return this.getOutputPrefix() + 
	    this.dbase.getProcessDate(md5) + "/";
    }
    
    public String getLocalOutputPrefix(){
	return "/opt/gtisc/tmp/";
    }

    public String getLocalOutputPath(String md5){
	return this.getLocalOutputPrefix() + md5 + "/";
    }
    
    public String getLocalPcapPath(String md5){
	return this.getLocalOutputPath(md5) + md5 + ".pcap";
    }

    public String getTCPDumpExpression(String macAddr){
	return "ether host " + macAddr;
    }
}
