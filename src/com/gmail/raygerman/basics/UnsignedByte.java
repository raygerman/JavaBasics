package com.gmail.raygerman.basics;

public class UnsignedByte
{
	
	public static int toUnsigned(byte value)
	{
		if (value >= 0)
		{
			return (value);
		}
		return(256 + value);
	}
	
	public static byte toByte(int value) throws NamedExceptions.InvalidArgumentException
	{
		if (value < 0)
		{
			throw (new NamedExceptions.InvalidArgumentException("Cannot convert negative number to byte")); //$NON-NLS-1$
		}
		if (value <= 127)
		{
			return((byte)value);
		}
		return((byte)(value - 256));
	}
	
	public static String toHexString(byte value)
	{
		return(Integer.toHexString((toUnsigned(value))));
	}
	
	public static String toHexString(byte[] array)
	{
		StringBuilder builder = new StringBuilder(array.length * 3);
		for (int i = 0; i < array.length; i++)
		{
			builder.append(String.format("%02X", Integer.valueOf(toUnsigned(array[i]))) + " "); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return(builder.toString());
	}
	
	public static String toHexString(byte[] array, int length)
	{
		StringBuilder builder = new StringBuilder(length * 3);
		for (int i = 0; i < length; i++)
		{
			builder.append(String.format("0x%02X", Integer.valueOf(toUnsigned(array[i]))) + " "); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return(builder.toString());
	}
	
	

}
