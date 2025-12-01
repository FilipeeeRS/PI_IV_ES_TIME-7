package org.example.domain.result;

import java.io.Serializable;
import java.util.Objects;

public class Resultado implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean sucesso;
    private final String mensagem;

    public Resultado(boolean sucesso, String mensagem) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
    }
    public boolean isSucesso() { return sucesso; }
    public String getMensagem(){ return mensagem; }

    @Override
    public String toString() {
        String status = sucesso ? "SUCESSO" : "FALHA";
        return "Resultado [Status: " + status + ", Mensagem: '" + mensagem + "']";
    }

    @Override
    public int hashCode() {
        // Combina o hash do booleano e da String.
        return Objects.hash(sucesso, mensagem);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj == null || getClass() != obj.getClass()) return false;

        Resultado other = (Resultado) obj;

        return sucesso == other.sucesso &&
                Objects.equals(mensagem, other.mensagem);
    }
}
