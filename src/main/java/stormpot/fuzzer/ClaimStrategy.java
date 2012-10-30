package stormpot.fuzzer;

import stormpot.Pool;
import stormpot.PoolException;

public interface ClaimStrategy {
  public void invoke(Pool<GenericPoolable> pool)
      throws PoolException, InterruptedException;
}
