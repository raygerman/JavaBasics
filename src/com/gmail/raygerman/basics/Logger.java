package com.gmail.raygerman.basics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.LinkedBlockingQueue;

public class Logger implements Runnable {
	
	@SuppressWarnings("unused")
	public Logger(File outputfile) throws IllegalArgumentException
	{
		if (outputfile.exists() == false || outputfile.canWrite())
		{
			try
			{
				this.queue = new LinkedBlockingQueue<String>();
				this.writer = new BufferedWriter(new FileWriter(outputfile.getPath()));
				
			} catch (IOException e)
			{
				throw (new IllegalArgumentException("Cannot create or overwrite " + outputfile.getPath() + e.getMessage())); //$NON-NLS-1$
			}
		}
		else
		{
			throw (new IllegalArgumentException("Cannot create or overwrite " + outputfile.getPath())); //$NON-NLS-1$
		}
	}
	
	@Override
	public void run()
	{
		if (null != this.writer)
		{
			try
			{
				while (true)
				{
					String item = this.queue.take();
					if (null != item)
					{
					  this.writer.write(item + "\n"); //$NON-NLS-1$
					  this.writer.flush();
					}
				}
			} catch (@SuppressWarnings("unused") IOException e)
			{
				this.writer = null;
			} catch (@SuppressWarnings("unused") InterruptedException e)
			{
				this.writer = null;
			}
		}
	}
	
	public void log(String text)
	{
		String timeStamp = DATE_FORMAT.format(Calendar.getInstance().getTime());
		try
		{
			this.queue.add(timeStamp + "     " + text); //$NON-NLS-1$
		} catch (@SuppressWarnings("unused") IllegalStateException e)
		{
			this.writer = null;
		}
	}
	
	public void log(Exception e)
	{
		PrintWriter outerWriter = new PrintWriter(new StringWriter(512));
		e.printStackTrace(outerWriter);
		this.log(outerWriter.toString());
	}
	
	public static Logger getLog()
	{
		return(staticLog);
	}
	
	public static void setLog(Logger log)
	{
		if (null == staticLog)
		{
			staticLog = log;
		}
	}
	
	public static void staticLog(String text)
	{
		if (null != staticLog)
		{
			staticLog.log(text);
		}
	}
	
	public static void staticLog(Exception e)
	{
		if (null != staticLog)
		{
			staticLog.log(e);
		}
	}

	private BufferedWriter writer = null;
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); //$NON-NLS-1$
	private static Logger staticLog = null;
	private LinkedBlockingQueue<String> queue = null;

}
