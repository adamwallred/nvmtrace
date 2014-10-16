package edu.gatech.nvmtrace;

import java.sql.*;

public class NVMThreadDB{
    
    private static final String dbHost = "<host>";
    private static final String dbUser = "nvmtrace";
    private static final String dbName = "nvmtrace";
    private static final String dbPass = "<password>";
    
    private Connection connection = null;

    static{
	try{
            Class.forName("org.postgresql.Driver").newInstance();
        }catch(Exception e){
            e.printStackTrace();
	}
    }
    
    private Connection currentConnection() throws SQLException{
	if(this.connection == null){
            this.connection = DriverManager.
                getConnection("jdbc:postgresql://" + NVMThreadDB.dbHost +
                              ":5432/" + NVMThreadDB.dbName + "?" +
                              "user=" + NVMThreadDB.dbUser + 
                              "&password=" + NVMThreadDB.dbPass);
            return this.connection;
            
        }
        else{
            try{
                Statement stmt = connection.createStatement();
                stmt.executeQuery("SELECT TRUE");
                stmt.close();
            }catch(Exception e){
                this.connection = DriverManager.
                    getConnection("jdbc:postgresql://" + NVMThreadDB.dbHost +
                                  ":5432/" + NVMThreadDB.dbName + "?" +
                                  "user=" + NVMThreadDB.dbUser + 
                                  "&password=" + NVMThreadDB.dbPass);
            }
            
            return this.connection;
        }
    }
    
    public String getUnprocessedSampleMD5(){
	String md5 = null;
	try{
	    Statement stmt = this.currentConnection().createStatement();
	    ResultSet rset = 
		stmt.executeQuery("SELECT md5 FROM sample WHERE " +
				  "process_date IS NULL " +
				  "ORDER BY submit_time ASC " +
				  "LIMIT 64 FOR UPDATE");
	    if(rset.next()){
		md5 = rset.getString("md5");
	    }
	    
	    stmt.close();
	}catch(Exception e){
	    try{
		this.currentConnection().rollback();
	    }catch(Exception e2){
		e2.printStackTrace();
		System.exit(-1);
	    }
	}
	
	return md5;
    }
    
    public String getNextSampleMD5(){
	String nextSampleMD5 = null;
	try{
	    this.currentConnection().setAutoCommit(false);
	    nextSampleMD5 = this.getUnprocessedSampleMD5();
	    if(nextSampleMD5 != null)
		this.setProcessDate(nextSampleMD5, 
				    new Date(System.currentTimeMillis()));
	    this.currentConnection().commit();
	    this.currentConnection().setAutoCommit(true);
	}catch(Exception e){
	    e.printStackTrace();
	    System.exit(-1);
	}
	
	return nextSampleMD5;
    }
    
    public String getProcessDate(String md5){
	String processDate = null;
	try{
	    Statement stmt = this.currentConnection().createStatement();
	    ResultSet rset = 
		stmt.executeQuery("SELECT process_date FROM sample " +
				  "WHERE md5=" + "'" + md5 + "'");
	    if(rset.next()){
		processDate = 
		    rset.getString("process_date").replaceAll("-", "");
	    }
	    
	    stmt.close();
	}catch(Exception e){
	    e.printStackTrace();
	    System.exit(-1);
	}
	
	return processDate;
    }
        
    public void setProcessDate(String md5, Date processDate){
	try{
            Statement stmt = this.currentConnection().createStatement();
            stmt.executeUpdate("UPDATE sample SET " +
			       "process_date=" + "'" + processDate + "' " +
			       "WHERE md5=" + "'" + md5 + "'");
	    stmt.close();
        }catch(Exception e){
            e.printStackTrace();
            System.exit(-1);
        }
    }
} 
