package io.listened.worker.service;

import io.listened.common.model.podcast.*;
import io.listened.worker.repo.EpisodeKeywordRepo;
import io.listened.worker.repo.KeywordRepo;
import io.listened.worker.repo.PodcastKeywordRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;

/**
 * Created by Clay on 8/31/2015.
 */
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

    public void linkEpisodeToKeywords(Episode episode, String[] keywords) {
        for (String kw : keywords) {
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
        if(kw.trim().length() < 3)
            return null;
        Keyword keyword = new Keyword(kw.trim());
        keyword = keywordRepo.save(keyword);
        return keyword;
    }

}
