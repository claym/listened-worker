package io.listened.worker.delegate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.listened.common.model.Genre;
import io.listened.worker.service.GenreService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;

/**
 * Created by Clay on 6/28/2015.
 * Loads iTunes Genres as well and populates itunes charts data
 */
@Service
public class ITunesGenreDelegate {
    private static final Logger log = LoggerFactory.getLogger(ITunesGenreDelegate.class);
    @Value("${itunes.url.genre}")
    private String iTunesGenreUrl;

    @Autowired
    GenreService genreService;

    public void handleMessage(String message) {
        try {
            log.info("Loading genres from {}" + iTunesGenreUrl);
            String genreJson = IOUtils.toString(new URL(iTunesGenreUrl));
            // create an ObjectMapper instance.
            ObjectMapper mapper = new ObjectMapper();
            // use the ObjectMapper to read the json string and create a tree
            JsonNode node = mapper.readTree(genreJson);
            log.debug("Got genre tree:");
            Genre g = genreService.mapGenre(null, node.elements().next());
        } catch (IOException e) {
            log.error("Unable to load itunes genres from {}", iTunesGenreUrl);
            log.error(e.getMessage());
        }
    }

}
