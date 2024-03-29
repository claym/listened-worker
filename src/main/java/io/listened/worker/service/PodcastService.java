package io.listened.worker.service;

import com.github.slugify.Slugify;
import com.rometools.modules.itunes.EntryInformation;
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
import io.listened.common.model.podcast.PodcastMeta;
import io.listened.worker.repo.EpisodeRepo;
import io.listened.worker.repo.PodcastRepo;
import io.listened.worker.repo.PodcastMetaRepo;
import io.listened.worker.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.io.IOException;
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
    private PodcastRepo podcastRepo;
    @Autowired
    private EpisodeRepo episodeRepo;
    @Autowired
    private ITunesService iTunesService;
    @Autowired
    private GenreService genreService;
    @Autowired
    private KeywordService keywordService;
    @Autowired
    private PodcastMetaRepo podcasteMetaRepo;

    @Transactional
    public void processPodcast(Long podcastId, boolean forceUpdate) {
        if (forceUpdate) {
            log.info("Performaing full refresh on {}", podcastId);
        }
        Date processTime = new Date();
        Podcast podcast = podcastRepo.findOne(podcastId);
        try {
            SyndFeed feed = retrieveFeed(podcast.getFeedUrl());
            podcast.setStatus(Podcast.STATUS_PROCESSING);
            podcast = podcastRepo.save(podcast);
            podcast = mapPodcast(podcast, feed);
            log.info("Saving podcast {}", podcastId);
            FeedInformation feedInfo = (FeedInformation) feed.getModule(ITunes.URI);
            genreService.linkPodcastToGenres(podcast, feedInfo.getCategories());
            keywordService.linkPodcastToKeywords(podcast, feedInfo.getKeywords());
            if (forceUpdate || podcast.getItunesId() == null) {
                Long iTunesId = iTunesService.findItunesId(podcast.getTitle(), podcast.getAuthor());
                podcast.setItunesId(iTunesId);
            }
            PodcastMeta podcastMeta = generateMeta(podcast);
            podcast.setPodcastMeta(podcastMeta);
            podcast = podcastRepo.save(podcast);
            List<SyndEntry> entries = feed.getEntries();
            for (SyndEntry entry : entries) {
                if (forceUpdate || shouldDoUpdate(podcast.getLastProcessed(), entry.getPublishedDate())) {
                    Episode episode = episodeRepo.findByPodcastAndGuid(podcast, entry.getUri());
                    if (episode == null) {
                        log.info("Creating new episode for guid {}", entry.getUri());
                        episode = new Episode();
                        episode.setGuid(entry.getUri());
                    } else {
                        log.info("Updating episode {}", entry.getUri());
                    }
                    episode = episodeService.mapEpisode(episode, entry);
                    episode.setLastProcessed(processTime);
                    episode.setPodcast(podcast);
                    episode = episodeRepo.save(episode);
                    EntryInformation entryInfo = (EntryInformation) entry.getModule(ITunes.URI);
                    keywordService.linkEpisodeToKeywords(episode, entryInfo.getKeywords(), feedInfo.getKeywords());
                } else {
                    log.info("Skipping episode {}: {}, {} < {}", entry.getUri(), entry.getPublishedDate(), entry.getUpdatedDate(), podcast.getLastProcessed());
                }
            }

            podcast.setStatus(Podcast.STATUS_COMPLETED);
            podcast.setLastProcessed(processTime);
            podcastRepo.save(podcast);
        } catch (Exception e) {
            log.error("Unable to retrieve feed {}", podcast.getFeedUrl());
            log.error(e.toString());
            e.printStackTrace();
        }
        return;

    }

    private boolean shouldDoUpdate(@NotNull Date lastProcessed, Date publishedDate) {
        log.debug("Last Processed: {}", lastProcessed);
        log.debug("Published Date: {}", publishedDate);
        if (publishedDate != null && lastProcessed.before(publishedDate))
            return true;
        return false;
    }

    public SyndFeed retrieveFeed(String feedUrl) throws FeedException, IOException {
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(new URL(feedUrl)));
        return feed;
    }

    public Podcast mapPodcast(Podcast podcast, SyndFeed feed) {
        Module module = feed.getModule(ITunes.URI);
        FeedInformation info = (FeedInformation) module;
        podcast.setAuthor(TextUtils.removeHtml(info.getAuthor()));
        podcast.setBlock(info.getBlock());
        podcast.setCopyright(TextUtils.removeHtml(feed.getCopyright()));
        // podcast.setDescription(TextUtils.removeHtml(feed.getDescription()));
        podcast.setDescription(feed.getDescription());
        podcast.setDocs(feed.getDocs());
        podcast.setExplicit(info.getExplicit());
        podcast.setLanguage(feed.getLanguage());
        podcast.setLink(feed.getLink());
        podcast.setPublishDate(feed.getPublishedDate());
        podcast.setSubtitle(TextUtils.removeHtml(info.getSubtitle()));
        podcast.setSummary(TextUtils.removeHtml(info.getSummary()));
        podcast.setTitle(TextUtils.removeHtml(feed.getTitle()));
        if (feed.getImage() != null) {
            podcast.setImageLink(feed.getImage().getLink());
            podcast.setImageTitle(feed.getImage().getTitle());
            podcast.setImageUrl(feed.getImage().getUrl());

        } else {
            podcast.setImageUrl(((FeedInformation) module).getImage().toString());
        }
        return podcast;
    }

    public PodcastMeta generateMeta(Podcast podcast) throws IOException {
        PodcastMeta podcastMeta = podcast.getPodcastMeta();
        if(podcastMeta == null) {
            podcastMeta = new PodcastMeta();
            podcastMeta.setPodcast(podcast);
        }
        Slugify slgfy = new Slugify();
        String slug = slgfy.slugify(podcast.getTitle());
        List<PodcastMeta> podcastMetaList = podcasteMetaRepo.isSlugUsed(podcast, slug);
        if(podcastMetaList != null && !podcastMetaList.isEmpty()) {
            log.warn("slug already used: {}", slug);
            slug = slgfy.slugify(podcast.getAuthor() + " " + podcast.getTitle());
        }
        podcastMeta.setSlug(slug);
        return podcastMeta;
    }
}
