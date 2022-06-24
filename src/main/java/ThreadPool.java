import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ThreadPool {
  private static final int THREAD_POOL_SIZE = 20;
  private ScheduledExecutorService executor;

  public ThreadPool() {
    executor = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);
  }

  public Future<?> execute(Runnable runnable) {
    return this.executor.submit(runnable);
  }

  public ScheduledFuture<?> execute(Runnable runnable, int time) {
    return this.executor.schedule(runnable, time, TimeUnit.MILLISECONDS);
  }

  public void shutDown() {
    this.executor.shutdown();
  }
}  