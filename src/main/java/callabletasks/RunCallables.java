package callabletasks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class Task implements Callable<String> {
  // these two lines are executed (in my example) by main Thread
  // I don't care that this is not thread-safe
  private static int nextId = 0;
  private int myId = nextId++;

  @Override
  public String call() throws Exception {
    // code in here will be executed by threads in the pool
    // (in my example) -- need to be sure that myId is "safe published"
    System.out.println(Thread.currentThread().getName()
        + " starting task "  + myId);
//    if (Thread.interrupted()) {
//      System.out.println("I got an interrupt already!");
//      return null; // I'm shutting down tidily
//    }
    try {
      Thread.sleep((int) (Math.random() * 4000) + 1000);
    } catch (InterruptedException ie) {
      System.out.println("interrupted in task " + myId);
    }
    System.out.println(Thread.currentThread().getName()
        + " completing task "  + myId);
    return "Data returned from task " + myId;
  }
}

public class RunCallables {
  public static void main(String[] args) throws Throwable {
//    Thread notInteresting = new Thread();
//    notInteresting.setDaemon(true);
//    notInteresting.interrupt();

    final int TASK_COUNT = 6;
    ExecutorService es = Executors.newFixedThreadPool(2);
    List<Future<String>> handles = new ArrayList<>();
    for (int i = 0; i < TASK_COUNT; i++) {
      handles.add(es.submit(new Task()));
    }
    System.out.println("All tasks submitted");
    Thread.sleep(500);
    handles.get(1).cancel(true);

    // on shutdown:
    // 1) block of the task input queue -- exception if new task submitted
    // 2) run the tasks to completion, then shutdown all the worker threads
//    es.shutdown();
//    es.submit(new Task());

    // sends interrupt to running tasks
    // closes the input queue
    // removes non-started tasks from the input queue
    // reports the Runnables that were abandoned
//    List<Runnable> canceledTasks = es.shutdownNow();

    while (handles.size() > 0) {
      Iterator<Future<String>> ifs = handles.iterator();
      while (ifs.hasNext()) {
        Future<String> fs = ifs.next();
        if (fs.isCancelled()) {
          System.out.println("A task was canceled");
          ifs.remove();
        }
        if (fs.isDone()) {
          ifs.remove();
          String rv = fs.get(); // simple get blocks if not "isDone"
          System.out.println("A task returned: " + rv);
        }
      }
    }
  }
}
