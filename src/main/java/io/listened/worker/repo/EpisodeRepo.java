package io.listened.worker.repo;

import io.listened.common.model.podcast.Episode;
import io.listened.common.model.podcast.Podcast;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Clay on 9/9/2015.
 */
@Repository
public interface EpisodeRepo extends CrudRepository<Episode, Long> {
    Episode findByPodcastAndGuid(Podcast podcast, String uri);
}
