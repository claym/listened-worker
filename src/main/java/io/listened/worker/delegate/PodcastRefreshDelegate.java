package io.listened.worker.delegate;

import io.listened.worker.service.GenreService;
import io.listened.worker.service.PodcastService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Clay on 6/21/2015.
 */
@Service
public class PodcastRefreshDelegate {
    private static final Logger log = LoggerFactory.getLogger(PodcastSubmitDelegate.class);

    @Autowired
    private GenreService genreService;

    @Autowired
    private PodcastService podcastService;


    public void handleMessage(Long podcastId) {
        log.info("Refreshing podcast {}", podcastId);
        try {
            podcastService.processPodcast(podcastId, true);
            log.info("Finished refreshing podcast {}", podcastId);
        } catch (Exception e) {
            log.error("Error refreshing podcast {}", podcastId);
            e.printStackTrace();
        }
    }

}
