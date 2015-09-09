package io.listened.worker.service;

import com.codingstories.itunes.SearchApi;
import com.codingstories.itunes.parameters.SearchParameters;
import com.codingstories.itunes.parameters.parameter.Media;
import com.codingstories.itunes.result.SearchResult;
import com.codingstories.itunes.result.SearchResults;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by Clay on 9/7/2015.
 */
@Service
public class ITunesService {

    public Long findItunesId(@NotNull String podcastName, @Nullable String iTunesAuthorName) {

        SearchParameters searchParams = new SearchParameters();
        searchParams.addQueryTerm(podcastName);
        if(iTunesAuthorName != null) {
            searchParams.addQueryTerm("Mike Duncan");
        }
        searchParams.setMedia(Media.POD_CAST);
        SearchResults results = SearchApi.search(searchParams);
        List<SearchResult> resultList = results.getResults();
        //if(resultList)
        return null;
    }

}
