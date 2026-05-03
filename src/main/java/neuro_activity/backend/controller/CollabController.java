package neuro_activity.backend.controller;

import neuro_activity.backend.dto.CollabDtos.*;
import neuro_activity.backend.service.CollabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.stereotype.Controller;

@Controller
public class CollabController {

    @Autowired
    private CollabService collabService;

    @MessageMapping("/diagram.{diagramaId}.join")
    public void join(@DestinationVariable String diagramaId,
                     JoinRequest req) {
        req.diagramaId = diagramaId;
        collabService.join(req);
    }

    @MessageMapping("/diagram.{diagramaId}.heartbeat")
    public void heartbeat(@DestinationVariable String diagramaId,
                          HeartbeatRequest req) {
        req.diagramaId = diagramaId;
        collabService.heartbeat(req);
    }

    @MessageMapping("/diagram.{diagramaId}.leave")
    public void leave(@DestinationVariable String diagramaId,
                      HeartbeatRequest req) {
        collabService.leave(diagramaId, req.usuarioId);
    }

    @MessageMapping("/diagram.{diagramaId}.update")
    public void update(@DestinationVariable String diagramaId,
                       UpdateRequest req) {
        req.diagramaId = diagramaId;
        collabService.broadcastDiagram(req.diagramaId, req.json);
    }
    @MessageMapping("/diagram.{diagramaId}.cursor")
public void cursor(@DestinationVariable String diagramaId,
                   CursorRequest req) {
    req.diagramaId = diagramaId;
    collabService.broadcastCursor(req);
}
}