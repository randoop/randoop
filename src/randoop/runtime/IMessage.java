package randoop.runtime;

import java.io.Serializable;

/**
 * IMessages are messages created by Randoop during generation, to inform
 * clients of any relevant information; example messages are whether the generation
 * has started or stopped, percentage done, the names of any files created, or
 * whether an error in the classes under test has been discovered.
 */
public interface IMessage extends Serializable {

}