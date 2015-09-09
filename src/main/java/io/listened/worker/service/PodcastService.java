package io.listened.worker.service;

import com.rometools.modules.itunes.FeedInformation;
import com.rometools.modules.itunes.ITunes;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import io.listened.common.model.podcast.Episode;
import io.listened.common.model.podcast.Podcast;
import io.listened.worker.util.TextUtils;
import org.jsoup.Jsoup;
import org.jsoup.examples.HtmlToPlainText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.List;

/**
 * Created by Clay on 8/23/2015.
 */
@Service
public class PodcastService {

    private static final Logger log = LoggerFactory.getLogger(PodcastService.class);
    @Autowired
    EpisodeService episodeService;
    @Autowired
    RestTemplate restTemplate;

    @Value("${listened.api.url}")
    private String api;

    public void processPodcast(Long podcastId, boolean forceAll) {
        if(forceAll) {
            log.info("Performaing full refresh on {}", podcastId);
        }
        Date processTime = new Date();
        Resource<Podcast> podcastResource = this.getPodcastResource(podcastId);
        Podcast podcast = podcastResource.getContent();
        String podcastLocation = podcastResource.getLink(Link.REL_SELF).getHref();
        podcast.setStatus(Podcast.STATUS_PROCESSING);
        SyndFeed feed;
        try {
            feed = retrieveFeed(podcast.getFeedUrl());
            podcast = mapPodcast(podcast, feed);
            podcast.setStatus(Podcast.STATUS_COMPLETED);
            podcast.setLastProcessed(processTime);
            log.info("Submitting podcast {} to {}", podcastId, api);
            log.debug(podcast.toString());
            restTemplate.put(api + "/podcast/" + podcastId, podcast);
            List<SyndEntry> entries = feed.getEntries();

            for (SyndEntry entry : entries) {
                Episode episode = null;
                String episodeLocation = null;
                Resource<Episode> episodeResource = episodeService.findResourceByGuid(podcastId, entry.getUri());
                if (episodeResource == null) {
                    log.info("Creating new episode for guid {}" + entry.getUri());
                    episode = new Episode();
                    episode.setGuid(entry.getUri());
                    URI episodeUri = restTemplate.postForLocation(api + "/{episode}", episode, "episode");
                    log.info("Got new location uri {}", episodeUri);
                    episodeLocation = episodeUri.toString();
                } else {
                    episodeLocation = episodeResource.getLink(Link.REL_SELF).toString();
                    log.info("Updating existing episode: {}", episodeLocation);
                    episode = episodeResource.getContent();
                }
                episode = episodeService.mapEpisode(entry, episode);
                episode.setLastProcessed(processTime);
                log.debug("Updating episode: {}", episodeLocation);
                restTemplate.put(URI.create(episodeLocation), episode);
                episodeResource = episodeService.getEpisodeResource(episodeLocation);
                episodeService.associateEpisode(podcastLocation, episodeResource.getLink("podcast").getHref());

            }
        } catch (FeedException e) {
            log.error("Unable to retrieve feed {}", podcast.getFeedUrl());
            log.error(e.toString());
            return;
        } catch (IOException e) {
            log.error("Unable to handle feed {}", podcast.getFeedUrl());
            log.error(e.toString());
            return;
        }
        return;

    }

    private Resource<Podcast> getPodcastResource(Long podcastId) {
        String lookupUrl = api + "/podcast/" + podcastId;
        return getPodcastResource(lookupUrl);
    }

    private Resource<Podcast> getPodcastResource(String podcastLocation) {
        ParameterizedTypeReference<Resource<Podcast>> resourceParameterizedTypeReference = new ParameterizedTypeReference<Resource<Podcast>>() {
        };
        try {
            ResponseEntity<Resource<Podcast>> responseEntity = restTemplate.exchange(podcastLocation, HttpMethod.GET,
                    null, resourceParameterizedTypeReference);
            return responseEntity.getBody();
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().is4xxClientError()) {
                return null;
            }
            throw ex;
        }
    }

    public SyndFeed retrieveFeed(String feedUrl) throws FeedException, IOException {
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(new URL(feedUrl)));
        return feed;
    }

    public Podcast mapPodcast(Podcast podcast, SyndFeed feed) {
        Module module = feed.getModule(ITunes.URI);
        FeedInformation info = (FeedInformation) module;
        podcast.setBlock(info.getBlock());
        podcast.setCopyright(TextUtils.removeHtml(feed.getCopyright()));
        podcast.setDescription(TextUtils.removeHtml(feed.getDescription()));
        podcast.setDocs(feed.getDocs());
        podcast.setExplicit(info.getExplicit());
        podcast.setLanguage(feed.getLanguage());
        podcast.setLink(feed.getLink());
        podcast.setPublishDate(feed.getPublishedDate());
        podcast.setSubtitle(TextUtils.removeHtml(info.getSubtitle()));
        podcast.setSummary(TextUtils.removeHtml(info.getSummary()));
        podcast.setTitle(TextUtils.removeHtml(feed.getTitle()));
        return podcast;
    }
}
