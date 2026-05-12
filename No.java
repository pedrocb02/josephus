/**
 * No de uma lista ligada.
 * 
 * @author Julio Arakaki 
 * @version 1.0 2023/05/15
 */
public class No<T>{
    /**
     * Atributos
     */
    T conteudo; // conteudo
    No<T> proximo; // proximo


    /**
     * No Construtor
     *
     * @param conteudo Object a ser inserido no no
     */
    public No(T conteudo){
        setConteudo(conteudo);
        setProximo(null);
    }
    /**
     * setters e getters
     */
    public void setConteudo(T conteudo){
        this.conteudo = conteudo;
    }
    
    public void setProximo(No proximo){
        this.proximo = proximo;
    }
    public T getConteudo(){
        return(this.conteudo);
    }
    
    public No getProximo(){
        return(this.proximo);
    }
    public String toString(){
        return(conteudo.toString());
    }
}
