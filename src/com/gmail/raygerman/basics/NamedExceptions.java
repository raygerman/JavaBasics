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


}
