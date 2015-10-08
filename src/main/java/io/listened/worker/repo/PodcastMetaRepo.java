package io.listened.worker.repo;

import io.listened.common.model.podcast.Podcast;
import io.listened.common.model.podcast.PodcastMeta;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Clay on 10/7/2015.
 */
@Repository
public interface PodcastMetaRepo extends CrudRepository<PodcastMeta, Long> {

    @Query("select pm from PodcastMeta pm where pm.podcast <> :podcast and slug = :slug")
    List<PodcastMeta> isSlugUsed(@Param("podcast") Podcast podcast, @Param("slug") String slug);
}
