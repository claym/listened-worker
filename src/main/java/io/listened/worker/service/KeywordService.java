package io.listened.worker.service;

import com.google.common.collect.Lists;
import io.listened.common.model.podcast.*;
import io.listened.worker.repo.EpisodeKeywordRepo;
import io.listened.worker.repo.KeywordRepo;
import io.listened.worker.repo.PodcastKeywordRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Clay on 8/31/2015.
 */
@Slf4j
@Service
public class KeywordService {

    @Autowired
    KeywordRepo keywordRepo;

    @Autowired
    PodcastKeywordRepo podcastKeywordRepo;

    @Autowired
    EpisodeKeywordRepo episodeKeywordRepo;

    public void linkPodcastToKeywords(Podcast podcast, String[] keywords) {
        for (String kw : keywords) {
            Keyword keyword = keywordRepo.findByNameIgnoreCase(kw.trim());
            if (keyword == null) {
                keyword = createKeyword(kw);
            }
            if (keyword != null) {
                PodcastKeyword pk = podcastKeywordRepo.findByPodcastAndKeyword(podcast, keyword);
                if (pk == null) {
                    pk = new PodcastKeyword(podcast, keyword);
                    podcastKeywordRepo.save(pk);
                }
            }
        }
    }

    public void linkEpisodeToKeywords(Episode episode, String[] episodeKeywords, String[] podcastKeywords) {
        // Remove any keywords that match the podcast's keywords, those will be inherited
        List<String> episodeKeywordList = Lists.newArrayList(episodeKeywords);
        episodeKeywordList.removeAll(Lists.newArrayList(podcastKeywords));
        // remove any words that are shorter than 3 characters
        episodeKeywordList.removeIf((word) -> word.trim().length() < 3);
        log.info("Remaining keywords: " + episodeKeywordList);
        for (String kw : episodeKeywordList) {
            Keyword keyword = keywordRepo.findByNameIgnoreCase(kw.trim());
            if (keyword == null) {
                keyword = createKeyword(kw);
            }
            if (keyword != null) {
                EpisodeKeyword ek = episodeKeywordRepo.findByEpisodeAndKeyword(episode, keyword);
                if (ek == null) {
                    ek = new EpisodeKeyword(episode, keyword);
                    episodeKeywordRepo.save(ek);
                }
            }
        }
    }

    public Keyword createKeyword(@NotNull String kw) {
        Keyword keyword = new Keyword(kw.trim());
        keyword = keywordRepo.save(keyword);
        return keyword;
    }

}
