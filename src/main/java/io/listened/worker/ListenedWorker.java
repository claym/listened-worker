package io.listened.worker;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.orm.jpa.EntityScan;

@SpringBootApplication
@EntityScan(value = "io.listened.common.model")
public class ListenedWorker {

    public static void main(String[] args) {
        //SpringApplication.run(ListenedWorkerApplication.class, args);
        new SpringApplicationBuilder(ListenedWorker.class).showBanner(false).web(false).logStartupInfo(true).run(args);
    }
}
