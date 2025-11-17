package org.example;

public class Medicamento extends Comunicado {
    private String id;
    private String nome;
    private String dia;
    private String horario;
    private String descricao;
    private String idUsuario;

    public Medicamento() {}

    public Medicamento(String id, String nome, String dia, String horario, String descricao, String idUsuario) {
        this.id = id;
        this.nome = nome;
        this.dia = dia;
        this.horario = horario;
        this.descricao = descricao;
        this.idUsuario = idUsuario;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDia() { return dia; }
    public void setDia(String dia) { this.dia = dia; }
    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    @Override
    public String toString() {
        return "Medicamento{" +
                "id='" + id + '\'' +
                ", nome='" + nome + '\'' +
                ", dia='" + dia + '\'' +
                ", horario='" + horario + '\'' +
                ", descricao='" + descricao + '\'' +
                ", idUsuario='" + idUsuario + '\'' +
                '}';
    }
}