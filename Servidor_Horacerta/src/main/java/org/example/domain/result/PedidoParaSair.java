package org.example.domain.result;
import java.io.Serializable;
import java.util.Objects;

public class PedidoParaSair implements Serializable {

    public PedidoParaSair(PedidoParaSair outro) {

    }
    @Override
    public String toString() {
        return "PEDIDO PARA SAIR (Encerrar Sessão do Usuário)";
    }
    @Override
    public int hashCode() {
        // Valor fixo e simples, pois todos os objetos desta classe são funcionalmente idênticos.
        return 456;
    }
    @Override
    public boolean equals(Object obj) {

        if (this == obj) return true;

        return obj != null && getClass() == obj.getClass();
    }

}
