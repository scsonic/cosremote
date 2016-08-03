package com.ygk.cosremote;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface BTSerialInterface {
	
	public void write( byte b) throws IOException ;
	public void write( byte b[] ) throws IOException;
	public void flush() throws IOException;
	public boolean isConnect() ;
	
	public int read(byte []b, int len) throws IOException;
	public int read(byte [] b) throws IOException;
	
	public InputStream getInputStream() ;
	public OutputStream getOutputStream() ;
}
