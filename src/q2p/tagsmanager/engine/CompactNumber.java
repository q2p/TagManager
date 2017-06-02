package q2p.tagsmanager.engine;

public class CompactNumber {
	private static final String digits = "0123456789abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNOPQRSTUVWXYZ";

	public static final String to(int decimical) {
		final char[] ret = new char[6];
		
		byte i = 0;
		do {
			ret[i++] = digits.charAt(decimical % 60);
			decimical /= 60;
		} while(decimical != 0);
		
		return new StringBuilder().append(ret, 0, i).reverse().toString();
	}
	
	public static final int from(final String compact) {
		int length = compact.length();
		
		if(length > 6 || length == 0)
			return -1;
		
		int ret = 0;
		
		int power = 1;
		
		byte idx;
		do {
			idx = (byte)digits.indexOf(compact.charAt(--length));
			if(idx == -1)
				return -1;
			
			ret += idx * power;
			
			if(ret < 0)
				return -1;
			power *= 60;
		} while(length != -1);
		
		return ret;
	}
}