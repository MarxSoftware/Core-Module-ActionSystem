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
			freq.put(key, new MutableInt());
		} else {
			count.increment();
		}
	}
	
	public int get (final String key) {
		MutableInt count = freq.getOrDefault(key, new MutableInt());
		return count.get();
	}

	public static class MutableInt {

		int value = 1; // note that we start at 1 since we're counting

		public void increment() {
			++value;
		}

		public int get() {
			return value;
		}
	}
}
