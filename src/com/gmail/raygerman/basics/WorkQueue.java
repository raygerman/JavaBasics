package com.gmail.raygerman.basics;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class WorkQueue<T> extends LoggingObject
{
	
	public static enum Policy {FIXED_THREAD_COUNT, THREAD_COUNT_GROWS}
	
	protected class Worker extends Thread
	{

		@Override
		public void run()
		{
			while (WorkQueue.this.isRunning_.get())
			{
				T item;
				try
				{
					item = WorkQueue.this.queue_.take();
					long runningThreadCount = WorkQueue.this.threadsRunning_.incrementAndGet();
					WorkQueue.this.implementer_.doWork(WorkQueue.this, item);
					if (runningThreadCount == WorkQueue.this.numberOfThreads_ && WorkQueue.this.threadsGrow_.get())
					{
						WorkQueue.this.log("Adding thread to " + WorkQueue.this.name_ + " due to congestion");  //$NON-NLS-1$//$NON-NLS-2$
						Worker newWorker = new Worker();
						newWorker.start();
						WorkQueue.this.workers_.add(newWorker);
					}
					runningThreadCount = WorkQueue.this.threadsRunning_.decrementAndGet();
				} catch (@SuppressWarnings("unused") InterruptedException e)
				{
					// Nothing to do here
				}
			}
		}
	}
	
	public WorkQueue(WorkQueueImplementer<T> owner, String name)
	{
		super(name);
		this.commonConstructor(owner, 1, null);
	}
	
	public WorkQueue(WorkQueueImplementer<T> owner, String name, long threadCount)
	{
		super(name);
		this.commonConstructor(owner, threadCount, null);
	}
	
	public WorkQueue(WorkQueueImplementer<T> owner, String name, long threadCount, Logger newLog)
	{
		super(name);
		this.log_ = newLog;
		this.commonConstructor(owner, threadCount, newLog);
	}
	
	@SuppressWarnings("unused")
	public void commonConstructor(WorkQueueImplementer<T> owner, long threadCount, Logger newLog)
	{
		if (threadCount > 0)
		{
			if (null == newLog)
			{
				this.log_ = Logger.getLog();
			}
			else
			{
				this.log_ = newLog;
			}
			this.implementer_ = owner;
			this.threadsGrow_ = new AtomicBoolean(false);
			this.numberOfThreads_ = threadCount;
  		this.queue_ = new LinkedBlockingQueue<T>();
  		this.isRunning_ = new AtomicBoolean(true);
  		this.threadsRunning_ = new AtomicLong(0);
  		this.workers_ = new LinkedList<Worker>();
  		this.initialized_ = new AtomicBoolean(false);
  		for (long i = 0; i < this.numberOfThreads_; i++)
  		{
  			this.workers_.add(new Worker());
  		}
		}
		else
		{
			throw (new IllegalArgumentException("Cannot create WorkQueue with negative number of threads")); //$NON-NLS-1$
		}
	}
	
	public void Add(T item)
	{
		if (null != this.queue_ && this.isRunning_.get()) 
		{
			// Lazy initialization is the best initialization
			if (!this.initialized_.getAndSet(true))
			{
				for (Worker worker : this.workers_)
				{
					worker.start();
				}
			}
			this.queue_.add(item);
		}
	}
	
	public void stop()
	{
		this.log("Stopping all threads in " + this.name_); //$NON-NLS-1$
		this.isRunning_.set(false);
		for (Worker worker : this.workers_)
		{
			worker.interrupt();
		}
	}
	
	public void setPolicy(Policy policy)
	{
		switch (policy) 
		{
			case FIXED_THREAD_COUNT:
				this.threadsGrow_.set(false);
				break;
			case THREAD_COUNT_GROWS:
				this.threadsGrow_.set(true);
				break;
			default:
				break;
		}
	}
	
	protected AtomicLong threadsRunning_ = null;
	protected AtomicBoolean isRunning_ = null;
	protected AtomicBoolean initialized_ = null;
	protected AtomicBoolean threadsGrow_ = null;
	protected LinkedBlockingQueue<T> queue_ = null;
	protected LinkedList<Worker> workers_ = null;
	protected long numberOfThreads_ = 0;
	protected WorkQueueImplementer<T> implementer_ = null;
}
