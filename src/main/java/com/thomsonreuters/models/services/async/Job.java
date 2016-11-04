package com.thomsonreuters.models.services.async;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import com.thomsonreuters.models.services.suggesterOperation.DictionaryLoader;
import com.thomsonreuters.models.services.util.GroupTerms;
import com.thomsonreuters.models.services.util.Property;
 



public class Job<K> {

	private final DictionaryLoader<K> suggesterList;
	private final String propertyName;
	private final Property property=new GroupTerms();
	

	public FutureTask<K> inputTask;

	public Job(DictionaryLoader<K> suggesterList, String propertyName) {
		this.suggesterList = suggesterList;
		this.propertyName = propertyName;
		inputTask = new FutureTask<K>(new PostRequest(this));
	}

	class PostRequest implements Callable<K> {

		private Job<K> job;

		public PostRequest(Job<K> job) {
			this.job = job;
		}

		@Override
		public K call() throws Exception {

			recall();
			return null;
		}

		public void recall() {
			 

			if (property.isBucketName(propertyName)) {
				try {
					job.suggesterList.initializeSuggesterList();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else if (property.isDictionaryRelated(propertyName)) {
				try {
					job.suggesterList.reloadDictionary(job.propertyName);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}

}
