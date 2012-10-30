package stormpot.fuzzer;

import stormpot.Pool;

public interface ThreadBuilder {
  public Thread buildThread(Pool<GenericPoolable> pool);
}
