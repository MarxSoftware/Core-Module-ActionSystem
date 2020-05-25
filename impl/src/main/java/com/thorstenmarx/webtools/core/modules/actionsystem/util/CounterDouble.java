package com.thorstenmarx.webtools.core.modules.actionsystem.util;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author marx
 */
public class CounterDouble {

	private Map<String, MutableDouble> freq = new HashMap<>();

	public void add(final String key) {
		MutableDouble count = freq.get(key);
		if (count == null) {
			freq.put(key, new MutableDouble(1));
		} else {
			count.increment();
		}
	}
	public void add(final String key, final double value) {
		MutableDouble count = freq.get(key);
		if (count == null) {
			freq.put(key, new MutableDouble(value));
		} else {
			count.increment(value);
		}
	}
	
	public double get (final String key) {
		MutableDouble count = freq.getOrDefault(key, new MutableDouble(0));
		return count.get();
	}

	public static class MutableDouble {

		double value = 0;// note that we start at 1 since we're counting

		protected MutableDouble () {
			this(0);
		}
		protected MutableDouble (final double initial) {
			this.value = initial;
		}
		
		public void increment() {
			++value;
		}
		public void increment(final double inc) {
			value += inc;
		}

		public double get() {
			return value;
		}
	}
}
