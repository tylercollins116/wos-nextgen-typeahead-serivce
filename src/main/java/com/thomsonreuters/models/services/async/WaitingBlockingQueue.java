package com.thomsonreuters.models.services.async;

import java.util.concurrent.ArrayBlockingQueue;

public class WaitingBlockingQueue<E> extends ArrayBlockingQueue<E> {
	private static final long serialVersionUID = 1L;

	public WaitingBlockingQueue() {
		super(12, true);
	}

	public boolean offer(E e) {
		try {
			put(e);
			return true;
		} catch (InterruptedException e1) {
			return false;
		}
	}
}
