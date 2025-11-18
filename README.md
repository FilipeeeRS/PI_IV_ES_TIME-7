# PI_IV_ES_TIME-7 "HoraCerta"
Projeto Integrador 4 / PUC CAMPINAS Engenharia de Software / 2025

Integrantes: 
- Anderson Lucas do Nascimento Gondim RA: 24787293
- Arthur Sebastian Guarniz de Castro RA: 24795528
- Felipe Nonato Leoneli RA: 24021973
- Filipe Ribeiro Simões RA: 24007657
- Rafael Roveri Pires RA: 24007131
- William Kenzo Nakao RA: 24005718

Orientadores:
- Professora Renata Arantes
- Professor André Luis dos Reis de Carvalho

TEMA DO PROJETO:  Gerenciador de Medicamentos com Monitoramento Remoto
-------------------------------
HoraCerta é um aplicativo Android desenvolvido em Kotlin (App) e Java (Backend) com o objetivo de oferecer uma solução prática e segura
para o gerenciamento de medicamentos dos dependentes, fortalecendo a conexão e a tranquilidade entre eles e seus responsáveis.

O projeto aborda um desafio real: a dificuldade que familiares e responsaveis têm em garantir que os dependente consumam seus medicamentos 
nos horários corretos, especialmente à distância. Alarmes convencionais tocam, mas não oferecem a confirmação de que a medicação foi, de fato tomada.

Para solucionar essa lacuna, o sistema foi dividido em duas interfaces distintas e complementares:
- Interface (Perfil) para o Dependente: Com um design simples. Suas funções são visualizar remédios futuros, receber os alarmes e confirmar a ingestão do medicamento com um toque.
- Interface(Perfil) para o Responsavel: Uma interface mais complexa que permite o cadastro, edição e exclusão de medicamentos, e o acompanhamento em tempo real das confirmações do dependente por um histórico.

Dessa forma, o HoraCerta garante mais segurança para o dependente e, acima de tudo, mais tranquilidade e confiança para quem cuida.

Requisitos Funcionais Obrigatórios:
-------------------------------
Perfil do dependente:
- Recebimento de Lembretes: alertas sonoros e visuais nos horários programados de cada remédio (alarme/notificação);
- Identificação do Medicamento: Notificação/alarme exibindo o nome e a dosagem do medicamento de forma clara;
- Confirmações Simples: Um único botão simples para desativar o alarme e confirmar que tomou a dosagem;
- Interface Acessível: A interface simples para facilitar o uso, com apenas uma tela principal, mostrando o remédio futuro;
- Registro de Confirmação: Cada ação do dependente (tomou/não tomou) é registrada no banco de dados.
	
Perfil do responsável:
- Cadastro, Edição e Exclusão de Medicamentos: Deve poder cadastrar, editar e excluir medicamentos (possuindo: nome, dosagem, dia e horário que será tomado);
- Acompanhar Histórico: visualização do histórico dos remédios passados que já foram tomados (ou não) pelo idoso em tempo real. Destacando os alarmes que não foram respondidos;
- Agendamento de Alarmes: Definir os horários em que os alarmes serão disparados para o idoso;
- Contato: Capacidade de ligar para o idoso através de um acesso rápido ao telefone do idoso para contato direto.
	
Funcionalidades gerais:
- Autenticação de Usuário: Sistema de login e cadastro para os perfis de idoso e responsável;
- Associação de Contas: Deve permitir a associação entre a conta do idoso e a conta do responsável;
- Armazenamento de Dados: Armazenamento dos dados dos perfis em MongoDB;
- Sincronização de Dados: as informações cadastradas pelo responsável devem ser refletidas em tempo real na conta do idoso.

TECNOLOGIAS UTILIZADAS
-------------------------------
- Kotlin (aplicativo Android)
- Java (Servidor Backend)
- Jetpack Compose
- Banco de Dados MongoDB (Armazenamento dos dados)
- Autenticação por Firebase Authentication
- Android Studio
- IntelliJ IDEA

COMO CONFIGURAR E RODAR O PROJETO
-------------------------------
Siga os passos abaixo para configurar o ambiente de desenvolvimento e executar o aplicativo.
O sistema é composto por duas partes que devem rodar simultaneamente: o Servidor Java e o App Android. Siga os passos abaixo:

Pré-requisitos:
- Android Studio
- IntelliJ IDEA
- Conta do MongoDB configurada

Passo 1: Configurando Servidor
- Abra o servidor do projeto no IntelliJ IDEA.
- Verifique se a conexão com o MongoDB está configurada.
- Execute a classe principal Servidor.java.
- Aguarde a mensagem no console.

Passo 2: App Android
- Abra o app do projeto no Android Studio.
- Descubra o Endereço IPv4 do seu computador.
- Vá até os arquivos de conexão do app e atualize a SERVER_IP.
- Conecte seu celular/emulador.
- Clique em Run para instalar e abrir o aplicativo.


