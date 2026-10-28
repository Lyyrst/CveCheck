package fr.lyyrst.cvecheck.config.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NvdCveResponseDto(
        @JsonProperty("totalResults") Integer totalResults,
        @JsonProperty("vulnerabilities") List<NvdVulnerabilityWrapperDto> vulnerabilities
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NvdVulnerabilityWrapperDto(
            @JsonProperty("cve") NvdCveDto cve
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NvdCveDto(
            @JsonProperty("id") String id,
            @JsonProperty("published") String published,
            @JsonProperty("lastModified") String lastModified,
            @JsonProperty("descriptions") List<DescriptionDto> descriptions,
            @JsonProperty("metrics") MetricsDto metrics,
            @JsonProperty("configurations") List<ConfigurationDto> configurations
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DescriptionDto(
            @JsonProperty("lang") String lang,
            @JsonProperty("value") String value
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MetricsDto(
            @JsonProperty("cvssMetricV31") List<CvssV31ContainerDto> cvssMetricV31,
            @JsonProperty("cvssMetricV30") List<CvssV31ContainerDto> cvssMetricV30
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CvssV31ContainerDto(
            @JsonProperty("cvssData") CvssDataDto cvssData
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CvssDataDto(
            @JsonProperty("baseScore") Float baseScore
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ConfigurationDto(
            @JsonProperty("nodes") List<NodeDto> nodes
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NodeDto(
            @JsonProperty("cpeMatch") List<CpeMatchDto> cpeMatch
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CpeMatchDto(
            @JsonProperty("criteria") String criteria
    ) {}
}
