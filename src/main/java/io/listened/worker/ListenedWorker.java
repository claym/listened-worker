package io.listened.worker;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class ListenedWorker {

    public static void main(String[] args) {
        //SpringApplication.run(ListenedWorkerApplication.class, args);
        new SpringApplicationBuilder(ListenedWorker.class).showBanner(false).web(false).logStartupInfo(true).run(args);
    }
}
