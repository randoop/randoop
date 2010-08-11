package randoop.experiments;

public interface StatsComputer {

  /**
   * Process one experiment data record.
   * Return true if no more data should be processed.
   */
  boolean processOneRecord(StatsWriter writer);

}
