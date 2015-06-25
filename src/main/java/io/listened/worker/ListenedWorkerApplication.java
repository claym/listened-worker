package io.listened.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class ListenedWorkerApplication {

    public static void main(String[] args) {
        //SpringApplication.run(ListenedWorkerApplication.class, args);
        new SpringApplicationBuilder(ListenedWorkerApplication.class).showBanner(false).web(false).logStartupInfo(true).run(args);
    }
}
