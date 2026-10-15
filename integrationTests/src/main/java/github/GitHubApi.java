package github;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openmarkov.core.exception.UnreachableException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class GitHubApi {
    /**
     * @param relativePath the folders and the name of the file
     * @param blobHash     the hash of the contents of the file, which changes when they change
     * @param href         where the contents of the file can be read
     */
    record GitHubFileRef(List<String> relativePath, String blobHash, URL href) implements Serializable {
    }

    /**
     * Lists all files inside a GitHub repository
     *
     * @param repository the owner and the name of the repository, such as "OpenMarkov/probmodelxmlnetworks"
     */
    static Stream<GitHubFileRef> streamOfGitHubFiles(String repository, String branch) throws IOException {
        JSONObject tree = GitHubApi.readJsonFromUrl(
                "https://api.github.com/repos/" + repository + "/git/trees/" + branch + "?recursive=1");
        JSONArray entries = tree.getJSONArray("tree");
        return IntStream.range(0, entries.length())
                        .mapToObj(entries::getJSONObject)
                        .filter(entry -> entry.getString("type").equals("blob"))
                        .map(entry -> {
                            String path = entry.getString("path");
                            try {
                                URL href = new URI("https", "raw.githubusercontent.com",
                                                   "/" + repository + "/" + branch + "/" + path, null).toURL();
                                return new GitHubFileRef(Arrays.asList(path.split("/")), entry.getString("sha"), href);
                            } catch (URISyntaxException | MalformedURLException e) {
                                throw new UnreachableException(e);
                            }
                        });
    }

    private static JSONObject readJsonFromUrl(String url) throws IOException {
        try (InputStream inputStream = URI.create(url).toURL().openStream()) {
            return new JSONObject(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
        }
    }
}
