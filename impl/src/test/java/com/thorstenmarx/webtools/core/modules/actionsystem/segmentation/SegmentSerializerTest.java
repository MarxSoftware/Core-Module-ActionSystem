package com.thorstenmarx.webtools.actions.segmentation;

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

import com.thorstenmarx.webtools.api.actions.model.Segment;
import com.thorstenmarx.webtools.api.model.Pair;
import com.thorstenmarx.webtools.core.modules.actionsystem.segmentation.SegmentSerializer;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;


/**
 *
 * @author marx
 */
public class SegmentSerializerTest {
	
	public SegmentSerializerTest() {
	}

	@Test
	public void test_segment() {
		SegmentSerializer serializer = new SegmentSerializer();
		
		Segment segment = new Segment();
		
		final Pair<String, String> pair = serializer.serialize(segment);
		Assertions.assertThat(pair.left).isEqualTo(SegmentSerializer.VERSION_SEGMENT);
		
		final Pair<String, Segment> segmentPair = serializer.deserialize(SegmentSerializer.VERSION_SEGMENT, pair.right);
		Assertions.assertThat(segmentPair.right).isNotNull();
		Assertions.assertThat(segmentPair.right).isInstanceOf(Segment.class).isInstanceOf(Segment.class);
	}
	
}
