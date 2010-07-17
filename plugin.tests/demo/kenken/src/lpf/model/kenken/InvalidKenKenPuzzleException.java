package lpf.model.kenken;

public class InvalidKenKenPuzzleException extends Exception {
	private static final long serialVersionUID = -156264194610152093L;

	public InvalidKenKenPuzzleException() {
		super();
	}

	public InvalidKenKenPuzzleException(String str) {
		super(str);
	}
}
