package q2p.tagsmanager.kostyak;

import java.util.Comparator;

public final class TagAmountDescendingComparator implements Comparator<Tag> {
	public static final TagAmountDescendingComparator INSTANCE = new TagAmountDescendingComparator();
	private TagAmountDescendingComparator() {};
	
	public final int compare(final Tag t1, final Tag t2) {
		if(t1.amount == t2.amount)
			return 0;
		
		if(t1.amount > t2.amount)
			return -1;
		
		return 1;
	}
}
