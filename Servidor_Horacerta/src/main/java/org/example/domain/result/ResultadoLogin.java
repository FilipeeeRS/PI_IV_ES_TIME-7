package org.example.domain.result;
import com.google.gson.Gson;
import org.example.domain.Usuario;
import java.util.Objects;

public class ResultadoLogin extends ResultadoOperacao {
    private Usuario usuario;

    public ResultadoLogin(boolean resultado, String operacao, Usuario usuario) {
        super(resultado, operacao); // inicializa os campos do pai
        this.usuario = usuario;
    }

    public String getResultado() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    @Override
    public int hashCode() {
        // Combina o hash do pai com o hash do campo 'usuario'.
        // O Objects.hash() é a forma mais segura.
        return Objects.hash(super.hashCode(), usuario);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        // Verifica o pai primeiro. Se o pai não for igual, não precisamos checar os campos desta classe.
        if (!super.equals(obj)) return false;

        if (getClass() != obj.getClass()) return false;

        ResultadoLogin other = (ResultadoLogin) obj;

        // Compara o campo específico desta classe.
        return Objects.equals(usuario, other.usuario);
    }
    @Override
    public String toString() {
        // Chamamos o toString do pai para incluir o status (true/false) e a operação.
        String base = super.toString();

        if (usuario != null) {
            // Se o login foi bem-sucedido e o usuário existe, mostra os dados chave.
            return "ResultadoLogin [Status: SUCESSO. " + base +
                    ", Usuario: " + usuario.getNome() +
                    " (" + usuario.getUid() + ")]";
        } else {
            // Se o usuário é null, o login falhou.
            return "ResultadoLogin [Status: FALHA. " + base +
                    ", Detalhe: Usuário NÃO autenticado/encontrado.]";
        }
    }
}