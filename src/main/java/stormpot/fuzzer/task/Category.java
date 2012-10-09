package stormpot.fuzzer.task;

public enum Category {
  threadCount(true),
  poolSize(true),
  experiment(false);
  
  /**
   * True if only a single one of these tasks can run at a time.
   */
  public final boolean exclusive;
  
  private Category(boolean exclusive) {
    this.exclusive = exclusive;
  }
}
