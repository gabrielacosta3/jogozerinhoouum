import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Cliente {
    private static final String SERVIDOR_HOST = "localhost";
    private static final int SERVIDOR_PORTA = 8080;
    private Socket socket;
    private PrintWriter saida;
    private BufferedReader entrada;
    private int idJogador;

    public static void main(String[] args) {
        new Cliente().executar();
    }

    public void executar() {
        try {
            socket = new Socket(SERVIDOR_HOST, SERVIDOR_PORTA);
            saida = new PrintWriter(socket.getOutputStream(), true);
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Conectado ao servidor!\n");

            Scanner scanner = new Scanner(System.in);
            
            while (true) {
                String mensagem = entrada.readLine();
                if (mensagem == null) {
                    System.out.println("\nConexão com o servidor foi encerrada.");
                    break;
                }
                
                if (mensagem.startsWith("BEM_VINDO:")) {
                    idJogador = Integer.parseInt(mensagem.substring(10));
                    System.out.println("=== Bem-vindo ao Jogo 0 ou 1 ===");
                    System.out.println("Você é o Jogador " + idJogador);
                    System.out.println("================================\n");

                } else if (mensagem.equals("AGUARDANDO_CONFIRMACAO")) {
                    System.out.println("O servidor está aguardando sua confirmação.");
                    System.out.print("Digite 'PRONTO' para começar: ");
                    String resposta = scanner.nextLine();
                    if (resposta.equalsIgnoreCase("PRONTO")) {
                        saida.println("PRONTO");
                        System.out.println("Confirmação enviada!\n");
                    } else {
                        System.out.println(" Confirmação inválida. Digite PRONTO.");
                    }

                } else if (mensagem.equals("INICIO_RODADA")) {
                    System.out.println("\n  RODADA INICIADA!");
                    System.out.println("Faça sua escolha:\n");

                } else if (mensagem.equals("SUA_ESCOLHA")) {
                    boolean escolhaValida = false;
                    while (!escolhaValida) {
                        System.out.print("Digite 0 ou 1: ");
                        String escolha = scanner.nextLine();
                        try {
                            int numero = Integer.parseInt(escolha);
                            if (numero == 0 || numero == 1) {
                                saida.println("ESCOLHA:" + numero);
                                escolhaValida = true;
                                System.out.println(" Aguardando resultado...\n");
                            } else {
                                System.out.println(" Número inválido! Digite 0 ou 1.");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println(" Digite um número válido!");
                        }
                    }

                } else if (mensagem.equals("SEGURO")) {
                    System.out.println("VOCÊ ESTÁ SEGURO! Continua no jogo.\n");
                    Thread.sleep(1000);

                } else if (mensagem.equals("ELIMINADO")) {
                    System.out.println(" VOCÊ FOI ELIMINADO! Aguarde o fim da partida.\n");
                    Thread.sleep(1000);

                } else if (mensagem.startsWith("FIM_JOGO:VENCEDOR:")) {
                    String[] partes = mensagem.split(":");
                    int vencedor = Integer.parseInt(partes[2]);

                    System.out.println("\n" + "=".repeat(40));
                    System.out.println(" FIM DO JOGO!");
                    System.out.println("=".repeat(40));
                    
                    if (vencedor == idJogador) {
                        System.out.println(" PARABÉNS! VOCÊ VENCEU! 🎉");
                    } else {
                        System.out.println("O VENCEDOR é o Jogador " + vencedor);
                    }
                    System.out.println("=".repeat(40) + "\n");
                    Thread.sleep(1500);

                } else if (mensagem.equals("JOGAR_NOVAMENTE?")) {
                    System.out.println("O jogo acabou!");
                    boolean respostaValida = false;
                    while (!respostaValida) {
                        System.out.print("Quer jogar novamente? (SIM/NAO): ");
                        String novamente = scanner.nextLine().toUpperCase();
                        if (novamente.equals("SIM") || novamente.equals("NAO")) {
                            saida.println(novamente);
                            respostaValida = true;
                            
                            if (novamente.equals("SIM")) {
                                System.out.println(" Pronto para a proxima partida!\n");
                            } else {
                                System.out.println(" Obrigado por jogar! Até logo.\n");
                            }
                        } else {
                            System.out.println(" Resposta invalida! Digite SIM ou NAO.");
                        }
                    }

                } else if (mensagem.equals("SAIR")) {
                    System.out.println("\n" + "=".repeat(40));
                    System.out.println("SERVIDOR ENCERROU A SESSAO");
                    System.out.println("Obrigado por jogar! Conexão encerrada.");
                    System.out.println("=".repeat(40));
                    break;
                }
            }

        } catch (IOException | InterruptedException e) {
            System.out.println("Erro na conexao: " + e.getMessage());
        } finally {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Cliente finalizado.");
        }
    }
}