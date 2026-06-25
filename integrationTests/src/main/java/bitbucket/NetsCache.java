package bitbucket;

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
        USES_LOCAL_CACHE, USES_BITBUCKET_FILE_REFS;
    }
    
    private static final File RESOURCE_DIRECTORY = ClassUtils.getResourceAsFile(NetsRepository.class, "/integrationTests")
                                                             .getParentFile();
    private static final File LOCAL_REPOSITORIES_CACHE_TRACKER_FILE = new File(NetsCache.RESOURCE_DIRECTORY, "clone_of_probmodelxml_networks_cache.json").getAbsoluteFile();
    private static final File LOCAL_REPOSITORIES_DIR = new File(NetsCache.RESOURCE_DIRECTORY, "clone_of_probmodelxml_networks").getAbsoluteFile();
    private static final Path LOCAL_REPOSITORIES_PATH = LOCAL_REPOSITORIES_DIR.toPath();
    
    static Stream<BitbucketFile> resolveCache() {
        try {
            NetsCache.LOCAL_REPOSITORIES_DIR.mkdirs();
        } catch (RuntimeException ignored) {
        }
        
        @Nullable List<BitbucketApi.BitbucketFileRef> bitbucketFileRefs = null;
        try {
            bitbucketFileRefs = BitbucketApi
                    .streamOfBitbucketFiles("cisiad/org.probmodelxml.networks", "master")
                    .filter(jsonFileRead -> jsonFileRead.relativePath()
                                                        .getLast()
                                                        .endsWith(".pgmx"))
                    .toList();
        } catch (IOException e) {
        }
        
        ArrayList<BitbucketFileCache> localCaches = new ArrayList<>();
        try {
            localCaches = JavaSerializationUtils
                    .javaDeserialize(Files.readString(NetsCache.LOCAL_REPOSITORIES_CACHE_TRACKER_FILE.toPath()));
        } catch (RuntimeException | IOException e) {
            NetsCache.LOCAL_REPOSITORIES_CACHE_TRACKER_FILE.delete();
        }
        NetsCache.reloadCache(localCaches, bitbucketFileRefs);
        
        if (bitbucketFileRefs == null) {
            return localCaches.stream().map(v -> new BitbucketFile(CacheMethod.USES_LOCAL_CACHE, v.fileRef));
        }
        
        
        var localCachesByPath = localCaches.stream().collect(Collectors.toMap(v -> v.fileRef.relativePath(), v -> v));
        var remoteFilesByPath = bitbucketFileRefs.stream()
                                                 .collect(Collectors.toMap(BitbucketApi.BitbucketFileRef::relativePath, v -> v));
        Stream<List<String>> allPaths = Stream.concat(localCachesByPath.keySet().stream(), remoteFilesByPath.keySet()
                                                                                                            .stream())
                                              .distinct();
        return allPaths
                .sorted(NetsCache.getListComparator())
                .map(path -> {
                    System.out.println("Resolving URL for " + path);
                    if (localCachesByPath.containsKey(path)) {
                        BitbucketFile bitbucketFile = new BitbucketFile(CacheMethod.USES_LOCAL_CACHE, localCachesByPath.get(path).fileRef);
                        try (var stream = bitbucketFile.resolveURL().openStream()) {
                            System.out.println("URL is being used from the FileSystem");
                            return bitbucketFile;
                        } catch (IOException ignored) {
                            System.err.println("Cannot use URL from FileSystem due to " + ignored);
                            ignored.printStackTrace();
                        }
                    }
                    if (remoteFilesByPath.containsKey(path)) {
                        BitbucketFile bitbucketFile = new BitbucketFile(CacheMethod.USES_BITBUCKET_FILE_REFS, remoteFilesByPath.get(path));
                        try (var stream = bitbucketFile.resolveURL().openStream()) {
                            System.out.println("URL is being used from an HTTP URL");
                            return bitbucketFile;
                        } catch (IOException ignored) {
                            System.err.println("Cannot use URL from HTTP URL due to " + ignored);
                            ignored.printStackTrace();
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull);
    }
    
    record BitbucketFile(CacheMethod cacheMethod, BitbucketApi.BitbucketFileRef bitbucketFileRef) {
        
        public URL resolveURL() {
            return switch (this.cacheMethod) {
                case USES_BITBUCKET_FILE_REFS -> this.bitbucketFileRef.href();
                case USES_LOCAL_CACHE -> {
                    var resultingFile = NetsCache.getFileFromComponents(
                            NetsCache.LOCAL_REPOSITORIES_DIR, this.bitbucketFileRef.relativePath());
                    try {
                        yield resultingFile.toURI().toURL();
                    } catch (MalformedURLException e) {
                        throw new UnreachableException(e);
                    }
                }
            };
        }
        
    }
    
    private static void reloadCache(@NotNull ArrayList<BitbucketFileCache> localCaches, @Nullable List<BitbucketApi.BitbucketFileRef> bitbucketFileRefs) {
        NetsCache.reloadCache_removeModifiedFiles(localCaches);
        if (bitbucketFileRefs != null) {
            var bitbucketFilesByPath = bitbucketFileRefs.stream()
                                                        .collect(Collectors.toMap(
                                                                BitbucketApi.BitbucketFileRef::relativePath,
                                                                v -> v));
            NetsCache.reloadCache_removeOldFiles(localCaches, bitbucketFilesByPath);
            NetsCache.reloadCache_downloadMissingFiles(localCaches, bitbucketFilesByPath);
        }
        try {
            var asJson = JavaSerializationUtils.javaSerialize(localCaches);
            Files.writeString(NetsCache.LOCAL_REPOSITORIES_CACHE_TRACKER_FILE.toPath(), asJson);
        } catch (IOException e) {
            throw new UnreachableException(e);
        }
        NetsCache.reloadCache_removeExternalFiles(localCaches);
        
    }
    
    private static void reloadCache_removeExternalFiles(@NotNull ArrayList<BitbucketFileCache> localCaches) {
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
    
    private static void reloadCache_removeModifiedFiles(@NotNull ArrayList<BitbucketFileCache> localCaches) {
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
    
    private static void reloadCache_removeOldFiles(@NotNull ArrayList<BitbucketFileCache> localCaches, Map<List<String>, BitbucketApi.@NotNull BitbucketFileRef> bitbucketFilesByPath) {
        localCaches.removeIf(cache -> {
            boolean shouldRemove = !bitbucketFilesByPath.containsKey(cache.fileRef().relativePath()) ||
                    !bitbucketFilesByPath.get(cache.fileRef.relativePath())
                                         .commitHash()
                                         .equals(cache.fileRef().commitHash());
            if (shouldRemove) {
                getFileFromComponents(NetsCache.LOCAL_REPOSITORIES_DIR, cache.fileRef().relativePath()).delete();
            }
            return shouldRemove;
        });
    }
    
    private static void reloadCache_downloadMissingFiles(@NotNull ArrayList<BitbucketFileCache> localCaches, Map<List<String>, BitbucketApi.@NotNull BitbucketFileRef> bitbucketFilesByPath) {
        var localFiles = localCaches.stream()
                                    .map(cache -> cache.fileRef.relativePath())
                                    .collect(Collectors.toSet());
        
        for (var bitbucketFileRef : bitbucketFilesByPath.entrySet()) {
            if (!localFiles.contains(bitbucketFileRef.getKey())) {
                var bitbucketFileRes = bitbucketFileRef.getValue();
                try {
                    NetsCache.downloadFileToCache(localCaches, bitbucketFileRes);
                } catch (IOException ignored) {
                }
            }
        }
    }
    
    private static void downloadFileToCache(@NotNull ArrayList<BitbucketFileCache> localCaches, BitbucketApi.BitbucketFileRef bitbucketFileRes) throws IOException {
        File resultingFile = NetsCache.getFileFromComponents(NetsCache.LOCAL_REPOSITORIES_DIR, bitbucketFileRes.relativePath());
        if (resultingFile.getParentFile() != null) {
            resultingFile.getParentFile().mkdirs();
        }
        System.out.println("Downloading " + bitbucketFileRes.href() + " into " + resultingFile.getAbsolutePath());
        try {
            String bitbucketFileContents = new String(
                    bitbucketFileRes.href().openStream().readAllBytes(), StandardCharsets.UTF_8);
            Files.writeString(resultingFile.toPath(), bitbucketFileContents);
            localCaches.add(new BitbucketFileCache(bitbucketFileRes, DigestUtils.sha256Hex(bitbucketFileContents)));
            System.out.println("Downloaded!");
        }catch (IOException|RuntimeException exception){
            System.out.println("Could not download " + bitbucketFileRes.href());
            throw exception;
        }
    }
    
    record BitbucketFileCache(BitbucketApi.BitbucketFileRef fileRef, String hash) implements Serializable {
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
