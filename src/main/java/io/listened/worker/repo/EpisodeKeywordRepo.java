package io.listened.worker.repo;

import io.listened.common.model.podcast.Episode;
import io.listened.common.model.podcast.EpisodeKeyword;
import io.listened.common.model.podcast.Keyword;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Clay on 9/9/2015.
 */
@Repository
public interface EpisodeKeywordRepo extends CrudRepository<EpisodeKeyword, Long>{

    public EpisodeKeyword findByEpisodeAndKeyword(Episode episode, Keyword keyword);
}
