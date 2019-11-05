package com.thorstenmarx.webtools.core.modules.actionsystem.segmentStore;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.thorstenmarx.webtools.api.cache.CacheLayer;
import com.thorstenmarx.webtools.api.datalayer.SegmentData;
import com.thorstenmarx.webtools.core.modules.actionsystem.CacheKey;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Anforderungen an den Segmente Store:
 *
 * Entfernen: - pro Benutzer - Pro Segmente
 *
 * @author marx
 */
public class LocalUserSegmentStore implements UserSegmentStore {

	private final CacheLayer cachelayer;

	private final KeyLookup segmentLookup;
	private final KeyLookup userLookup;

	private Lock lock = new ReentrantLock();

	public LocalUserSegmentStore(final CacheLayer cachelayer) {
		this.cachelayer = cachelayer;

		segmentLookup = new KeyLookup();
		userLookup = new KeyLookup();
	}

	@Override
	public void lock() {
		lock.lock();
	}

	@Override
	public void unlock() {
		lock.unlock();
	}

	@Override
	public void add(final String userid, final SegmentData segmentData) {
		final String identifier = CacheKey.key(userid, SegmentData.KEY, segmentData.getSegment());

		userLookup.add(userid, identifier);
		segmentLookup.add(segmentData.getSegment().id, identifier);
		cachelayer.add(identifier, segmentData, 5, TimeUnit.MINUTES);
	}

	@Override
	public List<SegmentData> get(final String userid) {
		lock.lock();

		try {
			List<SegmentData> result = new ArrayList<>();
			userLookup.getUUIDs(userid).forEach((identifier) -> {
				var segmentData = cachelayer.get(identifier, SegmentData.class);
				if (segmentData.isPresent()) {
					result.add(segmentData.get());
				} else {
					clearLookups(identifier);
				}
			});
			return result;
		} finally {
			lock.unlock();
		}

		
	}

	private void clearLookups(final String identifier) {
		final String[] splitted = CacheKey.split(identifier);
		userLookup.remove(splitted[0], identifier);
		segmentLookup.remove(splitted[2], identifier);
	}

	@Override
	public void removeByUser(final String user_id) {
		final Collection<String> uuiDs = userLookup.getUUIDs(user_id);

		uuiDs.forEach((identifier) -> {
			final String[] splitted = CacheKey.split(identifier);
			cachelayer.invalidate(identifier);

			userLookup.remove(user_id, identifier);
			segmentLookup.remove(splitted[2], identifier);

		});
	}

	@Override
	public void removeBySegment(final String segment_id) {
		segmentLookup.getUUIDs(segment_id).forEach((identifier) -> {
			final String[] splitted = CacheKey.split(identifier);
			cachelayer.invalidate(identifier);

			segmentLookup.remove(segment_id, identifier);
			userLookup.remove(splitted[0], identifier);
		});
	}

	private class KeyLookup {

		private final Multimap<String, String> key_uuid_mapping = Multimaps.synchronizedMultimap(ArrayListMultimap.create());

		ReadWriteLock lock = new ReentrantReadWriteLock();
		Lock writeLock = lock.writeLock();

		public KeyLookup() {

		}

		public void add(final String key, final String uuid) {
			key_uuid_mapping.put(key, uuid);
		}

		public void remove(final String key, final String uuid) {
			try {
				writeLock.lock();

				if (key_uuid_mapping.containsKey(key)) {
					key_uuid_mapping.remove(key, uuid);
					if (key_uuid_mapping.get(key).isEmpty()) {
						_removeAll(key);
					}
				}
			} finally {
				writeLock.unlock();
			}
		}

		public void removeAll(final String key) {
			try {
				writeLock.lock();

				key_uuid_mapping.removeAll(key);
			} finally {
				writeLock.unlock();
			}
		}

		public void _removeAll(final String key) {
			key_uuid_mapping.removeAll(key);
		}

		public Collection<String> getUUIDs(final String key) {
			if (key_uuid_mapping.containsKey(key)) {
				return new ArrayList<>(key_uuid_mapping.get(key));
			}
			return Collections.EMPTY_LIST;
		}
	}
}
