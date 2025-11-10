package org.example;

public class PedidoDeCadastro extends Comunicado
{
    private static final long serialVersionUID = 1L;
    private String nome;
    private String email;
    private String senha;
    private String tipo;

    public PedidoDeCadastro(String nome, String email, String senha, String tipo)
    {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("nome inválido");

        if (email == null || email.isBlank())
            throw new IllegalArgumentException("email inválido");

        if (senha == null || senha.isBlank())
            throw new IllegalArgumentException("senha inválida");

        if (tipo == null || tipo.isBlank())
            throw new IllegalArgumentException("tipo inválido");

        this.nome  = nome;
        this.email  = email;
        this.senha = senha;
        this.tipo  = tipo;
    }

    public String getNome()  { return nome;  }
    public String getEmail() { return email; }
    public String getSenha() { return senha; }
    public String getTipo()  { return tipo;  }

    @Override
    public String toString()
    {
        return "PedidoDeCadastro{" +
                "nome='" + this.nome + '\'' +
                ", email='" + this.email + '\'' +
                ", tipo='" + this.tipo + '\'' +
                ", senha='***'" +
                '}';
    }
}
