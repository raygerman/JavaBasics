package com.gmail.raygerman.basics;


public class NamedExceptions
{
	public static class InvalidArgumentException extends Exception
	{
		private static final long serialVersionUID = -3783242314593913021L;

		public InvalidArgumentException(String message)
		{
			super(message);
		}
	}

	public static class ConstructionFailedException extends Exception
	{
		private static final long serialVersionUID = -3214350491092890871L;

		public ConstructionFailedException(String message)
		{
			super(message);
		}
	}
	
	public static class QueueUnderflowException extends Exception
	{
		private static final long serialVersionUID = 3111766278276045091L;

		public QueueUnderflowException(String name)
		{
			super(name);
		}
	}

	public static class QueueOverflowException extends Exception
	{
		private static final long serialVersionUID = 8268239619884500213L;

		public QueueOverflowException(String name)
		{
			super(name);
		}
	}
	
	public static class ResourceUnavailableException extends Exception
	{
		private static final long serialVersionUID = -9054681854627273271L;

		public ResourceUnavailableException(String message)
		{
			super(message);
		}
	}
	
	public static class DataCorruptedException extends Exception
	{
		private static final long serialVersionUID = 5647206540675305998L;

		public DataCorruptedException(String message)
		{
			super(message);
		}
	}
}
