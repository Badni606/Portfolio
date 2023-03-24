package uk.ac.soton.comp1206.component;

/**
 * A cycle queue data structure for ints
 */
public class CyclicQueue {
  int[] array;
  int head=0;
  int tail=0;
  boolean isFull=false;

  /**
   * Create a cyclic queue for storing ints
   * @param arraySize Size of queue
   */
  public CyclicQueue(int arraySize){
    this.array = new int[arraySize];
  }

  /**
   * Add an int to the queue if not full
   * @param i int to add to the queue
   * @throws IndexOutOfBoundsException If queue is full
   */
  public void enqueue(int i) throws IndexOutOfBoundsException {
    if(isFull){
      throw new IndexOutOfBoundsException("Queue full");
    }
    array[tail] = i;
    tail++;
    if(tail==array.length){
      tail=0;
    }
    if(tail==head){
      isFull=true;
    }
  }

  /**
   * Removes the first int on the queue
   * @return First int on the queue
   * @throws IndexOutOfBoundsException If empty
   */
  public int dequeue() throws IndexOutOfBoundsException {
    if(isEmpty()&&isFull==false){
      throw new IndexOutOfBoundsException("Queue empty");
    }
    int temp = array[head];
    head++;
    if(head== array.length){
      head = 0;
    }
    isFull=false;
    return temp;
  }

  /**
   * Checks whether the queue is empty
   * @return Boolean representing whether queue is empty
   */
  private boolean isEmpty() {
    return((tail == head) && !isFull);
  }
}
