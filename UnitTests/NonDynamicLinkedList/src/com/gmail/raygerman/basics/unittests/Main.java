
package com.gmail.raygerman.basics.unittests;
import com.gmail.raygerman.basics.*;
import com.gmail.raygerman.basics.NamedExceptions.DataCorruptedException;
import com.gmail.raygerman.basics.NamedExceptions.InvalidArgumentException;
import com.gmail.raygerman.basics.NamedExceptions.QueueOverflowException;
public class Main {

	public static void main(String[] args) {
		
		try 
		{
			final int LIST_SIZE = 100;
			NonDynamicLinkedList<Integer> list = new NonDynamicLinkedList<>(LIST_SIZE, "TestList", false);
			
			
			// Fill the list
			for (int i = 0; i < LIST_SIZE; i++)
			{
				list.add(Integer.valueOf(i));
			}
			
			if (list.isFull())
			{
				int count = LIST_SIZE - 1;
				for (Integer integer : list)
				{
					if (integer.intValue() != count)
					{
						System.out.println("Incorrect integer found in list " + Integer.toString(integer.intValue()) + " count is " + Integer.toString(count));
						break;
					}
					count--;
				}
			}
			else
			{
				System.out.println("List is not full when it should be");
			}
			
			boolean exceptionFired = false;
			try 
			{
				list.add(Integer.valueOf(Integer.MAX_VALUE));
				
			} catch (@SuppressWarnings("unused") IllegalStateException e)
			{
				exceptionFired = true;
			}
			
			if (exceptionFired == false)
			{
				System.out.println("List did not throw exception for overflow");
			}
			
			list.clear();
			
			if (list.isEmpty() == false)
			{
				System.out.println("List is not empty when it should be, size is " + Integer.toString(list.size()));
			}
			
			// Fill the list
			for (int i = 0; i < LIST_SIZE; i++)
			{
				try 
				{
					list.addToBack(Integer.valueOf(i));
					
				} catch (QueueOverflowException | DataCorruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if (list.isFull())
			{
				int count = 0;
				for (Integer integer : list)
				{
					if (integer.intValue() != count)
					{
						System.out.println("Incorrect integer found in list " + Integer.toString(integer.intValue()) + " count is " + Integer.toString(count));
						break;
					}
					count++;
				}
				
				boolean[] remainingValues = new boolean[LIST_SIZE];
				for (int i = 0; i < LIST_SIZE; i++)
				{
					remainingValues[i] = true;
				}
				int remainingCount = LIST_SIZE;
				while (list.isEmpty() == false)
				{
					boolean keeplooking = true;
					int number = -1;
					while (keeplooking)
					{
						number = (int)Math.random() * (LIST_SIZE + 1);
						if (number < LIST_SIZE && remainingValues[number] == true)
						{
							keeplooking = false;
						}
					}
					list.remove(Integer.valueOf(number));
					remainingValues[number] = false;
					remainingCount--;
					int numberFound = 0;
					for (Integer value : list)
					{
						numberFound++;
						if (remainingValues[value.intValue()] == false)
						{
							System.out.println("Found a value which has been removed");
							break;
						}
					}
					if (numberFound != list.size() || numberFound != remainingCount)
					{
						System.out.println("Size mismatch");
					}
				}
			}
		} catch (InvalidArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

}
