package neuro_activity.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "collab_sessions")
public class CollabSession {

    @Id
    private String id;

    private String diagramaId;
    private String usuarioId;
    private String nombre;
    private String avatarInitial;
    private String avatarColor;

    @Indexed(expireAfterSeconds = 90)  // ← era 30, subido a 90
    private Date lastSeen;

    public CollabSession() {}

    public CollabSession(String diagramaId, String usuarioId,
                         String nombre, String avatarInitial,
                         String avatarColor) {
        this.diagramaId    = diagramaId;
        this.usuarioId     = usuarioId;
        this.nombre        = nombre;
        this.avatarInitial = avatarInitial;
        this.avatarColor   = avatarColor;
        this.lastSeen      = new Date();
    }

    public String getId()                                  { return id; }
    public void   setId(String id)                        { this.id = id; }
    public String getDiagramaId()                         { return diagramaId; }
    public void   setDiagramaId(String diagramaId)        { this.diagramaId = diagramaId; }
    public String getUsuarioId()                          { return usuarioId; }
    public void   setUsuarioId(String usuarioId)          { this.usuarioId = usuarioId; }
    public String getNombre()                             { return nombre; }
    public void   setNombre(String nombre)                { this.nombre = nombre; }
    public String getAvatarInitial()                      { return avatarInitial; }
    public void   setAvatarInitial(String avatarInitial)  { this.avatarInitial = avatarInitial; }
    public String getAvatarColor()                        { return avatarColor; }
    public void   setAvatarColor(String avatarColor)      { this.avatarColor = avatarColor; }
    public Date   getLastSeen()                           { return lastSeen; }
    public void   setLastSeen(Date lastSeen)              { this.lastSeen = lastSeen; }
}