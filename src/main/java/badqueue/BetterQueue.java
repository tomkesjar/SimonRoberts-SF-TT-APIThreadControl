package badqueue;

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BetterQueue<E> {
  private static final int CAPACITY = 10;
  private E[] data = (E[])new Object[CAPACITY];
  private int count = 0;
//  private Object rendezvous = new Object();
  private ReentrantLock lock = new ReentrantLock();
  Condition notFull = lock.newCondition();
  Condition notEmpty = lock.newCondition();

  public void put(E e) throws InterruptedException {
//    synchronized (rendezvous) {
    lock.lock();
    try {
      while (count >= CAPACITY) {
//        rendezvous.wait();
        notFull.await();
      }
      data[count++] = e;
//      rendezvous.notifyAll();
      notEmpty.signal();
    } finally {
      lock.unlock();
    }
  }

  public E take() throws InterruptedException {
    lock.lock();
    // ways you can keep liveness!
//    lock.lockInterruptibly();
//    lock.tryLock();
    try {
//    synchronized (rendezvous) { // NEVER RETURNS unless successfully locked
      while (count <= 0) {
//        rendezvous.wait();
        notEmpty.await();
      }
      E rv = data[0];
      System.arraycopy(data, 1, data, 0, --count);
//      rendezvous.notifyAll();
      notFull.signal();
      return rv;
    } finally {
      lock.unlock();
    }
  }
}

class TryTheBadQueue {
  public static void main(String[] args) throws InterruptedException {
//    BetterQueue<int[]> q = new BetterQueue<>();
    BlockingQueue<int[]> q = new ArrayBlockingQueue<>(10);

    Thread producer = new Thread(() -> {
      System.out.println("Producer starting...");
      try {
        for (int i = 0; i < 50_000_000; i++) {
          int[] data = {-1, i}; // transactionally invalid!!!
          if (i < 500) {
            Thread.sleep(1);
          }
//          if (i == 5_000) {
//            data[0] = -100;
//          }
          data[0] = i; // transactionally valid

          q.put(data);
          data = null; // shared now, lose my reference
        }
      } catch (InterruptedException ie) {
        System.out.println("Odd, shutdown requested!");
      }
      System.out.println("Producer ending...");
    });

    Thread consumer = new Thread(() -> {
      System.out.println("Consumer starting...");
      try {
        for (int i = 0; i < 50_000_000; i++) {
          int [] data = q.take();
//          if (i > 9_500) {
//            Thread.sleep(1);
//          }
          if (data[0] != data[1] || data[0] != i) {
            System.out.println("****error at index " + i
                + " data " + Arrays.toString(data));
          }
        }
      } catch (InterruptedException ie) {
        System.out.println("Odd, shutdown of consumer??");
      }
      System.out.println("Consumer ending...");
    });

    producer.start();
    consumer.start();
    long start = System.nanoTime();
    producer.join();
    consumer.join();
    long time = System.nanoTime() - start;
    System.out.println("Everything finished");
    System.out.printf("Time take: %7.3f\n", (time / 1_000_000_000.0));
  }
}