package ru.ifmo.rain.telnov.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by Telnov Sergey on 22.04.2018.
 */
public class WebCrawler implements Crawler {
    private final Downloader downloader;
    private final int perHost;
    private final ExecutorService downloadExecutor;
    private final ExecutorService extractorExecutor;

    public static void main(String[] args) {
        try (WebCrawler crawler = new WebCrawler(new CachingDownloader(),
                Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]))) {
            crawler.download(args[0], 10);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Usage: <url> <downloads> <extractors> <perHost>");
        }
    }

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.perHost = perHost;

        downloadExecutor = Executors.newFixedThreadPool(downloaders);
        extractorExecutor = Executors.newFixedThreadPool(extractors);
    }

    private Optional<Document> getPage(final String url, final List<String> links, final Map<String, IOException> errors) {
        Document doc = null;
        try {
            doc = downloader.download(url);
            links.add(url);
        } catch (IOException e) {
            errors.put(url, e);
        }
        return Optional.ofNullable(doc);
    }

    private List<String> getLinks(final Future<Optional<Document>> page) {
        try {
            return page.get().map(el -> {
                try {
                    return el.extractLinks();
                } catch (IOException e) {
                    return new ArrayList<String>();
                }
            }).orElse(Collections.emptyList());
        } catch (InterruptedException | ExecutionException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public Result download(String url, int depth) {
        final List<String> links = new CopyOnWriteArrayList<>();
        final Map<String, IOException> errors = new ConcurrentHashMap<>();
        final Set<String> visitedLinks = new HashSet<>();

        links.add(url);
        visitedLinks.add(url);

        Queue<String> queue = new ArrayDeque<>();
        Queue<Future<List<String>>> nextLinks = new ConcurrentLinkedQueue<>();
        queue.add(url);

        for (int i = 0; i != depth; i++) {
            while (!queue.isEmpty()) {
                final String link = queue.poll();
                Future<Optional<Document>> fDoc = downloadExecutor.submit(() -> getPage(link, links, errors));
                Future<List<String>> fList = extractorExecutor.submit(() -> getLinks(fDoc));
                nextLinks.add(fList);
            }

            for (Future<List<String>> fList : nextLinks) {
                try {
                    fList
                            .get()
                            .forEach(link -> {
                                if (!visitedLinks.contains(link)) {
                                    visitedLinks.add(link);
                                    queue.add(link);
                                }
                            });
                } catch (InterruptedException | ExecutionException ignored) {
                }
            }
            nextLinks.clear();
        }
        return new Result(links, errors);
    }

    @Override
    public void close() {
        downloadExecutor.shutdown();
        extractorExecutor.shutdown();
    }
}
