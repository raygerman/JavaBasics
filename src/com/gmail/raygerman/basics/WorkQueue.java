package com.gmail.raygerman.basics;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public abstract class WorkQueue<T>
{
	
	public static enum Policy {FIXED_THREAD_COUNT, THREAD_COUNT_GROWS}
	
	protected class Worker extends Thread
	{

		@Override
		public void run()
		{
			while (WorkQueue.this.running.get())
			{
				T item;
				try
				{
					item = WorkQueue.this.queue.take();
					long runningThreadCount = WorkQueue.this.threadsRunning.incrementAndGet();
					WorkQueue.this.doWork(item);
					if (runningThreadCount == WorkQueue.this.numberOfThreads && WorkQueue.this.threadsGrow.get())
					{
						Worker newWorker = new Worker();
						newWorker.start();
						WorkQueue.this.workers.add(newWorker);
					}
					runningThreadCount = WorkQueue.this.threadsRunning.decrementAndGet();
				} catch (@SuppressWarnings("unused") InterruptedException e)
				{
					// Nothing to do here
				}
			}
		}
	}
	
	public WorkQueue(WorkQueueWorker worker)
	
		this.commonConstructor(1, worker);
	}
	
	public WorkQueue(WorkQueueWorker worker, long threadCount) throws NamedException.InvalidArgumentException
	{
		this.commonConstructor(threadCount, worker);
	}
	
	@SuppressWarnings("unused")
	public void commonConstructor(long threadCount, WorkQueueWorker worker) throws NamedException.InvalidArgumentException
	{
		this.worker = worker;
		if (threadCount > 0)
		{
			this.threadsGrow = new AtomicBoolean(false);
			this.numberOfThreads = threadCount;
			this.queue = new LinkedBlockingQueue<T>();
			this.running = new AtomicBoolean(true);
			this.threadsRunning = new AtomicLong(0);
			this.workers = new LinkedList<Worker>();
			this.initialized = new AtomicBoolean(false);
			for (long i = 0; i < this.numberOfThreads; i++)
			{
				this.workers.add(new Worker());
			}
		}
		else
		{
			throw (new NamedException.InvalidArgumentException("Cannot create WorkQueue with less than one threads")); //$NON-NLS-1$
		}
	}
	
	public void Add(T item)
	{
		if (null != this.queue && this.running.get()) 
		{
			// Lazy initialization is the best initialization
			if (!this.initialized.getAndSet(true))
			{
				for (Worker worker : this.workers)
				{
					worker.start();
				}
			}
			this.queue.add(item);
		}
	}
	
	public void stop()
	{
		this.running.set(false);
		for (Worker worker : this.workers)
		{
			worker.interrupt();
		}
	}
	
	public void setPolicy(Policy policy)
	{
		switch (policy) 
		{
			case FIXED_THREAD_COUNT:
				this.threadsGrow.set(false);
				break;
			case THREAD_COUNT_GROWS:
				this.threadsGrow.set(true);
				break;
			default:
				break;
		}
	}
	
	protected WorkQueueWorker worker = null;	
	protected AtomicLong threadsRunning = null;
	protected AtomicBoolean running = null;
	protected AtomicBoolean initialized = null;
	protected AtomicBoolean threadsGrow = null;
	protected LinkedBlockingQueue<T> queue = null;
	protected LinkedList<Worker> workers = null;
	protected long numberOfThreads = 0;

}
