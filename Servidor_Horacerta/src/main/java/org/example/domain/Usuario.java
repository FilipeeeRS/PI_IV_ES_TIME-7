package org.example.domain;

import java.util.Objects;

public class Usuario {
    private String _id;
    private String uid;
    private String nome;
    private String email;
    private String tipo;

    public Usuario(){}

    public Usuario(String _id,String uid, String nome, String email, String tipo) {
        this._id = _id;
        this.uid = uid;
        this.nome = nome;
        this.email = email;
        this.tipo = tipo;

    }

    public String getUid() { return uid; }
    public String getEmail() { return email; }
    public String getNome() { return nome; }
    public String getTipo(){ return  tipo;}


    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", email='" + email + '\'' +
                ", nome='" + nome + '\'' +
                ", tipo='" + tipo + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(_id, uid);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj == null || getClass() != obj.getClass()) return false;

        Usuario other = (Usuario) obj;

        return Objects.equals(_id, other._id) &&
                Objects.equals(uid, other.uid);
    }
}