package org.example.domain;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.example.protocol.ComunicadoJson;

import java.util.Objects;
public class Medicamento extends ComunicadoJson implements Cloneable {

    // Campos Privados
    private String id;
    private String nome;
    private String dia;
    private String horario;
    private String descricao;
    private String idUsuario;
    private boolean tomou;

    // Construtores
    public Medicamento() {
        super("Medicamento");
    }

    //Construtor de copia
    public Medicamento(Medicamento outro) {
        // Copia o tipo do comunicado
        super(outro.getTipo());

        // Copia todos os campos
        this.id = outro.id;
        this.nome = outro.nome;
        this.dia = outro.dia;
        this.horario = outro.horario;
        this.descricao = outro.descricao;
        this.idUsuario = outro.idUsuario;
        this.tomou = outro.tomou;
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
    public boolean isTomou() { return tomou; }

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
            m.setId(objectId.toHexString());
        } else {
            Object idObj = doc.get("_id");
            if (idObj != null) m.setId(idObj.toString());
        }

        m.setNome(doc.getString("nome"));
        m.setDia(doc.getString("dia"));
        m.setHorario(doc.getString("horario"));
        m.setDescricao(doc.getString("descricao"));
        m.setIdUsuario(doc.getString("idUsuario"));

        m.setTomou(doc.getBoolean("tomou", false));

        return m;
    }


    /**
     * Calcula o código hash baseado EXCLUSIVAMENTE no ID.
     * Isso garante que o hash code não mude (e quebre coleções) após o objeto ser salvo.
     */
    @Override
    public int hashCode() {
        // Se o ID é null, o hash será 0. Se for String, usa o hash da string.
        return Objects.hash(id);
    }

    /**
     * Compara se dois objetos Medicamento são logicamente iguais.
     * Para Entidades de banco de dados, a igualdade é definida APENAS pelo ID.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Medicamento other = (Medicamento) obj;

        // A igualdade é definida se ambos os IDs forem iguais (lidando com nulls).
        // Dois objetos novos (id=null) serão considerados iguais.
        return Objects.equals(id, other.id);
    }

    /*
     Métod0 auxiliar para comparar APENAS os campos de dados, ignorando o ID.
     Útil para verificar se um objeto novo é igual a outro novo antes de salvar.
     */
    public boolean temOsMesmosDados(Medicamento outro) {
        if (outro == null) return false;

        // Compara todos os campos que definem o medicamento, tirando o ID e o 'tomou' (que é status)
        return Objects.equals(this.nome, outro.nome) &&
                Objects.equals(this.dia, outro.dia) &&
                Objects.equals(this.horario, outro.horario) &&
                Objects.equals(this.descricao, outro.descricao) &&
                Objects.equals(this.idUsuario, outro.idUsuario);
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
    @Override
    public Medicamento clone() {
        // Uso do construtor de cópia para garantir uma cópia segura (deep copy implícita, pois só tem Strings/primitivos)
        return new Medicamento(this);
    }
}