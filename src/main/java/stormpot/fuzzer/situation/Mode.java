package stormpot.fuzzer.situation;

public enum Mode {
  passive, // will not eat up a thread while running
  background, // will require a dedicated service thread for running
  // (a service thread does not count against the thread-count)
  active;  // will require a dedicated thread for running
}