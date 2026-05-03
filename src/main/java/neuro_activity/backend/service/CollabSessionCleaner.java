package neuro_activity.backend.service;

import neuro_activity.backend.repository.CollabSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CollabSessionCleaner {

    @Autowired private CollabSessionRepository sessionRepo;
    @Autowired private CollabService           collabService;

    @Scheduled(fixedDelay = 20_000)
    public void cleanStaleSessions() {
        Date cutoffDate = new Date(System.currentTimeMillis() - 45_000);

        List<String> diagramasAfectados = sessionRepo
            .findByLastSeenBefore(cutoffDate)
            .stream()
            .map(s -> s.getDiagramaId())
            .distinct()
            .collect(Collectors.toList());

        if (diagramasAfectados.isEmpty()) return;

        sessionRepo.deleteByLastSeenBefore(cutoffDate);

        diagramasAfectados.forEach(id -> collabService.broadcastPresencePublic(id));
    }
}