package com.gmail.raygerman.basics;

public class LoggingObject
{
	
	public LoggingObject(String name)
	{
		this.name_ = name;
		this.log_ = Logger.getLog();
	}
	
	public LoggingObject(String name, Logger newLog)
	{
		this.name_ = name;
		if (null == newLog)
		{
			this.log_ = Logger.getLog();
		}
		this.log_ = newLog;
	}
	
	protected void log(String text)
	{
		if (null != this.log_)
		{
			this.log_.log(text);
		}
	}
	
	protected void log(Exception e)
	{
		if (null != this.log_)
		{
			this.log_.log(e);
		}
	}
	
	protected final String name_;
	protected Logger log_ = null;

}
