package q2p.tagsmanager.engine;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public final class Encoder {
	public static final String encodeHTML(final String string) {
		final StringBuilder builder = new StringBuilder();
		encodeHTML(builder, string);
		return builder.toString();
	}
	public static final void encodeHTML(final StringBuilder builder, final String string) {
		final int to = string.length();
		for (int i = 0; i != to; i++) {
			final char c = string.charAt(i);
			if(c > 127 || c == '"' || c == '<' || c == '>' || c == '&') {
				builder.append("&#");
				builder.append((int) c);
				builder.append(';');
			} else
				builder.append(c);
		}
	}
	public static final String encodeJSON(final String string) {
		final StringBuilder builder = new StringBuilder();
		encodeHTML(builder, string);
		return builder.toString();
	}
	public static final void encodeJSON(final StringBuilder builder, final String string) {
		final int to = string.length();
		
		for(int i = 0; i != to; i++) {
			final char c = string.charAt(i);
			if(string.charAt(i) == '\n')
				builder.append("\\n");
			else if(string.charAt(i) == '\"')
				builder.append("\\\"");
			else if(string.charAt(i) == '\\')
				builder.append("\\\\");
			else
				builder.append(c);
		}
	}
	public static final String encodeJS(final String string) {
		final StringBuilder builder = new StringBuilder();
		encodeJS(builder, string);
		return builder.toString();
	}
	public static final void encodeJS(final StringBuilder builder, final String string) {
		final int to = string.length();
		
		for(int i = 0; i != to; i++) {
			final char c = string.charAt(i);
			if(c == '\'')
				builder.append("\\\'");
			else if(string.charAt(i) == '\"')
				builder.append("\\\"");
			else if(string.charAt(i) == '\\')
				builder.append("\\\\");
			else if(string.charAt(i) == '\t')
				builder.append("\\t");
			else
				builder.append(c);
		}
	}
	public static final void arrayJS(final StringBuilder builder, final String ... elements) {
		builder.append('[');
		boolean needBreak = false;
		for(final String element : elements) {
			if(needBreak)
				builder.append(',');
			else
				needBreak = true;
			builder.append(element);
		}
		builder.append(']');
	}
	public static final String encodeURL(final String string) {
		try {
			return URLEncoder.encode(string, "UTF-8");
		} catch(final UnsupportedEncodingException e) {}
		return null;
	}
	public static final String decodeURL(final String string) throws BadURL {
		try {
			return URLDecoder.decode(string, "UTF-8");
		} catch(final IllegalArgumentException e) {
			throw new BadURL(e.getMessage().substring(urlExceptionLength, urlExceptionLength+2));
		} catch(final UnsupportedEncodingException e) {}
		return null;
	}
	private static final int urlExceptionLength = "URLDecoder: Illegal hex characters in escape (%) pattern - For input string: \"".length();
	@SuppressWarnings("serial")
	public static final class BadURL extends Exception {
		public final String sequence;
		private BadURL(final String sequence) {
			super("URL содержит не допустимую последовательность символов кодировки: \""+sequence+"\"");
			this.sequence = sequence;
		}
	}
}