package com.example.commitoncancelproblems;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import reactor.core.Disposable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author rpuch
 */
@SpringBootTest
class PartialCommitsOnCancelTest {
    private static final MongoContainer mongo = new MongoContainer();
    static {
        mongo.start();
    }

    @Autowired
    private ReactiveMongoOperations mongoOperations;

    @Autowired
    private BootService bootService;

    private String collection;

    @BeforeEach
    void setUp() {
        collection = UUID.randomUUID().toString().replaceAll("-", "_");
        mongoOperations.createCollection(collection).block();
    }

    @Test
    void cancelShouldNotLeadToPartialCommit() throws InterruptedException {
        // latch is used to make sure that we cancel the subscription only after the first insert has been done
        CountDownLatch latch = new CountDownLatch(1);

        Disposable disposable = bootService.savePair(collection, latch).subscribe();

        // wait for the first insert to be executed
        latch.await();

        // now cancel the reactive pipeline
        disposable.dispose();

        // Now see what we have in the DB. Atomicity requires that we either see 0 or 2 documents.
        List<Boot> boots = mongoOperations.findAll(Boot.class, collection).collectList().block();

        assertEquals(0, boots.size());
    }
}