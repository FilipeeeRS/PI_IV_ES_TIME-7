package org.example;

public class PedidoDeCadastro extends Comunicado {
    private static final long serialVersionUID = 1L;

    private final String nome;
    private final String login;
    private final String senha;
    private final String funcao;

    public PedidoDeCadastro(String nome, String login, String senha, String funcao) {

        if (nome == null || nome.isBlank())  throw new IllegalArgumentException("Nome inválido");
        if (login == null || login.isBlank())throw new IllegalArgumentException("Login inválido");
        if (senha == null || senha.isBlank())throw new IllegalArgumentException("Senha inválida");
        if (funcao == null || funcao.isBlank())throw new IllegalArgumentException("Funcao inválida");

        this.nome = nome;
        this.login = login;
        this.senha = senha;
        this.funcao = funcao;
    }

    public String getNome()  { return nome; }
    public String getLogin() { return login; }
    public String getSenha() { return senha; }
    public String getFuncao() { return funcao; }
}