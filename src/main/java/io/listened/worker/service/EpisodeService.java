package io.listened.worker.service;

import com.rometools.modules.itunes.EntryInformation;
import com.rometools.modules.itunes.FeedInformation;
import com.rometools.modules.itunes.ITunes;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import io.listened.common.model.podcast.Episode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.client.Traverson;
import org.springframework.hateoas.mvc.TypeReferences.PagedResourcesType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
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

    public Episode mapEpisode(SyndEntry entry) throws UnsupportedEncodingException, URISyntaxException {
        EntryInformation info = (EntryInformation) entry.getModule(ITunes.URI);
        Episode e = findByGuid(entry.getUri());
        if (e == null) {
            e = new Episode();
        }
        e.setBlock(info.getBlock());
        e.setComments(entry.getComments());
        if (entry.getDescription() != null) {
            e.setDescription(entry.getDescription().getValue());
        }
        e.setExplicit(info.getExplicit());
        e.setLink(entry.getLink());
        e.setPublishedDate(entry.getPublishedDate());
        e.setUpdatedDate(entry.getUpdatedDate());
        e.setSummary(info.getSummary());
        e.setTitle(entry.getTitle());

        // enclosure
        List<SyndEnclosure> enclosures = entry.getEnclosures();
        if (enclosures != null && !enclosures.isEmpty()) {
            SyndEnclosure enclosure = enclosures.get(0);
            e.setType(enclosure.getType());
            e.setUrl(enclosure.getUrl());
            e.setLength(enclosure.getLength());
        }
        return e;
    }

    public Episode findByGuid(String guid) throws UnsupportedEncodingException, URISyntaxException {
        guid = URLEncoder.encode(guid, "utf-8");
        Episode episode = restTemplate.getForObject(api+"/episode/search/findByGuid?guid=", Episode.class, guid);
        return episode;
    }
}
