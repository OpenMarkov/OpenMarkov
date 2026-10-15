package github;

import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.gui.configuration.JavaSerializationUtils;
import org.openmarkov.java.classUtils.ClassUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class NetsCache {
    
    enum CacheMethod {
        USES_LOCAL_CACHE, USES_GITHUB_FILE_REFS;
    }
    
    private static final File RESOURCE_DIRECTORY = ClassUtils.getResourceAsFile(NetsRepository.class, "/integrationTests")
                                                             .getParentFile();
    private static final File LOCAL_REPOSITORIES_CACHE_TRACKER_FILE = new File(NetsCache.RESOURCE_DIRECTORY, "clone_of_probmodelxml_networks_cache.json").getAbsoluteFile();
    private static final File LOCAL_REPOSITORIES_DIR = new File(NetsCache.RESOURCE_DIRECTORY, "clone_of_probmodelxml_networks").getAbsoluteFile();
    private static final Path LOCAL_REPOSITORIES_PATH = LOCAL_REPOSITORIES_DIR.toPath();
    
    static Stream<RepositoryFile> resolveCache() {
        try {
            NetsCache.LOCAL_REPOSITORIES_DIR.mkdirs();
        } catch (RuntimeException ignored) {
        }
        
        @Nullable List<GitHubApi.GitHubFileRef> gitHubFileRefs = null;
        try {
            gitHubFileRefs = GitHubApi
                    .streamOfGitHubFiles("OpenMarkov/probmodelxmlnetworks", "main")
                    .filter(jsonFileRead -> jsonFileRead.relativePath()
                                                        .getLast()
                                                        .endsWith(".pgmx"))
                    .toList();
        } catch (IOException e) {
        }
        
        ArrayList<RepositoryFileCache> localCaches = new ArrayList<>();
        try {
            localCaches = JavaSerializationUtils
                    .deserialize(Files.readString(NetsCache.LOCAL_REPOSITORIES_CACHE_TRACKER_FILE.toPath()));
        } catch (RuntimeException | IOException e) {
            NetsCache.LOCAL_REPOSITORIES_CACHE_TRACKER_FILE.delete();
        }
        NetsCache.reloadCache(localCaches, gitHubFileRefs);
        
        if (gitHubFileRefs == null) {
            return localCaches.stream().map(v -> new RepositoryFile(CacheMethod.USES_LOCAL_CACHE, v.fileRef));
        }
        
        
        var localCachesByPath = localCaches.stream().collect(Collectors.toMap(v -> v.fileRef.relativePath(), v -> v));
        var remoteFilesByPath = gitHubFileRefs.stream()
                                                 .collect(Collectors.toMap(GitHubApi.GitHubFileRef::relativePath, v -> v));
        Stream<List<String>> allPaths = Stream.concat(localCachesByPath.keySet().stream(), remoteFilesByPath.keySet()
                                                                                                            .stream())
                                              .distinct();
        return allPaths
                .sorted(NetsCache.getListComparator())
                .map(path -> {
                    System.out.println("Resolving URL for " + path);
                    if (localCachesByPath.containsKey(path)) {
                        RepositoryFile repositoryFile = new RepositoryFile(CacheMethod.USES_LOCAL_CACHE, localCachesByPath.get(path).fileRef);
                        try (var stream = repositoryFile.resolveURL().openStream()) {
                            System.out.println("URL is being used from the FileSystem");
                            return repositoryFile;
                        } catch (IOException ignored) {
                            System.err.println("Cannot use URL from FileSystem due to " + ignored);
                            ignored.printStackTrace();
                        }
                    }
                    if (remoteFilesByPath.containsKey(path)) {
                        RepositoryFile repositoryFile = new RepositoryFile(CacheMethod.USES_GITHUB_FILE_REFS, remoteFilesByPath.get(path));
                        try (var stream = repositoryFile.resolveURL().openStream()) {
                            System.out.println("URL is being used from an HTTP URL");
                            return repositoryFile;
                        } catch (IOException ignored) {
                            System.err.println("Cannot use URL from HTTP URL due to " + ignored);
                            ignored.printStackTrace();
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull);
    }
    
    record RepositoryFile(CacheMethod cacheMethod, GitHubApi.GitHubFileRef gitHubFileRef) {
        
        public URL resolveURL() {
            return switch (this.cacheMethod) {
                case USES_GITHUB_FILE_REFS -> this.gitHubFileRef.href();
                case USES_LOCAL_CACHE -> {
                    var resultingFile = NetsCache.getFileFromComponents(
                            NetsCache.LOCAL_REPOSITORIES_DIR, this.gitHubFileRef.relativePath());
                    try {
                        yield resultingFile.toURI().toURL();
                    } catch (MalformedURLException e) {
                        throw new UnreachableException(e);
                    }
                }
            };
        }
        
    }
    
    private static void reloadCache(@NotNull ArrayList<RepositoryFileCache> localCaches, @Nullable List<GitHubApi.GitHubFileRef> gitHubFileRefs) {
        NetsCache.reloadCache_removeModifiedFiles(localCaches);
        if (gitHubFileRefs != null) {
            var gitHubFilesByPath = gitHubFileRefs.stream()
                                                        .collect(Collectors.toMap(
                                                                GitHubApi.GitHubFileRef::relativePath,
                                                                v -> v));
            NetsCache.reloadCache_removeOldFiles(localCaches, gitHubFilesByPath);
            NetsCache.reloadCache_downloadMissingFiles(localCaches, gitHubFilesByPath);
        }
        try {
            var asJson = JavaSerializationUtils.serialize(localCaches);
            Files.writeString(NetsCache.LOCAL_REPOSITORIES_CACHE_TRACKER_FILE.toPath(), asJson);
        } catch (IOException e) {
            throw new UnreachableException(e);
        }
        NetsCache.reloadCache_removeExternalFiles(localCaches);
        
    }
    
    private static void reloadCache_removeExternalFiles(@NotNull ArrayList<RepositoryFileCache> localCaches) {
        var localFiles = localCaches.stream()
                                    .map(cache -> cache.fileRef.relativePath())
                                    .collect(Collectors.toSet());
        List<File> filesInLocalDir;
        try {
            filesInLocalDir = Files.walk(NetsCache.LOCAL_REPOSITORIES_DIR.toPath())
                                   .map(Path::toFile)
                                   .filter(File::isFile)
                                   .map(File::getAbsoluteFile)
                                   .toList();
        } catch (IOException ignored) {
            return;
        }
        for (File fileInLocalDir : filesInLocalDir) {
            var fileInLocalPath = fileInLocalDir.toPath();
            var pathList = IntStream.range(NetsCache.LOCAL_REPOSITORIES_PATH.getNameCount(), fileInLocalPath.getNameCount())
                                    .mapToObj(i -> fileInLocalPath.getName(i).toFile().getName())
                                    .toList();
            if (!localFiles.contains(pathList)) {
                fileInLocalDir.delete();
            }
        }
        
    }
    
    private static void reloadCache_removeModifiedFiles(@NotNull ArrayList<RepositoryFileCache> localCaches) {
        localCaches.removeIf(cache -> {
            File cacheFile = getFileFromComponents(NetsCache.LOCAL_REPOSITORIES_DIR, cache.fileRef().relativePath());
            boolean shouldRemove = !cacheFile.exists();
            try {
                shouldRemove = shouldRemove ||
                        !DigestUtils.sha256Hex(Files.readString(cacheFile.toPath())).equals(cache.hash());
            } catch (IOException e) {
                shouldRemove = true;
            }
            if (shouldRemove) {
                cacheFile.delete();
            }
            return shouldRemove;
        });
    }
    
    private static void reloadCache_removeOldFiles(@NotNull ArrayList<RepositoryFileCache> localCaches, Map<List<String>, GitHubApi.@NotNull GitHubFileRef> gitHubFilesByPath) {
        localCaches.removeIf(cache -> {
            boolean shouldRemove = !gitHubFilesByPath.containsKey(cache.fileRef().relativePath()) ||
                    !gitHubFilesByPath.get(cache.fileRef.relativePath())
                                         .blobHash()
                                         .equals(cache.fileRef().blobHash());
            if (shouldRemove) {
                getFileFromComponents(NetsCache.LOCAL_REPOSITORIES_DIR, cache.fileRef().relativePath()).delete();
            }
            return shouldRemove;
        });
    }
    
    private static void reloadCache_downloadMissingFiles(@NotNull ArrayList<RepositoryFileCache> localCaches, Map<List<String>, GitHubApi.@NotNull GitHubFileRef> gitHubFilesByPath) {
        var localFiles = localCaches.stream()
                                    .map(cache -> cache.fileRef.relativePath())
                                    .collect(Collectors.toSet());
        
        for (var gitHubFileRef : gitHubFilesByPath.entrySet()) {
            if (!localFiles.contains(gitHubFileRef.getKey())) {
                var gitHubFileRes = gitHubFileRef.getValue();
                try {
                    NetsCache.downloadFileToCache(localCaches, gitHubFileRes);
                } catch (IOException ignored) {
                }
            }
        }
    }
    
    private static void downloadFileToCache(@NotNull ArrayList<RepositoryFileCache> localCaches, GitHubApi.GitHubFileRef gitHubFileRes) throws IOException {
        File resultingFile = NetsCache.getFileFromComponents(NetsCache.LOCAL_REPOSITORIES_DIR, gitHubFileRes.relativePath());
        if (resultingFile.getParentFile() != null) {
            resultingFile.getParentFile().mkdirs();
        }
        System.out.println("Downloading " + gitHubFileRes.href() + " into " + resultingFile.getAbsolutePath());
        try {
            String fileContents = new String(
                    gitHubFileRes.href().openStream().readAllBytes(), StandardCharsets.UTF_8);
            Files.writeString(resultingFile.toPath(), fileContents);
            localCaches.add(new RepositoryFileCache(gitHubFileRes, DigestUtils.sha256Hex(fileContents)));
            System.out.println("Downloaded!");
        }catch (IOException|RuntimeException exception){
            System.out.println("Could not download " + gitHubFileRes.href());
            throw exception;
        }
    }
    
    record RepositoryFileCache(GitHubApi.GitHubFileRef fileRef, String hash) implements Serializable {
    }
    
    private static @NotNull File getFileFromComponents(File parent, List<String> pathComponents) {
        var resultingFile = parent;
        for (var pathComponent : pathComponents) {
            resultingFile = new File(resultingFile, pathComponent);
        }
        return resultingFile;
    }
    
    private static @NotNull Comparator<List<String>> getListComparator() {
        return (l1, l2) -> {
            int maxLength = Math.max(l1.size(), l2.size());
            for (int i = 0; i < maxLength; i++) {
                if (l1.size() <= i) {
                    return -1;
                }
                if (l2.size() <= i) {
                    return 1;
                }
                int comparison = l1.get(i).compareTo(l2.get(i));
                if (comparison != 0) {
                    return comparison;
                }
            }
            return 0;
        };
    }
}
