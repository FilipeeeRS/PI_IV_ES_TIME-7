package org.example;

import org.example.domain.Medicamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


// Classe fictícia para simular a dependência de persistência
class GerenciadorMed {
    public void salvarMedicamento(Medicamento med) {
        // Implementação real de persistência (Banco de Dados)
    }
}

// Classe fictícia para simular o resultado do servidor (Sucesso/Erro)
class ResultadoOperacao {
    private boolean sucesso;
    private String mensagem;

    public ResultadoOperacao(boolean sucesso, String mensagem) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
    }
    public boolean isSucesso() { return sucesso; }
    public String getMensagem() { return mensagem; }
}

// Classe fictícia que simula o serviço do servidor que processa o pedido
class ServicoPrincipal {
    private final GerenciadorMed gerenciadorMed;

    // Construtor usado pelo @InjectMocks
    public ServicoPrincipal(GerenciadorMed gerenciadorMed) {
        this.gerenciadorMed = gerenciadorMed;
    }

    public ResultadoOperacao processar(PedidoDeCriarMedicamento pedido) {
        Medicamento med = pedido.getMedicamento();

        // **Variação 2: Validação (DEVE SER FEITA ANTES DE SALVAR)**
        if (med.getNome() == null || med.getNome().trim().isEmpty()) {
            return new ResultadoOperacao(false, "Nome do medicamento é inválido.");
        }

        try {
            // **Cenário Normal: Colaboração com o Gerenciador**
            gerenciadorMed.salvarMedicamento(med);
            return new ResultadoOperacao(true, "Medicamento criado com sucesso.");
        } catch (DuplicidadeException e) {
            // **Variação 1: Tratamento de Erro de Negócio**
            return new ResultadoOperacao(false, "Erro: Medicamento já cadastrado.");
        }
    }
}


/**
 * Testes INTERCLASSES para o principal serviço do servidor: Criar Medicamento.
 * Utiliza Mockito para simular a camada de persistência (GerenciadorMed).
 */
class ServicoPrincipalTest {

    // Simula a dependência (não queremos testar o banco de dados real)
    @Mock
    private GerenciadorMed gerenciadorMedMock;

    // A classe a ser testada, onde o Mock será injetado
    @InjectMocks
    private ServicoPrincipal servicoPrincipal;

    @BeforeEach
    void setup() {
        // Inicializa os objetos com anotações Mockito
        MockitoAnnotations.openMocks(this);
    }

    // ----------------------------------------------------------------------
    // CENÁRIO NORMAL (Sequência 1)
    // ----------------------------------------------------------------------
    @Test
    void cenarioNormal_deveCriarMedicamentoComSucesso() throws DuplicidadeException {
        // ARRANGE
        Medicamento med = new Medicamento("1", "AAS", "10mg", "D", "D", "1", false);
        PedidoDeCriarMedicamento pedido = new PedidoDeCriarMedicamento(med);

        // Stubbing: Configura o mock para não fazer nada quando salvar (Comportamento padrão)
        doNothing().when(gerenciadorMedMock).salvarMedicamento(med);

        // ACT
        ResultadoOperacao resultado = servicoPrincipal.processar(pedido);

        // ASSERT
        // 1. INTERCLASSE: Verifica se a colaboração aconteceu (GerenciadorMed foi chamado 1 vez)
        verify(gerenciadorMedMock, times(1)).salvarMedicamento(med);

        // 2. Resultado Final: Verifica o retorno
        assertTrue(resultado.isSucesso());
        assertEquals("Medicamento criado com sucesso.", resultado.getMensagem());
    }

    // ----------------------------------------------------------------------
    // CENÁRIO DE VARIAÇÃO 1: ERRO DE NEGÓCIO (Duplicidade)
    // ----------------------------------------------------------------------
    @Test
    void cenarioVariacao1_deveRetornarErroQuandoMedicamentoJaExiste() throws DuplicidadeException {
        // ARRANGE
        Medicamento medDuplicado = new Medicamento("1", "Ibuprofeno", "1", "D", "D", "1", false);
        PedidoDeCriarMedicamento pedido = new PedidoDeCriarMedicamento(medDuplicado);

        // Stubbing: Configura o mock para LANÇAR uma exceção simulando o erro de negócio
        doThrow(new DuplicidadeException("Medicamento já existe")).when(gerenciadorMedMock)
                .salvarMedicamento(any(Medicamento.class));

        // ACT
        ResultadoOperacao resultado = servicoPrincipal.processar(pedido);

        // ASSERT
        // 1. INTERCLASSE: Verifica se a tentativa de salvar ocorreu
        verify(gerenciadorMedMock, times(1)).salvarMedicamento(medDuplicado);

        // 2. Resultado Final: Verifica o retorno de erro
        assertFalse(resultado.isSucesso());
        assertTrue(resultado.getMensagem().contains("Medicamento já cadastrado"));
    }

    // ----------------------------------------------------------------------
    // CENÁRIO DE VARIAÇÃO 2: RESULTADO ALTERNATIVO (Dados Inválidos)
    // ----------------------------------------------------------------------
    @Test
    void cenarioVariacao2_naoDeveChamarPersistenciaComNomeVazio() throws DuplicidadeException {
        // ARRANGE
        // Nome vazio (dado inválido)
        Medicamento medInvalido = new Medicamento("1", "", "1", "D", "D", "1", false);
        PedidoDeCriarMedicamento pedido = new PedidoDeCriarMedicamento(medInvalido);

        // ACT
        ResultadoOperacao resultado = servicoPrincipal.processar(pedido);

        // ASSERT
        // 1. INTERCLASSE: Verifica que o método do Gerenciador NÃO FOI chamado (times(0))
        // Isso prova que a validação impediu a chamada à dependência
        verify(gerenciadorMedMock, times(0)).salvarMedicamento(any(Medicamento.class));

        // 2. Resultado Final: Verifica o retorno de erro de validação
        assertFalse(resultado.isSucesso());
        assertTrue(resultado.getMensagem().contains("Nome do medicamento é inválido"));
    }
}

// Classes Auxiliares necessárias para o Mockito
class PedidoDeCriarMedicamento {
    private Medicamento medicamento;
    public PedidoDeCriarMedicamento(Medicamento medicamento) { this.medicamento = medicamento; }
    public Medicamento getMedicamento() { return medicamento; }
}
class DuplicidadeException extends RuntimeException {
    public DuplicidadeException(String msg) { super(msg); }
}