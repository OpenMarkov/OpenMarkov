package bitbucket;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openmarkov.core.exception.UnreachableException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class BitbucketApi {
    record BitbucketFileRef(List<String> relativePath, String commitHash, URL href) implements Serializable {
    }
    
    /**
     * Lists all files inside a Bitbucket repository
     */
    static Stream<BitbucketFileRef> streamOfBitbucketFiles(String repository, String branch) throws IOException {
        String nextPageToRead = "https://api.bitbucket.org/2.0/repositories/" + repository + "/src/" + branch + "/?max_depth=100&pagelen=100";
        ArrayList<JSONObject> readFiles = new ArrayList<>();
        while (nextPageToRead != null) {
            JSONObject currentPage = BitbucketApi.readJsonFromUrl(nextPageToRead);
            readFiles.addAll(BitbucketApi.jsonArrayToJsonList(currentPage.getJSONArray("values")).stream()
                                         .map(JSONObject.class::cast)
                                         .toList());
            nextPageToRead = currentPage.optString("next", null);
        }
        return readFiles.stream().map(readFile -> {
            var path = Arrays.asList(readFile.getString("path").split("/"));
            var commitHash = readFile.getJSONObject("commit").getString("hash");
            var href = URI.create(readFile.getJSONObject("links").getJSONObject("self").getString("href"));
            try {
                return new BitbucketFileRef(path, commitHash, href.toURL());
            } catch (MalformedURLException e) {
                throw new UnreachableException(e);
            }
        });
    }
    
    private static ArrayList<JSONObject> jsonArrayToJsonList(JSONArray jsonArray) {
        return IntStream.range(0, jsonArray.length())
                        .mapToObj(jsonArray::get)
                        .map(JSONObject.class::cast)
                        .collect(Collectors.toCollection(() -> new ArrayList<>(jsonArray.length())));
    }
    
    private static JSONObject readJsonFromUrl(String url) throws IOException {
        try (InputStream inputStream = URI.create(url).toURL().openStream()) {
            return new JSONObject(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
        }
    }
}
