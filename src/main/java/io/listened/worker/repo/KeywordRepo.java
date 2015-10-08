package io.listened.worker.repo;

import io.listened.common.model.podcast.Keyword;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Clay on 9/9/2015.
 */
@Repository
public interface KeywordRepo extends CrudRepository<Keyword, Long> {

    public Keyword findByNameIgnoreCase(String name);

}
