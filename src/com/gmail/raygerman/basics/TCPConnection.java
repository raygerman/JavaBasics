package com.gmail.raygerman.basics;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class TCPConnection extends SocketInterface
{
	
	public enum ConnectionType {NONE, SERVER, CLIENT}
	private IPAddress localIP_ = null;
	protected IPAddress remoteIP_ = null;
	protected ConnectionType type_ = ConnectionType.NONE;
	private Thread readThread_ = null;
	protected Thread acceptThread_ = null;
	protected Thread connectThread_ = null;
	protected Semaphore acceptSemaphore_ = null;
	@SuppressWarnings("unused")
	protected LinkedList<ReadThread> clients_ = new LinkedList<ReadThread>();
	protected ServerSocketChannel serverChannel_ = null;
	protected Client client_ = null;
	protected int initialReadInBytes_ = 0;
	protected TCPUser user_ = null;
	protected int maxMessageSize_ = 0;
	protected Semaphore reconnectSemaphore_ = null;
	protected Semaphore waitForConnectionSemaphore_ = null;
	protected AtomicBoolean connectedToServer_ = null;
	
	public TCPConnection(ConnectionType type, String name, IPAddress localIP, IPAddress remoteIP, int initialReadInBytes, TCPUser user, int maxMessageSize) throws NamedExceptions.ConstructionFailedException
	{
		super(name);
		this.localIP_ = localIP;
		this.remoteIP_ = remoteIP;
		this.type_ = type;
		this.initialReadInBytes_ = initialReadInBytes;
		if (this.initialReadInBytes_ < 1)
		{
			throw (new NamedExceptions.ConstructionFailedException("InitialReadInBytes must be greater than zero")); //$NON-NLS-1$
		}
		if (null == user)
		{
			throw (new NamedExceptions.ConstructionFailedException("Must pass in a TCPUser")); //$NON-NLS-1$
		}
		if (maxMessageSize <= this.initialReadInBytes_)
		{
			throw (new NamedExceptions.ConstructionFailedException("Max Message size must be greater than InitialReadInBytes")); //$NON-NLS-1$
		}
		this.maxMessageSize_ = maxMessageSize;
		this.user_ = user;
		if (ConnectionType.SERVER == type)
		{
			try
			{
				this.serverChannel_ = ServerSocketChannel.open();
				this.serverChannel_.socket().bind(this.localIP_.toInetSocketAddress());
				this.acceptThread_ = new AcceptThread(name);
				this.acceptThread_.start();
			} catch (IOException e)
			{
				this.log(e);
				throw (new NamedExceptions.ConstructionFailedException("Could not start server channel " + e.getMessage())); //$NON-NLS-1$
			}		
		}
		else if (ConnectionType.CLIENT == type)
		{
			try
			{
				this.defaultDestination_ = this.remoteIP_;
				this.connectedToServer_ = new AtomicBoolean(false);
				this.reconnectSemaphore_ = new Semaphore(0);
				this.waitForConnectionSemaphore_ = new Semaphore(0);
				this.client_ = new Client(this.remoteIP_.toInetSocketAddress(), SocketChannel.open());
				this.readThread_ = new ReadThread("Client.ReadThread", this.client_); //$NON-NLS-1$
				this.readThread_.start();
				this.connectThread_ = new ConnectThread("Connect Thread"); //$NON-NLS-1$
				this.connectThread_.start();
			} catch (IOException e)
			{
				this.log_.log(e);
			}
		}
	}
	
	public class Client
	{
		private InetSocketAddress ip_ = null;
		private SocketChannel channel_ = null;
		
		public Client(InetSocketAddress ip, SocketChannel channel)
		{
			this.ip_ = ip;
			this.channel_ = channel;
		}
		
		public InetSocketAddress ip()
		{
			return(this.ip_);
		}
		
		public SocketChannel channel()
		{
			return(this.channel_);
		}
	}
	
	protected class ConnectThread extends Thread
	{
		public ConnectThread(String name)
		{
			super(name);
		}
		
		@Override
		public void run()
		{
			while (TCPConnection.this.running_.get())
			{
				try
				{
					TCPConnection.this.client_.channel().connect(TCPConnection.this.remoteIP_.toInetSocketAddress());
					TCPConnection.this.connectedToServer_.set(true);
					System.out.println("Client connected"); //$NON-NLS-1$
					TCPConnection.this.waitForConnectionSemaphore_.release();
					TCPConnection.this.reconnectSemaphore_.acquire();
					
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	protected class AcceptThread extends Thread
	{
		public AcceptThread(String name)
		{
			super(name + ".acceptThread"); //$NON-NLS-1$
		}
		
		@Override
		public void run()
		{
			while (TCPConnection.this.running_.get())
			{
				try
				{
				  @SuppressWarnings("resource")
					SocketChannel client = TCPConnection.this.serverChannel_.accept();
				  System.out.println("A new client has connected"); //$NON-NLS-1$
					// We will do the accept and then
					Client newClient = new Client((InetSocketAddress)client.getRemoteAddress(), client);
					ReadThread newReadThread = new ReadThread(TCPConnection.this.name_, newClient); 
					TCPConnection.this.clients_.add(newReadThread);
					newReadThread.start();
				} catch (Exception e)
				{
					TCPConnection.this.log(e);
				}
			}
		}
	}
	
	private class ReadThread extends Thread
	{
		
		private Client readThreadClient_ = null;
		private byte[][] data_ = new byte[2][TCPConnection.this.maxMessageSize_];
		private ByteBuffer[] buffer_ = {ByteBuffer.wrap(this.data_[0]), ByteBuffer.wrap(this.data_[1])};
		private int currentBuffer_ = 0;
		
		public ReadThread(String name, Client client)
		{
			super(name + ".readthread"); //$NON-NLS-1$
			this.readThreadClient_ = client;
			System.out.println("Starting new read thread"); //$NON-NLS-1$
		}
		
		private void swapBuffer(int bytes)
		{
			int otherBuffer = 0;
			if (0 == this.currentBuffer_)
			{
				otherBuffer = 1;
			}
			if (this.buffer_[this.currentBuffer_].position() > bytes)
			{
			  this.buffer_[otherBuffer].clear();
			  this.buffer_[otherBuffer].put(this.data_[this.currentBuffer_], bytes, this.buffer_[this.currentBuffer_].position());
			  this.buffer_[otherBuffer].position(this.buffer_[this.currentBuffer_].position() - bytes);
			}
			this.currentBuffer_ = otherBuffer;
		}
		
		@Override
		public void run()
		{
			int bytesRead = 0;
			int remainingBytes = 0;
			
			while (TCPConnection.this.running_.get())
			{
				while (TCPConnection.ConnectionType.CLIENT == TCPConnection.this.type_ && false == TCPConnection.this.connectedToServer_.get())
				{
					try
					{
						TCPConnection.this.waitForConnectionSemaphore_.acquire();
					} catch (InterruptedException e)
					{
						TCPConnection.this.log(e);
					}
				}
				
				try
				{
					while (this.buffer_[this.currentBuffer_].position() < TCPConnection.this.initialReadInBytes_)
					{
					  bytesRead = this.readThreadClient_.channel().read(this.buffer_[this.currentBuffer_]);
					  if (bytesRead < 0)
					  {
					  	TCPConnection.this.closeConnection(this.readThreadClient_);
					  	return;
					  }
					}
					remainingBytes = TCPConnection.this.user_.messageSize(TCPConnection.this, this.data_[this.currentBuffer_]);
					while (this.buffer_[this.currentBuffer_].position() < TCPConnection.this.initialReadInBytes_ + remainingBytes)
					{
						bytesRead = this.readThreadClient_.channel().read(this.buffer_[this.currentBuffer_]);
						if (bytesRead < 0)
					  {
					  	TCPConnection.this.closeConnection(this.readThreadClient_);
					  	return;
					  }
					}
					TCPConnection.this.user_.processReceivedMessage(TCPConnection.this, this.data_[this.currentBuffer_]);
					this.swapBuffer(TCPConnection.this.initialReadInBytes_ + remainingBytes);
					
				} catch (IOException e)
				{
					TCPConnection.this.log(e);
					TCPConnection.this.closeConnection(this.readThreadClient_);
			  	return;
				}
			}
		}
		
		public Client client()
		{
			return(this.readThreadClient_);
		}
	}

	protected void closeConnection(Client client)
	{
		if (ConnectionType.SERVER == this.type_)
		{
			this.clients_.remove((Object)client);
			try
			{
				client.channel().close();
			} catch (IOException e)
			{
				this.log_.log(e);
			}
		}
		else if (ConnectionType.CLIENT == this.type_)
		{
			this.connectedToServer_.set(false);
			try
			{
				this.client_.channel().close();
				
			} catch (IOException e)
			{
				this.log_.log(e);
			} finally
			{
				this.reconnectSemaphore_.release();
			}
		}
	}
	
	// This function is called from the sendQueue thread and implements sending bytes out
	@Override
	public void doWork(WorkQueue<IPMessage> queue, IPMessage item)
	{
		if (null != item.desintation_)
		{
			ByteBuffer sendBuffer = ByteBuffer.wrap(item.data_);
			sendBuffer.limit(item.data_.length);
			if (ConnectionType.CLIENT == this.type_)
			{
				try
				{
					this.client_.channel().write(sendBuffer);
				} catch (IOException e)
				{
					this.log(e);
				}
			}
			else if (ConnectionType.SERVER == this.type_)
			{
				// Find the client
				for (ReadThread client : this.clients_)
				{
					if (client.client().ip().equals(item.desintation_.toInetSocketAddress()))
					{
						try
						{
							client.client().channel().write(sendBuffer);
						} catch (IOException e)
						{
							this.log(e);
						}
					}
				}
			}
		}
	}


}
