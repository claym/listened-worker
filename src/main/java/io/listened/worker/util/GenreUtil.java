package io.listened.worker.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.listened.common.model.Genre;
import io.listened.common.model.GenreCharts;
import io.listened.common.model.GenreRss;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Clay on 6/29/2015.
 */
public class GenreUtil {

    private static final Logger log = LoggerFactory.getLogger(GenreUtil.class);

    public static Genre mapGenre(String api, Genre parent, JsonNode node) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Genre genre = new Genre();
        genre.setId(node.get("id").asLong());
        //genre.setParent(parent);
        genre.setName(node.get("name").asText());
        genre.setUrl(node.get("url").asText());

        JsonNode jgc = node.get("chartUrls");
        GenreCharts chart = mapGenreCharts(jgc);
        chart.setId(genre.getId());
        genre.setGenreCharts(chart);

        JsonNode jgr = node.get("rssUrls");
        GenreRss rss = mapGenreRss(jgr);
        rss.setId(genre.getId());
        genre.setGenreRss(rss);

        log.debug(genre.toString());
        RestTemplate restTemplate = new RestTemplate();
        genre = restTemplate.postForObject(api + "/genre", genre, Genre.class);
        log.info("Genre ({}, {}) posted to {}", genre.getId(), genre.getName(), (api + "/genre"));

        JsonNode jsg = node.get("subgenres");
        if (jsg != null) {
            Iterator<JsonNode> children = jsg.elements();
            if (children != null) {
                List<Genre> subGenres = new ArrayList<>();
                while (children.hasNext()) {
                    Genre sub = mapGenre(api, genre, children.next());
                }
            }
        }
        return genre;
    }

    public static GenreCharts mapGenreCharts(JsonNode jgc) {
        GenreCharts gc = new GenreCharts();
        gc.setAudioPodcastEpisodes(jgc.get("audioPodcastEpisodes").asText());
        gc.setAudioPodcasts(jgc.get("audioPodcasts").asText());
        gc.setPodcastEpisodes(jgc.get("podcastEpisodes").asText());
        gc.setPodcasts(jgc.get("podcasts").asText());
        gc.setVideoPodcastEpisodes(jgc.get("videoPodcastEpisodes").asText());
        gc.setVideoPodcasts(jgc.get("videoPodcasts").asText());
        return gc;
    }

    public static GenreRss mapGenreRss(JsonNode jgr) {
        GenreRss gr = new GenreRss();
        gr.setTopAudioPodcastEpisodes(jgr.get("topAudioPodcastEpisodes").asText());
        gr.setTopAudioPodcasts(jgr.get("topAudioPodcasts").asText());
        gr.setTopPodcastEpisodes(jgr.get("topPodcastEpisodes").asText());
        gr.setTopPodcasts(jgr.get("topPodcasts").asText());
        gr.setTopVideoPodcastEpisodes(jgr.get("topVideoPodcastEpisodes").asText());
        gr.setTopVideoPodcasts(jgr.get("topVideoPodcasts").asText());
        return gr;
    }

}
