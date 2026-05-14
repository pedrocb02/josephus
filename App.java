
/**
 * Escreva uma descrição da classe App aqui.
 * 
 * @author (seu nome) 
 * @version (um número da versão ou uma data)
 */
public class App
{
    public static void main(String[] args){
        ListaLigadaCircular lista = new ListaLigadaCircular();
        int total = 15;
        for(int i = 1; i <= total; i++){
            lista.inserirFim(i);
        }
        int passo = 2;
        int contador;
        No aux = lista.getInicio();
        No aux2;
        System.out.println(lista.toString());
        while(lista.getQtdNos() > 1){
            contador = 1;
            for(contador = 1; contador < passo; contador++){
                aux = aux.getProximo();
            }
            System.out.println("removido: " + aux.getConteudo());
            aux2 = aux.getProximo();
            lista.removerMeio(aux);
            aux = aux2;
            System.out.println(lista.toString());
        }
        
        //lista.removerMeio(lista.getFim());
        //System.out.println(lista.toString());
    }
}