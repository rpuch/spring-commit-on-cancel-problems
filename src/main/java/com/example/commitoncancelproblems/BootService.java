package com.example.commitoncancelproblems;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.concurrent.CountDownLatch;

/**
 * @author rpuch
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class BootService {
    private final ReactiveMongoOperations mongoOperations;

    @Transactional
    public Mono<Void> savePair(String collection, CountDownLatch latch) {
        return Mono.defer(() -> {
            Boot left = new Boot();
            left.setKind("left");
            Boot right = new Boot();
            right.setKind("right");

            return mongoOperations.insert(left, collection)
                    // signaling to the test that the first insert has been done and the subscription can be cancelled
                    .then(Mono.fromRunnable(latch::countDown))
                    // do not proceed to the second insert ever
                    .then(Mono.fromRunnable(this::blockForever))
                    .then(mongoOperations.insert(right, collection))
                    .then();
        });
    }

    private void blockForever() {
        while (true);
    }
}
