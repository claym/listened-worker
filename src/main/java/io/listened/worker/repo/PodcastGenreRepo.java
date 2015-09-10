package io.listened.worker.repo;

import io.listened.common.model.Genre;
import io.listened.common.model.podcast.Podcast;
import io.listened.common.model.podcast.PodcastGenre;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Clay on 9/9/2015.
 */
@Repository
public interface PodcastGenreRepo extends CrudRepository<PodcastGenre, Long> {

    public PodcastGenre findByPodcastAndGenre(Podcast podcast, Genre genre);

}
