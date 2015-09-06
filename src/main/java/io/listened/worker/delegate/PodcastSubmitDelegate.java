package io.listened.worker.delegate;

import com.rometools.modules.itunes.FeedInformation;
import com.rometools.modules.itunes.ITunes;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import io.listened.common.model.podcast.Podcast;
import io.listened.worker.service.GenreService;
import io.listened.worker.service.PodcastService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URL;

/**
 * Created by Clay on 6/21/2015.
 * Handles processing a newly submitted podcasts
 * Processes basic podcast and episode data, fires queue jobs for NLP and keyword processing
 */
@Service
public class PodcastSubmitDelegate {

    private static final Logger log = LoggerFactory.getLogger(PodcastSubmitDelegate.class);

    @Autowired
    private GenreService genreService;

    @Autowired
    private PodcastService podcastService;



    public void handleMessage(Long podcastId) {

        log.info("Handling podcast {}", podcastId);
        try {
            podcastService.processPodcast(podcastId, true);
            log.info("Finished handling podcast {}", podcastId);
        } catch (Exception e) {
            log.error("Error handling podcast {}", podcastId);
            e.printStackTrace();
        }

    }

}
