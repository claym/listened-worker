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
 */
@Component
public class PodcastSubmitDelegate {

    private static final Logger log = LoggerFactory.getLogger(PodcastSubmitDelegate.class);

    @Value("${listened.api.url}")
    private String api;

    public void handleMessage(Long podcastId) {

/**
        SyndFeed feed = null;
        FeedInformation feedInfo = null;
        try {
            FeedFetcher feedFetcher = new HttpURLFeedFetcher();
            System.out.println("Retrieving feed " + message);
            feed = feedFetcher.retrieveFeed(new URL(message));
            System.out.println("Got Feed");
            Module module = feed.getModule("http://www.itunes.com/DTDs/Podcast-1.0.dtd");
            System.out.println("Got Module");
            feedInfo = (FeedInformation) module;
            System.out.println("Converted module");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FeedException e) {
            e.printStackTrace();
        } catch (FetcherException e) {
            e.printStackTrace();
        }
        System.out.println(feedInfo);
        //System.out.println(feed);
**/
        RestTemplate restTemplate = new RestTemplate();
        Podcast podcast = restTemplate.getForObject(api+"/podcast/"+podcastId, Podcast.class);

        SyndFeedInput input = new SyndFeedInput();
        SyndFeed syndfeed = null;
        try {
            syndfeed = input.build(new XmlReader(new URL(podcast.getFeedUrl())));
        } catch (FeedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Module module = syndfeed.getModule("http://www.itunes.com/dtds/podcast-1.0.dtd");
        FeedInformation feedInfo = (FeedInformation) module;
        System.out.println(feedInfo);
        for(SyndEntry e : syndfeed.getEntries()) {
            module = e.getModule("http://www.itunes.com/dtds/podcast-1.0.dtd");
            EntryInformation entryInformation = (EntryInformation) module;
            System.out.println(entryInformation);
        }
    }

}
