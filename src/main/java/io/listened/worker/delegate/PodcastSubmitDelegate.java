package io.listened.worker.delegate;

import com.rometools.modules.itunes.EntryInformation;
import com.rometools.modules.itunes.FeedInformation;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import io.listened.common.model.Podcast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URL;

/**
 * Created by Clay on 6/21/2015.
 * Handles processing a newly submitted podcasts
 * Processes basic podcast and episode data, fires queue jobs for NLP and keyword processing
 */
@Component
public class PodcastSubmitDelegate {

    private static final Logger log = LoggerFactory.getLogger(PodcastSubmitDelegate.class);

    @Value("${listened.api.url}")
    private String api;

    public void handleMessage(Long podcastId) {
        RestTemplate restTemplate = new RestTemplate();
        log.info("Processing podcast %d", podcastId);

        Podcast podcast = restTemplate.getForObject(api+"/podcast/"+podcastId, Podcast.class);
        if(podcast == null) {
            log.error("Unable to retrieve podcast %l, aborting", podcastId);
            return;
        }
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = null;
        try {
            log.info("Loading podcast feed for {} {}", podcast.getId(), podcast.getFeedUrl());
            feed = input.build(new XmlReader(new URL(podcast.getFeedUrl())));
        } catch (FeedException e) {
            log.error("Unable to retrieve feed {}", podcast.getFeedUrl());
            log.error(e.toString());
            return;
        } catch (IOException e) {
            log.error("Unable to handle feed {}", podcast.getFeedUrl());
            log.error(e.toString());
            return;
        }

        Module module = feed.getModule("http://www.itunes.com/dtds/podcast-1.0.dtd");
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
        log.info("Submitting podcast {} to {}", podcastId, api);
        log.debug(podcast.toString());
        restTemplate.put(api+"/podcast/"+podcastId, podcast);
    }

}
