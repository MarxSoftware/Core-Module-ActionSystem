package com.thorstenmarx.webtools.core.modules.actionsystem.util;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author marx
 */
public class Counter {

	private Map<String, MutableInt> freq = new HashMap<>();

	public void add(final String key) {
		MutableInt count = freq.get(key);
		if (count == null) {
			freq.put(key, new MutableInt(1));
		} else {
			count.increment();
		}
	}
	public void add(final String key, final int value) {
		MutableInt count = freq.get(key);
		if (count == null) {
			freq.put(key, new MutableInt(value));
		} else {
			count.increment(value);
		}
	}
	
	public int get (final String key) {
		MutableInt count = freq.getOrDefault(key, new MutableInt(0));
		return count.get();
	}

	public static class MutableInt {

		int value = 0;// note that we start at 1 since we're counting

		protected MutableInt () {
			this(0);
		}
		protected MutableInt (final int initial) {
			this.value = initial;
		}
		
		public void increment() {
			++value;
		}
		public void increment(final int inc) {
			value += inc;
		}

		public int get() {
			return value;
		}
	}
}
