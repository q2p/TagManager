package q2p.tagsmanager.engine;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import q2p.tagsmanager.engine.Encoder.BadURL;

public final class Request {
	private static final byte[] buffer = new byte[64*1024*1024]; // 64MB
	private static int pointer;
	private static String stringBuffer;
	private static boolean skipped;
	
	private static final int TIME_OUT = 2000; // TODO: 1000
	private static final long SLEEP_STEP = 10;
	
	public final Method method;
	public static enum Method {
		HEAD("HEAD /"),
		GET("GET /"),
		POST("POST /");
		
		public final String validationText;
		private Method(final String validationText) {
			this.validationText = validationText;
		}
		public static final int longest;
		static {
			int l = 0;
			for(final Method method : values())
				l = Math.max(l, method.validationText.length());
			
			longest = l;
		}
	}
	public final PathChain path;
	public final byte[] body;
	
	private static Method currentMethod;
	private static int contetntLength;
	private static int cli;
	private static boolean ucheckedCli;
	private static boolean received;
	private static int headerSeparator;
	private static int usedBreakes;
	private static int requiredSize;
	
	private static final String[] breaks = new String[] {
		"\n\n","\r\r","\r\n\r\n","\n\r\n\r"
	};
	private static final byte[][] breaksBytes;
	static {
		breaksBytes = new byte[breaks.length][];
		for(byte i = 0; i != breaks.length; i++)
			breaksBytes[i] = breaks[i].getBytes(Assist.ASCII);
	}
	
	public static Request fabric(final InputStream in) throws AnsweringException, IOException {
		pointer = 0;
		int left = buffer.length;
		
		int stringPointer = 0;
		stringBuffer = "";
		
		skipped = false;
		
		currentMethod = null;
		contetntLength = -1;
		cli = -1;
		ucheckedCli = true;
		headerSeparator = -1;
		usedBreakes = -1;
		requiredSize = -1;
		
		received = false;
		
		final long timing = System.currentTimeMillis();
		
		while(left != 0) {
			final int o = in.read(buffer, pointer, Math.min(left, in.available()));
			if(o == -1) {
				validate();
				break;
			} if(o == 0) {
				if(System.currentTimeMillis()-timing > TIME_OUT) {
					skipped = true;
					break;
				}
				if(stringPointer != pointer) {
					stringBuffer = new String(buffer, 0, pointer, Assist.ASCII);
					stringPointer = pointer;
				}
				validate();
				if(skipped || received)
					break;
				
				try{ Thread.sleep(SLEEP_STEP); }
				catch(final InterruptedException e) {}
			} else {
				pointer += o;
				left -= o;
			}
		}
		
		if(skipped) {
			stringBuffer = null;
			return null;
		}

		validate();
		
		if(!received) {
			stringBuffer = null;
			if(left == 0)
				throw new AnsweringException("Запрос больше 64МБ");
			return null;
		}
				
		try {
			return new Request();
		} catch(final AnsweringException e) {
			throw e;
		} catch(final Exception e) {
			return null;
		} finally {
			stringBuffer = null;
		}
	}
	
	private static void validate() {
		if(currentMethod == null) {
			if(stringBuffer.length() >= Method.longest) {
				for(final Method method : Method.values()) {
					if(stringBuffer.startsWith(method.validationText)) {
						currentMethod = method;
						break;
					}
				}
				if(currentMethod == null) {
					skipped = true;
					return;
				}
			}
		}
		if(currentMethod == Method.POST) {
			if(contetntLength == -1) {
				if(cli == -1) {

					cli = findClosest(0, "\rContent-Length: ", "\nContent-Length: ");
					if(cli == -1)
						return;
					cli += "\nContent-Length: ".length();
				}
				if(ucheckedCli) {
					final int breaks = findBreak(stringBuffer);
					if(breaks != -1 && breaks < cli) {
						skipped = true;
						return;
					}
					ucheckedCli = false;
				}
				int clw = findClosest(cli, "\n", "\r");
				
				if(clw == -1)
					return;
				
				try {
					contetntLength = Integer.parseInt(stringBuffer.substring(cli, clw));
				} catch(final NumberFormatException e) {
					e.printStackTrace();
					skipped = true;
					return;
				}
			}
			if(headerSeparator == -1) {
				headerSeparator = findBreak(stringBuffer);
				if(headerSeparator != -1)
					requiredSize = byteIndex(buffer, breaksBytes[usedBreakes])+breaksBytes[usedBreakes].length+contetntLength;
				if(requiredSize > buffer.length) {
					skipped = true;
					return;
				}
			}
			if(requiredSize == pointer)
				received = true;
		} else {
			if(findBreak(stringBuffer) != -1)
				received = true;
		}
	}
	private static int findClosest(final int fromIndex, final String ... strings) {
		int min = -1;
		for(final String s : strings) {
			final int idx = stringBuffer.indexOf(s, fromIndex);
			if(idx != -1) {
				if(min == -1)
					min = idx;
				else
					min = Math.min(min, idx);
			}
		}
		return min;
	}

	private static final int byteIndex(byte[] array, byte[] pattern) {
		for(int i = 0; i != array.length - pattern.length+1; i++) {
			boolean found = true;
			for(int j = 0; j != pattern.length; j++) {
				if (array[i+j] != pattern[j]) {
					found = false;
					break;
				}
			}
			if(found)
				return i;
		}
		return -1;  
	}

	private Request() throws AnsweringException, Exception {
		int idx = findClosest(0, "\r", "\n");
		if(idx == -1)
			throw new IllegalArgumentException();
		
		final LinkedList<String> words = Expressions.split(stringBuffer.substring(0, idx).trim(), " ");
		if(words.size() != 3)
			throw new IllegalArgumentException();
		
		method = currentMethod;
		
		String word = words.removeLast();
		
		if(!word.equals("HTTP/1.1") && !word.equals("HTTP/1.0"))
			throw new IllegalArgumentException();
		
		path = new PathChain(words.removeLast());
				
		if(method == Method.POST)
			body = Arrays.copyOfRange(buffer, headerSeparator, pointer);
		else
			body = new byte[0];
	}

	private static final int findBreak(final String heder) {
		int min = -1;
		int len = 0;
		int ub = -1;
		for(int i = 0; i != breaks.length; i++) {
			final int idx = heder.indexOf(breaks[i]);
			if(idx != -1 && (min == -1 || idx < min)) {
				min = idx;
				len = breaks[i].length();
				ub = i;
			}
		}
		usedBreakes = ub;
		return min+len;
	}

	public static final class PathChain {
		public final String path;
		public final String fullEncodedPath;
		private final Argument[] arguments;
		
		private PathChain(final String uri) throws AnsweringException {
			fullEncodedPath = uri;
	
			final int qidx = fullEncodedPath.indexOf('?');
	
			if(fullEncodedPath.indexOf('?', qidx+1) != -1)
				throw new AnsweringException("URL содержит более одного символа: \"?\"");

			try {
				if(qidx == -1) {
						path = Encoder.decodeURL(fullEncodedPath);
					arguments = new Argument[0];
				} else {
					path = Encoder.decodeURL(fullEncodedPath.substring(0, qidx));
					final LinkedList<String> args = Expressions.split(fullEncodedPath.substring(qidx+1), "&");
					arguments = new Argument[args.size()];
					for(int i = 0; !args.isEmpty(); i++)
						arguments[i] = new Argument(args.removeFirst());
					
					for(int i = arguments.length-1; i != -1; i--) {
						final Argument argument = arguments[i];
						for(int j = i-1; j != -1; j--)
							if(argument.name.equals(arguments[j]))
								throw new AnsweringException("URL содержит повторяющиеся аргументы: \""+argument.name+"\"");
					}
				}
			} catch(final BadURL e) {
				throw new AnsweringException(e.getMessage());
			}
		}
		
		public final Argument[] getArguments() {
			return arguments;
		}
		public final String getArgument(final String name) {
			for(final Argument argument : arguments)
				if(argument.name.equals(name))
						return argument.value;
			
			return null;
		}
		public final String[] limitArguments(final String ... names) {
			if(arguments.length != names.length)
				return null;
			
			final String[] ret = new String[names.length];
			
			int i = 0;
			for(final String name : names) {
				final String insertion = getArgument(name);
				if(insertion == null)
					return null;
				ret[i] = insertion;
				i++;
			}
			
			return ret;
		}
	}
	
	public static final class Argument {
		public final String name;
		public final String value;
		private Argument(final String argument) throws BadURL, AnsweringException {
			final int idx = argument.indexOf("=");
			
			if(argument.indexOf("=", idx+1) != -1)
				throw new AnsweringException("Аргумент в URL содержит более одного символа \"=\"");
			if(idx == -1)
				throw new AnsweringException("Аргумент в URL не содержит значения");

			name = Encoder.decodeURL(argument.substring(0, idx));
			value = Encoder.decodeURL(argument.substring(idx+1));
		}
	}
}