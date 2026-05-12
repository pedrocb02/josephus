/**
 * ListaLigadaSimples. Ed para Lista Ligada simples
 * 
 * @author Julio Arakaki 
 * @version 1.0 2023/05/15
 */
public class ListaLigadaSimples<T> {
    No inicio, fim;
    int qtdNos;

    /**
     * ListaLigadaSimples Construtor
     *
     */
    public ListaLigadaSimples(){
        setInicio(null);
        setFim(null);
        setQtdNos(0);
    }

    /**
     * setInicio
     *
     * @param inicio No inicio da lista ligada
     */
    private void setInicio(No inicio){
        this.inicio = inicio;
    }

    /**
     * getInicio
     *
     * @return No de inicio da lista ligada
     */
    public No getInicio(){
        return(this.inicio);
    }

    /**
     * getFim
     *
     * @return No do final da lista ligada
     */
    public No getFim(){
        return(this.fim);
    }

    /**
     * setFim
     *
     * @param fim No fim da lista ligada
     */
    private void setFim(No fim){
        this.fim = fim;
    }

    /**
     * getQtdNos
     *
     * @return int, qtde de nos na lista ligada
     */
    public int getQtdNos(){
        return this.qtdNos;
    }

    /**
     * setQtdNos
     *
     * @param qtdNos Um parâmetro
     */
    private void setQtdNos(int qtdNos){
        this.qtdNos = qtdNos;
    }

    /**
     * estaVazia
     *
     * @return boolean, true se estiver vazia e false caso contrario
     */
    public boolean estaVazia(){
        boolean vazia = false; 
        if (getQtdNos() == 0 && getInicio() == null && getFim() == null){
            vazia = true;
        }
        return vazia;
    }

    /**
     * inserirInicio
     *
     * @param elem Objeto a ser inserido no inicio da lista ligada
     */
    public void inserirInicio(T elem) {
        No novo = new No(elem); //1

        if (estaVazia()){
            setInicio(novo);
            setFim(novo);
        }
        else{
            novo.setProximo(inicio);
            setInicio(novo);
        }
        setQtdNos(getQtdNos() + 1);
    }

    /**
     * inserirFim
     *
     * @param elem Objeto a ser inserido no fim da lista ligada
     */
    public void inserirFim(T elem){
        No novo = new No(elem);
        if (estaVazia()){
            setInicio(novo);
            setFim(novo);
        }
        else{
            getFim().setProximo(novo);
            setFim(novo);
        }
        setQtdNos(getQtdNos() + 1);
    }

    /**
     * removerInicio
     *
     * @return Objeto removido
     */
    public T removerInicio(){
        No aux = null;
        T obj = null; 
        if(!estaVazia()){
            if ((getInicio() == getFim())){
                aux = getInicio();
                setInicio(null);
                setFim(null);
            } else {
                aux = getInicio();
                setInicio(aux.getProximo());
                aux.setProximo(null);
            }
            setQtdNos(getQtdNos() - 1);
            obj = (T)aux.getConteudo();
        }

        return(obj);

    }

    /**
     * removerFim
     *
     * @return Objeto removido
     */
    public T removerFim(){
        No ant = getInicio();
        No aux = null;
        T obj = null; 
        if (!estaVazia()){
            if ((getInicio() == getFim())){
                aux = getInicio();
                setInicio(null);
                setFim(null);
            } else {            
                // percorre ate achar o no antes do fim
                while(ant.getProximo() != fim){
                    ant = ant.getProximo();
                }
                ant.setProximo(null);
                aux = fim;
                setFim(ant);
            }
            setQtdNos(getQtdNos() - 1);
            obj = (T)aux.getConteudo();           
        }
        return obj;
    }
    /**
     * toString monta lista dos elementos
     */
    public String toString(){
        No temp = getInicio();
        String valores = "[";
        int contador = 1;
        if (! estaVazia() ){
            while (contador <= getQtdNos()){ // Pega todos os elementos
                if (temp.getProximo() != null)
                    // Separa com virgula
                    valores += temp.getConteudo() + ","; 
                else
                    // O ultimo nao tem virgula
                    valores += temp.getConteudo() + ""; 
                temp = temp.getProximo();
                contador++;
            }
        }
        valores += "]";
        return valores;
    }
}