package org.example;

import org.bson.Document;
import org.bson.types.ObjectId;


public class Medicamento extends ComunicadoJson {

    // Campos Privados
    private String id;
    private String nome;
    private String dia;
    private String horario;
    private String descricao;
    private String idUsuario;
    private boolean tomou;

    public Medicamento() {
        super("Medicamento");
    }


    public Medicamento(String json) {
        super(json);
    }


    public Medicamento(String id, String nome, String dia, String horario, String descricao, String idUsuario, boolean tomou) {
        super("Medicamento");
        this.id = id;
        this.nome = nome;
        this.dia = dia;
        this.horario = horario;
        this.descricao = descricao;
        this.idUsuario = idUsuario;
        this.tomou = tomou;
    }

    // Getters
    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getDia() { return dia; }
    public String getHorario() { return horario; }
    public String getDescricao() { return descricao; }
    public String getIdUsuario() { return idUsuario; }
    public boolean isTomou() { return tomou; } // Getter padrão para boolean é 'is'

    // Setters (Todos PUBLIC)
    public void setId(String id) { this.id = id; }
    public void setNome(String nome) { this.nome = nome; }
    public void setDia(String dia) { this.dia = dia; }
    public void setHorario(String horario) { this.horario = horario; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }
    public void setTomou(boolean tomou) { this.tomou = tomou; }


    public static Medicamento fromDocument(Document doc) {
        if (doc == null) return null;

        Medicamento m = new Medicamento();

        ObjectId objectId = doc.getObjectId("_id");
        if (objectId != null) {
            // toHexString garante que venha apenas o código limpo
            m.setId(objectId.toHexString());
        } else {
            // Fallback caso o ID tenha sido salvo como String manualmente
            Object idObj = doc.get("_id");
            if (idObj != null) m.setId(idObj.toString());
        }

        // Mapeamento dos campos String
        m.setNome(doc.getString("nome"));
        m.setDia(doc.getString("dia"));
        m.setHorario(doc.getString("horario"));
        m.setDescricao(doc.getString("descricao"));
        m.setIdUsuario(doc.getString("idUsuario"));

        // Trata o campo booleano tomou
        m.setTomou(doc.getBoolean("tomou", false));

        return m;
    }

    @Override
    public String toString() {
        return "Medicamento[" +
                "id='" + id + '\'' +
                ", nome='" + nome + '\'' +
                ", dia='" + dia + '\'' +
                ", horario='" + horario + '\'' +
                ", descricao='" + descricao + '\'' +
                ", idUsuario=" + idUsuario + '\'' +
                ", tomou=" + tomou +
                ']';
    }
}