package q2p.tagsmanager.engine;

import java.awt.Dimension;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Random;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public final class Assist {
	public static final Charset UTF_8 = StandardCharsets.UTF_8;
	public static final Charset ASCII = StandardCharsets.US_ASCII;
	
	private static final Random random = new Random();
	public static final int random(final int bound) {
		return random.nextInt(bound);
	}

	public static final void writePartialyAndFlush(final OutputStream outputStream, final int bufferSize, final byte[] data) throws IOException {
		int offset = 0;
		int left = data.length;
		while(left != 0) {
			final int length = Math.min(bufferSize, left);
			outputStream.write(data, offset, length);
			outputStream.flush();
			offset += length;
			left -= length;
		}
	}
	public static final void writePartialyAndFlush(final InputStream inputStream, final OutputStream outputStream, final int bufferSize) throws IOException {
		writePartialyAndFlush(inputStream, outputStream, new byte[bufferSize]);
	}
	public static final void writePartialyAndFlush(final InputStream inputStream, final OutputStream outputStream, final byte[] buffer) throws IOException {
		while(inputStream.available() > 0) {
			final int length = inputStream.read(buffer);
			outputStream.write(buffer, 0, length);
			outputStream.flush();
		}
	}
	
	private static final LinkedList<Closeable> closeables = new LinkedList<Closeable>();
	public static final void storeCloseable(final Closeable closeable) {
		if(closeable == null)
			throw new NullPointerException();
		synchronized(closeables) {
			if(!closeables.contains(closeable))
				closeables.addFirst(closeable);
		}
	}
	public static final void storeCloseable(final Closeable ... closeable) {
		if(closeable == null)
			throw new NullPointerException();
		synchronized(closeables) {
			for(final Closeable c : closeable) {
				if(c == null)
					throw new NullPointerException();
				if(!closeables.contains(c))
					closeables.addFirst(c);
			}
		}
	}
	public static final void freeCloseable(final Closeable closeable) {
		synchronized(closeables) {
			closeables.removeFirstOccurrence(closeable);
		}
	}
	public static final void freeCloseable(final Closeable ... closeable) {
		synchronized(closeables) {
			for(final Closeable c : closeable)
				closeables.removeFirstOccurrence(c);
		}
	}
	public static final void abort(final String reason, final Exception exception) {
		abort(reason+'\n'+exception.getMessage());
	}
	public static final void abort(final String reason) {
		finalizeOnClose();
		try {
			final JTextArea textArea = new JTextArea(reason);
			textArea.setEditable(false);
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
			JScrollPane scrollPane = new JScrollPane(textArea);
			scrollPane.setPreferredSize(new Dimension(480, 320));
			JOptionPane.showMessageDialog(null, scrollPane, "Ошибка", JOptionPane.ERROR_MESSAGE);
		} catch(final Exception e) {
			System.out.println(reason);
		}
		exit(1);
	}
	private static final void finalizeOnClose() {
		synchronized(closeables) {
			while(!closeables.isEmpty())
				safeClose(closeables.removeLast());
		}
	}
	static final void exit(final int exitCode) {
		finalizeOnClose();
		System.exit(exitCode);
	}

	public static final boolean contains(final String checkToLowerCase, final String containsLowerCase) {
		return checkToLowerCase.toLowerCase().contains(containsLowerCase);
	}
	
	public static final void safeClose(final Closeable closeable) {
		if(closeable != null) {
			try { closeable.close(); }
			catch(final Exception e) {}
			Assist.freeCloseable(closeable);
		}
	}
	public static final void safeClose(final Closeable ... closeable) {
		for(final Closeable c : closeable) {
			if(c != null) {
				try { c.close(); }
				catch(final Exception e) {}
				Assist.freeCloseable(c);
			}
		}
	}
	
	public static char[] appendChars(final int length, final String original, final char filler) {
		final char[] chars = new char[length];
		int i = length-1;
		
		for(int j = original.length()-1; i != -1 && j != -1; i--, j--)
			chars[i] = original.charAt(j);
		for(;i != -1; i--)
			chars[i] = filler;
		
		return chars;
	}
	
	public static int perfectCeil(final int number, final int devisor) {
		return number/devisor+(number%devisor==0?0:1);
	}
}