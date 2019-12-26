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




}
