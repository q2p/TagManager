package q2p.tagsmanager.engine;

import java.util.Iterator;
import java.util.LinkedList;

public final class Profiler {
	public static final LinkedList<Counter> counters = new LinkedList<Counter>();

	public static final void push(final String name, final boolean recording, final String flushDescription) {
		counters.addFirst(new Counter(name, recording, flushDescription));
	}
	
	public static final long pull(final String name) {
		final Iterator<Counter> i = counters.iterator();
		while(i.hasNext()) {
			final Counter counter = i.next();
			if(counter.name.equals(name)) {
				i.remove();
				return counter.collect();
			}
		}
		return -1;
	}
	
	public static final void pullFlush(final String name) {
		final Iterator<Counter> i = counters.iterator();
		while(i.hasNext()) {
			final Counter counter = i.next();
			if(counter.name.equals(name)) {
				i.remove();
				System.out.println(counter.flushDescription+": "+counter.collect());
				return;
			}
		}
		System.out.println("Счётчик \""+name+"\" не был найден.");
	}
	
	public static final long check(final String name) {
		for(final Counter counter : counters)
			if(counter.name.equals(name))
				return counter.collect();
		
		return -1;
	}
	
	public static final void checkFlush(final String name) {
		for(final Counter counter : counters) {
			if(counter.name.equals(name)) {
				System.out.println(counter.flushDescription+": "+counter.collect());
				return;
			}
		}

		System.out.println("Счётчик \""+name+"\" не был найден.");
	}

	public static final void pause(final String name) {
		for(final Counter counter : counters) {
			if(counter.name.equals(name)) {
				counter.pause();
				return;
			}
		}
	}

	public static final void resume(final String name) {
		for(final Counter counter : counters) {
			if(counter.name.equals(name)) {
				counter.resume();
				return;
			}
		}
	}
	
	private static final class Counter {
		private final String name;
		private long timeHolded = 0;
		private long ticker = -1;
		private final String flushDescription;
		private Counter(final String name, final boolean recording, final String flushDescription) {
			this.name = name;
			this.flushDescription = flushDescription;
			if(recording)
				ticker = System.currentTimeMillis();
		}
		public void resume() {
			if(ticker == -1)
				ticker = System.currentTimeMillis();
		}
		public void pause() {
			if(ticker != -1) {
				timeHolded += System.currentTimeMillis() - ticker;
				ticker = -1;
			}
		}
		private long collect() {
			if(ticker == -1)
				return timeHolded;
			
			return System.currentTimeMillis() - ticker + timeHolded;
		}
	}
}