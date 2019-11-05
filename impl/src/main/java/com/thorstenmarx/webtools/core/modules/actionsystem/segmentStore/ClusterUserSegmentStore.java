/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thorstenmarx.webtools.core.modules.actionsystem.segmentStore;

import com.google.gson.Gson;
import com.thorstenmarx.webtools.api.cluster.Cluster;
import com.thorstenmarx.webtools.api.cluster.Message;
import com.thorstenmarx.webtools.api.cluster.services.MessageService;
import com.thorstenmarx.webtools.api.datalayer.SegmentData;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author marx
 */
public class ClusterUserSegmentStore implements UserSegmentStore, MessageService.MessageListener{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ClusterUserSegmentStore.class);
	
	Gson gson = new Gson();

	private static final String ADD = "usersegmentstore_add";
	private static final String REMOVE_BY_USER = "usersegmentstore_remove_by_user";
	private static final String REMOVE_BY_SEGMENT = "usersegmentstore_remove_by_segment";
	
	final UserSegmentStore localUserSegmentStore;
	final Cluster cluster;

	public ClusterUserSegmentStore(final UserSegmentStore localUserSegmentStore, final Cluster cluster) {
		this.localUserSegmentStore = localUserSegmentStore;
		this.cluster = cluster;
		
		cluster.getMessageService().registerMessageListener(this);
	}
	
	public void close () {
		cluster.getMessageService().unregisterMessageListener(this);
	}
	
	
	
	@Override
	public void add(final String userid, final SegmentData segmentData) {
		localUserSegmentStore.add(userid, segmentData);
		
		AddPayload payload = new AddPayload();
		payload.segmentData = segmentData;
		payload.userid = userid;
		
		sendMessage(ADD, payload);
	}

	@Override
	public List<SegmentData> get(final String userid) {
		return this.localUserSegmentStore.get(userid);
	}

	@Override
	public void lock() {
		this.localUserSegmentStore.lock();
	}

	@Override
	public void removeBySegment(final String segment_id) {
		localUserSegmentStore.removeBySegment(segment_id);
		
		RemoveBySegmentPayload payload = new RemoveBySegmentPayload();
		payload.segmentid = segment_id;
		sendMessage(REMOVE_BY_SEGMENT, payload);
			
	}

	@Override
	public void removeByUser(final String user_id) {
		localUserSegmentStore.removeByUser(user_id);
		
		RemoveByUserPayload payload = new RemoveByUserPayload();
		payload.userid = user_id;
		sendMessage(REMOVE_BY_USER, payload);
	}

	@Override
	public void unlock() {
		this.localUserSegmentStore.unlock();
	}

	@Override
	public void handle(final Message message) {
		if (ADD.equals(message.getType())){
			AddPayload payload = gson.fromJson(message.getPayload(), AddPayload.class);
			localUserSegmentStore.add(payload.userid, payload.segmentData);
		} else if (REMOVE_BY_SEGMENT.equals(message.getType())) {
			RemoveBySegmentPayload payload = gson.fromJson(message.getPayload(), RemoveBySegmentPayload.class);
			localUserSegmentStore.removeBySegment(payload.segmentid);
		} else if (REMOVE_BY_USER.equals(message.getType())) {
			RemoveByUserPayload payload = gson.fromJson(message.getPayload(), RemoveByUserPayload.class);
			localUserSegmentStore.removeByUser(payload.userid);
		}
	}
	
	private void sendMessage (final String type, final Object payload) {
		Message message = new Message();
		message.setType(type);
		message.setPayload(gson.toJson(payload));
		try {	
			cluster.getMessageService().publish(message);
		} catch (IOException ex) {
			LOGGER.error("", ex);
		}
	}
	
	
	public static class AddPayload {
		String userid; 
		SegmentData segmentData;
	}

	public static class RemoveByUserPayload {
		String userid;
	}
	public static class RemoveBySegmentPayload {
		String segmentid;
	}
	
}
