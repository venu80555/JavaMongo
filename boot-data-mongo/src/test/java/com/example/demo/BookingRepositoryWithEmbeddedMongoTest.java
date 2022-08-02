package com.example.demo;

import com.example.demo.dto.Booking;
import com.example.demo.repository.BookingRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@TestPropertySource(properties = "spring.mongodb.embedded.version=3.5.5")
@Slf4j
@ActiveProfiles("test")
public class BookingRepositoryWithEmbeddedMongoTest {

    @Autowired
    BookingRepository posts;

    @SneakyThrows
    @BeforeEach
    public void setup() {
        CountDownLatch latch = new CountDownLatch(1);
        this.posts.saveAll(
                        List.of(
                                Booking.builder().location("Bengaluru").content("my test title").build(),
                                Booking.builder().location("Hyderabad").content("another post title").build()
                        )
                )
                .doOnComplete(latch::countDown)
                .subscribe();

        latch.await(5000, TimeUnit.MILLISECONDS);
    }


    @Test
    public void testGetAllPost() {
        this.posts.findAll(Sort.by("title"))
                .log()
                .as(StepVerifier::create)
                .consumeNextWith(p -> {
                    assertThat(p.getContent()).isEqualTo("my test title");
                })
                .consumeNextWith(p -> assertThat(p.getContent()).isEqualTo("another post title"))
                .verifyComplete();
    }

}
