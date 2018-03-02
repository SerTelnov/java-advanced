package ru.ifmo.rain.telnov.walk;

import com.sun.istack.internal.NotNull;
import ru.ifmo.rain.telnov.walk.exceptions.WalkerException;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Created by Telnov Sergey on 10.02.2018.
 */
public class Walk {

    private BufferedWriter writer;

    public static void main(String[] args) {
        new Walk().run(args);
    }

    protected void run(String[] args) {
        if (args.length == 2) {
            try {
                walk(args[0], args[1]);
            } catch (IOException | NullPointerException e) {
                System.err.println(e.getMessage());
            }
        } else {
            System.err.println("incorrect number of arguments\n" +
                    "usage java Walk <input file> <output file>");
        }
    }

    public void walk(@NotNull final String inputFileName, @NotNull final String outputFileName) throws WalkerException {
        Path inputFile = getPath(inputFileName);
        Path outputFile = getPath(outputFileName);
        try {
            Stream<String> fileNames = Files.lines(inputFile, Charset.forName("UTF-8"));

            try {
                Files.createDirectories(outputFile.getParent());
            } catch (IOException e) {
                throw new WalkerException("can't create directories for file: '" + outputFileName + "'", e);
            }

            try (BufferedWriter bw = Files.newBufferedWriter(outputFile, Charset.forName("UTF-8"))) {
                writer = bw;
                fileNames.forEach((fileName) -> {
                    try {
                        walkThroughFile(getPath(fileName));
                    } catch (IOException e) {
                        writeData(fileName, 0);
                    }
                });
            } catch (IOException e) {
                throw new WalkerException("can't open BufferWriter for file: '" + outputFileName + "'", e);
            }
        } catch (IOException e) {
            throw new WalkerException("can't get lines from: '" + inputFile + "'", e);
        }
    }

    private Path getPath(final String name) throws WalkerException {
        try {
            return Paths.get(name);
        } catch (InvalidPathException e) {
            throw new WalkerException("can't open file: '" + name + "'", e);
        }
    }

    protected void walkThroughFile(final Path path) throws WalkerException {
        writeData(path);
    }

    private void writeData(String fileName, final int data) {
        try {
            writer.write(String.format("%08x", data) + " " + fileName + "\n");
        } catch (IOException e) {
            System.err.println("can't write data for file: '" + fileName + "'");
        }
    }

    protected void writeData(Path path) {
        int hash = getHashFromFile(path);
        writeData(path.toString(), hash);
    }

    private int getHashFromFile(final Path dataFile) {
        try {
            SeekableByteChannel inChannel = Files.newByteChannel(dataFile);
            ByteBuffer buffer = ByteBuffer.allocate(2048);

            int hash = 0x811c9dc5;

            while (inChannel.read(buffer) >= 0) {
                hash = generateHash(buffer.array(), buffer.position(), hash);
                buffer.flip();
            }

            buffer.clear();
            inChannel.close();

            return hash;
        } catch (IOException | NullPointerException e) {
            return 0;
        }
    }

    private int generateHash(final byte[] bytes, final int length, int hash) {
        for (int i = 0; i != length; i++) {
            hash *= 0x01000193;
            hash ^= (bytes[i] & 0xff);
        }
        return hash;
    }
}
