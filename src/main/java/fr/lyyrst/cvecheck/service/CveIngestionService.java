package fr.lyyrst.cvecheck.service;

import fr.lyyrst.cvecheck.config.dto.NvdCveResponseDto;
import fr.lyyrst.cvecheck.persistence.entities.CveEntity;
import fr.lyyrst.cvecheck.persistence.entities.IngestionStateEntity;
import fr.lyyrst.cvecheck.persistence.repository.ClientSubscriptionRepository;
import fr.lyyrst.cvecheck.persistence.repository.CveRepository;
import fr.lyyrst.cvecheck.persistence.repository.IngestionStateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class CveIngestionService {

    private static final Logger log = LoggerFactory.getLogger(CveIngestionService.class);
    private static final String GLOBAL_INGESTION_ID = "NVD_GLOBAL_STATE";

    private final WebClient nvdWebClient;
    private final CveRepository cveRepository;
    private final IngestionStateRepository ingestionStateRepository;
    private final ClientSubscriptionRepository clientSubscriptionRepository;
    private final JsonMapper jsonMapper;

    public CveIngestionService(WebClient nvdWebClient,
                               CveRepository cveRepository,
                               IngestionStateRepository ingestionStateRepository,
                               ClientSubscriptionRepository clientSubscriptionRepository,
                               JsonMapper jsonMapper) {
        this.nvdWebClient = nvdWebClient;
        this.cveRepository = cveRepository;
        this.ingestionStateRepository = ingestionStateRepository;
        this.clientSubscriptionRepository = clientSubscriptionRepository;
        this.jsonMapper = jsonMapper;
    }

    @Transactional
    public void runIngestion() {
        Set<String> trackedTechs = clientSubscriptionRepository.findAllSubscribedTechnologies();
        if (trackedTechs.isEmpty()) {
            log.info("Aucune technologie surveillée en base, fin de l'ingestion.");
            return;
        }

        IngestionStateEntity state = ingestionStateRepository.findById(GLOBAL_INGESTION_ID)
                .orElseGet(() -> new IngestionStateEntity(GLOBAL_INGESTION_ID, Instant.now().minus(7, ChronoUnit.DAYS)));

        Instant startDate = state.getLastPollTimestamp().truncatedTo(ChronoUnit.SECONDS);
        Instant endDate = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        // NVD impose que la plage ne dépasse pas 120 jours consécutifs
        if (startDate.isBefore(endDate.minus(119, ChronoUnit.DAYS))) {
            startDate = endDate.minus(119, ChronoUnit.DAYS);
        }

        String formattedStart = DateTimeFormatter.ISO_INSTANT.format(startDate);
        String formattedEnd = DateTimeFormatter.ISO_INSTANT.format(endDate);

        log.info("Lancement de l'ingestion NVD depuis {} jusqu'à {} pour les technologies : {}",
                formattedStart, formattedEnd, trackedTechs);

        String jsonResponse;
        try {
            jsonResponse = nvdWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("lastModStartDate", formattedStart)
                            .queryParam("lastModEndDate", formattedEnd)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.error("Échec de l'appel à l'API NVD : {}", e.getMessage());
            return;
        }

        if (jsonResponse == null || jsonResponse.isBlank()) {
            log.info("Réponse NVD vide.");
            return;
        }

        NvdCveResponseDto responseDto;
        try {
            responseDto = jsonMapper.readValue(jsonResponse, NvdCveResponseDto.class);
        } catch (Exception e) {
            log.error("Erreur lors du parsing JSON de la réponse NVD : {}", e.getMessage());
            return;
        }

        List<NvdCveResponseDto.NvdVulnerabilityWrapperDto> items = responseDto.vulnerabilities();
        if (items == null || items.isEmpty()) {
            log.info("Aucune CVE modifiée sur cette plage temporelle dans NVD.");
            state.setLastPollTimestamp(endDate);
            state.setUpdatedAt(Instant.now());
            ingestionStateRepository.save(state);
            return;
        }

        log.info("{} CVE reçues de NVD. Filtrage sur nos technologies...", items.size());

        int savedCount = 0;
        for (var wrapper : items) {
            var cve = wrapper.cve();
            if (cve == null || cve.id() == null) continue;

            Optional<String> matchedTech = matchTechnology(cve, trackedTechs);
            if (matchedTech.isPresent()) {
                saveCve(cve, matchedTech.get());
                savedCount++;
            }
        }

        state.setLastPollTimestamp(endDate);
        state.setUpdatedAt(Instant.now());
        ingestionStateRepository.save(state);

        log.info("Ingestion NVD terminée avec succès : {} CVE enregistrées pour nos technologies.", savedCount);
    }

    private Optional<String> matchTechnology(NvdCveResponseDto.NvdCveDto cve, Set<String> trackedTechs) {
        // 1. Analyse des CPE dans les configurations
        if (cve.configurations() != null) {
            for (var config : cve.configurations()) {
                if (config.nodes() == null) continue;
                for (var node : config.nodes()) {
                    if (node.cpeMatch() == null) continue;
                    for (var cpe : node.cpeMatch()) {
                        String criteria = cpe.criteria() != null ? cpe.criteria().toLowerCase() : "";
                        for (String tech : trackedTechs) {
                            if (criteria.contains(":" + tech.toLowerCase() + ":") || criteria.contains(":" + tech.toLowerCase())) {
                                return Optional.of(tech);
                            }
                        }
                    }
                }
            }
        }

        // 2. Repli sur le texte de description
        if (cve.descriptions() != null) {
            for (var desc : cve.descriptions()) {
                String val = desc.value() != null ? desc.value().toLowerCase() : "";
                for (String tech : trackedTechs) {
                    if (val.contains(tech.toLowerCase())) {
                        return Optional.of(tech);
                    }
                }
            }
        }

        return Optional.empty();
    }

    private void saveCve(NvdCveResponseDto.NvdCveDto dto, String technology) {
        CveEntity entity = cveRepository.findById(dto.id()).orElseGet(CveEntity::new);
        entity.setCveId(dto.id());
        entity.setTechnology(technology);

        // Parsing robuste du format NVD (qui omet souvent le trailing 'Z')
        entity.setPublishedAt(parseNvdDate(dto.published()));

        if (dto.descriptions() != null && !dto.descriptions().isEmpty()) {
            dto.descriptions().stream()
                    .filter(d -> "en".equalsIgnoreCase(d.lang()))
                    .findFirst()
                    .ifPresentOrElse(
                            d -> entity.setDescription(d.value()),
                            () -> entity.setDescription(dto.descriptions().getFirst().value())
                    );
        }

        if (dto.metrics() != null) {
            if (dto.metrics().cvssMetricV31() != null && !dto.metrics().cvssMetricV31().isEmpty()) {
                entity.setCvssScore(dto.metrics().cvssMetricV31().getFirst().cvssData().baseScore());
            } else if (dto.metrics().cvssMetricV30() != null && !dto.metrics().cvssMetricV30().isEmpty()) {
                entity.setCvssScore(dto.metrics().cvssMetricV30().getFirst().cvssData().baseScore());
            }
        }

        try {
            entity.setRawPayload(jsonMapper.writeValueAsString(dto));
        } catch (Exception e) {
            entity.setRawPayload("{}");
        }

        cveRepository.save(entity);
        log.info("CVE sauvegardée en base : {} ({}) - Publiée le : {}",
                entity.getCveId(), entity.getTechnology(), entity.getPublishedAt());
    }

    private Instant parseNvdDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            // Si la chaîne se termine déjà par Z ou un offset, Instant.parse suffit
            if (dateStr.endsWith("Z") || dateStr.contains("+")) {
                return Instant.parse(dateStr);
            }
            // Format classique NVD : "2026-10-25T14:32:00.000" (considéré en UTC par le NIST)
            return java.time.LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .toInstant(java.time.ZoneOffset.UTC);
        } catch (Exception e) {
            log.warn("Impossible de parser la date NVD '{}' : {}", dateStr, e.getMessage());
            return null;
        }
    }
}