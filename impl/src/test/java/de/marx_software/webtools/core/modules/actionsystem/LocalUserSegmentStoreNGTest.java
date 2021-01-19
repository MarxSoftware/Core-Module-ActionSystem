/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.marx_software.webtools.core.modules.actionsystem;

import de.marx_software.webtools.api.datalayer.SegmentData;
import de.marx_software.webtools.core.modules.actionsystem.segmentStore.LocalUserSegmentStore;
import de.marx_software.webtools.test.MockCacheLayer;
import org.assertj.core.api.Assertions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author marx
 */
public class LocalUserSegmentStoreNGTest {

	LocalUserSegmentStore userSegmentStore;

	@BeforeMethod
	public void setUpClass() throws Exception {
		userSegmentStore = new LocalUserSegmentStore(new MockCacheLayer());
		
		final var segmentData1 = new SegmentData();
		segmentData1.setSegment(new SegmentData.Segment("test1", 1, "1"));
		final var segmentData2 = new SegmentData();
		segmentData2.setSegment(new SegmentData.Segment("test2", 2, "2"));
		userSegmentStore.add("testuser", segmentData1);
		userSegmentStore.add("testuser", segmentData2);
	}

	@AfterClass
	public void tearDownClass() throws Exception {
		
	}

	@Test
	public void test_size() {
		Assertions.assertThat(userSegmentStore.get("testuser")).isNotEmpty().hasSize(2);
	}
	@Test
	public void test_remove_by_user() {
		userSegmentStore.removeByUser("testuser");
		Assertions.assertThat(userSegmentStore.get("testuser")).isEmpty();
	}
	@Test
	public void test_remove_by_segment() {
		userSegmentStore.removeBySegment("1");
		Assertions.assertThat(userSegmentStore.get("testuser")).isNotEmpty().hasSize(1);
	}
	
}
