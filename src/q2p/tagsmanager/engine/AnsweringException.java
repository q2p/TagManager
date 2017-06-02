package q2p.tagsmanager.engine;

@SuppressWarnings("serial")
public final class AnsweringException extends Exception {
	public final String message;
	
	public AnsweringException(final String message) {
		super(message);
		this.message = message;
	}
}
