package lpf.model.core;

import java.util.ArrayList;
import java.util.Iterator;


public class CellsIterator implements Iterator<Cell> {
	/** State of iteration. */
	int idx;
	
	int max;
	
	ArrayList<Cell> arr = new ArrayList<Cell>();
	
	/** Construct iterator object. */
	public CellsIterator (Cell[][] cells, int w, int h) {
		for (int i = 0; i < w; i++ ) {
			for (int j = 0; j < h; j++) {
				arr.add(cells[i][j]);
			}
		}
		
		this.idx = 0;
		this.max = w * h; 
	}
	
	@Override
	public boolean hasNext() {
		return (idx < max);
	}

	@Override
	public Cell next() {
		if (idx < max) {
			Cell val = arr.get(idx++);
			return val;
		}
		return null;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Unable to remove values from underlying array.");
	}
	
}
