package net.lax1dude.eaglercraft.v1_8.buildtools.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Copyright (c) 2022 LAX1DUDE. All Rights Reserved.
 * 
 * WITH THE EXCEPTION OF PATCH FILES, MINIFIED JAVASCRIPT, AND ALL FILES
 * NORMALLY FOUND IN AN UNMODIFIED MINECRAFT RESOURCE PACK, YOU ARE NOT ALLOWED
 * TO SHARE, DISTRIBUTE, OR REPURPOSE ANY FILE USED BY OR PRODUCED BY THE
 * SOFTWARE IN THIS REPOSITORY WITHOUT PRIOR PERMISSION FROM THE PROJECT AUTHOR.
 * 
 * NOT FOR COMMERCIAL OR MALICIOUS USE
 * 
 * (please read the 'LICENSE' file this repo's root directory for more info)
 * 
 */
public class JARSubprocess {
	
	public static final char classPathSeperator;
	
	static {
		classPathSeperator = System.getProperty("os.name").toLowerCase().contains("windows") ? ';' : ':';
	}

	public static int runJava(File directory, String[] javaExeArguments, String logPrefix) throws IOException {
		if(logPrefix.length() > 0 && !logPrefix.endsWith(" ")) {
			logPrefix = logPrefix + " ";
		}
		String javaHome = System.getProperty("java.home");
		if(classPathSeperator == ';') {
			File javaExe = new File(javaHome, "bin/java.exe");
			if(!javaExe.isFile()) {
				javaExe = new File(javaHome, "java.exe");
				if(!javaExe.isFile()) {
					throw new IOException("Could not find /bin/java.exe equivelant on java.home! (java.home=" + javaHome + ")");
				}
			}
			javaHome = javaExe.getAbsolutePath();
		}else {
			File javaExe = new File(javaHome, "bin/java");
			if(!javaExe.isFile()) {
				javaExe = new File(javaHome, "java");
				if(!javaExe.isFile()) {
					throw new IOException("Could not find /bin/java equivelant on java.home! (java.home=" + javaHome + ")");
				}
			}
			javaHome = javaExe.getAbsolutePath();
		}
		
		String[] fullArgs = new String[javaExeArguments.length + 1];
		fullArgs[0] = javaHome;
		System.arraycopy(javaExeArguments, 0, fullArgs, 1, javaExeArguments.length);
		
		ProcessBuilder exec = new ProcessBuilder(fullArgs);
		exec.directory(directory);
		
		Process ps = exec.start();
		InputStream is = ps.getInputStream();
		InputStream ise = ps.getErrorStream();
		BufferedReader isb = new BufferedReader(new InputStreamReader(is));
		BufferedReader iseb = new BufferedReader(new InputStreamReader(ise));
		
		String isbl = "";
		String isebl = "";
		int maxReadPerLoop = 128;
		int c = 0;
		do {
			boolean tick = false;
			c = 0;
			while(isb.ready() && (!iseb.ready() || ++c < maxReadPerLoop)) {
				char cc = (char)isb.read();
				if(cc != '\r') {
					if(cc == '\n') {
						System.out.println(logPrefix + isbl);
						isbl = "";
					}else {
						isbl += cc;
					}
				}
				tick = true;
			}
			c = 0;
			while(iseb.ready() && (!isb.ready() || ++c < maxReadPerLoop)) {
				char cc = (char)iseb.read();
				if(cc != '\r') {
					if(cc == '\n') {
						System.err.println(logPrefix + isebl);
						isebl = "";
					}else {
						isebl += cc;
					}
				}
				tick = true;
			}
			if(!tick) {
				try {
					Thread.sleep(10l);
				} catch (InterruptedException e) {
				}
			}
		} while(ps.isAlive());
		
		while(true) {
			try {
				return ps.waitFor();
			} catch (InterruptedException e) {
			}
		}
	}
	
}
