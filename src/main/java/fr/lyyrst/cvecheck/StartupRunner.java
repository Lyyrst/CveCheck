package fr.lyyrst.cvecheck;

import fr.lyyrst.cvecheck.service.CveIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupRunner.class);
    private final CveIngestionService cveIngestionService;

    public StartupRunner(CveIngestionService cveIngestionService) {
        this.cveIngestionService = cveIngestionService;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Initialisation de l'ingestion CVE au démarrage du serveur...");
        cveIngestionService.runIngestion();
    }
}