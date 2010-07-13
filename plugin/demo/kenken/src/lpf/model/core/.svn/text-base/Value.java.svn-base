package lpf.model.core;

import java.io.Serializable;

/**
 * Set of operation in Value
 * 
 * @author wanghan
 */
public class Value implements Serializable {
	/** SerialUID **/
	private static final long serialVersionUID = 6888919972137321700L;
	/** value **/
	public final char value;
	
	/**
	 * Set Value with initial value
	 * @param value
	 */
	public Value(char value) {
		this.value = value;
	}

	/**
	 * Check two Values whether they are equal
	 * @param obj	the reference object with which to compare.
	 * @return		true if two Locations have the same row coordinate and column coordinate; 
	 * 				false if otherwise.
	 */
	public boolean equals(Object obj) {
		if (obj instanceof Value) {
			Value otherValue = (Value) obj;
			return this.value == otherValue.value;
		} else {
			return false;
		}
	}
	
	public int hashCode() {
		return value;
	}
	
}
