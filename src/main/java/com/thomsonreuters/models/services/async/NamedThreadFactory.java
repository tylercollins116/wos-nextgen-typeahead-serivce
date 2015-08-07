package com.thomsonreuters.models.services.async;

import java.util.concurrent.ThreadFactory;

public class NamedThreadFactory implements ThreadFactory {

	private final String name;
	int threadCount;

	public NamedThreadFactory(String name) {
		this.name = name;
	}

	@Override
	public Thread newThread(Runnable r) {
		return new Thread(r, name + "-" + threadCount++);
	}
}