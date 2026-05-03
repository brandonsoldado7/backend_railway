package neuro_activity.backend.service;

import neuro_activity.backend.dto.CollabDtos.*;
import neuro_activity.backend.model.CollabSession;
import neuro_activity.backend.repository.CollabSessionRepository;
import neuro_activity.backend.repository.DiagramaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CollabService {

    @Autowired private CollabSessionRepository sessionRepo;
    @Autowired private DiagramaRepository       diagramaRepo;
    @Autowired private SimpMessagingTemplate    broker;

    // ── JOIN ─────────────────────────────────────────────────────

    public void join(JoinRequest req) {
        System.out.println("[JOIN] usuarioId=" + req.usuarioId + " | diagramaId=" + req.diagramaId);

        CollabSession session = sessionRepo
            .findByDiagramaIdAndUsuarioId(req.diagramaId, req.usuarioId)
            .orElse(new CollabSession());

        session.setDiagramaId(req.diagramaId);
        session.setUsuarioId(req.usuarioId);
        session.setNombre(req.nombre);
        session.setAvatarInitial(req.avatarInitial);
        session.setAvatarColor(req.avatarColor);
        session.setLastSeen(new Date());
        sessionRepo.save(session);

        List<CollabSession> todas = sessionRepo.findByDiagramaId(req.diagramaId);
        System.out.println("[JOIN] Sesiones en BD: " + todas.size());
        todas.forEach(s -> System.out.println("  -> " + s.getUsuarioId() + " | " + s.getNombre()));

        sendCurrentDiagram(req.diagramaId);
        broadcastPresence(req.diagramaId);
    }

    // ── HEARTBEAT ────────────────────────────────────────────────

    public void heartbeat(HeartbeatRequest req) {
        sessionRepo
            .findByDiagramaIdAndUsuarioId(req.diagramaId, req.usuarioId)
            .ifPresent(s -> {
                s.setLastSeen(new Date());
                sessionRepo.save(s);
                broadcastPresence(req.diagramaId);
            });
    }

    // ── LEAVE ────────────────────────────────────────────────────

    public void leave(String diagramaId, String usuarioId) {
        sessionRepo.deleteByDiagramaIdAndUsuarioId(diagramaId, usuarioId);
        broadcastPresence(diagramaId);
    }

    // ── BROADCAST MODELO (llamado desde DiagramaService al guardar) ──

    public void broadcastDiagram(String diagramaId, String json, long version) {
        broker.convertAndSend(
            "/topic/diagram." + diagramaId,
            new DiagramPayload(diagramaId, json, version)
        );
    }

    // ── BROADCAST MODELO (llamado desde WebSocket update, version = now) ──

    public void broadcastDiagram(String diagramaId, String json) {
        broadcastDiagram(diagramaId, json, System.currentTimeMillis());
    }

    // ── PÚBLICO para CollabSessionCleaner ────────────────────────

    public void broadcastPresencePublic(String diagramaId) {
        broadcastPresence(diagramaId);
    }

    // ── helpers ──────────────────────────────────────────────────

    private void sendCurrentDiagram(String diagramaId) {
        diagramaRepo.findById(diagramaId).ifPresent(d -> {
            long version = d.getUpdated_at() != null
                ? d.getUpdated_at().getTime() : 0L;
            broker.convertAndSend(
                "/topic/diagram." + diagramaId,
                new DiagramPayload(diagramaId, d.getJson(), version)
            );
        });
    }

    private void broadcastPresence(String diagramaId) {
        List<ActiveUser> users = sessionRepo
            .findByDiagramaId(diagramaId)
            .stream()
            .map(s -> new ActiveUser(
                s.getUsuarioId(), s.getNombre(),
                s.getAvatarInitial(), s.getAvatarColor()))
            .collect(Collectors.toList());

        broker.convertAndSend(
            "/topic/presence." + diagramaId,
            new PresencePayload(users)
        );
    }
    public void broadcastCursor(CursorRequest req) {
    broker.convertAndSend(
        "/topic/cursor." + req.diagramaId,
        new CursorPayload(
            req.usuarioId, req.nombre, req.avatarColor,
            req.x, req.y, req.nodeKey
        )
    );
}
}