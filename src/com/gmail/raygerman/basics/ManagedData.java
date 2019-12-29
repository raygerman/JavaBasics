package com.gmail.raygerman.basics;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.gmail.raygerman.basics.NamedExceptions.InvalidArgumentException;
import com.gmail.raygerman.basics.NamedExceptions.ResourceUnavailableException;

public class ManagedData extends LoggingObject
{
	
	private LinkedList<Data> inUseList_ = null;
	private LinkedList<Data> freePoolList_ = null;
	
	@SuppressWarnings("unused")
	public ManagedData(String name, int numberOfElements, int sizeOfData) throws NamedExceptions.ConstructionFailedException
	{
		super(name);
		if (numberOfElements < 1 || sizeOfData < 1)
		{
			throw (new NamedExceptions.ConstructionFailedException("Cannot create manageddata or data with size less than one")); //$NON-NLS-1$
		}
		this.freePoolList_ = new LinkedList<Data>();
		this.inUseList_ = new LinkedList<Data>();
		for (int i = 0; i < numberOfElements; i++)
		{
			this.freePoolList_.add(new Data(this, sizeOfData));
		}
	}
	
	public synchronized Data create(String user) throws NamedExceptions.QueueUnderflowException
	{
		if (this.freePoolList_.isEmpty())
		{
			throw (new NamedExceptions.QueueUnderflowException("ManagedData has no items available")); //$NON-NLS-1$
		}
		Data item = this.freePoolList_.remove();
		try
		{
			item.addUser(user);
			item.reset();
		} catch (InvalidArgumentException | ResourceUnavailableException e)
		{
			this.log(e);
		}
		this.inUseList_.add(item);
		return(item);
	}
	
	protected synchronized void delete(Data item)
	{
		for (Data data : this.inUseList_)
		{
			if (data == item)
			{
				this.inUseList_.remove(item);
				this.freePoolList_.add(item);
				break;
			}
		}
	}
	
	public class Data
	{
		private ManagedData owner_ = null;
		private byte[] data_ = null;
		private int currentIndex_ = 0;
		private ReentrantReadWriteLock readWriteMutex_ = null;
		private LinkedList<String> users_ = null;
		
		@SuppressWarnings("unused")
		protected Data(ManagedData owner, int size) throws NamedExceptions.ConstructionFailedException
		{
			if (size < 1)
			{
				throw (new NamedExceptions.ConstructionFailedException("Cannot create data with less than one byte of space")); //$NON-NLS-1$
			}
			this.readWriteMutex_ = new ReentrantReadWriteLock();
			this.owner_ = owner;
			this.data_ = new byte[size];
			this.users_ = new LinkedList<String>();
		}
		
		public void addUser(String user) throws NamedExceptions.InvalidArgumentException
		{
			this.readWriteMutex_.writeLock().lock();
			try
			{
				for (String existingUser : this.users_)
				{
					if (existingUser.equals(user))
					{
						throw (new NamedExceptions.InvalidArgumentException("User already associated with data: " + user)); //$NON-NLS-1$
					}
				}
				this.users_.add(user);
			} finally
			{
				this.readWriteMutex_.writeLock().unlock();
			}
		}
		
		public void delete(String user) throws NamedExceptions.InvalidArgumentException, NamedExceptions.ResourceUnavailableException
		{
			this.readWriteMutex_.writeLock().lock();
			try
			{
				if (this.users_.size() == 0)
				{
					throw (new NamedExceptions.ResourceUnavailableException("Data has been deleted")); //$NON-NLS-1$
				}
				for (int i = 0; i < this.users_.size(); i++)
				{
					if (this.users_.get(i).equals(user))
					{
						if (null != this.owner_)
						{
							this.users_.remove(i);		
							if (this.users_.isEmpty())
							{
							  this.owner_.delete(this);
							}
							return;
						}
					}
				}
				// If I get here I did not find this user
				throw (new NamedExceptions.InvalidArgumentException("User not found in current user list: " + user)); //$NON-NLS-1$
			} finally
			{
				this.readWriteMutex_.writeLock().unlock();
			}
		}
		
		public void reset() throws NamedExceptions.ResourceUnavailableException
		{
			this.readWriteMutex_.writeLock().lock();
			try
			{
				if (this.users_.size() == 0)
				{
					throw (new NamedExceptions.ResourceUnavailableException("Data has been deleted")); //$NON-NLS-1$
				}
				this.currentIndex_ = 0;
			} finally
			{
			  this.readWriteMutex_.writeLock().unlock();
			}
		}
		
		public void appendByte(int newByte) throws NamedExceptions.InvalidArgumentException, NamedExceptions.ResourceUnavailableException
		{
			if (newByte < 0)
			{
				throw (new NamedExceptions.InvalidArgumentException("Cannot convert a negative int to byte")); //$NON-NLS-1$
			}
			this.readWriteMutex_.writeLock().lock();
			try
			{
				if (this.users_.size() == 0)
				{
					throw (new NamedExceptions.ResourceUnavailableException("Data has been deleted")); //$NON-NLS-1$
				}
			  this.data_[this.currentIndex_] = UnsignedByte.toByte(newByte);
			} finally
			{
			  this.readWriteMutex_.writeLock().unlock();
			}
		}
	}

}
