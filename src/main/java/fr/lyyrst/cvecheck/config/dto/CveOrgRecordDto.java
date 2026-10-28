package fr.lyyrst.cvecheck.config.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CveOrgRecordDto(
        @JsonProperty("cveMetadata") CveMetadataDto cveMetadata,
        @JsonProperty("containers") ContainersDto containers
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CveMetadataDto(
            @JsonProperty("cveId") String cveId,
            @JsonProperty("datePublished") String datePublished,
            @JsonProperty("dateUpdated") String dateUpdated
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ContainersDto(
            @JsonProperty("cna") CnaDto cna
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CnaDto(
            @JsonProperty("descriptions") List<DescriptionDto> descriptions,
            @JsonProperty("affected") List<AffectedProductDto> affected,
            @JsonProperty("metrics") List<MetricDto> metrics
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DescriptionDto(
            @JsonProperty("lang") String lang,
            @JsonProperty("value") String value
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AffectedProductDto(
            @JsonProperty("vendor") String vendor,
            @JsonProperty("product") String product
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MetricDto(
            @JsonProperty("cvssV3_1") CvssDto cvssV31,
            @JsonProperty("cvssV3_0") CvssDto cvssV30
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CvssDto(
            @JsonProperty("baseScore") Float baseScore
    ) {}
}