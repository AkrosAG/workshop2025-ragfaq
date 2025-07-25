package ch.akros.workshop.ragfaq;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.springframework.ai.document.Document;

/**
 * Utility class for loading Markdown files from a specified path.
 * This class provides methods to read Markdown files and convert them into a list of {@link Document} objects.
 * It supports reading entire files as single documents or treating each line in the file as a separate document.
 *
 * <p>Usage example:</p>
 * <pre>
 *     List&lt;Document&gt; documents = MarkdownLoader.loadMarkdownFiles("path/to/docs");
 * </pre>
 *
 */
public final class MarkdownLoader {

    private MarkdownLoader() {
    }

    /**
     * Loads all Markdown files from the specified path and returns them as a list of {@link Document} objects.
     *
     * @param docsPath the path to the directory containing Markdown files
     * @return a list of {@link Document} objects containing the content of the Markdown files
     */
    static List<Document> loadMarkdownFiles(String docsPath) {
        var docsUrl = MarkdownLoader.class.getClassLoader().getResource(docsPath);
        assert docsUrl != null : "Documentation path not found: " + docsPath;
        try (var paths = Files.walk(Paths.get(docsUrl.toURI()))) {
            return paths
                .filter(Files::isRegularFile)
                .map(path -> {
                    try {
                        return new Document(Files.readString(path));
                    } catch (IOException e) {
                        throw new IllegalStateException(String.format("Could not read file %s!", path.getFileName()), e);
                    }
                })
                .toList();
        } catch (Exception e) {
            throw new IllegalStateException("Something went wrong during reading Markdown files!", e);
        }
    }

    /**
     * Loads all Markdown files from the specified path and returns them as a list of {@link Document} objects,
     * where each line in the file is treated as a separate document.
     *
     * @param docsPath the path to the directory containing Markdown files
     * @return a list of {@link Document} objects, each representing a line in the Markdown files
     */
    static List<Document> loadMarkdownFilesByLine(String docsPath) {
        var docsUrl = MarkdownLoader.class.getClassLoader().getResource(docsPath);
        assert docsUrl != null : "Documentation path not found: " + docsPath;
        try (var paths = Files.walk(Paths.get(docsUrl.toURI()))) {
            return paths
                .filter(Files::isRegularFile)
                .map(path -> {
                    try {
                        return Files.readAllLines(path).stream()
                            .map(Document::new)
                            .toList();
                    } catch (IOException e) {
                        throw new IllegalStateException(String.format("Could not read file %s!", path.getFileName()), e);
                    }
                })
                .flatMap(List::stream)
                .toList();
        } catch (Exception e) {
            throw new IllegalStateException("Something went wrong during reading Markdown files!", e);
        }
    }
}
