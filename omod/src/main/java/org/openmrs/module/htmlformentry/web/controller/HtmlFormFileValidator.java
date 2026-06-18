package org.openmrs.module.htmlformentry.web.util;

import org.openmrs.util.OpenmrsUtil;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class HtmlFormFileValidator {

    private HtmlFormFileValidator() {
    }

    public static File validate(String filePath) throws IOException {

        if (!StringUtils.hasText(filePath)) {
            throw new IllegalArgumentException("File path is required");
        }

        Path baseDir = Paths.get(
                OpenmrsUtil.getApplicationDataDirectory())
                .toRealPath();

        Path requestedFile = Paths.get(filePath)
                .toRealPath();

        if (!requestedFile.startsWith(baseDir)) {
            throw new SecurityException(
                    "File must reside within the OpenMRS application data directory");
        }

        File file = requestedFile.toFile();

        if (!file.exists()) {
            throw new IllegalArgumentException(
                    "File does not exist");
        }

        if (!file.isFile()) {
            throw new IllegalArgumentException(
                    "Path does not reference a file");
        }

        if (!file.canRead()) {
            throw new IllegalArgumentException(
                    "File is not readable");
        }

        String name = file.getName().toLowerCase();

        if (!name.endsWith(".xml")
                && !name.endsWith(".html")
                && !name.endsWith(".htm")) {

            throw new IllegalArgumentException(
                    "Only XML and HTML files may be previewed");
        }

        return file;
    }
}