package com.gmail.raygerman.basics;

import java.util.Collection;
import java.util.Iterator;

public class NonDynamicLinkedList<T> extends LoggingObject implements Collection<T>
{
	protected long updateCount_ = 0;
	protected SaferBoolean canGrow_ = null;
	protected int size_ = 0;
	
	protected class ListItem
	{
		public ListItem next_ = null;
		public T item_ = null;
		
		public ListItem(T item, ListItem next)
		{
			this.item_ = item;
			this.next_ = next;
		}
	}
	
	public class NonDynamicLinkedListIterator implements Iterator<T>
	{
		long updateCheck = NonDynamicLinkedList.this.updateCount_;
		ListItem currentNext = NonDynamicLinkedList.this.inUseListHead_;
		
		@Override
		public boolean hasNext() 
		{
			if (null != this.currentNext && this.updateCheck == NonDynamicLinkedList.this.updateCount_)
			{
				return(true);
			}
			return false;
		}

		@Override
		public T next() 
		{
			if (null != this.currentNext && this.updateCheck == NonDynamicLinkedList.this.updateCount_)
			{
				T item = this.currentNext.item_;
				this.currentNext = this.currentNext.next_;
				return(item);
			}
			return(null);
		}	
	}
	
	protected ListItem freeListHead_ = null;
	protected ListItem inUseListHead_ = null;
	protected int inUseCount_ = 0;
	
	public NonDynamicLinkedList(int size, String name, boolean canGrow) throws NamedExceptions.InvalidArgumentException
	{
		super(name);
		this.canGrow_ = new SaferBoolean(canGrow);
		if (size < 1)
		{
			throw (new NamedExceptions.InvalidArgumentException("Cannot create NonDynamicLinkedList with less than one element"));
		}
		this.size_ = size;
		for (int i = 0; i < this.size_; i++)
		{
			this.freeListHead_ = new ListItem(null, this.freeListHead_);
		}
	}
	
	@Override
	public synchronized boolean isEmpty()
	{
		return((null == this.inUseListHead_));
	}
	
	public synchronized boolean isFull()
	{
		return((null == this.freeListHead_));
	}
	
	private void internalAddToFront(T item) throws NamedExceptions.QueueOverflowException, NamedExceptions.DataCorruptedException
	{
		this.verifyFreeAvailable();
		ListItem newItem = this.freeListHead_;
		// Increment the freeList
		this.freeListHead_ = newItem.next_;
		// Set values in the new item
		newItem.item_ = item;
		newItem.next_ = this.inUseListHead_;
		// Insert to the front if the inUse list
		this.inUseCount_++;
		this.inUseListHead_ = newItem;
	}
	
	public synchronized void addToFront(T item) throws NamedExceptions.QueueOverflowException, NamedExceptions.DataCorruptedException
	{
		internalAddToFront(item);
		this.updateCount_++;
	}
	
	private void internalAddToBack(T item) throws NamedExceptions.QueueOverflowException, NamedExceptions.DataCorruptedException
	{
		this.verifyFreeAvailable();
		ListItem newItem = this.freeListHead_;
		// Increment the freeList
		this.freeListHead_ = newItem.next_;
		// Set values in the new item
		newItem.item_ = item;
		newItem.next_ = null;
		// Insert at the end of the inUse list
		this.inUseCount_++;
		if (null == this.inUseListHead_)
		{
			this.inUseListHead_ = newItem;
		}
		else
		{
			ListItem searchItem = this.inUseListHead_;
			int count = 0;
			// Add a sanity check count
			while (null != searchItem.next_ && count < this.size_)
			{
				searchItem = searchItem.next_;
				count++;
			}
			searchItem.next_ = newItem;
		}
	}
	
	public synchronized void addToBack(T item) throws NamedExceptions.QueueOverflowException, NamedExceptions.DataCorruptedException
	{
		this.updateCount_++;
		internalAddToBack(item);
	}
	
	private void internalInsertAfterOrToBack(T itemToInsert, T itemToSearchFor) throws NamedExceptions.QueueOverflowException, NamedExceptions.DataCorruptedException
	{
		if (null == this.inUseListHead_)
		{
			this.internalAddToFront(itemToInsert);
			return;
		}
	    this.verifyFreeAvailable();
		ListItem searchItem = this.inUseListHead_;
		int count = 0;
		// Add a sanity check count
		while (null != searchItem.next_ && count < this.size_)
		{
			if (searchItem == itemToSearchFor)
			{
				break;
			}
			searchItem = searchItem.next_;
			count++;
		}
		this.freeListHead_.item_ = itemToInsert;
		searchItem.next_ = this.freeListHead_;
		searchItem.next_.next_ = null;
		this.freeListHead_ = this.freeListHead_.next_;
	}
	
	public synchronized void insterAfterOrToBack(T itemToInsert, T itemToSearchFor) throws NamedExceptions.QueueOverflowException, NamedExceptions.DataCorruptedException
	{
		this.updateCount_++;
		internalInsertAfterOrToBack(itemToInsert, itemToSearchFor);
	}
	
	private void internalInsertAfterOrFail(T itemToInsert, T itemToSearchFor) throws NamedExceptions.InvalidArgumentException, NamedExceptions.QueueOverflowException, NamedExceptions.DataCorruptedException
	{
		if (null == this.inUseListHead_)
		{
			this.internalAddToFront(itemToInsert);
			return;
		}
	    this.verifyFreeAvailable();
		ListItem searchItem = this.inUseListHead_;
		boolean found = false;
		int count = 0;
		// Add a sanity check count
		while (null != searchItem.next_ && count < this.size_)
		{
			if (searchItem == itemToSearchFor)
			{
				found = true;
				break;
			}
			searchItem = searchItem.next_;
			count++;
		}
		if (true == found)
		{
			this.freeListHead_.item_ = itemToInsert;
			searchItem.next_ = this.freeListHead_;
			searchItem.next_.next_ = null;
			this.freeListHead_ = this.freeListHead_.next_;
		}
		else
		{
			throw (new NamedExceptions.InvalidArgumentException("Item not found"));
		}
	}
	
	public synchronized void insertAfterOrfail(T itemToInsert, T itemToSearchFor) throws NamedExceptions.InvalidArgumentException, NamedExceptions.QueueOverflowException, NamedExceptions.DataCorruptedException
	{
		this.updateCount_++;
		internalInsertAfterOrFail(itemToInsert, itemToSearchFor);
	}
	
	public synchronized T peek()
	{
		if (null != this.inUseListHead_)
		{
			return(this.inUseListHead_.item_);
		}
		return(null);
	}
	
	private void verifyFreeAvailable() throws NamedExceptions.QueueOverflowException, NamedExceptions.DataCorruptedException
	{
		if (null == this.freeListHead_ && false == this.canGrow_.value())
		{
			throw (new NamedExceptions.QueueOverflowException("Could not add item to queue as it is full"));
		}
		if (null == this.freeListHead_)
		{
			this.size_++;
			this.freeListHead_ = new ListItem(null, null);
		}
	}

	@Override
	public Iterator<T> iterator() 
	{
		NonDynamicLinkedListIterator iterator = new NonDynamicLinkedListIterator();
		return(iterator);
	}

	@Override
	public synchronized int size() 
	{
		return(this.inUseCount_);
	}

	@Override
	public boolean add(T arg0) throws IllegalStateException
	{
		try 
		{
			this.addToFront(arg0);
			return(true);
		} catch (Exception e) {
			this.log(e);
			throw (new IllegalStateException("Cannot Add at this time"));
		}
	}

	@Override
	public boolean addAll(Collection<? extends T> arg0) throws UnsupportedOperationException
	{
		throw (new UnsupportedOperationException("This class does not support addAll"));
	}

	@Override
	public synchronized void clear() {
		ListItem item = this.inUseListHead_;
		while (null != item && this.inUseCount_ > 0)
		{
			this.inUseListHead_ = item.next_;
			item.next_ = this.freeListHead_;
			this.freeListHead_ = item;
			this.inUseCount_--;
			item = this.inUseListHead_;
		}
	}

	@Override
	public synchronized boolean contains(Object arg0) 
	{
		ListItem item = this.inUseListHead_;
		while (null != item)
		{
			if (item.item_.equals(arg0))
			{
				return(true);
			}
			item = item.next_;
		}
		return(false);
	}

	@Override
	public boolean containsAll(Collection<?> arg0) throws UnsupportedOperationException
	{
		throw (new UnsupportedOperationException("This class does not support containsAll"));
	}

	@Override
	public synchronized boolean remove(Object arg0) 
	{
		if (null == this.inUseListHead_)
		{
			return(false);
		}
		ListItem item = this.inUseListHead_;
		int count = 0;
		ListItem previousItem = null;
		while (null != item && count < this.size_)
		{
			if (item.item_.equals(arg0))
			{
				if (null != previousItem)
				{
					previousItem.next_ = item.next_;
				}
				else
				{
					this.inUseListHead_ = item.next_;
				}
				item.next_ = this.freeListHead_;
				this.freeListHead_ = item;
				this.inUseCount_--;
				return(true);
			}
			previousItem = item;
			item = item.next_;
		}
		return(false);
	}

	@Override
	public boolean removeAll(Collection<?> arg0) throws UnsupportedOperationException
	{
		throw (new UnsupportedOperationException("This class does not support removeAll"));
	}

	@Override
	public boolean retainAll(Collection<?> arg0) throws UnsupportedOperationException
	{
		throw (new UnsupportedOperationException("This class does not support retainAll"));
	}

	@Override
	public Object[] toArray() throws UnsupportedOperationException {
		throw (new UnsupportedOperationException("This class does not support toArray"));
	}

	@SuppressWarnings("hiding")
	@Override
	public <T> T[] toArray(T[] arg0) throws UnsupportedOperationException
	{
		throw (new UnsupportedOperationException("This class does not support toArray"));
	}


}
