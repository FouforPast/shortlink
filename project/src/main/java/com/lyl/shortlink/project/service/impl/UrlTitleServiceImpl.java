package com.lyl.shortlink.project.service.impl;

import com.lyl.shortlink.project.service.UrlTitleService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
@RequiredArgsConstructor
public class UrlTitleServiceImpl implements UrlTitleService {

    private final ExecutorService urlThreadPoolTaskExecutor;

    @Override
    public String getTitleByUrl(String url) {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> trueGetTitleByUrl(url), urlThreadPoolTaskExecutor);
        try {
            return future.get(2, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            log.error("Timeout while fetching title.", e);
            return "请手动输入标题";
        } catch (Exception e) {
            log.error("Error while fetching title2", e);
            return "请手动输入标题";
        }
    }

    @SneakyThrows
    public String trueGetTitleByUrl(String url) {
        URL targetUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            Document document = Jsoup.connect(url).get();
            return document.title();
        }
        log.warn("Error while fetching title, response code: {} for url: {}", responseCode, url);
        return "请手动输入标题";
    }
}
