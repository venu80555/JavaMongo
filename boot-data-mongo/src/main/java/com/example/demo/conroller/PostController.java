package com.example.demo.conroller;

import com.example.demo.dto.Booking;
import com.example.demo.repository.BookingRepository;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController()
@RequestMapping(value = "/posts")
public class PostController {

    private final BookingRepository posts;

    public PostController(BookingRepository posts) {
        this.posts = posts;
    }

    @GetMapping("")
    public Flux<Booking> all() {
        return this.posts.findAll();
    }

    @PostMapping("")
    public Mono<Booking> create(@RequestBody Booking post) {
        return this.posts.save(post);
    }

    @GetMapping("/{id}")
    public Mono<Booking> get(@PathVariable("id") String id) {
        return this.posts.findById(id);
    }

    @PutMapping("/{id}")
    public Mono<Booking> update(@PathVariable("id") String id, @RequestBody Booking post) {
        return this.posts.findById(id)
                .map(p -> {
                    p.setLocation(post.getLocation());
                    p.setContent(post.getContent());

                    return p;
                })
                .flatMap(this.posts::save);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable("id") String id) {
        return this.posts.deleteById(id);
    }

}
