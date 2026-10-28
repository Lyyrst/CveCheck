package fr.lyyrst.cvecheck.config.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CveDeltaDto(
        @JsonProperty("fetchTime") String fetchTime,
        @JsonProperty("new") List<DeltaItemDto> newCves,
        @JsonProperty("updated") List<DeltaItemDto> updatedCves
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DeltaItemDto(
            @JsonProperty("cveId") String cveId,
            @JsonProperty("cvePath") String cvePath
    ) {}
}