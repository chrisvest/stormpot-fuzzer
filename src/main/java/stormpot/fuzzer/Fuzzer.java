package stormpot.fuzzer;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import stormpot.Allocator;
import stormpot.Config;
import stormpot.Expiration;
import stormpot.LifecycledPool;
import stormpot.Pool;
import stormpot.PoolException;
import stormpot.Timeout;

public class Fuzzer {
  private static final Timeout DEFAULT_TIMEOUT =
      new Timeout(1, TimeUnit.SECONDS);
  
  private static final ClaimStrategy DEFAULT_CLAIM = new ClaimStrategy() {
    @Override
    public void invoke(Pool<GenericPoolable> pool)
        throws PoolException, InterruptedException {
      pool.claim(DEFAULT_TIMEOUT).release();
    }
  };
  
  private ThreadFactory threadFactory;
  private int workerThreadCount;
  private List<ThreadBuilder> serviceThreads;
  private Deque<ClaimStrategy> claimStrategies;
  private PoolFactory poolFactory;
  private Config<GenericPoolable> config;
  
  public Fuzzer() {
    threadFactory = null;
    serviceThreads = new ArrayList<>();
    config = new Config<GenericPoolable>();
  }
  
  public void setWorkerThreadCount(int count) {
    workerThreadCount = count;
  }
  
  public void addServiceThread(ThreadBuilder serviceThread) {
    serviceThreads.add(serviceThread);
  }
  
  public void addClaimStrategy(ClaimStrategy strategy) {
    claimStrategies.add(strategy);
  }
  
  public void setExpiration(Expiration<GenericPoolable> expiration) {
    config.setExpiration(expiration);
  }
  
  public void setPoolFixture(PoolFactory factory) {
    poolFactory = factory;
  }
  
  public void setAllocator(Allocator<GenericPoolable> allocator) {
    config.setAllocator(allocator);
  }
  
  public void setPoolSize(int size) {
    config.setSize(size);
  }
  
  public void run(long runTimeMillis) throws InterruptedException {
    Pool<GenericPoolable> pool = poolFactory.createPool(config);
    
    List<Thread> threads = new ArrayList<>();
    addAllServiceThreads(pool, threads);
    addAllWorkerThreads(pool, threads);
    
    startAll(threads);
    Thread.sleep(runTimeMillis);
    interruptAll(threads);
    shutDown(pool);
    joinAll(threads);
  }

  private void startAll(List<Thread> threads) {
    for (Thread thread : threads) {
      thread.start();
    }
  }

  private void interruptAll(List<Thread> threads) {
    for (Thread thread : threads) {
      thread.interrupt();
    }
  }

  private void shutDown(Pool<?> pool) throws InterruptedException {
    if (pool instanceof LifecycledPool) {
      ((LifecycledPool<?>) pool).shutdown().await(DEFAULT_TIMEOUT);
    }
  }
  
  private void joinAll(List<Thread> threads) throws InterruptedException {
    for (Thread thread : threads) {
      thread.join();
    }
  }

  private void addAllServiceThreads(
      Pool<GenericPoolable> pool,
      List<Thread> threads) {
    for (ThreadBuilder builder : serviceThreads) {
      threads.add(builder.buildThread(pool));
    }
  }

  private void addAllWorkerThreads(
      Pool<GenericPoolable> pool,
      List<Thread> threads) {
    for (int i = 0; i < workerThreadCount; i++) {
      ClaimStrategy strategy = coalesce(claimStrategies.poll(), DEFAULT_CLAIM);
      threads.add(createWorkerThread(strategy, pool));
    }
    
    if (!claimStrategies.isEmpty()) {
      String msg = "" +
      		"WARNING: Not enough worker threads for all ClaimStrategies." +
      		" %s strategies left!\n";
      System.err.printf(msg, claimStrategies.size());
    }
  }

  private <T> T coalesce(T a, T b) {
    return a == null? b : a;
  }

  private Thread createWorkerThread(
      final ClaimStrategy strategy,
      final Pool<GenericPoolable> pool) {
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        try {
          for (;;) {
            strategy.invoke(pool);
          }
        } catch (PoolException e) {
          e.printStackTrace();
          // TODO we might get this because the pool is shut down.
          // Figure out a nice way to deal with that.
          // Ideally, the fuzzer should run such that we can trust, that any
          // stack trace produced, means that we've encountered a bug.
        } catch (InterruptedException e) {
          // stop signal.
        }
      }
    };
    return threadFactory.newThread(runnable);
  }
}
