package io.listened.worker.repo;

import io.listened.common.model.Genre;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Clay on 9/9/2015.
 */
@Repository
public interface GenreRepo extends CrudRepository<Genre, Long> {

    public Genre findByName(String name);

}
