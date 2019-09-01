package com.gmail.raygerman.basics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Logger extends WorkQueue<String> {
	
	public Logger(File outputfile) throws IllegalArgumentException
	{
		if (outputfile.exists() == false || outputfile.canWrite())
		{
			try
			{
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
	protected void doWork(String item)
	{
		if (null != this.writer)
		{
			try
			{
				this.writer.write(item + "\n"); //$NON-NLS-1$
			} catch (@SuppressWarnings("unused") IOException e)
			{
				this.writer = null;
			}
		}
	}
	
	public void log(String text)
	{
		String timeStamp = DATE_FORMAT.format(Calendar.getInstance().getTime());
		this.Add(timeStamp + "     " + text); //$NON-NLS-1$
	}
	
	public void log(Exception e)
	{
		PrintWriter outerWriter = new PrintWriter(new StringWriter(512));
		e.printStackTrace(outerWriter);
		this.log(outerWriter.toString());
	}

	private BufferedWriter writer = null;
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); //$NON-NLS-1$


}
