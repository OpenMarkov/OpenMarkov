package org.openmarkov.full;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openmarkov.gui.license.License;
import org.openmarkov.gui.license.LicenseHolder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

@EnabledIf(value = "org.openmarkov.full.AllLicensesAreVisited#isAfterReviewDate",
        disabledReason = "Review of licenses is postponed until 07/09/2026")
public class AllLicensesAreVisited {
    
    private static boolean isAfterReviewDate() {
        var date = LocalDateTime.of(2026, 9, 7, 0, 0, 0);
        return LocalDateTime.now().isAfter(date);
    }
    
    public static Set<String> VERIFIED_LICENSES_FOR_OPENMARKOV = Set.of(
            "/third-party-licenses/creative commons zero v1.0 universal - deed.html"
    );
    
    static Stream<LicenseByResource> licensesByResource() {
        var licenses = LicenseHolder.LICENSE_HOLDERS.stream().map(LicenseHolder::licenses).flatMap(Collection::stream)
                                                    .toList();
        var licenseResourceMap = new HashMap<String, List<License>>();
        for (var license : licenses) {
            if (!licenseResourceMap.containsKey(license.resource)) {
                licenseResourceMap.put(license.resource, new ArrayList<>());
            }
            licenseResourceMap.get(license.resource).add(license);
        }
        return licenseResourceMap.entrySet()
                                 .stream()
                                 .map(entry -> new LicenseByResource(entry.getKey(), entry.getValue()));
    }
    
    record LicenseByResource(String licenseResource, List<License> licenses) {
    
    }
    
    @ParameterizedTest
    @MethodSource("licensesByResource")
    public void licenseIsVerifiedByCISIAD(LicenseByResource licenseByResource) {
        if (licenseByResource.licenseResource == null) {
            return;
        }
        if (!VERIFIED_LICENSES_FOR_OPENMARKOV.contains(licenseByResource.licenseResource)) {
            fail("License from file " + licenseByResource.licenseResource + " (URL: " + licenseByResource.licenses.getFirst().URL + ") has not been added to verified licenses, meaning it might not be a valid license for OpenMarkov. This affects the following artifacts: " + System.lineSeparator() + this.nameLicenses(licenseByResource.licenses));
        }
    }
    
    @ParameterizedTest
    @MethodSource("licensesByResource")
    public void licenseHasAResourceToBeDisplayedInGUI(LicenseByResource licenseByResource) {
        try {
            String licenseContent = new String(LicenseHolder.RESOURCE_RESOLVER.getResourceAsStream(licenseByResource.licenseResource)
                                                                              .readAllBytes());
        } catch (IOException | NullPointerException e) {
            var licensesByURL = licenseByResource.licenses.stream()
                                                          .collect(Collectors.groupingBy(license -> license.URL));
            var licensesByURLDescription = licensesByURL.entrySet().stream()
                                                        .sorted(Map.Entry.comparingByKey())
                                                        .map(entry -> "- " + entry.getKey() + " (from artifact(s): " + entry.getValue()
                                                                                                                            .stream()
                                                                                                                            .map(AllLicensesAreVisited::nameLicense)
                                                                                                                            .distinct()
                                                                                                                            .sorted()
                                                                                                                            .collect(Collectors.joining(", ")) + ")")
                                                        .collect(Collectors.joining(System.lineSeparator()));
            
            fail("There is no license file for some licenses " + System.lineSeparator() + licensesByURLDescription);
        }
    }
    
    String nameLicenses(List<License> licenses) {
        return licenses.stream()
                       .map(license -> "-  " + AllLicensesAreVisited.nameLicense(license))
                       .distinct()
                       .collect(Collectors.joining(System.lineSeparator()));
    }
    
    private static @NonNull String nameLicense(License license) {
        return license.holder().descriptor();
    }
    
    static Stream<LicenseHolder> licenseHoldersStream() {
        return LicenseHolder.LICENSE_HOLDERS.stream();
    }
    
    
    @ParameterizedTest
    @MethodSource("licenseHoldersStream")
    public void holderHasAtLeaseOneLicense(LicenseHolder licenseHolder) {
        if (!licenseHolder.licenses().isEmpty()) {
            return;
        }
        fail("There is no license found for holder " + licenseHolder.descriptor() + ". It probably has one, but finding it might require some Internet-browsing.");
    }
    
    
}
