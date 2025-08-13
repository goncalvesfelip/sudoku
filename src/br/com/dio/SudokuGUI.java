// Define o pacote da aplicação
package br.com.dio;

// Importa as classes do modelo do tabuleiro e das células
import br.com.dio.model.Board;
import br.com.dio.model.Space;

// Importa bibliotecas para interface gráfica
import javax.swing.*;
import java.awt.*;

// Importa utilitários para manipulação de listas e streams
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// Importa os templates de tabuleiro de diferentes dificuldades
import static br.com.dio.util.BoardTemplate.*;
// Importa utilitário para verificar se objetos são não nulos
import static java.util.Objects.nonNull;

// Define a classe principal da interface gráfica, que herda de JFrame
public class SudokuTela extends JFrame {

    // Representa o tabuleiro do jogo
    private Board tabuleiro;

    // Matriz de campos de texto que representam as células do Sudoku
    private final JTextField[][] celulas = new JTextField[9][9];

    // Painel que contém o tabuleiro
    private final JPanel painelCentral = new JPanel();

    // Construtor da interface gráfica
    public SudokuTela() {
        setTitle("Sudoku DIO - Alternativo"); // Título da janela
        setSize(520, 620); // Tamanho da janela
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Fecha a aplicação ao clicar no X
        setLayout(new BorderLayout()); // Layout principal da janela

        painelCentral.setLayout(new GridLayout(9, 9)); // Grade 9x9 para o tabuleiro
        Font fonte = new Font("Monospaced", Font.BOLD, 22); // Fonte usada nas células

        // Inicializa os campos de texto do tabuleiro
        for (int linha = 0; linha < 9; linha++) {
            for (int coluna = 0; coluna < 9; coluna++) {
                JTextField campo = new JTextField(); // Cria campo de texto
                campo.setHorizontalAlignment(JTextField.CENTER); // Centraliza texto
                campo.setFont(fonte); // Aplica fonte
                celulas[linha][coluna] = campo; // Armazena na matriz
                painelCentral.add(campo); // Adiciona ao painel
            }
        }

        add(painelCentral, BorderLayout.CENTER); // Adiciona painel ao centro da janela

        // Cria painel de botões
        JPanel painelBotoes = new JPanel();
        JButton botaoNovo = new JButton("Iniciar");
        JButton botaoVerificar = new JButton("Checar");
        JButton botaoLimpar = new JButton("Resetar");

        // Adiciona botões ao painel
        painelBotoes.add(botaoNovo);
        painelBotoes.add(botaoVerificar);
        painelBotoes.add(botaoLimpar);

        add(painelBotoes, BorderLayout.SOUTH); // Adiciona painel de botões à parte inferior

        // Define ações dos botões
        botaoNovo.addActionListener(e -> selecionarNivel());
        botaoLimpar.addActionListener(e -> limparTabuleiro());
        botaoVerificar.addActionListener(e -> validarSolucao());
    }

    // Exibe diálogo para escolher dificuldade
    private void selecionarNivel() {
        Object[] niveis = {"Fácil", "Intermediário", "Difícil"};
        int escolha = JOptionPane.showOptionDialog(this,
                "Escolha a dificuldade:",
                "Novo Jogo",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                niveis,
                niveis[0]);

        // Seleciona template com base na escolha
        String modelo = switch (escolha) {
            case 0 -> BOARD_TEMPLATE_EASY;
            case 1 -> BOARD_TEMPLATE_MEDIUM;
            case 2 -> BOARD_TEMPLATE_HARD;
            default -> null;
        };

        // Inicia o jogo com o modelo escolhido
        if (modelo != null) {
            carregarTabuleiro(modelo);
        }
    }

    // Constrói o tabuleiro a partir do template
    private void carregarTabuleiro(String modelo) {
        // Cria mapa com posições e valores fixos
        var mapa = Stream.of(modelo.split(";"))
                .map(item -> item.split(","))
                .collect(Collectors.toMap(
                        partes -> partes[0] + "," + partes[1],
                        partes -> partes[2] + "," + partes[3]
                ));

        // Cria lista de linhas com objetos Space
        var linhas = Stream.iterate(0, i -> i < 9, i -> i + 1)
                .map(i -> Stream.iterate(0, j -> j < 9, j -> j + 1)
                        .map(j -> {
                            String chave = i + "," + j;
                            String valor = mapa.getOrDefault(chave, "0,false");
                            int numero = Integer.parseInt(valor.split(",")[0]);
                            boolean fixo = Boolean.parseBoolean(valor.split(",")[1]);
                            return new Space(numero, fixo);
                        }).collect(Collectors.toList()))
                .collect(Collectors.toList());

        tabuleiro = new Board(linhas); // Cria o tabuleiro
        atualizarVisual(); // Atualiza a interface
    }

    // Atualiza os campos de texto com os valores do tabuleiro
    private void atualizarVisual() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                Space casa = tabuleiro.getSpaces().get(i).get(j);
                JTextField campo = celulas[i][j];

                campo.setText(casa.getActual() != null && casa.getActual() != 0 ? String.valueOf(casa.getActual()) : "");
                campo.setEditable(!casa.isFixed()); // Bloqueia edição se for fixo
                campo.setForeground(casa.isFixed() ? Color.BLUE : Color.BLACK); // Cor azul para fixos
            }
        }
    }

    // Limpa os valores inseridos pelo usuário
    private void limparTabuleiro() {
        tabuleiro.reset(); // Reseta valores não fixos
        atualizarVisual(); // Atualiza interface
    }

    // Verifica se o tabuleiro está completo e correto
    private void validarSolucao() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (!tabuleiro.getSpaces().get(i).get(j).isFixed()) {
                    String texto = celulas[i][j].getText();
                    try {
                        int valor = Integer.parseInt(texto);
                        tabuleiro.getSpaces().get(i).get(j).setActual(valor);
                    } catch (NumberFormatException e) {
                        tabuleiro.getSpaces().get(i).get(j).setActual(null);
                    }
                }
            }
        }

        // Exibe mensagem com base no estado do jogo
        if (tabuleiro.gameIsFinished()) {
            JOptionPane.showMessageDialog(this, "Parabéns! Sudoku resolvido!");
        } else if (tabuleiro.hasErrors()) {
            JOptionPane.showMessageDialog(this, "Há erros no tabuleiro. Corrija e tente novamente.");
        } else {
            JOptionPane.showMessageDialog(this, "O jogo ainda não está completo.");
        }
    }

    // Método principal para iniciar a interface
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SudokuTela().setVisible(true));
    }
}

