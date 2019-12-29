package com.gmail.raygerman.basics;

import java.util.concurrent.atomic.AtomicBoolean;


public abstract class SocketInterface extends LoggingObject implements WorkQueueImplementer<SocketInterface.IPMessage>
{	
	protected WorkQueue<IPMessage> sendQueue_ = null;
	protected AtomicBoolean running_ = new AtomicBoolean(true);
	protected IPAddress defaultDestination_ = null;
	
	protected class IPMessage
	{
		public IPAddress desintation_ = null;
		public byte[] data_ = null;
		
		public IPMessage(IPAddress destination, byte[] data)
		{
			this.desintation_ = destination;
			this.data_ = data;
		}
	}

	protected SocketInterface(String name)
	{
		super(name);
		this.commonConstructor();
	}
	
	protected SocketInterface(String name, Logger log)
	{
		super(name, log);
		this.commonConstructor();
	}
	
	@SuppressWarnings("unused")
	private void commonConstructor()
	{
		this.sendQueue_ = new WorkQueue<IPMessage>(this, this.name_ + ".sendqueue"); //$NON-NLS-1$
	}
	
	public void send(byte[] data)
	{
		this.sendQueue_.Add(new IPMessage(this.defaultDestination_, data));
	}
	
	public void sendTo(IPAddress destination, byte[] data)
	{
		this.sendQueue_.Add(new IPMessage(destination, data));
	}
	

}
