import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Servidor {
    private static final int PORTA = 8080;
    private static final int NUM_JOGADORES = 3;
    private ServerSocket socketServidor;

    private Socket[] socketsClientes = new Socket[NUM_JOGADORES];
    private PrintWriter[] saidas = new PrintWriter[NUM_JOGADORES];
    private BufferedReader[] entradas = new BufferedReader[NUM_JOGADORES];
    private boolean[] jogando = new boolean[NUM_JOGADORES];

    private final ExecutorService gerenciadorThreads = Executors.newCachedThreadPool();

    public static void main(String[] args) throws IOException {
        new Servidor().executar();
    }

    public void executar() {
        try {
            socketServidor = new ServerSocket(PORTA);
            System.out.println("Servidor iniciado na porta " + PORTA);
            
            while (true) { 
                System.out.println("\n" + "=".repeat(50));
                System.out.println("AGUARDANDO 3 NOVOS JOGADORES...");
                System.out.println("=".repeat(50) + "\n");
                
                
                for (int i = 0; i < NUM_JOGADORES; i++) {
                    if (socketsClientes[i] != null && !socketsClientes[i].isClosed()) {
                        try {
                            socketsClientes[i].close();
                        } catch (IOException e) {
                            
                        }
                    }
                    socketsClientes[i] = null;
                    saidas[i] = null;
                    entradas[i] = null;
                    jogando[i] = false;
                }
                
                
                for (int i = 0; i < NUM_JOGADORES; i++) {
                    Socket socket = socketServidor.accept();
                    socketsClientes[i] = socket;
                    saidas[i] = new PrintWriter(socket.getOutputStream(), true);
                    entradas[i] = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    jogando[i] = true;
                    
                    System.out.println("Jogador " + i + " conectado!");
                    saidas[i].println("BEM_VINDO:" + i);
                }

                System.out.println("\n Todos os jogadores conectados!");
                
                
                boolean sessaoAtiva = executarSessao();
                

                if (!sessaoAtiva) {
                    System.out.println("\n" + "=".repeat(50));
                    System.out.println("FECHANDO CONEXÕES E AGUARDANDO NOVOS JOGADORES...");
                    System.out.println("=".repeat(50));
                    
                    
                    for (int i = 0; i < NUM_JOGADORES; i++) {
                        if (socketsClientes[i] != null && !socketsClientes[i].isClosed()) {
                            try {
                                saidas[i].println("SAIR");
                                socketsClientes[i].close();
                            } catch (IOException e) {
                                System.out.println("Erro ao fechar conexão do Jogador " + i);
                            }
                        }
                    }
                }
                
            }

        } catch (IOException e) {
            System.out.println("Erro no servidor: " + e.getMessage());
        } finally {
            gerenciadorThreads.shutdownNow();
            try {
                if (socketServidor != null) socketServidor.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Servidor encerrado.");
        }
    }
    
    private boolean executarSessao() {
        try {
            
            @SuppressWarnings("unchecked")
            BlockingQueue<String>[] filas = new BlockingQueue[NUM_JOGADORES];
            for (int i = 0; i < NUM_JOGADORES; i++) {
                filas[i] = new LinkedBlockingQueue<>();
                final int id = i;
                final BufferedReader leitor = entradas[id];
                final BlockingQueue<String> fila = filas[id];
                gerenciadorThreads.submit(() -> {
                    try {
                        String linha;
                        while ((linha = leitor.readLine()) != null) {
                            fila.put(linha);
                        }
                    } catch (IOException | InterruptedException e) {
                        System.out.println("Jogador " + id + " desconectou.");
                    }
                });
            }

            
            System.out.println("\n Aguardando confirmação de início...");
            CountDownLatch confirmacaoLatch = new CountDownLatch(NUM_JOGADORES);
            for (int i = 0; i < NUM_JOGADORES; i++) {
                saidas[i].println("AGUARDANDO_CONFIRMACAO");
            }

            
            for (int i = 0; i < NUM_JOGADORES; i++) {
                final int id = i;
                gerenciadorThreads.submit(() -> {
                    try {
                        while (true) {
                            String msg = filas[id].take();
                            if (msg.equalsIgnoreCase("PRONTO")) {
                                System.out.println(" Jogador " + id + " confirmou PRONTO.");
                                confirmacaoLatch.countDown();
                                break;
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }

            confirmacaoLatch.await();
            System.out.println("\n TODOS CONFIRMARAM! INICIANDO PARTIDA...\n");

            boolean partidaAtiva = true;
            while (partidaAtiva) {
                
                Arrays.fill(jogando, true);
                
                boolean rodando = true;
                while (rodando) {
                    
                    for (int i = 0; i < NUM_JOGADORES; i++) {
                        if (jogando[i]) {
                            saidas[i].println("INICIO_RODADA");
                        }
                    }
                    
                    System.out.println("\n" + "=".repeat(30));
                    System.out.println("RODADA INICIADA");
                    System.out.println("=".repeat(30));
                    System.out.println("Jogadores ativos: " + contarJogadoresAtivos());

                    
                    CountDownLatch escolhasLatch = new CountDownLatch(contarJogadoresAtivos());
                    int[] escolhas = new int[NUM_JOGADORES];
                    Arrays.fill(escolhas, -1);

                    for (int i = 0; i < NUM_JOGADORES; i++) {
                        if (jogando[i]) {
                            final int id = i;
                            final BlockingQueue<String> fila = filas[id];
                            
                            saidas[i].println("SUA_ESCOLHA");
                            
                            gerenciadorThreads.submit(() -> {
                                try {
                                    while (true) {
                                        String msg = fila.take();
                                        if (msg.startsWith("ESCOLHA:")) {
                                            try {
                                                int escolha = Integer.parseInt(msg.substring(8));
                                                if (escolha == 0 || escolha == 1) {
                                                    escolhas[id] = escolha;
                                                    escolhasLatch.countDown();
                                                    System.out.println("Jogador " + id + " escolheu: " + escolha);
                                                    break;
                                                }
                                            } catch (NumberFormatException e) {
                                                
                                            }
                                        }
                                    }
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                            });
                        }
                    }

                    escolhasLatch.await();

                    
                    int contagem0 = 0, contagem1 = 0;
                    for (int i = 0; i < NUM_JOGADORES; i++) {
                        if (jogando[i]) {
                            if (escolhas[i] == 0) contagem0++;
                            else if (escolhas[i] == 1) contagem1++;
                        }
                    }

                    System.out.println("Resultado: 0 = " + contagem0 + " | 1 = " + contagem1);

                    
                    int eliminado = -1;
                    int jogadoresAtivos = contarJogadoresAtivos();
                    
                    if (contagem0 > contagem1 && contagem1 > 0) {
                        for (int i = 0; i < NUM_JOGADORES; i++) {
                            if (jogando[i] && escolhas[i] == 1) {
                                eliminado = i;
                                break;
                            }
                        }
                    } else if (contagem1 > contagem0 && contagem0 > 0) {
                        for (int i = 0; i < NUM_JOGADORES; i++) {
                            if (jogando[i] && escolhas[i] == 0) {
                                eliminado = i;
                                break;
                            }
                        }
                    } else if (contagem0 == 1 && contagem1 == 1 && jogadoresAtivos == 2) {
                        Random rand = new Random();
                        for (int i = 0; i < NUM_JOGADORES; i++) {
                            if (jogando[i]) {
                                eliminado = i;
                                break;
                            }
                        }
                        System.out.println("Empate! Eliminando aleatoriamente jogador " + eliminado);
                    }

                    
                    for (int i = 0; i < NUM_JOGADORES; i++) {
                        if (jogando[i]) {
                            if (eliminado == i) {
                                saidas[i].println("ELIMINADO");
                                jogando[i] = false;
                                System.out.println(" Jogador " + i + " ELIMINADO!");
                            } else {
                                saidas[i].println("SEGURO");
                                System.out.println(" Jogador " + i + " SEGURO!");
                            }
                        }
                    }

                    
                    if (contarJogadoresAtivos() == 1) {
                        
                        int vencedor = -1;
                        for (int i = 0; i < NUM_JOGADORES; i++) {
                            if (jogando[i]) {
                                vencedor = i;
                                break;
                            }
                        }
                        
                        System.out.println("\n" + "=".repeat(40));
                        System.out.println(" VENCEDOR: Jogador " + vencedor + " ");
                        System.out.println("=".repeat(40));
                        
                        for (int i = 0; i < NUM_JOGADORES; i++) {
                            saidas[i].println("FIM_JOGO:VENCEDOR:" + vencedor);
                        }
                        
                        
                        Thread.sleep(2000);
                        System.out.println("\n" + "=".repeat(40));
                        System.out.println("PERGUNTA FINAL:");
                        System.out.println("=".repeat(40));
                        
                        CountDownLatch respostaLatch = new CountDownLatch(NUM_JOGADORES);
                        boolean[] respostas = new boolean[NUM_JOGADORES]; // true = SIM, false = NÃO
                        
                        for (int i = 0; i < NUM_JOGADORES; i++) {
                            saidas[i].println("JOGAR_NOVAMENTE?");
                        }
                        
                        
                        for (int i = 0; i < NUM_JOGADORES; i++) {
                            final int id = i;
                            gerenciadorThreads.submit(() -> {
                                try {
                                    String msg = filas[id].take();
                                    if (msg.equalsIgnoreCase("SIM")) {
                                        System.out.println("Jogador " + id + " respondeu: SIM");
                                        respostas[id] = true;
                                    } else if (msg.equalsIgnoreCase("NAO")) {
                                        System.out.println("Jogador " + id + " respondeu: NÃO");
                                        respostas[id] = false;
                                    }
                                    respostaLatch.countDown();
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                            });
                        }
                        
                        respostaLatch.await();
                        
                        
                        boolean todosQueremContinuar = true;
                        for (boolean resposta : respostas) {
                            if (!resposta) {
                                todosQueremContinuar = false;
                                break;
                            }
                        }
                        
                        if (todosQueremContinuar) {
                            System.out.println("\n TODOS QUEREM CONTINUAR! Nova partida iniciando...\n");
                            Thread.sleep(1000);
                            rodando = false; 
                            partidaAtiva = true; 
                            
                        } else {
                            System.out.println("\n ALGUÉM DISSE NÃO! Encerrando sessão...");
                            
                            for (int i = 0; i < NUM_JOGADORES; i++) {
                                saidas[i].println("SAIR");
                            }
                            return false; 
                        }
                        
                    } else if (contarJogadoresAtivos() == 0) {
                        System.out.println("Erro: Nenhum jogador ativo!");
                        rodando = false;
                        partidaAtiva = false;
                    } else {
                        Thread.sleep(1500);
                        System.out.println();
                    }
                }
            }
            
            return true; 
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Sessão interrompida.");
            return false;
        }
    }

    private int contarJogadoresAtivos() {
        int contagem = 0;
        for (boolean ativo : jogando) {
            if (ativo) contagem++;
        }
        return contagem;
    }
}