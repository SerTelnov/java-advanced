package ru.ifmo.rain.telnov.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

/**
 * Created by Telnov Sergey on 11.03.2018.
 */
public class Implementor implements JarImpler {

    public static void main(String[] args) throws ImplerException {
        if (args.length < 3) {
            throw new ImplerException("usage: -jar <class-name> <file.jar>");
        } else if (args[0].equals("-jar")) {
            JarImpler impler = new Implementor();
            try {
                impler.implementJar(Class.forName(args[1]), Paths.get(args[2]));
            } catch (ClassNotFoundException e) {
                System.err.println("can't found class: '" + args[2] + "'");
            }
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

    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        String root = jarFile.getParent().toString();
        String fileName = getFileName(token.getCanonicalName());
        String sourceDirectoryName = getFullDirectoryPath(jarFile.getFileName());

        compileFile(String.format("%s/%s/%sImpl.java", root, sourceDirectoryName, fileName));
        String compiledFile = String.format("%s/%sImpl.class", sourceDirectoryName, fileName);

        try (JarOutputStream jarWriter = new JarOutputStream(
                new FileOutputStream(jarFile.toFile()), getManifest()))
        {
            jarWriter.putNextEntry(new ZipEntry(compiledFile));

            ByteChannel inChannel = Files.newByteChannel(Paths.get(String.format("%s/%s", root, compiledFile)));
            ByteBuffer buffer = ByteBuffer.allocate(2048);

            while (inChannel.read(buffer) >= 0) {
                jarWriter.write(buffer.array(), 0, buffer.position());
                buffer.flip();
            }

            buffer.clear();
            inChannel.close();
            jarWriter.closeEntry();
        } catch (IOException e) {
            throw new ImplerException(e);
        } finally {
            Path fileToDelete = getPath(compiledFile);
            if (Files.exists(fileToDelete)) {
                try {
                    Files.delete(fileToDelete);
                } catch (IOException ignored) { }
            }
        }
    }

    private void compileFile(String file) throws ImplerException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        int exitCode = compiler.run(null, null, null,
                file, "-cp", System.getProperty("java.class.path"));

        if (exitCode != 0) {
            throw new ImplerException("can't compile source file\nExit code: '" + exitCode + "'");
        }
    }

    private Manifest getManifest() {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        return manifest;
    }

    private String getFileName(String path) {
        String[] tokens = path.split("[.]");
        return Stream.of(tokens)
                .skip(tokens.length - 1)
                .collect(Collectors
                        .joining());
    }

    private String getFullDirectoryPath(Path path) {
        String[] tokens = path
                .toString().split("[.|\\\\]");
        return Stream.of(tokens)
                .limit(tokens.length - 2)
                .collect(Collectors
                        .joining("/"));
    }

    private Path getPath(String name) throws ImplerException {
        try {
            return Paths.get(name);
        } catch (InvalidPathException e) {
            throw new ImplerException("can't open Path: '" + name + "'\n", e);
        }
    }

    private Path createPath(Path root, String fileName) throws ImplerException {
        try {
            Path path = getPath(String.format("%s%c%sImpl.java",
                    root.toAbsolutePath(), File.separatorChar,
                    fileName.replace('.', File.separatorChar)));
            Files.createDirectories(path.getParent());
            return path;
        } catch (IOException e) {
            throw new ImplerException("can't create path", e);
        }
    }
}
