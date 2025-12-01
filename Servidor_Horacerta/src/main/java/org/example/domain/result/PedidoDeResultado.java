package org.example.domain.result;

import java.util.Objects;
import java.io.Serializable;

public class PedidoDeResultado implements Serializable {

    public PedidoDeResultado(PedidoDeResultado outro) {
    }

    @Override
    public String toString() {
        return "PEDIDO DE RESULTADO (Requisição de status/resultado genérico)";
    }

    @Override
    public int hashCode() {
        // Valor fixo e simples, diferente dos outros marcadores.
        return 789;
    }

    @Override
    public boolean equals(Object obj) {
        // 1. Reflexividade
        if (this == obj) return true;

        // 2. A igualdade é definida APENAS pelo tipo da classe.
        return obj != null && getClass() == obj.getClass();
    }


}

