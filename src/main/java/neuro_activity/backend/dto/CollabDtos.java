package neuro_activity.backend.dto;

import java.util.List;

public class CollabDtos {

    public static class UpdateRequest {
        public String diagramaId;
        public String json;
        public long   version = System.currentTimeMillis();
    }

    public static class JoinRequest {
        public String diagramaId;
        public String usuarioId;
        public String nombre;
        public String avatarInitial;
        public String avatarColor;
    }

    public static class HeartbeatRequest {
        public String diagramaId;
        public String usuarioId;
    }

    public static class PresencePayload {
        public List<ActiveUser> users;
        public PresencePayload(List<ActiveUser> users) {
            this.users = users;
        }
    }

    public static class ActiveUser {
        public String usuarioId;
        public String nombre;
        public String avatarInitial;
        public String avatarColor;

        public ActiveUser() {}
        public ActiveUser(String usuarioId, String nombre,
                          String avatarInitial, String avatarColor) {
            this.usuarioId     = usuarioId;
            this.nombre        = nombre;
            this.avatarInitial = avatarInitial;
            this.avatarColor   = avatarColor;
        }
    }

    public static class DiagramPayload {
        public String diagramaId;
        public String json;
        public long   version;

        public DiagramPayload(String diagramaId, String json, long version) {
            this.diagramaId = diagramaId;
            this.json       = json;
            this.version    = version;
        }
    }

    // ── Cursor ──────────────────────────────────────────────
    public static class CursorRequest {
        public String  diagramaId;
        public String  usuarioId;
        public String  nombre;
        public String  avatarColor;
        public double  x;
        public double  y;
        public Integer nodeKey;
    }

    public static class CursorPayload {
        public String  usuarioId;
        public String  nombre;
        public String  avatarColor;
        public double  x;
        public double  y;
        public Integer nodeKey;

        public CursorPayload() {}
        public CursorPayload(String usuarioId, String nombre, String avatarColor,
                             double x, double y, Integer nodeKey) {
            this.usuarioId   = usuarioId;
            this.nombre      = nombre;
            this.avatarColor = avatarColor;
            this.x           = x;
            this.y           = y;
            this.nodeKey     = nodeKey;
        }
    }
}