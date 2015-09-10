package io.listened.worker.repo;

import io.listened.common.model.podcast.Keyword;
import io.listened.common.model.podcast.Podcast;
import io.listened.common.model.podcast.PodcastKeyword;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by Clay on 9/9/2015.
 */
public interface PodcastKeywordRepo extends CrudRepository<PodcastKeyword, Long> {

    public PodcastKeyword findByPodcastAndKeyword(Podcast podcast, Keyword keyword);
}
