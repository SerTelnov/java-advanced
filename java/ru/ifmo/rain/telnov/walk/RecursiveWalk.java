package ru.ifmo.rain.telnov.walk;

import ru.ifmo.rain.telnov.walk.exceptions.WalkerException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by Telnov Sergey on 13.02.2018.
 */
public class RecursiveWalk extends Walk {

    public static void main(String[] args) {
        new RecursiveWalk().run(args);
    }

    @Override
    protected void walkThroughFile(final Path path) throws WalkerException {
        if (Files.exists(path)) {
            try {
                Files.walk(path)
                        .filter(Files::isRegularFile)
                        .forEach(this::writeData);
            } catch (IOException e) {
                throw new WalkerException("problem with 'walk' method for path: '" + path + "'", e);
            }
        } else {
            writeData(path);
        }
    }
}
