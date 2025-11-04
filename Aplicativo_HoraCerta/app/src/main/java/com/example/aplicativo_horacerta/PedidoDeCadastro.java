package com.example.aplicativo_horacerta;


public class PedidoDeCadastro extends ComunicadoJson {
    private static final long serialVersionUID = 1L;

    private final String nome;
    private final String login;



    public PedidoDeCadastro(String nome, String login) {
        super("Cadastro");

        if (nome == null || nome.isBlank())  throw new IllegalArgumentException("Nome inválido");
        if (login == null || login.isBlank())throw new IllegalArgumentException("Login inválido");



        this.nome = nome;
        this.login = login;


    }

    public String getNome()   { return nome; }
    public String getLogin()  { return login; }


}
