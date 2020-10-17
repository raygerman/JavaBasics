package com.gmail.raygerman.basics;

import java.util.NoSuchElementException;

public class NonDynamicQueue<T> extends NonDynamicLinkedList<T>
{
	public NonDynamicQueue(int size, String name, boolean canGrow) throws NamedExceptions.InvalidArgumentException
	{
		super(size, name, canGrow);
	}
	
	@Override
	public boolean add(T arg0) throws IllegalStateException
	{
		try
		{
			this.addToBack(arg0);
			return(true);
		} catch (Exception e)
		{
			this.log(e);
			throw (new IllegalStateException(e.getMessage()));
		}
	}
	
	public boolean offer(T item)
	{
		try
		{
			add(item);
			return(true);
		} catch (IllegalStateException e)
		{
			this.log(e);
			return(false);
		}
	}
	
	public synchronized T remove() throws NoSuchElementException
	{
		if (null == this.inUseListHead_)
		{
			throw (new NoSuchElementException("Queue is empty"));
		}
		ListItem temp = this.inUseListHead_;
		this.inUseListHead_ = temp.next_;
		temp.next_ = this.freeListHead_;
		this.freeListHead_ = temp;
		return(temp.item_);
	}
	
	public T poll()
	{
		try
		{
			return(remove());
		} catch (NoSuchElementException e)
		{
			this.log(e);
			return(null);
		}
	}
	
	public synchronized T element() throws NoSuchElementException
	{
		if (null != this.inUseListHead_)
		{
			return(this.inUseListHead_.item_);
		}
		throw (new NoSuchElementException("Queue is empty"));
	}
	
	@Override
	public synchronized T peek()
	{
		if (null != this.inUseListHead_)
		{
			return(this.inUseListHead_.item_);
		}
		return(null);
	}
}
