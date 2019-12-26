package com.gmail.raygerman.basics;

public interface WorkQueueWorker<T>
{
  public void doWork(WorkQueue queue, T item);
}
