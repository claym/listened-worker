package io.listened.worker.delegate;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * Created by Clay on 6/21/2015.
 */
@Service
public class PodcastUpdateDelegate {


    public void handleMessage(String message) {
        System.out.println(this.getClass().toGenericString() + " " + message);
    }

}
