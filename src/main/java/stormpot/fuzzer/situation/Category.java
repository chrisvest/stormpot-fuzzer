package stormpot.fuzzer.situation;

import static stormpot.fuzzer.situation.Category.Exclusitivity.*;

public enum Category {
  
  threadCount(exclusive),
  poolSize(exclusive),
  condition(inclusive),
  usage(inclusive);
  
  public enum Exclusitivity {
    exclusive, // only one of these can run at a time
    inclusive; // many of these can run at a time
  }
  
  public final Exclusitivity exclusitivity;
  
  private Category(Exclusitivity exclusitivity) {
    this.exclusitivity = exclusitivity;
  }
}
