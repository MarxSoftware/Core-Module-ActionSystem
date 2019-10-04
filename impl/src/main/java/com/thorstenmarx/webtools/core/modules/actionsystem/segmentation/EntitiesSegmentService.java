package com.thorstenmarx.webtools.core.modules.actionsystem.segmentation;

/*-
 * #%L
 * webtools-actions
 * %%
 * Copyright (C) 2016 - 2018 Thorsten Marx
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.thorstenmarx.webtools.api.actions.SegmentService;
import com.thorstenmarx.webtools.api.actions.model.Segment;
import com.thorstenmarx.webtools.api.entities.Entities;
import com.thorstenmarx.webtools.api.entities.Store;
import com.thorstenmarx.webtools.api.entities.criteria.Criteria;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author marx
 */
public class EntitiesSegmentService implements SegmentService {

	private final List<ChangedEventListener> listeners = new CopyOnWriteArrayList<>();
	
	
	private Store<Segment> store;
	
	public EntitiesSegmentService (final Entities entities) {
		store = entities.store(Segment.class, new SegmentSerializer());
	}
	
	@Override
	public void add(final Segment segment) {
		store.save(segment);
	}

	@Override
	public Criteria<Segment> criteria () {
		return store.criteria();
	}

	@Override
	public Collection<Segment> all() {
		return store.list(0, Integer.MAX_VALUE);
	}

	@Override
	public Segment get(String id) {
		return store.get(id);
	}

	@Override
	public void remove(String id) {
		Segment segment = store.get(id);
		if (segment != null) {
			store.delete(segment);
		}
	}

		@Override
	public synchronized void addEventListener(final ChangedEventListener listener) {
		listeners.add(listener);
	}

	@Override
	public synchronized void removeEventListener(final ChangedEventListener listener) {
		listeners.remove(listener);
	}
	private synchronized void fireEvent(ChangedEvent event) {
		listeners.forEach((eh) -> {
			eh.changed(event);
		});
	}
	
}
