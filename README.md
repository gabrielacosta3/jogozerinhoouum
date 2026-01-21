# Jogo 0 ou 1 - Socket TCP Java

**Equipe :**

- Amanda Vanessa Brito da Silva.
- Ana Vitória dos Santos Barros.
- Jamilly da Silva Gomes Portugal.
- Gabriela Costa Ferreira.
- Mayara de Azevedo Costa.

**Descrição do Projeto**
Desenvolvemos um jogo de rede multijogador chamado "0 ou 1" usando Socket TCP em Java. O servidor coordena uma competição com eliminação entre três jogadores conectados simultaneamente, gerenciando toda a lógica e sincronização do jogo.

**Funcionamento do Jogo**
- 3 jogadores conectados simultaneamente: O jogo só inicia quando todos os três clientes estão conectados ao servidor
- Rodadas simultâneas: Todos os jogadores escolhem um número (0 ou 1) ao mesmo tempo em cada rodada
- Regra de eliminação: O jogador que ficar isolado em sua escolha (em minoria) é eliminado imediatamente
- Progressão: O jogo continua com os jogadores restantes até restar apenas um participante
- Vencedor: O último jogador remanescente é declarado campeão.

**Instruções para Executar o Projeto Compilação**

- Abra o projeto no VS Code
- Você pode compilar automaticamente indo na parte de executar
- Importante: Sempre inicie primeiro o servidor

**Execução**

**Inicie o servidor:**

- Execute o arquivo Servidor.java
- O servidor ficará aguardando a conexão dos três jogadores.

**Conecte os jogadores:**
- Execute o arquivo Cliente.java três vezes em terminais separados.
- Cada jogador deve inserir seu nome quando solicitado.

**Processamento do resultado: Após todas as escolhas:**

- Se dois jogadores escolherem 0 e um escolher 1 → o jogador que escolheu 1 é eliminado
- Se dois jogadores escolherem 1 e um escolher 0 → o jogador que escolheu 0 é eliminado
- Se todos escolherem o mesmo número → ninguém é eliminado

**Feedback do jogo:**

- Se você não foi eliminado: "VOCÊ ESTÁ SEGURO! Continua no jogo!"
- Se você foi eliminado: "VOCÊ FOI ELIMINADO! Sua escolha era a minoria."
- Continuação: O processo se repete com os jogadores restantes até que sobre apenas um.
- Vitória: O último jogador que permanecer no jogo é declarado vencedor.

**Controles**

- PRONTO: Confirma sua preparação para iniciar ou continuar o jogo
- 0 ou 1: Faz sua escolha numérica na rodada
- SIM ou NAO: Responde se deseja jogar novamente ao final da partida