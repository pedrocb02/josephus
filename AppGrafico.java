import Eds.ListaLigadaCircular;
import Eds.No;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class AppGrafico extends Application {

    private Pane areaDesenho = new Pane();
    private StackPane[] circulosVisuais; 
    private ListaLigadaCircular lista;
    private No noAtual;
    private Timeline animacao;

    @Override
    public void start(Stage palco) {
        // Controles super simples no topo
        TextField txtTotal = new TextField("10");
        txtTotal.setPrefWidth(50);
        
        TextField txtPasso = new TextField("2");
        txtPasso.setPrefWidth(50);
        
        Button btnIniciar = new Button("Iniciar Simulação");

        HBox controles = new HBox(15, new Label("Total:"), txtTotal, new Label("Passo:"), txtPasso, btnIniciar);
        
        // Tela principal juntando controles em cima e desenho embaixo
        VBox telaPrincipal = new VBox(10, controles, areaDesenho);
        areaDesenho.setPrefSize(400, 400);

        // Ação do botão
        btnIniciar.setOnAction(e -> {
            int total = Integer.parseInt(txtTotal.getText());
            int passo = Integer.parseInt(txtPasso.getText());
            iniciarJosephus(total, passo);
        });

        palco.setScene(new Scene(telaPrincipal, 420, 450));
        palco.setTitle("Josephus Simples");
        palco.show();
    }

    private void iniciarJosephus(int total, int passo) {
        if(animacao != null) animacao.stop(); // Para animações anteriores
        areaDesenho.getChildren().clear();

        // Array para guardar os desenhos. O tamanho é total+1 para podermos usar o índice igual ao número do nó.
        circulosVisuais = new StackPane[total + 1]; 
        lista = new ListaLigadaCircular();

        double centro = 200, raio = 160;

        // 1. Preenche sua Lista Ligada e desenha os círculos
        for (int i = 1; i <= total; i++) {
            lista.inserirFim(i);

            // Matemática básica para posicionar em círculo
            double angulo = 2 * Math.PI * i / total - (Math.PI / 2);
            double x = centro + raio * Math.cos(angulo);
            double y = centro + raio * Math.sin(angulo);

            Circle bola = new Circle(14, Color.LIGHTCORAL);
            Text numero = new Text(String.valueOf(i));
            StackPane desenhoNo = new StackPane(bola, numero);
            
            desenhoNo.setLayoutX(x - 14);
            desenhoNo.setLayoutY(y - 14);

            circulosVisuais[i] = desenhoNo;
            areaDesenho.getChildren().add(desenhoNo);
        }

        noAtual = lista.getInicio();

        // 2. Cria a animação que roda a cada meio segundo (0.5s)
        animacao = new Timeline(new KeyFrame(Duration.seconds(0.5), e -> {
            
            if (lista.getQtdNos() > 1) {
                // Pula a quantidade de passos
                for (int c = 1; c < passo; c++) {
                    noAtual = noAtual.getProximo();
                }

                // Identifica quem vai sair
                No removido = noAtual;
                int idRemovido = (int) removido.getConteudo();
                
                // Esconde o círculo da tela
                circulosVisuais[idRemovido].setVisible(false); 
                
                // Remove da sua estrutura de dados
                noAtual = removido.getProximo();
                lista.removerMeio(removido);
            }

            // Se sobrou 1, pinta de verde
            if (lista.getQtdNos() == 1) {
                animacao.stop();
                int idVencedor = (int) lista.getInicio().getConteudo();
                Circle c = (Circle) circulosVisuais[idVencedor].getChildren().get(0);
                c.setFill(Color.LIGHTGREEN); 
            }
        }));
        
        animacao.setCycleCount(total - 1); // Roda até sobrar 1
        animacao.play(); // Inicia a brincadeira
    }

    public static void main(String[] args) {
        launch(args);
    }
}