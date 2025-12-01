package org.example;

import org.example.domain.Medicamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes INTRACLASSES para a classe Medicamento, cobrindo:
 * 1. Particionamento por ATRIBUTOS (Inicialização e Setters).
 * 2. Particionamento por ESTADOS (Mudança do campo 'tomou').
 */
class MedicamentoTest {

    private Medicamento medicamentoPadrao;

    @BeforeEach
    void setUp() {
        // Inicialização de um objeto Medicamento válido para cada teste
        medicamentoPadrao = new Medicamento(
                "656f5a3a2e24177242194c79",
                "Dipirona",
                "Segunda, Quarta",
                "10:00",
                "Tomar após o café",
                "user123",
                false // Estado inicial: Não tomado
        );
    }

    // ----------------------------------------------------------------------
    // TESTES DE ATRIBUTOS (Sequência 1 e 2 do Passo 1)
    // ----------------------------------------------------------------------

    @Test
    @DisplayName("Teste de Atributos: Deve inicializar o objeto com todos os valores corretos")
    void testeAtributos_deveInicializarComValoresCorretos() {
        // Sequência 2: new Medicamento(valores) -> getNome() -> getHorario()
        assertEquals("656f5a3a2e24177242194c79", medicamentoPadrao.getId());
        assertEquals("Dipirona", medicamentoPadrao.getNome());
        assertEquals("10:00", medicamentoPadrao.getHorario());
        assertFalse(medicamentoPadrao.isTomou());
    }

    @Test
    @DisplayName("Teste de Atributos: Deve permitir a alteração e leitura dos atributos via setters")
    void testeAtributos_deveAlterarEDevolverNovoValor() {
        // Sequência 3: new Medicamento(valores) -> setDescricao(novo) -> getDescricao()
        final String novaDescricao = "Tomar antes de dormir";

        medicamentoPadrao.setDescricao(novaDescricao);

        assertEquals(novaDescricao, medicamentoPadrao.getDescricao());
    }

    // ----------------------------------------------------------------------
    // TESTES DE ESTADOS (Sequências 1, 2 e 3 do Passo 1)
    // ----------------------------------------------------------------------

    @Test
    @DisplayName("Teste de Estado 1: Estado Inicial e Verificação da Sequência")
    void testeEstados_deveEstarInicialmenteNaoTomado() {
        // Sequência 1: new Medicamento(false) -> isTomou()
        assertFalse(medicamentoPadrao.isTomou(), "O status inicial deve ser false.");
    }

    @Test
    @DisplayName("Teste de Estado 2: Transição Válida para Tomado")
    void testeEstados_deveMudarStatusParaTomado() {
        // Sequência 2: setTomou(true) -> isTomou()
        medicamentoPadrao.setTomou(true);
        assertTrue(medicamentoPadrao.isTomou(), "O status deve ser true após a transição.");
    }

    @Test
    @DisplayName("Teste de Estado 3: Transição de Retorno para Não Tomado")
    void testeEstados_deveMudarStatusDeVoltaParaNaoTomado() {
        // Prepara o estado como 'Tomado' primeiro
        medicamentoPadrao.setTomou(true);
        assertTrue(medicamentoPadrao.isTomou());

        // Sequência 3: setTomou(false) -> isTomou()
        medicamentoPadrao.setTomou(false);
        assertFalse(medicamentoPadrao.isTomou(), "O status deve voltar para false.");
    }
    @Test
    @DisplayName("Teste de Falha SIMULADA: Setar nome como null deve ser possível, expondo o risco de NPE")
    void testeFalhaSimulada_setNomeComNull() {
        // 1. Simula a chamada do setter com o valor problemático (null)
        medicamentoPadrao.setNome(null);

        // 2. Verifica se o valor realmente foi definido como null (confirmando a falta de validação)
        assertNull(medicamentoPadrao.getNome(), "O setter falhou em validar e permitiu um valor nulo.");

        // (O NullPointerException real ocorreria em um método posterior, como um .toString() ou na serialização)
    }
}