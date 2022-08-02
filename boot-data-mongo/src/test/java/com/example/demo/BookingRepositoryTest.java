package com.example.demo;

import com.example.demo.dto.Booking;
import com.example.demo.repository.BookingRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
// or exclude via @ImportAutoConfiguration
//@ImportAutoConfiguration(exclude = EmbeddedMongoAutoConfiguration.class)
// but @EnableAutoConfiguration(exclude=...) does not work,
// see: https://stackoverflow.com/questions/70047380/excluding-embededmongoautoconfiguration-failed-in-spring-boot-2-6-0
@ContextConfiguration(initializers = {MongodbContainerInitializer.class})
@Slf4j
@ActiveProfiles("test")
public class BookingRepositoryTest {

    @Autowired
    ReactiveMongoTemplate reactiveMongoTemplate;

    @Autowired
    BookingRepository bookingRepository;

    @BeforeEach
    public void setup() {
        this.reactiveMongoTemplate.remove(Booking.class).all()
                .subscribe(r -> log.debug("delete all Bookings: " + r), e -> log.debug("error: " + e), () -> log.debug("done"));
    }

//    @Test
//    public void testSaveBookingAndFindByTitleContains() {
//        this.bookingRepository.save(Booking.builder().location("Palo alto").content("my test title").build())
//                .flatMapMany(p -> this.bookingRepository.findByEventIdContains("test"))
//                .as(StepVerifier::create)
//                .consumeNextWith(p -> assertThat(p.getTitle()).isEqualTo("my test title"))
//                .expectComplete()
//                .verify();
//    }

    @Test
    public void testSaveBooking() {
        StepVerifier.create(this.bookingRepository.save(Booking.builder().location("my test location").content("my test title").build()))
                .consumeNextWith(p -> assertThat(p.getContent()).isEqualTo("my test title"))
                .expectComplete()
                .verify();
    }

    @Test
    public void testSaveAndVerifyBooking() {
        Booking saved = this.bookingRepository.save(Booking.builder().content("my test content").eventId("my test title").build()).block();
        assertThat(saved.getEventId()).isNotNull();
        assertThat(this.reactiveMongoTemplate.collectionExists(Booking.class).block()).isTrue();
        assertThat(this.reactiveMongoTemplate.findById(saved.getEventId(), Booking.class).block().getContent()).isEqualTo("my test content");
    }


    @SneakyThrows
    @Test
    public void testGetAllBooking() {
        Booking Booking1 = Booking.builder().location("Palo Alto").content("my test title").build();
        Booking Booking2 = Booking.builder().location("Austin").content("another Booking title").build();

        var countDownLatch = new CountDownLatch(1);
        Flux.just(Booking1, Booking2)
                .flatMap(this.bookingRepository::save)
                .doOnTerminate(countDownLatch::countDown)
                .subscribe(
                        data -> log.debug("saved: {} ", data)
                );

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);

        var allBookings = this.bookingRepository.findAll(Sort.by((Sort.Direction.ASC), "content"));
        StepVerifier.create(allBookings)
                .expectNextMatches(p -> p.getContent().equals("another Booking title"))
                .expectNextMatches(p -> p.getContent().equals("my test title"))
                .verifyComplete();
    }

}
