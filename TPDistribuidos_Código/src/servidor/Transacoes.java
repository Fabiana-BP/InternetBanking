/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor;



/**
 * @author Fabiana Barreto e Yasmine de Melo
 * Curso: Sistemas de Informação
 * Disciplina: Sistemas Distribuídos
 */
public class Transacoes {

    /**
     * @return the novoSaldo
     */
    public double getNovoSaldo() {
        return novoSaldo;
    }

    /**
     * @param novoSaldo the novoSaldo to set
     */
    public void setNovoSaldo(double novoSaldo) {
        this.novoSaldo = novoSaldo;
    }
    
    public String toString(){
        return "data: "+data+"\t"+"tipo: "+tipo+"\t"+"valor: "+valor;
    }

    /**
     * @return the data
     */
    public String getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * @return the valor
     */
    public double getValor() {
        return valor;
    }

    /**
     * @param valor the valor to set
     */
    public void setValor(double valor) {
        this.valor = valor;
    }

    /**
     * @return the tipo
     */
    public String getTipo() {
        return tipo;
    }

    /**
     * @param tipo the tipo to set
     */
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    private String data;
    private double valor;
    private String tipo="";
    private double novoSaldo;
}
