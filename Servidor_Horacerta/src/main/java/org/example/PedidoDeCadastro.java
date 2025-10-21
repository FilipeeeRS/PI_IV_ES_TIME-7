package org.example;

public class PedidoDeCadastro extends Comunicado
{
    private static final long serialVersionUID = 1L;
    private String nome;
    private String login;
    private String senha;
    private String funcao;

    public PedidoDeCadastro(String nome, String login, String senha, String funcao)
    {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("nome inválido");

        if (login == null || login.isBlank())
            throw new IllegalArgumentException("login inválido");

        if (senha == null || senha.isBlank())
            throw new IllegalArgumentException("senha inválida");

        if ( funcao == null || funcao.isBlank())
            throw new IllegalArgumentException("funcao invalida");

        this.nome  = nome;
        this.login = login;
        this.senha = senha;
        this.funcao = funcao;
    }

    public String getNome()  { return nome;  }
    public String getLogin() { return login; }
    public String getSenha() { return senha; }
    public String getFuncao(){ return funcao;}

    @Override
    public String toString()
    {
        // evita expor a senha em logs
        return "PedidoDeCadastro{" +
                "nome='" + this.nome + '\'' +
                ", login='" + this.login + '\'' +
                ", senha='***'" + '\'' +
                ", funcao='" + this.funcao + '\'' +
                '}';
    }
}
