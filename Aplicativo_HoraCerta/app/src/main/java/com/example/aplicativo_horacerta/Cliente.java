package com.example.aplicativo_horacerta;
import java.net.*;
import java.io.*;

public class Cliente {
    public static final String HOST_PADRAO = "localhost";
    public static final int    PORTA_PADRAO = 3000;

    public static void main (String[] args) throws Exception {
        if(args.length > 2){
            System.err.println("Uso esperado: java Cliente [HOST [PORTA]]\n");
            return;
        }

        Socket conexao = null;

        try{
            String host = Cliente.HOST_PADRAO;
            int    porta= Cliente.PORTA_PADRAO;

            if(args.length > 0)
                host = args[0];

            if(args.length ==2)
                porta = Integer.parseInt(args[1]);

            conexao = new Socket (host, porta);

        }catch (Exception erro){
            System.err.println("Indique o servidor e a porta correta \n");
            return;
        }

        BufferedWriter transmissor = null;

        try {
            transmissor =
                    new BufferedWriter(new OutputStreamWriter
                            (conexao.getOutputStream() ) );
        }catch (Exception erro)
        {
           System.err.println("Indique o servidor e a porta correta");
           return;
        }

        BufferedReader receptor = null;
        try{
            receptor =
                    new BufferedReader(new InputStreamReader
                            (conexao.getInputStream() ) );
        }catch (Exception erro){
            System.err.println("Indique o servidor e a porta correta");
            return;
        }

        Parceiro servidor = null;
        try{
            servidor =
            new Parceiro( conexao, receptor, transmissor);
        }catch (Exception Error){
            System.err.println("Indique o servidor e a porta correta");
            return;
        }

        /*
        TratadoraDeComunicadoDeDesligamento tratadoraDeComunicadoDeDesligamento = null;
        try {
            tratadoraDeComunicadoDeDesligamento = new TratadoraDeComunicadoDeDesligamento(servidor);
        }
        catch (Exception erro)
        { } // servidor foi instanciado

    }

         */
}
}
