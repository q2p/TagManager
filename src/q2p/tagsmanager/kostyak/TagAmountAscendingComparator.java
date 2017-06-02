package q2p.tagsmanager.kostyak;

import java.util.Comparator;

public final class TagAmountAscendingComparator implements Comparator<Tag> {
	public static final TagAmountAscendingComparator INSTANCE = new TagAmountAscendingComparator();
	private TagAmountAscendingComparator() {};
	
	public final int compare(final Tag t1, final Tag t2) {
		if(t1.amount == t2.amount)
			return 0;
		
		if(t1.amount > t2.amount)
			return 1;
		
		return -1;
	}
}
