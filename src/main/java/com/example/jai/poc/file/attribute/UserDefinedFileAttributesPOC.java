package com.example.jai.poc.file.attribute;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.stream.Stream;

import static java.nio.charset.Charset.defaultCharset;

public class UserDefinedFileAttributesPOC {
    private static final int MAX_ATTRIB_SIZE = 50;
    private final Path parentFolder;

    public UserDefinedFileAttributesPOC(final String parentFolder) throws IOException {
        this.parentFolder = createDirectoriesIfNotExists(parentFolder);
    }

    public static void main(String[] args) throws IOException {
        final UserDefinedFileAttributesPOC udfa = new UserDefinedFileAttributesPOC("/tmp/test");
        final String attribName = "tenantName";

        final String[] tenants = new String[]{"pepsico", "nokia", "mi", "hp", "dell", "apple"};

        for (final String tenantName : tenants) {
            for (int i = 0; i < 100; i++) {
                final Path tmpFile = udfa.createFileIfNotExists(udfa.getFileName(tenantName, i));
                udfa.setAttribute(tmpFile, attribName, tenantName);
            }
        }

        final long startTime = System.currentTimeMillis();
        final Stream<Path> matchingFiles = udfa.getMatchingFiles(attribName, "nokia");
        System.out.println("Time taken : " + (System.currentTimeMillis() - startTime) + " ms");
        //matchingFiles.forEach(p -> System.out.println(p.getFileName()));
        System.out.println("Count of result : " + matchingFiles.count());
        System.out.println("Done");

    }

    private Stream<Path> getMatchingFiles(final String attribName, final String attribValue) throws IOException {
        return Files.find(parentFolder, 1, (path, basicFileAttributes) -> {
            if (path.equals(parentFolder)) {
                return false;
            }
            try {
                final String fileAttribValue = getAttribute(path, attribName);
                return fileAttribValue.equals(attribValue);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    private String getFileName(final String tenantName, final int i) {
        return this.parentFolder.toString() + "/" + tenantName + "_tmp" + i + ".txt";
    }

    private void setAttribute(final Path path, final String attribName, final String attribValue) throws IOException {
        final UserDefinedFileAttributeView userDefinedFileAttributeView = Files.getFileAttributeView(path, UserDefinedFileAttributeView.class);
        userDefinedFileAttributeView.write(attribName, defaultCharset().encode(attribValue));
    }

    private String getAttribute(final Path path, final String attribName) throws IOException {
        final UserDefinedFileAttributeView userDefinedFileAttributeView = Files.getFileAttributeView(path, UserDefinedFileAttributeView.class);
        final ByteBuffer buf = ByteBuffer.allocate(MAX_ATTRIB_SIZE);
        userDefinedFileAttributeView.read(attribName, buf);
        buf.flip();
        return defaultCharset().decode(buf).toString();
    }

    private Path createFileIfNotExists(final String fileName) throws IOException {
        final Path path = Paths.get(fileName);
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        return path;
    }

    private Path createDirectoriesIfNotExists(final String fileName) throws IOException {
        final Path path = Paths.get(fileName);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        return path;
    }
}
