package ru.ifmo.rain.telnov.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;
import ru.ifmo.rain.telnov.concurrent.IterativeParallelism;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by Telnov Sergey on 03.04.2018.
 */
public class MapperIterativeParallelism extends IterativeParallelism {
    private final ParallelMapper mapper;

    public MapperIterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected <T, R> R customMethod(int threads,
                                    final List<? extends T> list,
                                    final Function<Stream<? extends T>, ? extends R> func,
                                    final Function<Stream<? extends R>, ? extends R> rFunc)
            throws InterruptedException {
        final int nThread = Math.min(list.size(), threads);

        final List<Stream<? extends T>> blocks = getBlocks(list, nThread);
        List<R> values;

        if (mapper != null) {
            values = mapper.map(func, blocks);
        } else {
            final Thread[] workers = new Thread[nThread];
            values = new ArrayList<>(Collections.nCopies(nThread, null));

            for (int i = 0; i != nThread; i++) {
                final int index = i;
                workers[index] = new Thread(() -> {
                    values.set(index,
                            func.apply(blocks.get(index)));
                });
                workers[index].start();
            }
        }
        return rFunc.apply(values.stream());
    }
}
