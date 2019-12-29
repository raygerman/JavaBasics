package com.gmail.raygerman.basics;

public interface TCPUser
{
	public int messageSize(TCPConnection connection, byte[] data);
	
	public void processReceivedMessage(TCPConnection connection, byte[] data);
}
