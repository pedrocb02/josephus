import Eds.ListaLigadaCircular;
import Eds.No;
import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

/**
 * Simulador interativo do Problema de Josephus — Versão Nota 10.
 */
public class AppGrafico extends Application {

    // ---------- Paleta ----------
    private static final Color BG_TOP     = Color.web("#0b1020");
    private static final Color BG_BOTTOM  = Color.web("#1a1f3a");
    private static final Color TEXT       = Color.web("#e6e9f5");
    private static final Color MUTED      = Color.web("#8a93b8");
    private static final Color ACCENT_2   = Color.web("#22d3ee"); // ciano
    private static final Color OK         = Color.web("#10b981");
    private static final Color WARN       = Color.web("#f59e0b");
    private static final Color BAD        = Color.web("#ef4444");
    private static final Color NODE       = Color.web("#3b82f6");
    private static final Color NODE_RING  = Color.web("#1d4ed8");

    // ---------- Constantes de Validação ----------
    private static final int MAX_NOS = 200;

    // ---------- Estado ----------
    private final Pane areaDesenho = new Pane();
    private final Label lblStatus  = new Label("Pronto. Configure os parâmetros e inicie.");
    private final Label lblRodada  = new Label("0");
    private final Label lblVivos   = new Label("0");
    private final Label lblMortos  = new Label("0");
    private final Label lblUltimo  = new Label("—");
    
    // Nova label para mostrar o histórico ordenado de eliminados
    private final Label lblHistorico = new Label("Nenhum até agora");

    private final Map<Integer, StackPane> nodesMap = new HashMap<>();
    private Circle anelGuia;
    private Polygon ponteiro;
    private Group linhasGroup;

    private ListaLigadaCircular lista;
    private No noAtual;
    private Timeline animacao;

    private int totalAtual = 0;
    private int passoAtual = 0;
    private int rodada     = 0;
    private int eliminados = 0;
    private StringBuilder sequenciaEliminados = new StringBuilder();

    private double centroX, centroY, raioCaminho, raioNo;

    @Override
    public void start(Stage palco) {
        // ---------- Controles ----------
        TextField txtTotal = textField("10");
        TextField txtPasso = textField("2");

        Slider sldVelocidade = new Slider(150, 1500, 700);
        sldVelocidade.setShowTickMarks(false);
        sldVelocidade.setShowTickLabels(false);
        styleSlider(sldVelocidade);
        Label lblVel = new Label();
        lblVel.textProperty().bind(Bindings.format("Velocidade: %.0f ms", sldVelocidade.valueProperty()));
        lblVel.setTextFill(MUTED);
        lblVel.setFont(Font.font("Segoe UI", 12));

        Button btnIniciar = primaryButton("Iniciar");
        Button btnPausar  = ghostButton("Pausar");
        Button btnReset   = ghostButton("Resetar");
        btnPausar.setDisable(true);
        btnReset.setDisable(true);

        HBox botoes = new HBox(8, btnIniciar, btnPausar, btnReset);
        botoes.setAlignment(Pos.CENTER_LEFT);

        VBox painel = new VBox(18,
            tituloPainel("Josephus"),
            subtitulo("Simulador interativo"),
            separador(),
            campo("Total de nós (Máx: " + MAX_NOS + ")",  txtTotal),
            campo("Passo (salto)", txtPasso),
            new VBox(6, lblVel, sldVelocidade),
            botoes,
            separador(),
            hud(),
            separador(),
            painelHistorico(), // Adicionado painel visual do histórico
            separador(),
            legenda()
        );
        painel.setPadding(new Insets(24));
        painel.setPrefWidth(300);
        painel.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #131838, #0e1230);" +
            "-fx-border-color: #2a3160; -fx-border-width: 0 1 0 0;"
        );

        // ---------- Área de desenho ----------
        areaDesenho.setBackground(new Background(new BackgroundFill(
            new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, BG_TOP), new Stop(1, BG_BOTTOM)),
            CornerRadii.EMPTY, Insets.EMPTY)));

        // Recalcula a roda quando a janela for redimensionada
        areaDesenho.widthProperty().addListener((o, a, b) -> reposicionarRoda());
        areaDesenho.heightProperty().addListener((o, a, b) -> reposicionarRoda());

        // ---------- Status bar ----------
        lblStatus.setTextFill(TEXT);
        lblStatus.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        HBox barraStatus = new HBox(lblStatus);
        barraStatus.setAlignment(Pos.CENTER_LEFT);
        barraStatus.setPadding(new Insets(12, 20, 12, 20));
        barraStatus.setStyle("-fx-background-color: #0d1230; -fx-border-color: #2a3160; -fx-border-width: 1 0 0 0;");

        BorderPane root = new BorderPane();
        root.setLeft(painel);
        root.setCenter(areaDesenho);
        root.setBottom(barraStatus);

        // ---------- Eventos ----------
        btnIniciar.setOnAction(e -> {
            try {
                int total = Integer.parseInt(txtTotal.getText().trim());
                int passo = Integer.parseInt(txtPasso.getText().trim());
                
                // Validações estritas para evitar DPs!
                if (total <= 0 || passo <= 0) { 
                    mostrarErro("Os valores devem ser maiores que zero."); 
                    return; 
                }
                if (total > MAX_NOS) { 
                    mostrarErro("Limite máximo excedido! Por favor, utilize no máximo " + MAX_NOS + " nós."); 
                    return; 
                }
                
                iniciarJosephus(total, passo, (long) sldVelocidade.getValue());
                btnPausar.setDisable(false);
                btnReset.setDisable(false);
                btnPausar.setText("Pausar");
            } catch (NumberFormatException ex) {
                mostrarErro("Digite apenas números inteiros válidos.");
            }
        });

        btnPausar.setOnAction(e -> {
            if (animacao == null) return;
            if (animacao.getStatus() == Animation.Status.RUNNING) {
                animacao.pause();
                btnPausar.setText("Continuar");
            } else {
                animacao.play();
                btnPausar.setText("Pausar");
            }
        });

        btnReset.setOnAction(e -> {
            if (animacao != null) animacao.stop();
            areaDesenho.getChildren().clear();
            nodesMap.clear();
            rodada = 0; eliminados = 0;
            sequenciaEliminados.setLength(0);
            atualizarHud(0);
            lblUltimo.setText("—");
            lblHistorico.setText("Nenhum até agora");
            lblStatus.setText("Resetado. Configure e inicie novamente.");
            btnPausar.setDisable(true);
            btnReset.setDisable(true);
        });

        sldVelocidade.valueProperty().addListener((o, a, b) -> {
            if (animacao != null && totalAtual > 0) {
                boolean estavaRodando = animacao.getStatus() == Animation.Status.RUNNING;
                reconstruirTimeline(b.longValue());
                if (estavaRodando) animacao.play();
            }
        });

        Scene cena = new Scene(root, 1150, 750);
        palco.setTitle("Josephus • Simulador Interativo Profissional");
        palco.setMinWidth(950);
        palco.setMinHeight(650);
        palco.setScene(cena);
        palco.show();
    }

    // =========================================================
    //                        SIMULAÇÃO
    // =========================================================

    private void iniciarJosephus(int total, int passo, long velocidadeMs) {
        if (animacao != null) animacao.stop();
        areaDesenho.getChildren().clear();
        nodesMap.clear();

        this.totalAtual = total;
        this.passoAtual = passo;
        this.rodada     = 0;
        this.eliminados = 0;
        this.sequenciaEliminados.setLength(0);
        this.lblHistorico.setText("Nenhum até agora");

        lista = new ListaLigadaCircular();
        for (int i = 1; i <= total; i++) lista.inserirFim(i);

        calcularMetricas();

        // Anel guia
        anelGuia = new Circle(centroX, centroY, raioCaminho);
        anelGuia.setFill(Color.TRANSPARENT);
        anelGuia.setStroke(Color.web("#2a3160"));
        anelGuia.getStrokeDashArray().addAll(4.0, 6.0);
        anelGuia.setStrokeWidth(1.2);
        areaDesenho.getChildren().add(anelGuia);

        // Linhas conectando vizinhos
        linhasGroup = new Group();
        areaDesenho.getChildren().add(linhasGroup);
        desenharLinhas();

        // Nós
        for (int i = 1; i <= total; i++) {
            StackPane no = criarNoVisual(i);
            posicionarNo(no, i);
            nodesMap.put(i, no);
            areaDesenho.getChildren().add(no);

            // Animação de entrada
            no.setOpacity(0);
            no.setScaleX(0.4); no.setScaleY(0.4);
            FadeTransition ft = new FadeTransition(Duration.millis(350), no);
            ft.setToValue(1);
            ScaleTransition st = new ScaleTransition(Duration.millis(350), no);
            st.setToX(1); st.setToY(1);
            ParallelTransition pt = new ParallelTransition(ft, st);
            pt.setDelay(Duration.millis(i * 15L));
            pt.play();
        }

        // Ponteiro (seta) indicando nó atual
        ponteiro = new Polygon(0,-12, 10,8, -10,8);
        ponteiro.setFill(ACCENT_2);
        ponteiro.setEffect(new Glow(0.6));
        areaDesenho.getChildren().add(ponteiro);

        noAtual = lista.getInicio();
        moverPonteiroPara(noAtual, false);
        destacarAtual(noAtual);

        atualizarHud(total);
        lblStatus.setText("Simulação iniciada: " + total + " nós, salto de " + passo + ".");

        reconstruirTimeline(velocidadeMs);
        animacao.setDelay(Duration.millis(450));
        animacao.play();
    }

    private void reconstruirTimeline(long velocidadeMs) {
        if (animacao != null) animacao.stop();
        animacao = new Timeline(new KeyFrame(Duration.millis(velocidadeMs), e -> tick()));
        animacao.setCycleCount(Animation.INDEFINITE);
    }

    private void tick() {
        if (lista == null || lista.getQtdNos() <= 1) {
            finalizarSeVencedor();
            return;
        }

        resetarCores();

        // Caminha "passo" posições
        for (int c = 1; c < passoAtual; c++) {
            noAtual = noAtual.getProximo();
        }
        destacarAtual(noAtual);
        moverPonteiroPara(noAtual, true);

        // Elimina
        No removido = noAtual;
        int idRemovido = (int) removido.getConteudo();
        StackPane visualRemovido = nodesMap.get(idRemovido);

        rodada++;
        eliminados++;
        
        // Adiciona à string de histórico ordenado
        if (sequenciaEliminados.length() > 0) {
            sequenciaEliminados.append(" ➔ ");
        }
        sequenciaEliminados.append(idRemovido);
        lblHistorico.setText(sequenciaEliminados.toString());

        lblUltimo.setText("#" + idRemovido);
        atualizarHud(lista.getQtdNos() - 1);
        lblStatus.setText("Rodada " + rodada + " — eliminado: nó " + idRemovido);

        if (visualRemovido != null) {
            Circle bola = (Circle) visualRemovido.getChildren().get(0);
            bola.setFill(BAD);
            bola.setStroke(Color.web("#7f1d1d"));

            FadeTransition ft = new FadeTransition(Duration.millis(450), visualRemovido);
            ft.setToValue(0.15);
            ScaleTransition st = new ScaleTransition(Duration.millis(450), visualRemovido);
            st.setToX(0.6); st.setToY(0.6);
            new ParallelTransition(ft, st).play();
        }

        noAtual = removido.getProximo();
        lista.removerMeio(removido);

        if (lista.getQtdNos() > 1) {
            destacarAtual(noAtual);
            moverPonteiroPara(noAtual, true);
        } else {
            finalizarSeVencedor();
        }
    }

    private void finalizarSeVencedor() {
        if (lista != null && lista.getQtdNos() == 1) {
            animacao.stop();
            int vencedor = (int) lista.getInicio().getConteudo();
            StackPane visual = nodesMap.get(vencedor);
            if (visual != null) {
                Circle bola = (Circle) visual.getChildren().get(0);
                bola.setFill(OK);
                bola.setStroke(Color.web("#047857"));
                bola.setEffect(new Glow(0.8));
                animarVencedor(visual);
            }
            lblStatus.setText("🏆 Sobrevivente: nó " + vencedor + "  •  " + rodada + " rodadas, " + eliminados + " eliminados.");
        }
    }

    // =========================================================
    //                      GEOMETRIA / VISUAL
    // =========================================================

    private void calcularMetricas() {
        double w = Math.max(areaDesenho.getWidth(), 400);
        double h = Math.max(areaDesenho.getHeight(), 400);
        centroX = w / 2.0;
        centroY = h / 2.0;
        raioCaminho = Math.min(w, h) * 0.38;
        double circ = 2 * Math.PI * raioCaminho;
        double raioMax = (circ / Math.max(totalAtual, 1)) / 2.3;
        raioNo = Math.max(5, Math.min(28, raioMax));
    }

    private void reposicionarRoda() {
        if (totalAtual == 0 || nodesMap.isEmpty()) return;
        calcularMetricas();
        if (anelGuia != null) {
            anelGuia.setCenterX(centroX);
            anelGuia.setCenterY(centroY);
            anelGuia.setRadius(raioCaminho);
        }
        for (Map.Entry<Integer, StackPane> e : nodesMap.entrySet()) {
            posicionarNo(e.getValue(), e.getKey());
        }
        desenharLinhas();
        if (noAtual != null) moverPonteiroPara(noAtual, false);
    }

    private void posicionarNo(StackPane no, int i) {
        double angulo = 2 * Math.PI * (i - 1) / totalAtual - Math.PI / 2;
        double x = centroX + raioCaminho * Math.cos(angulo);
        double y = centroY + raioCaminho * Math.sin(angulo);
        Circle c = (Circle) no.getChildren().get(0);
        c.setRadius(raioNo);
        Text t = (Text) no.getChildren().get(1);
        t.setFont(Font.font("Segoe UI", FontWeight.BOLD, Math.max(8, raioNo * 0.7)));
        no.setLayoutX(x - raioNo);
        no.setLayoutY(y - raioNo);
    }

    private void desenharLinhas() {
        if (linhasGroup == null) return;
        linhasGroup.getChildren().clear();
        for (int i = 1; i <= totalAtual; i++) {
            int j = (i % totalAtual) + 1;
            double a1 = 2 * Math.PI * (i - 1) / totalAtual - Math.PI / 2;
            double a2 = 2 * Math.PI * (j - 1) / totalAtual - Math.PI / 2;
            Line l = new Line(
                centroX + raioCaminho * Math.cos(a1), centroY + raioCaminho * Math.sin(a1),
                centroX + raioCaminho * Math.cos(a2), centroY + raioCaminho * Math.sin(a2)
            );
            l.setStroke(Color.web("#2a3160"));
            l.setStrokeWidth(1);
            l.setOpacity(0.4);
            linhasGroup.getChildren().add(l);
        }
    }

    private StackPane criarNoVisual(int numero) {
        Circle circulo = new Circle(raioNo);
        circulo.setFill(NODE);
        circulo.setStroke(NODE_RING);
        circulo.setStrokeWidth(raioNo > 10 ? 2 : 1);

        DropShadow shadow = new DropShadow();
        shadow.setRadius(8);
        shadow.setOffsetY(3);
        shadow.setColor(Color.color(0, 0, 0, 0.45));
        circulo.setEffect(shadow);

        Text texto = new Text(String.valueOf(numero));
        texto.setFill(Color.WHITE);
        texto.setFont(Font.font("Segoe UI", FontWeight.BOLD, Math.max(8, raioNo * 0.7)));

        return new StackPane(circulo, texto);
    }

    private void moverPonteiroPara(No no, boolean animado) {
        if (no == null || ponteiro == null) return;
        int id = (int) no.getConteudo();
        StackPane sp = nodesMap.get(id);
        if (sp == null) return;
        double angulo = anguloDoNo(id);
        double r = raioCaminho - raioNo - 18;
        double x = centroX + r * Math.cos(angulo);
        double y = centroY + r * Math.sin(angulo);
        ponteiro.setRotate(Math.toDegrees(angulo) + 90);

        if (animado) {
            Timeline tl = new Timeline(
                new KeyFrame(Duration.millis(220),
                    new KeyValue(ponteiro.layoutXProperty(), x, Interpolator.EASE_BOTH),
                    new KeyValue(ponteiro.layoutYProperty(), y, Interpolator.EASE_BOTH))
            );
            tl.play();
        } else {
            ponteiro.setLayoutX(x);
            ponteiro.setLayoutY(y);
        }
    }

    private double anguloDoNo(int id) {
        return 2 * Math.PI * (id - 1) / totalAtual - Math.PI / 2;
    }

    private void destacarAtual(No no) {
        if (no == null) return;
        int id = (int) no.getConteudo();
        StackPane sp = nodesMap.get(id);
        if (sp == null) return;
        Circle c = (Circle) sp.getChildren().get(0);
        c.setFill(WARN);
        c.setStroke(Color.web("#b45309"));

        ScaleTransition pulse = new ScaleTransition(Duration.millis(220), sp);
        pulse.setFromX(1); pulse.setFromY(1);
        pulse.setToX(1.18); pulse.setToY(1.18);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(2);
        pulse.play();
    }

    private void resetarCores() {
        for (StackPane sp : nodesMap.values()) {
            if (sp == null) continue;
            Circle c = (Circle) sp.getChildren().get(0);
            Paint fill = c.getFill();
            if (!fill.equals(BAD) && !fill.equals(OK)) {
                c.setFill(NODE);
                c.setStroke(NODE_RING);
            }
        }
    }

    private void animarVencedor(StackPane vencedor) {
        ScaleTransition st = new ScaleTransition(Duration.seconds(0.7), vencedor);
        st.setToX(1.5); st.setToY(1.5);
        st.setCycleCount(Animation.INDEFINITE);
        st.setAutoReverse(true);
        st.play();
    }

    // =========================================================
    //                      UI HELPERS
    // =========================================================

    private void atualizarHud(int vivos) {
        lblRodada.setText(String.valueOf(rodada));
        lblVivos.setText(String.valueOf(vivos));
        lblMortos.setText(String.valueOf(eliminados));
    }

    private Label tituloPainel(String s) {
        Label l = new Label(s);
        l.setTextFill(TEXT);
        l.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 26));
        return l;
    }

    private Label subtitulo(String s) {
        Label l = new Label(s);
        l.setTextFill(MUTED);
        l.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        return l;
    }

    private Region separador() {
        Region r = new Region();
        r.setPrefHeight(1);
        r.setStyle("-fx-background-color: #2a3160;");
        return r;
    }

    private VBox campo(String rotulo, TextField tf) {
        Label l = new Label(rotulo);
        l.setTextFill(MUTED);
        l.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        return new VBox(6, l, tf);
    }

    private TextField textField(String valor) {
        TextField tf = new TextField(valor);
        tf.setPrefHeight(38);
        tf.setStyle(
            "-fx-background-color: #0c1130;" +
            "-fx-text-fill: #e6e9f5;" +
            "-fx-prompt-text-fill: #6b7390;" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: #2a3160;" +
            "-fx-border-radius: 8;" +
            "-fx-border-width: 1;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 0 12 0 12;"
        );
        return tf;
    }

    private Button primaryButton(String s) {
        Button b = new Button(s);
        String base =
            "-fx-background-color: linear-gradient(to right, #7c5cff, #22d3ee);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 10 18;" +
            "-fx-cursor: hand;";
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(base + "-fx-effect: dropshadow(gaussian, rgba(124,92,255,0.55), 18, 0, 0, 0);"));
        b.setOnMouseExited(e -> b.setStyle(base));
        return b;
    }

    private Button ghostButton(String s) {
        Button b = new Button(s);
        String base =
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #e6e9f5;" +
            "-fx-font-size: 13px;" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: #2a3160;" +
            "-fx-border-radius: 10;" +
            "-fx-border-width: 1;" +
            "-fx-padding: 10 14;" +
            "-fx-cursor: hand;";
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(base + "-fx-background-color: #1a2150;"));
        b.setOnMouseExited(e -> b.setStyle(base));
        return b;
    }

    private void styleSlider(Slider s) {
        s.setStyle("-fx-control-inner-background: #2a3160;");
    }

    private VBox hud() {
        Label t = new Label("Status");
        t.setTextFill(MUTED);
        t.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));

        GridPane g = new GridPane();
        g.setHgap(10); g.setVgap(8);
        g.add(hudCell("Rodada", lblRodada),   0, 0);
        g.add(hudCell("Vivos",  lblVivos),    1, 0);
        g.add(hudCell("Mortos", lblMortos),   0, 1);
        g.add(hudCell("Último", lblUltimo),   1, 1);
        return new VBox(8, t, g);
    }

    private VBox hudCell(String rotulo, Label valor) {
        Label r = new Label(rotulo);
        r.setTextFill(MUTED);
        r.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        valor.setTextFill(TEXT);
        valor.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        VBox v = new VBox(2, r, valor);
        v.setPadding(new Insets(10, 12, 10, 12));
        v.setStyle("-fx-background-color: #0c1130; -fx-background-radius: 10; -fx-border-color: #2a3160; -fx-border-radius: 10; -fx-border-width: 1;");
        v.setPrefWidth(120);
        return v;
    }

    // Componente visual para exibir o histórico de eliminados em ordem
    private VBox painelHistorico() {
        Label t = new Label("Histórico de Eliminação");
        t.setTextFill(MUTED);
        t.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));

        lblHistorico.setTextFill(ACCENT_2);
        lblHistorico.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblHistorico.setWrapText(true);

        ScrollPane scroll = new ScrollPane(lblHistorico);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(75);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background: #0c1130; -fx-background-color: #0c1130; -fx-border-color: #2a3160; -fx-border-radius: 8; -fx-background-radius: 8;");
        scroll.setPadding(new Insets(8));

        return new VBox(8, t, scroll);
    }

    private VBox legenda() {
        Label t = new Label("Legenda");
        t.setTextFill(MUTED);
        t.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        return new VBox(6, t,
            itemLegenda(NODE, "Ativo"),
            itemLegenda(WARN, "Atual"),
            itemLegenda(BAD,  "Eliminado"),
            itemLegenda(OK,   "Sobrevivente")
        );
    }

    private HBox itemLegenda(Color c, String s) {
        Circle dot = new Circle(6, c);
        Label l = new Label(s);
        l.setTextFill(TEXT);
        l.setFont(Font.font("Segoe UI", 12));
        HBox h = new HBox(8, dot, l);
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    private void mostrarErro(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Entrada inválida");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    public static void main(String[] args) { launch(args); }
}