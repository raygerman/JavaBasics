package com.gmail.raygerman.basics;

public class SaferBoolean {

	private static final int FALSE = 0x23890D77;
	private static final int TRUE  = 0x830025A1;
	private int value_ = FALSE;
	
	public SaferBoolean(boolean value)
	{
		if (value)
		{
			this.value_ = TRUE;
		}
		else
		{
			this.value_ = FALSE;
		}
	}
	
	public synchronized boolean value() throws NamedExceptions.DataCorruptedException
	{
		if (TRUE== this.value_)
		{
			return(true);
		}
		else if (FALSE == this.value_)
		{
			return(false);
		}
		throw (new NamedExceptions.DataCorruptedException("SaferBoolean value is corrupt (" + Integer.toHexString(this.value_) + ")"));
	}
	
	public synchronized void set(boolean value)
	{
		if (value)
		{
			this.value_ = TRUE;
		}
		else
		{
			this.value_ = FALSE;
		}
	}
	
}
