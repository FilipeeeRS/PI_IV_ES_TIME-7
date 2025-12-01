package org.example.protocol;

import java.io.Serializable;
import java.util.Objects;

public class ComunicadoDeDesligamento extends ComunicadoJson implements Serializable {

    @Override
    public String toString() {
        return "COMUNICADO DE DESLIGAMENTO (Encerrar Conexão)";
    }

    @Override
    public int hashCode() {
        // Usa um valor fixo (ex: 123) ou o hash da classe pai.
        return 123;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) return true;

        return obj != null && getClass() == obj.getClass();
    }
}

