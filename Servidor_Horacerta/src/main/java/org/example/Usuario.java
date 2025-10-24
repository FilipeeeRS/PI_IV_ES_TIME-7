package org.example;

import org.bson.types.ObjectId;

public class Usuario {
    private String _id;
    private String uid;
    private String nome;
    private String email;

    public Usuario(){}

    public Usuario(String _id,String uid, String nome, String email) {
        this._id = _id;
        this.uid = uid;
        this.nome = nome;
        this.email = email;

    }

    public String getUid() { return uid; }
    public String getEmail() { return email; }
    public String getNome() { return nome; }


    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", email='" + email + '\'' +
                ", nome='" + nome + '\'' +
                '}';
    }
}
