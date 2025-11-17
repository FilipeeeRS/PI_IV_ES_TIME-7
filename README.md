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
HoraCerta é um aplicativo Android desenvolvido em Kotlin com o objetivo de oferecer uma solução prática e segura para o gerenciamento de medicamentos de idosos, fortalecendo a conexão e a tranquilidade entre eles e seus cuidadores.

O projeto aborda um desafio real: a dificuldade que familiares e cuidadores têm em garantir que os idosos consumam seus medicamentos nos horários corretos, especialmente à distância. Alarmes convencionais não oferecem a confirmação de que a medicação foi, de fato, administrada.
Para solucionar essa lacuna, o sistema foi dividido em duas interfaces distintas e complementares:
- Interface para o Idoso: Com um design simples e acessível, focado na simplicidade. Suas funções são visualizar remédios futuros e receber os lembretes (alarmes visuais e sonoros) e confirmar a ingestão do medicamento com um toque.
- Interface para o Responsável/Cuidador: Uma interface mais complexa que permite o cadastro, edição e exclusão de medicamentos, e o acompanhamento em tempo real das confirmações do idoso (por notificação). Caso um medicamento não seja confirmado, o cuidador é notificado e pode entrar em contato pelo número de telefone do idoso.

Dessa forma, o HoraCerta garante mais segurança para o idoso e, acima de tudo, mais tranquilidade e confiança para quem cuida.

TECNOLOGIAS UTILIZADAS
-------------------------------
- Linguagem: Kotlin
- Jetpack Compose para interfaces modernas
- Banco de Dados: MongoDB Atlas (Cloud) como banco de dados NoSQL para armazenar dados
- Agendamento de Tarefas: AlarmManager / WorkManager para garantir a entrega dos lembretes no horário correto, mesmo com o app em segundo plano
- Visualização de Dados: MPAndroidChart / Compose Charts para a criação de gráficos e relatórios de adesão ao tratamento
- Autenticação: Firebase Authentication
- Notificações Push: Firebase Cloud Messaging para alertas em tempo real ao cuidador
- Ambiente de Desenvolvimento: Android Studio

COMO CONFIGURAR E RODAR O PROJETO
-------------------------------
Siga os passos abaixo para configurar o ambiente de desenvolvimento e executar o aplicativo

Pré-requisitos:
- Android Studio
- Conta no MongoDB Atlas para configurar o banco de dados na nuvem

Configuração Inicial:


