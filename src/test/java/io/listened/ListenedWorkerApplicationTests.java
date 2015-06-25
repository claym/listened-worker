package io.listened;

import io.listened.worker.ListenedWorker;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ListenedWorker.class)
public class ListenedWorkerApplicationTests {

	@Test
	public void contextLoads() {
	}

}
