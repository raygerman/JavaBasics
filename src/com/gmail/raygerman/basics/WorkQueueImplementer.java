package com.gmail.raygerman.basics;

public interface WorkQueueImplementer<T>
{
	public abstract void doWork(WorkQueue<T> queue, T item);
}
