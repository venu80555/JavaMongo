package com.example.demo;


import com.example.demo.conroller.PostController;
import com.example.demo.dto.Booking;
import com.example.demo.repository.BookingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@WebFluxTest(controllers = PostController.class)
public class PostControllerTests {

    @Autowired
    WebTestClient client;

    @MockBean
    BookingRepository posts;

    @Test
    public void getAllMessagesShouldBeOk() {
        Booking post1 = Booking.builder().content("my test content").content("my test title").build();
        Booking post2 = Booking.builder().content("content of another post").content("another post title").build();
        given(this.posts.findAll()).willReturn(Flux.just(post1, post2));

        client.get().uri("/posts").exchange()
                .expectStatus().isOk()
                .expectBody()



                .jsonPath("$.[0].content").isEqualTo("my test title")
                .jsonPath("$.[1].content").isEqualTo("another post title");

        verify(this.posts, times(1)).findAll();
        verifyNoMoreInteractions(this.posts);
    }

}
