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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
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

        Podcast podcast = restTemplate.getForObject(api + "/podcast/{podcastId}", Podcast.class, podcastId);
        podcast.setStatus(Podcast.STATUS_PROCESSING);
        SyndFeed feed;
        try {
            feed = retrieveFeed(podcast.getFeedUrl());
            podcast = mapPodcast(podcast, feed);
            log.info("Submitting podcast {} to {}", podcast.getId(), api);
            log.debug(podcast.toString());
            restTemplate.put(api + "/podcast/" + podcastId, podcast);
            List<SyndEntry> entries = feed.getEntries();

            for (SyndEntry entry : entries) {
                /**
                Resource<Episode> episodeResource = episodeService.findByGuid(entry.getUri());
                Episode episode = episodeResource.getContent();
                String episodeLocation = episodeResource.getLink(Link.REL_SELF).toString();
                if (episode == null) {
                    log.info("Creating new episode for guid {}" + entry.getUri());
                    URI episodeUri = restTemplate.postForLocation(api + "/{}", new Episode(), "episode");
                    log.info("Got new location uri {}", episodeUri);
                    episodeLocation = episodeUri.toString();
                }
                episode = episodeService.mapEpisode(entry, episode);
                restTemplate.put(URI.create(episodeLocation), episode);
                 **/
            }
            podcast.setStatus(Podcast.STATUS_COMPLETED);
            podcast.setLastProcessed(new Date());
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

    public SyndFeed retrieveFeed(String feedUrl) throws FeedException, IOException {
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(new URL(feedUrl)));
        return feed;
    }

    public Podcast mapPodcast(Podcast podcast, SyndFeed feed) {
        Module module = feed.getModule(ITunes.URI);
        FeedInformation info = (FeedInformation) module;
        podcast.setBlock(info.getBlock());
        podcast.setCopyright(feed.getCopyright());
        podcast.setDescription(feed.getDescription());
        podcast.setDocs(feed.getDocs());
        podcast.setExplicit(info.getExplicit());
        podcast.setLanguage(feed.getLanguage());
        podcast.setLink(feed.getLink());
        podcast.setPublishDate(feed.getPublishedDate());
        podcast.setSubtitle(info.getSubtitle());
        podcast.setSummary(info.getSummary());
        podcast.setTitle(feed.getTitle());
        return podcast;
    }
}
