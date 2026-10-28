package fr.lyyrst.cvecheck.config.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CveOrgResponseDto(
        @JsonProperty("cveRecords") List<CveOrgRecordDto> cveRecords,
        @JsonProperty("totalCount") Integer totalCount
) {}
