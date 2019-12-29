package com.gmail.raygerman.basics;

import java.lang.Class;

public class StringsEnum<L extends Enum<L>>
{
	
	private String[] values = null;
	public StringsEnum(Class<L> l, String[] strings) throws IllegalArgumentException
	{
		if (strings.length != l.getEnumConstants().length)
		{
			throw (new IllegalArgumentException("Incorrect number of strings for number of languages")); //$NON-NLS-1$
		}
		
		this.values = new String[l.getEnumConstants().length];
		for (int i = 0; i < this.values.length; i++)
		{
			this.values[i] = strings[i];
		}
	}
	
	public String get(L language)
	{
		return(this.values[language.ordinal()]);
	}
}
