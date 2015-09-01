package io.listened.worker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.listened.common.model.Genre;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.*;
import org.springframework.hateoas.client.Traverson;
import org.springframework.hateoas.mvc.TypeReferences.PagedResourcesType;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by Clay on 6/29/2015.
 */
@Service
@Slf4j
public class GenreService {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${listened.api.url}")
    private String api;

    @Cacheable("genres")
    public String getGenreLocation(String genreName) throws URISyntaxException, UnsupportedEncodingException {
        log.info("Manually searching for {}", genreName);

        //genreName = URLEncoder.encode(genreName, "utf-8");
        /**
         * Doesn't work due to double encoding https://github.com/spring-projects/spring-hateoas/issues/337
         */
        /**
        Traverson t = new Traverson(new URI(api), MediaTypes.HAL_JSON);
        //String link = t.follow(Link.REL_SELF).withTemplateParameters(ImmutableMap.of("name", genreName)).asLink().getHref();
        Traverson.TraversalBuilder builder = t.follow("genre", "search", "findByName");
        Map<String, Object> vars = new HashMap<>();
        vars.put("name", genreName);
        log.info("Vars: {}", vars);
        //PagedResources<Resource<Genre>> resources = builder.withTemplateParameters(ImmutableMap.of("name", genreName)).toObject(new PagedResourcesType<Resource<Genre>>() {});
        PagedResources<Resource<Genre>> resources = builder.withTemplateParameters(vars).toObject(new PagedResourcesType<Resource<Genre>>() {});
        String link = resources.getLink(Link.REL_SELF).getHref();
        //String link = t.follow("genre", "search", "findByNameIgnoreCase").withTemplateParameters(ImmutableMap.of("name", genreName)).follow(Link.REL_SELF).asLink().getHref();
        **/

        genreName = URLEncoder.encode(genreName, "utf-8");
        Traverson t = new Traverson(new URI(api+"/genre/search/findByName?name="+genreName), MediaTypes.HAL_JSON);
        String link = t.follow(Link.REL_SELF).asLink().getHref();
        return link;


    }

    public Genre mapGenre(String parentGenreLink, JsonNode node) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Long id = node.get("id").asLong();
        boolean isNew = true;
        Genre genre = null;
        try {
            genre = restTemplate.getForObject(api + "/genre/" + id, Genre.class);
            isNew = false;
        } catch (HttpClientErrorException e) {
            genre = new Genre();
        } catch (ResourceAccessException e) {

        }

        genre.setId(id);

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

        // add object before creating association
        // genre = restTemplate.postForObject(api + "/genre", genre, Genre.class);
        // URI location = restTemplate.postForLocation(api + "/genre/", Genre.class);
        /**
         restTemplate.exchange("http://localhost:8080/genre/26", HttpMethod.GET, null,
         new ParameterizedTypeReference<Resource<Genre>>() {
         }, Collections.emptyMap());
         **/

        ResponseEntity<Resource<Genre>> genreResponse = restTemplate.postForEntity(api + "/genre", genre, null, new ParameterizedTypeReference<Resource<Genre>>() {
        }, Collections.emptyMap());
        //String parentLink = genreResponse.getBody().getLink("parent").getHref();
        //String selfLink = genreResponse.getBody().getLink(Link.REL_SELF).getHref();
        URI selfLink = restTemplate.postForLocation(api + "/genre", genre, Collections.emptyMap());

        if (parentGenreLink != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.setContentType(new MediaType("text", "uri-list"));
            HttpEntity<String> entity = new HttpEntity<>(parentGenreLink, headers);
            try {
                System.out.println("self link: " + selfLink.toString());
                restTemplate.exchange(selfLink.toString() + "/parent", HttpMethod.PUT, entity, String.class, ImmutableMap.of());
                //restTemplate.put(selfLink.toString()+"/parent", entity, parentGenreLink );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        log.debug(genre.toString());


        log.info("Genre ({}, {}) posted to {}", genre.getId(), genre.getName(), (api + "/genre"));

        JsonNode jsg = node.get("subgenres");
        if (jsg != null) {
            Iterator<JsonNode> children = jsg.elements();
            if (children != null) {
                List<Genre> subGenres = new ArrayList<>();
                while (children.hasNext()) {
                    Genre sub = mapGenre(selfLink.toString(), children.next());
                }
            }
        }
        return genre;
    }


}
