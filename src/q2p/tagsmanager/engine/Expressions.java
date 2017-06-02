package q2p.tagsmanager.engine;

import java.util.LinkedList;

public class Expressions {
	public static final LinkedList<String> tokenize(final String string, final String[] operators) {
		final LinkedList<String> ret = new LinkedList<String>();
				
		int pidx = 0;

		int nextIdx = -1;
		int nextLength = -1;
		while(true) {
			for(final String o : operators) {
				final int idx = string.indexOf(o, pidx);
				if(idx == -1)
					continue;
				
				if(nextIdx == -1 || idx < nextIdx) {
					nextIdx = idx;
					nextLength = o.length();
				} else if(idx == nextIdx) {
					nextLength = Math.max(nextLength, o.length());
				}
			}
			if(nextIdx == -1) {
				if(pidx != string.length())
					ret.addLast(string.substring(pidx));
				break;
			}

			if(pidx != nextIdx)
				ret.addLast(string.substring(pidx, nextIdx));
			
			ret.addLast(string.substring(nextIdx, nextIdx+nextLength));
			
			pidx = nextIdx + nextLength;
			nextIdx = -1;
			nextLength = -1;
		}
		
		return ret;
	}

	public static boolean startsWith(final String string, final char pattern) {
		return string.length() != 0 && string.charAt(0) == pattern;
	}

	public static boolean equals(String string, char character) {
		return string.length() == 1 && string.charAt(0) == character;
	}
	
	public static boolean match(final char c, final char[] tokens) {
		for(int i = tokens.length-1; i != -1; i--)
			if(c == tokens[i])
				return true;
		
		return false;
	}
	public static boolean match(final char c, final String tokens) {
		return tokens.indexOf(c) != -1;
	}

	public static final LinkedList<String> split(final String string, final char pattern) {
		final LinkedList<String> ret = new LinkedList<String>();
		
		int pidx = 0;
		int idx;
		while(true) {
			idx = string.indexOf(pattern, pidx);
			if(idx == -1) {
				ret.addLast(string.substring(pidx));
				break;
			}
			ret.addLast(string.substring(pidx, idx));
			pidx = idx+1;
		}
		
		return ret;
	}
	public static final LinkedList<String> split(final String string, final String pattern) {
		final LinkedList<String> ret = new LinkedList<String>();
		
		int pidx = 0;
		int idx;
		while(true) {
			idx = string.indexOf(pattern, pidx);
			if(idx == -1) {
				ret.addLast(string.substring(pidx));
				break;
			}
			ret.addLast(string.substring(pidx, idx));
			pidx = idx+pattern.length();
		}
		
		return ret;
	}

	public static final boolean isFiltered(final char[] filter, final String string) {
		for(int i = string.length()-1; i != -1; i--) {
			final char c = string.charAt(i);
			boolean missed = true;
			for(int j = filter.length-1; j != -1; j--) {
				if(c == filter[j]) {
					missed = false;
					break;
				}
			}
			if(missed)
				return false;
		}
		return true;
	}
	public static final boolean isFiltered(final String filter, final String string) {
		for(int i = string.length()-1; i != -1; i--)
			if(filter.indexOf(string.charAt(i)) == -1)
				return false;
			
		return true;
	}

	public static final int amountOf(final String string, final char pattern) {
		int ret = 0;
		
		int pidx = 0;
		int idx;
		while(true) {
			idx = string.indexOf(pattern, pidx);
			if(idx == -1)
				return ret;
			
			ret++;
			pidx = idx+1;
		}
	}
	public static final int amountOf(final String string, final String pattern) {
		int ret = 0;
		
		int pidx = 0;
		int idx;
		while(true) {
			idx = string.indexOf(pattern, pidx);
			if(idx == -1)
				return ret;
			
			ret++;
			pidx = idx+pattern.length()+1;
		}
	}
}