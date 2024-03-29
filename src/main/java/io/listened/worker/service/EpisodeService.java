package io.listened.worker.service;

import com.google.common.collect.ImmutableMap;
import com.rometools.modules.itunes.EntryInformation;
import com.rometools.modules.itunes.ITunes;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import io.listened.common.model.podcast.Episode;
import io.listened.worker.util.TextUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.examples.HtmlToPlainText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.NotNull;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Created by Clay on 8/23/2015.
 */
@Slf4j
@Service
public class EpisodeService {

    /**
     * @param entry   - SyndFeed from Rome Tools
     * @param episode - existing, database stored episode. If null, IllegalArugment will be thrown
     * @return populated Episode item
     * @throws UnsupportedEncodingException
     * @throws URISyntaxException
     */
    public Episode mapEpisode(@NotNull Episode episode, @NotNull SyndEntry entry) {
        // if (episode == null) throw new IllegalArgumentException("Episode object must not be null");

        EntryInformation info = (EntryInformation) entry.getModule(ITunes.URI);

        episode.setGuid(entry.getUri());
        episode.setBlock(info.getBlock());
        episode.setComments(TextUtils.removeHtml(entry.getComments()));
        if (entry.getDescription() != null) {
            episode.setDescription(TextUtils.removeHtml(entry.getDescription().getValue()));
        }
        episode.setExplicit(info.getExplicit());
        episode.setLink(entry.getLink());
        episode.setPublishedDate(entry.getPublishedDate());
        episode.setSummary(TextUtils.removeHtml(info.getSummary()));
        episode.setTitle(TextUtils.removeHtml(entry.getTitle()));
        episode.setAuthor(TextUtils.removeHtml(info.getAuthor()));
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

/**
    public Resource<Episode> findResourceByGuid(Long podcastId, String guid) throws UnsupportedEncodingException {
        log.info("Looking for episode with podcastid = {}, guid = {}", podcastId, guid);
        guid = URLEncoder.encode(guid, StandardCharsets.UTF_8.toString());
        String lookupUrl = api + "/episode/search/findResourceByGuid?podcastId={podcastId}&guid={guid}";
        guid = URLEncoder.encode(guid, StandardCharsets.UTF_8.toString());
        ParameterizedTypeReference<Resource<Episode>> resourceParameterizedTypeReference = new ParameterizedTypeReference<Resource<Episode>>() {
        };
        try {
            ResponseEntity<Resource<Episode>> responseEntity = restTemplate.exchange(lookupUrl, HttpMethod.GET,
                    null, resourceParameterizedTypeReference, podcastId, guid);
            return responseEntity.getBody();
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().is4xxClientError()) {
                return null;
            }
            throw ex;
        }
    }

    public Resource<Episode> getEpisodeResource(Long episodeId) {
        String lookupUrl = api + "/episode/" + episodeId;
        return getEpisodeResource(lookupUrl);
    }

    public Resource<Episode> getEpisodeResource(String episodeLocation) {
        ParameterizedTypeReference<Resource<Episode>> resourceParameterizedTypeReference = new ParameterizedTypeReference<Resource<Episode>>() {
        };
        try {
            ResponseEntity<Resource<Episode>> responseEntity = restTemplate.exchange(episodeLocation, HttpMethod.GET,
                    null, resourceParameterizedTypeReference);
            return responseEntity.getBody();
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().is4xxClientError()) {
                return null;
            }
            throw ex;
        }
    }


    public void associateEpisode(String podcastLocation, String episodeLocation) {
        log.info("Creating assocation between {} and {}", episodeLocation, podcastLocation);
        HttpHeaders reqHeaders = new HttpHeaders();
        reqHeaders.add(HttpHeaders.CONTENT_TYPE, new MediaType("text", "uri-list").toString());
        HttpEntity<String> reqEntity = new HttpEntity<String>(podcastLocation, reqHeaders);
        ResponseEntity<String> string = restTemplate.exchange(episodeLocation, HttpMethod.PUT, reqEntity, String.class, ImmutableMap.of());
        log.info("Returned string: {}" + string);
    }
**/
}
