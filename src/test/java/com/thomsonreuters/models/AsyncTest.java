package com.thomsonreuters.models;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.search.suggest.Lookup;
import org.junit.Test;

import com.thomsonreuters.models.services.async.Job;
import com.thomsonreuters.models.services.async.NamedThreadFactory;
import com.thomsonreuters.models.services.async.WaitingBlockingQueue;
import com.thomsonreuters.models.services.suggesterOperation.DictionaryLoader;
import com.thomsonreuters.models.services.suggesters.BlankSuggester;
import com.thomsonreuters.models.services.util.Blockable;
import com.thomsonreuters.models.services.util.Property; 
import static org.junit.Assert.assertNotNull;

public class AsyncTest {

	@Test
	public void testAsync() {
		
		DictionaryLoader<Lookup> dictionaryReader = new BlankSuggester();
		
		  ExecutorService  reloadExecutor = new ThreadPoolExecutor(1, 2, 0L,
						TimeUnit.MICROSECONDS,
						new WaitingBlockingQueue<Runnable>(),
						new NamedThreadFactory("Suggester"));
		

		Job<Lookup> job1 = new Job<Lookup>(dictionaryReader,
				Property.TYPE.NONE.toString());
		reloadExecutor.execute(job1.inputTask);
		
		Job<Lookup> job2 = new Job<Lookup>(dictionaryReader,
				Property.TYPE.NONE.toString());
		reloadExecutor.execute(job2.inputTask);
		
		Job<Lookup> job3 = new Job<Lookup>(dictionaryReader,
				Property.TYPE.NONE.toString());
		reloadExecutor.execute(job3.inputTask);
		
		Job<Lookup> job4 = new Job<Lookup>(dictionaryReader,
				Property.TYPE.NONE.toString());
		reloadExecutor.execute(job4.inputTask);
		
		 Blockable<String, Lookup> blocakable=dictionaryReader.getSuggesterList();
		 
		 assertNotNull(blocakable);

	}

}
