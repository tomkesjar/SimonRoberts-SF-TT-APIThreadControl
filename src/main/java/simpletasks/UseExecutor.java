package simpletasks;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

class Task1 implements Runnable {
  @Override
  public void run() {
    System.out.println(Thread.currentThread().getName()
        + "Running task 1");
    try {
      Thread.sleep((int)(Math.random() * 4000) + 1000);
    } catch (InterruptedException e) {
      System.out.println("Odd, we were asked to shutdown!");;
    }
  }
}
class Task2 implements Runnable {
  @Override
  public void run() {
    System.out.println(Thread.currentThread().getName()
        + "Task 2 starting");
    try {
      Thread.sleep((int)(Math.random() * 6000) + 1000);
    } catch (InterruptedException e) {
      System.out.println("Odd (task 2) asked to shutdown!");;
    }
  }
}

public class UseExecutor {
  public static void main(String[] args) {
    Executor ex = Executors.newFixedThreadPool(1);
    ex.execute(new Task1());
    ex.execute(new Task2());
    System.out.println("Jobs submitted");
  }
}
