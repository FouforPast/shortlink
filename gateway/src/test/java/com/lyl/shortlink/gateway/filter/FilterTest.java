package com.lyl.shortlink.gateway.filter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class FilterTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> System.out.println("hello!"));
        System.out.println(future.get());
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> "hello!");
        System.out.println(future2.get());

    }
}
