/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Fabiana Barreto e Yasmine de Melo
 * Curso: Sistemas de Informação
 * Disciplina: Sistemas Distribuídos
 */
public interface Servico extends Remote{
    public byte[] AdicionarSaldoCliente(byte[] pgtoCriptografado) throws RemoteException,Exception;
    public byte[] criptografar(String texto) throws RemoteException,Exception;
    public String descriptografar(byte[] texto)throws RemoteException,Exception;
    public String criptografarSenha(String texto)throws RemoteException,Exception;
    public int solicitaCadastro(byte[] texto)throws RemoteException,Exception;
    public byte[] verificaUsuario(byte[] usuarioCriptografado)throws RemoteException,Exception;
    public byte[] buscaCliente(byte[] cpfCriptografado)throws RemoteException,Exception;
    public byte[] pagamentoCliente(byte[] pgtoCriptografado)throws RemoteException,Exception;
    public byte[] getSenhaCliente(byte[] cpfCriptografado)throws RemoteException,Exception;
    public byte[] getSaldo(byte[] cpfCriptografado)throws RemoteException,Exception;
    public byte[] buscaDadosExtrato(byte[] cpfCriptografado)throws RemoteException,Exception;
}
