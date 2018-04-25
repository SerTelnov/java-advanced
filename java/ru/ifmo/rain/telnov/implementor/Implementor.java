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
 * Implementation for interface {@link JarImpler}.
 */
public class Implementor implements JarImpler {
    /**
     * Constructor for class {@code Implementor}.
     */
    public Implementor() {
    }

    /**
     * Entry point for application to start from command line.
     * <p>
     * Usage:
     * <ul>
     * <li>{@code java -jar Implementor.jar -jar <interface-to-implement> <path-to-jar>}</li>
     * </ul>
     *
     * @param args arguments from command line.
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("usage: -jar <interface-to-implement> <file.jar>");
        } else if (args[0].equals("-jar")) {
            JarImpler impler = new Implementor();
            try {
                Class<?> clazz = Class.forName(args[1]);
                Path path = Paths.get(args[2]);

                impler.implementJar(clazz, path);
            } catch (ClassNotFoundException e) {
                System.err.println("can't found class: '" + args[2] + "'");
            } catch (ImplerException e) {
                System.err.println("can't generate jar with file");
            }
        }
    }

    /**
     * Implement interface <tt>token</tt> and locate it in file <tt>path</tt>.
     * <p>
     * <p>
     * Generated class should have full name as implementing interface with <tt>Impl</tt> at the end.
     * Generated file should be placed in the correct subdirectory of the specified <tt>root</tt>
     * directory and have correct file name.
     *
     * @param token interface to implement.
     * @param root  root of directory.
     * @throws ImplerException if can't implement <code>token</code>.
     */
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

    /**
     * Produce <tt>.jar</tt> file and collect implementation of <tt>token</tt>.
     * <p>
     * Generate <tt>.jar</tt> file, that collect implementing of given interface specified by provided <tt>token</tt>
     * and locate in path <code>jarFile</code>. If source implementation don't exist, build source file. Then compile
     * source file and write binary file to <tt>.jar</tt>. Then delete compiled file.
     *
     * @param token   type token to create implementation for.
     * @param jarFile target <tt>.jar</tt> file.
     * @throws ImplerException when can't generate <tt>.jar</tt> file and store implementing of <code>token</code>.
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        Path root = jarFile.getParent();
        String fullFileName = getPathName(jarFile.getFileName().toString());

        boolean sourceFileWasCreated = compileFile(token, String.format("%s/%sImpl.java", root, fullFileName), root);
        String compiledFile = String.format("%sImpl.class", fullFileName);

        try (JarOutputStream jarWriter = new JarOutputStream(
                new FileOutputStream(jarFile.toFile()), getManifest())) {
            jarWriter.putNextEntry(new ZipEntry(compiledFile));

            ByteChannel inChannel = Files.newByteChannel(getPath(String.format("%s/%s", root, compiledFile)));
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
            try {
                deleteFile(compiledFile);
                if (sourceFileWasCreated) {
                    deleteFile(String.format("%s/%sImpl.java", root, fullFileName));
                }
            } catch (IOException | ImplerException ignored) {
            }
        }
    }

    /**
     * Delete given <tt>file</tt>.
     *
     * @param file file to Delete
     * @throws ImplerException then can't get file's path.
     * @throws IOException     then can't delete file.
     */
    private void deleteFile(String file) throws ImplerException, IOException {
        Path fileToDelete = getPath(file);
        if (Files.exists(fileToDelete)) {
            Files.delete(fileToDelete);
        }
    }

    /**
     * Compile given <tt>file</tt>.
     * <p>
     * Compile source <tt>file</tt>. If <tt>file</tt> don't exist, implement <tt>token</tt> and write
     * information to <tt>file</tt> and compile it.
     *
     * @param token interface to implement.
     * @param file  to compile.
     * @param root  interface's path root.
     * @return true if source file <tt>.java</tt> was created, false otherwise.
     * @throws ImplerException when can't compile <code>file</code>.
     */
    private boolean compileFile(Class<?> token, String file, Path root) throws ImplerException {
        boolean wasCreated = false;
        Path sourceFile = getPath(file);
        if (Files.notExists(sourceFile)) {
            implement(token, root);
            wasCreated = true;
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        int exitCode = compiler.run(null, null, null,
                file, "-cp", System.getProperty("java.class.path"));

        if (exitCode != 0) {
            throw new ImplerException("can't compile source file\nExit code: '" + exitCode + "'");
        }
        return wasCreated;
    }

    /**
     * Generate <tt>Manifest</tt>.
     * <p>
     * Generate <tt>Manifest</tt> for <tt>.jar</tt> file.
     *
     * @return <tt>Manifest</tt>.
     */
    private Manifest getManifest() {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        return manifest;
    }

    /**
     * Generate absolute part from full path.
     * <p>
     *
     * @param path full path
     * @return absolute path
     */
    private String getPathName(String path) {
        String[] tokens = path.split("[.|\\\\]");
        return Stream.of(tokens)
                .limit(tokens.length - 1)
                .collect(Collectors
                        .joining("/"));
    }

    /**
     * Generate <tt>Path</tt>.
     * <p>
     *
     * @param name path name.
     * @return generated <tt>Path</tt>.
     * @throws ImplerException when name contains invalid path.
     */
    private Path getPath(String name) throws ImplerException {
        try {
            return Paths.get(name);
        } catch (InvalidPathException e) {
            throw new ImplerException("can't open Path: '" + name + "'\n", e);
        }
    }

    /**
     * Create <tt>Path</tt> from file <tt>root</tt> and file's package name.
     * <p>
     *
     * @param root     root of file.
     * @param fileName file name.
     * @return created <tt>Path</tt>
     * @throws ImplerException when can't generate <tt>path</tt>
     */
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
