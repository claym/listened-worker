package io.listened.worker.service;

import com.rometools.modules.itunes.EntryInformation;
import com.rometools.modules.itunes.ITunes;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import io.listened.common.model.podcast.Episode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Created by Clay on 8/23/2015.
 */
@Service
public class EpisodeService {

    @Autowired
    RestTemplate restTemplate;

    @Value("${listened.api.url}")
    private String api;

    /**
     * @param entry   - SyndFeed from Rome Tools
     * @param episode - existing, database stored episode. If null, IllegalArugment will be thrown
     * @return
     * @throws UnsupportedEncodingException
     * @throws URISyntaxException
     */
    public Episode mapEpisode(SyndEntry entry, Episode episode) {
        if (episode == null) throw new IllegalArgumentException("Episode object must not be null");

        EntryInformation info = (EntryInformation) entry.getModule(ITunes.URI);

        episode.setGuid(entry.getUri());
        episode.setBlock(info.getBlock());
        episode.setComments(entry.getComments());
        if (entry.getDescription() != null) {
            episode.setDescription(entry.getDescription().getValue());
        }
        episode.setExplicit(info.getExplicit());
        episode.setLink(entry.getLink());
        episode.setPublishedDate(entry.getPublishedDate());
        episode.setUpdatedDate(entry.getUpdatedDate());
        episode.setSummary(info.getSummary());
        episode.setTitle(entry.getTitle());

        // enclosure
        List<SyndEnclosure> enclosures = entry.getEnclosures();
        if (enclosures != null && !enclosures.isEmpty()) {
            SyndEnclosure enclosure = enclosures.get(0);
            episode.setType(enclosure.getType());
            episode.setUrl(enclosure.getUrl());
            episode.setLength(enclosure.getLength());
        }
        return episode;
    }

    public Resource<Episode> findByGuid(String guid) throws UnsupportedEncodingException {
        guid = URLEncoder.encode(guid, StandardCharsets.UTF_8.toString());
        String lookupUrl = api + "/episode/search/findByGuid?guid={guid}";
        guid = URLEncoder.encode(guid, StandardCharsets.UTF_8.toString());
        ParameterizedTypeReference<Resource<Episode>> resourceParameterizedTypeReference = new ParameterizedTypeReference<Resource<Episode>>() {};
        try {
            ResponseEntity<Resource<Episode>> responseEntity = restTemplate.exchange(lookupUrl, HttpMethod.GET,
                    null, resourceParameterizedTypeReference, guid);
            return responseEntity.getBody();
        } catch(HttpClientErrorException ex) {
            if(ex.getStatusCode().is4xxClientError()) {
                return null;
            }
            throw ex;
        }
    }
}
