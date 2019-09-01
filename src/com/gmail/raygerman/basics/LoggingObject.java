package com.gmail.raygerman.basics;

public class LoggingObject
{
	
	public LoggingObject()
	{
		this.log = null;
	}
	
	public LoggingObject(Logger newLog)
	{
		this.log = newLog;
	}
	
	protected void log(String text)
	{
		if (null != this.log)
		{
			this.log.log(text);
		}
	}
	
	protected void log(Exception e)
	{
		if (null != this.log)
		{
			this.log.log(e);
		}
	}
	
	
	protected Logger log = null;

}
