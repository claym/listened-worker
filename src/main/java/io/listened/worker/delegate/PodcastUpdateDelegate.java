package io.listened.worker.delegate;

import org.springframework.stereotype.Component;

/**
 * Created by Clay on 6/21/2015.
 */
@Component
public class PodcastUpdateDelegate {


    public void handleMessage(String message) {
        System.out.println(this.getClass().toGenericString() + " " + message);
    }

}
