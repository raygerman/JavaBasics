package com.gmail.raygerman.basics;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class IPAddress
{
	private int port_;
	private String ip_;
	private long ipLong_;
	
	public IPAddress(String ipPort) throws NamedExceptions.InvalidArgumentException
	{
		// Expect address in IP:Port form
		String[] parts = ipPort.split(":"); //$NON-NLS-1$
		if (parts.length <= 2)
		{
			String[] bytes = parts[0].split("\\."); //$NON-NLS-1$
			if (bytes.length == 4)
			{
				long[] octets = new long[4];
				for (int i = 0; i < 4; i++)
				{
					try
					{
					  octets[i] = Long.parseLong(bytes[i]);
					} catch (Exception e)
					{
						throw (new NamedExceptions.InvalidArgumentException("Invalid IP " + ipPort + " Message: " + e.getMessage())); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				this.ipLong_ = octets[0] * 256 * 256 * 256 + octets[1] * 256 * 256 + octets[2] * 256 + octets[3];
				this.ip_ = parts[0]; 
				
			}
			else
			{
				throw (new NamedExceptions.InvalidArgumentException("Invalid IP " + ipPort)); //$NON-NLS-1$
			}
			if (parts.length == 2)
			{
				try 
				{
				  this.port_ = Integer.parseInt(parts[1]);
				} catch (Exception e)
				{
					throw (new NamedExceptions.InvalidArgumentException("Invalid port in " + ipPort + "\n" + e.getMessage())); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			else
			{
				this.port_ = 0;
			}
		}
		else
		{
			throw (new NamedExceptions.InvalidArgumentException("Invalid IP " + ipPort)); //$NON-NLS-1$
		}
	}
	
	public int port()
	{
		return(this.port_);
	}
	
	public String ipAsString()
	{
		return(new String(this.ip_));
	}
	
	@Override
	public String toString()
	{
		return(this.ip_ + ":" + this.port_); //$NON-NLS-1$
	}
	
	public InetSocketAddress toInetSocketAddress()
	{
		if (0 == this.ipLong_)
		{
			return(new InetSocketAddress(this.port_));
		}
		try
		{
			return(new InetSocketAddress(InetAddress.getByName(this.ip_), this.port_));
			
		} catch (UnknownHostException e)
		{
			e.printStackTrace();
			return(null);
		}	
	}
}
