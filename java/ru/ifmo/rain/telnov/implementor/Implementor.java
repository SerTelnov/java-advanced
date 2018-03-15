package ru.ifmo.rain.telnov.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;

/**
 * Created by Telnov Sergey on 11.03.2018.
 */
public class Implementor implements Impler {

    private Path createPath(Path root, String fileName) throws ImplerException {
        try {
            Path path = Paths.get(String.format("%s%c%sImpl.java",
                    root.toAbsolutePath(), File.separatorChar,
                    fileName.replace('.', File.separatorChar)));

            Files.createDirectories(path.getParent());
            return path;
        } catch (InvalidPathException | IOException e) {
            throw new ImplerException("can't create path", e);
        }
    }

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (!token.isInterface()) {
            throw new ImplerException("invalid interface" + token.getCanonicalName());
        }

        try (BufferedWriter bw = Files.newBufferedWriter(
                createPath(root, token.getCanonicalName()))) {
            ClassCreator creator = new ClassCreator(bw);
            creator.writeClassName(token.getPackage().toString(), token.getSimpleName());

            Method[] methods = token.getMethods();
            for (int i = 0; i != methods.length; i++) {
                creator.writeMethod(methods[i]);
            }
            creator.flush();
        } catch (IOException e) {
            throw new ImplerException(e);
        }
    }
}
