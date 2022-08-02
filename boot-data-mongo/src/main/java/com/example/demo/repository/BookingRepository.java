package com.example.demo.repository;

import com.example.demo.dto.Booking;
import com.example.demo.dto.PostSummary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface BookingRepository extends ReactiveMongoRepository<Booking, String> {

    @Query(
            value = """
                    {
                         "eventId" : {
                             "$regularExpression" : { "pattern" : ?0, "options" : ""}
                         }
                    }
                    """,
            sort = """
                    { 
                        "eventId" : 1 , 
                        "createdDate" : -1
                    } 
                    """
    )
    Flux<Booking> findByKeyword(String q);

    Flux<PostSummary> findByEventIdContains(String eventId);

    Flux<PostSummary> findByEventIdContains(String eventId, Pageable page);
}
