package org.jboss.ddoyle.drools.model;

import java.util.Date;
import java.util.UUID;

public class SimpleEvent {
	
	private final long timestamp;
	
	private long technicalTimestamp;
	
	private final String id;
	
	public SimpleEvent() {
		this(UUID.randomUUID().toString(), new Date().getTime());
	}
	
	public SimpleEvent(final String id) {
		this(id, new Date().getTime());
	}
	
	public SimpleEvent(final long timestamp) {
		this(UUID.randomUUID().toString(), timestamp);
		
	}
	
	public SimpleEvent(final String id, final long timestamp) {
		this.id = id;
		this.timestamp = timestamp;
		this.technicalTimestamp = timestamp;
	}
	
	public SimpleEvent(final String id, final long timestamp, final long technicalTimestamp) {
		this.id = id;
		this.timestamp = timestamp;
		this.technicalTimestamp = technicalTimestamp;
	}


	public long getTimestamp() {
		return timestamp;
	}

	public String getId() {
		return id;
	}

	public long getTechnicalTimestamp() {
		return technicalTimestamp;
	}

	public void setTechnicalTimestamp(long technicalTimestamp) {
		this.technicalTimestamp = technicalTimestamp;
	}
	
}
