package q2p.tagsmanager.kostyak;

import java.util.Comparator;

public final class TagNameComparator implements Comparator<Tag> {
	public static final TagNameComparator INSTANCE = new TagNameComparator();
	private TagNameComparator() {}
	
	public final int compare(final Tag t1, final Tag t2) {
		return t1.name.compareTo(t2.name);
	}
}
