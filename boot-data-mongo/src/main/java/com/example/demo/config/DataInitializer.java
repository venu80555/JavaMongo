package com.example.demo.config;

import com.example.demo.dto.Booking;
import com.example.demo.repository.BookingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@Slf4j
@Profile("default")
public class DataInitializer implements CommandLineRunner {

    private final BookingRepository posts;

    public DataInitializer(BookingRepository posts) {
        this.posts = posts;
    }

    @Override
    public void run(String[] args) {
        log.info("start data initialization ...");
        this.posts
                .deleteAll()
                .thenMany(
                        Flux
                                .just("Post one", "Post two")
                                .flatMap(
                                        title -> this.posts.save(Booking.builder().eventId(title).content("content of " + title).build())
                                )
                )
                .thenMany(
                        this.posts.findAll()
                )
                .subscribe(
                        data -> log.info("found posts: {}", posts),
                        error -> log.error("" + error),
                        () -> log.info("done initialization...")
                );

    }

}
