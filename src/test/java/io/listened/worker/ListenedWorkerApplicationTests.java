package io.listened.worker;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import io.listened.common.model.podcast.Episode;
import io.listened.common.model.podcast.Podcast;
import io.listened.worker.ListenedWorker;
import io.listened.worker.service.EpisodeService;
import io.listened.worker.service.GenreService;
import io.listened.worker.service.PodcastService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ListenedWorker.class)
@TestPropertySource(locations="classpath:test.properties")
public class ListenedWorkerApplicationTests {

	@Autowired
	GenreService genreService;

	@Autowired
	PodcastService podcastService;

	@Autowired
	EpisodeService episodeService;

	@Test
	public void contextLoads() {
	}

	@Test
	public void cacheTest() throws UnsupportedEncodingException, URISyntaxException {
		String tv = genreService.getGenreLocation("TV & Film");
		assert tv != null;
		log.info("TV & Film: {}", tv);

		String games = genreService.getGenreLocation("Games & Hobbies");
		assert games != null;

		String vgs = genreService.getGenreLocation("Video Games");
		assert vgs != null;

		log.info("TV & Film: {}", tv);
		log.info("Games & Hobbies: {}", games);
		log.info("Video Games: {}", vgs);
	}

	@Test
	public void podcastProcessTest() throws IOException, FeedException, URISyntaxException {
		SyndFeed feed = podcastService.retrieveFeed("http://revolutionspodcast.libsyn.com/rss/");
		Podcast p = new Podcast();
		p = podcastService.mapPodcast(p, feed);
		log.info(p.toString());
		Episode e = episodeService.mapEpisode(feed.getEntries().get(0), new Episode());
		log.info(e.toString());

	}

}
