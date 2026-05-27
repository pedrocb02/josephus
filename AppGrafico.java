import Eds.ListaLigadaCircular;
import Eds.No;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
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

    // Lista de eliminados
    private VBox listaEliminados = new VBox(4);
    private int ordemEliminacao = 1;

    // Velocidade e passo guardados como campos
    private double velocidade = 0.5;
    private int passoAtual = 2;

    @Override
    public void start(Stage palco) {
        // --- Campos de entrada ---
        TextField txtTotal = new TextField("10");
        txtTotal.setPrefWidth(50);

        TextField txtPasso = new TextField("2");
        txtPasso.setPrefWidth(50);

        // Limite de 200: bloqueia digitação inválida
        txtTotal.textProperty().addListener((obs, antigo, novo) -> {
            if (!novo.matches("\\d*")) {
                txtTotal.setText(antigo);
                return;
            }
            if (!novo.isEmpty()) {
                try {
                    int val = Integer.parseInt(novo);
                    if (val > 200) txtTotal.setText("200");
                } catch (NumberFormatException ignored) {}
            }
        });

        txtPasso.textProperty().addListener((obs, antigo, novo) -> {
            if (!novo.matches("\\d*")) txtPasso.setText(antigo);
        });

        Button btnIniciar = new Button("Iniciar Simulação");

        HBox controlesEntrada = new HBox(10,
                new Label("Total (máx 200):"), txtTotal,
                new Label("Passo:"), txtPasso,
                btnIniciar);
        controlesEntrada.setStyle("-fx-padding: 5px;");

        // --- Controle de velocidade ---
        Slider sliderVel = new Slider(0.1, 2.0, 0.5);
        sliderVel.setShowTickLabels(true);
        sliderVel.setShowTickMarks(true);
        sliderVel.setMajorTickUnit(0.5);
        sliderVel.setPrefWidth(220);

        Label lblVel = new Label("Velocidade: 0.5s/passo");

        sliderVel.valueProperty().addListener((obs, antigo, novo) -> {
            velocidade = Math.round(novo.doubleValue() * 10.0) / 10.0;
            lblVel.setText(String.format("Velocidade: %.1fs/passo", velocidade));
        });

        HBox controleVel = new HBox(10, lblVel, sliderVel);
        controleVel.setStyle("-fx-padding: 5px;");

        // --- Painel de eliminados ---
        Label tituloEliminados = new Label("Eliminados:");
        tituloEliminados.setStyle("-fx-font-weight: bold;");

        ScrollPane scrollEliminados = new ScrollPane(listaEliminados);
        scrollEliminados.setPrefSize(150, 380);
        scrollEliminados.setFitToWidth(true);

        VBox painelEliminados = new VBox(5, tituloEliminados, scrollEliminados);
        painelEliminados.setStyle("-fx-padding: 5px; -fx-border-color: #ccc; -fx-border-radius: 4px;");

        // --- Layout geral ---
        VBox esquerda = new VBox(5, controlesEntrada, controleVel, areaDesenho);
        areaDesenho.setPrefSize(400, 380);

        HBox raiz = new HBox(10, esquerda, painelEliminados);
        raiz.setStyle("-fx-padding: 10px;");

        // Ação do botão
        btnIniciar.setOnAction(e -> {
            String textoTotal = txtTotal.getText();
            String textoPasso = txtPasso.getText();

            if (textoTotal.isEmpty() || textoPasso.isEmpty()) return;

            int total = Integer.parseInt(textoTotal);
            int passo = Integer.parseInt(textoPasso);

            if (total < 2) { txtTotal.setText("2"); return; }
            if (passo < 1) { txtPasso.setText("1"); return; }

            passoAtual = passo; // salva para uso na animação
            iniciarJosephus(total);
        });

        palco.setScene(new Scene(raiz, 600, 460));
        palco.setTitle("Josephus");
        palco.show();
    }

    private void iniciarJosephus(int total) {
        if (animacao != null) animacao.stop();
        areaDesenho.getChildren().clear();
        listaEliminados.getChildren().clear();
        ordemEliminacao = 1;

        circulosVisuais = new StackPane[total + 1];
        lista = new ListaLigadaCircular();

        double centro = 190, raio = 155;

        for (int i = 1; i <= total; i++) {
            lista.inserirFim(i);

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

        animacao = new Timeline(new KeyFrame(Duration.seconds(velocidade), e -> {

            if (lista.getQtdNos() > 1) {
                // Pula os passos
                for (int c = 1; c < passoAtual; c++) {
                    noAtual = noAtual.getProximo();
                }

                No removido = noAtual;
                int idRemovido = (int) removido.getConteudo();

                // Esconde visualmente
                circulosVisuais[idRemovido].setVisible(false);

                // Registra na lista de eliminados
                Label labelEliminado = new Label(ordemEliminacao + "º  Nó " + idRemovido);
                labelEliminado.setStyle("-fx-font-size: 12px;");
                listaEliminados.getChildren().add(labelEliminado);
                ordemEliminacao++;

                // Avança e remove da estrutura
                noAtual = removido.getProximo();
                lista.removerMeio(removido);
            }

            if (lista.getQtdNos() == 1) {
                animacao.stop();
                int idVencedor = (int) lista.getInicio().getConteudo();
                Circle c = (Circle) circulosVisuais[idVencedor].getChildren().get(0);
                c.setFill(Color.LIGHTGREEN);

                Label labelVencedor = new Label("Vencedor: No " + idVencedor);
                labelVencedor.setStyle("-fx-font-weight: bold; -fx-text-fill: green; -fx-font-size: 13px;");
                listaEliminados.getChildren().add(labelVencedor);
            }
        }));

        animacao.setCycleCount(total - 1);
        animacao.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}