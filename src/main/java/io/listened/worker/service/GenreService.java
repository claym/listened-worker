package io.listened.worker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.rometools.modules.itunes.types.Category;
import io.listened.common.model.Genre;
import io.listened.common.model.podcast.Podcast;
import io.listened.common.model.podcast.PodcastGenre;
import io.listened.worker.repo.GenreRepo;
import io.listened.worker.repo.PodcastGenreRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Clay on 6/29/2015.
 */
@Service
@Slf4j
public class GenreService {

    @Autowired
    GenreRepo genreRepo;

    @Autowired
    PodcastGenreRepo podcastGenreRepo;

    @Cacheable("genreIds")
    public Long getGenreId(String genreName) throws URISyntaxException, UnsupportedEncodingException {
        log.info("Manually searching for {}", genreName);
        Genre genre = genreRepo.findByName(genreName);
        return genre.getId();
    }

    public Genre mapGenre(Genre parentGenre, JsonNode node) throws IOException {
        Long id = node.get("id").asLong();
        Genre genre = genreRepo.findOne(id);
        if (genre == null) {
            genre = new Genre();
            genre.setId(id);
        }

        //genre.setParent(parent);
        genre.setName(node.get("name").asText());
        genre.setUrl(node.get("url").asText());

        JsonNode jgc = node.get("chartUrls");
        genre.setAudioPodcastEpisodesChart(jgc.get("audioPodcastEpisodes").asText());
        genre.setAudioPodcastsChart(jgc.get("audioPodcasts").asText());
        genre.setPodcastEpisodesChart(jgc.get("podcastEpisodes").asText());
        genre.setPodcastsChart(jgc.get("podcasts").asText());
        genre.setVideoPodcastEpisodesChart(jgc.get("videoPodcastEpisodes").asText());
        genre.setVideoPodcasts(jgc.get("videoPodcasts").asText());


        JsonNode jgr = node.get("rssUrls");
        genre.setTopAudioPodcastEpisodesRss(jgr.get("topAudioPodcastEpisodes").asText());
        genre.setTopAudioPodcastsRss(jgr.get("topAudioPodcasts").asText());
        genre.setTopPodcastEpisodesRss(jgr.get("topPodcastEpisodes").asText());
        genre.setTopPodcastsRss(jgr.get("topPodcasts").asText());
        genre.setTopVideoPodcastEpisodesRss(jgr.get("topVideoPodcastEpisodes").asText());
        genre.setTopVideoPodcastsRss(jgr.get("topVideoPodcasts").asText());

        genre.setParent(parentGenre);

        genre = genreRepo.save(genre);


        JsonNode jsg = node.get("subgenres");
        if (jsg != null) {
            Iterator<JsonNode> children = jsg.elements();
            if (children != null) {
                while (children.hasNext()) {
                    mapGenre(genre, children.next());
                }
            }
        }
        return genre;
    }

    public void linkPodcastToGenres(Podcast podcast, List<Category> categories) {
        for (Category category : categories) {
            Genre genre = genreRepo.findByName(category.getName());
            if (genre != null) {
                log.info("Creating new Podcast ({}) / Genre ({})", podcast.getId(), genre.getId());
                PodcastGenre pg = podcastGenreRepo.findByPodcastAndGenre(podcast, genre);
                if (pg == null) {
                    pg = new PodcastGenre(podcast, genre);
                    podcastGenreRepo.save(pg);
                }
            } else {
                log.warn("Unable to find genre {}", category.getName());
            }
        }
    }

}
