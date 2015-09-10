package io.listened.worker;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import io.listened.common.model.Genre;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
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

	@Autowired
	RestTemplate restTemplate;

	@Value("${listened.api.url}")
	private String api;

	@Test
	public void contextLoads() {
	}

	@Test
	public void cacheTest() throws UnsupportedEncodingException, URISyntaxException {
		Long tv = genreService.getGenreId("TV & Film");
		assert tv != null;
		log.info("TV & Film: {}", tv);

		Long games = genreService.getGenreId("Games & Hobbies");
		assert games != null;

		Long vgs = genreService.getGenreId("Video Games");
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
		Episode e = episodeService.mapEpisode(new Episode(), feed.getEntries().get(0));
		log.info(e.toString());

	}

	@Test
	public void restTemplateResource404() {
		String lookupUrl = api + "/episode/search/findResourceByGuid?guid=12345";
		ParameterizedTypeReference<Resource<Episode>> resourceParameterizedTypeReference = new ParameterizedTypeReference<Resource<Episode>>() {
		};
		try {
			ResponseEntity<Resource<Episode>> responseEntity = restTemplate.exchange(URI.create(lookupUrl), HttpMethod.GET,
					null, resourceParameterizedTypeReference);
		} catch (HttpClientErrorException ex) {
			Assert.isTrue(ex.getStatusCode().value() == 404);
		}
		/**
		 Assert.notNull(responseEntity);
		 Assert.notNull(responseEntity.getBody());
		 Assert.notNull(responseEntity.getBody().getContent());
		 Assert.isTrue(WIDGET_NAME.equals(responseEntity.getBody().getContent().getName()));
		 Assert.isTrue(lookupUrl.equals(responseEntity.getBody().getLink(Link.REL_SELF).getHref()));
		 **/
	}

}
