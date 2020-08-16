/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor;

/**
 * https://www.devmedia.com.br/utilizando-criptografia-simetrica-em-java/31170
 */
import conexaoPostgres.Conexao;
import conexaoPostgres.OperacoesBanco;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.Cipher;

/**
 * @author Fabiana Barreto e Yasmine de Melo
 * Curso: Sistemas de Informação
 * Disciplina: Sistemas Distribuídos
 */
public class Servidor extends UnicastRemoteObject implements Servico {

    private final String chaveencriptacao;
    private final String IV;

    public Servidor() throws RemoteException {
        //super();
        this.chaveencriptacao = "0123456789abcdef";
        this.IV = "AAAAAAAAAAAAAAAA";
    }

    public static void main(String[] args) {

        try {

            //Servidor servidor = (Servidor) Naming.lookup("localhost:1010/Servico");
            String ipDaMaquina = InetAddress.getLocalHost().getHostAddress();
            System.out.println(ipDaMaquina);

            System.setProperty("java.rmi.hostname", ipDaMaquina);
            Registry registy = LocateRegistry.createRegistry(1099);
            Servidor serv = new Servidor();
            registy.bind("rmi://localhost:3000/cliente", serv);
            //servidor serv = new servidor();
            // Registra nome do servidor
            // Naming.rebind("ServidorHello", serv);
            System.out.println("Servidor remoto pronto.");

        } catch (RemoteException | AlreadyBoundException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Método criptografa um texto
     *
     * @param texto normal
     * @return texto criptografado em byte[]
     * @throws java.rmi.RemoteException
     */
    @Override
    public byte[] criptografar(String texto) throws RemoteException, Exception {
        return encrypt(texto);

    }

    /**
     * Método descriptografa um texto
     *
     * @param texto em byte[]
     * @return texto descriptografado
     * @throws Exception
     */
    @Override
    public String descriptografar(byte[] texto) throws Exception {
        return decrypt(texto);
    }

    public byte[] encrypt(String string) throws RemoteException, Exception {
        Cipher encripta = Cipher.getInstance("AES/CBC/PKCS5Padding", "SunJCE");
        SecretKeySpec key = new SecretKeySpec(chaveencriptacao.getBytes("UTF-8"), "AES");
        encripta.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(IV.getBytes("UTF-8")));
        return encripta.doFinal(string.getBytes("UTF-8"));
    }

    public String decrypt(byte[] textoencriptado) throws RemoteException, Exception {
        Cipher decripta = Cipher.getInstance("AES/CBC/PKCS5Padding", "SunJCE");
        SecretKeySpec key = new SecretKeySpec(chaveencriptacao.getBytes("UTF-8"), "AES");
        decripta.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(IV.getBytes("UTF-8")));
        return new String(decripta.doFinal(textoencriptado), "UTF-8");
    }

    /**
     * Método criptografa um texto por hash
     *
     * @param texto
     * @return texto criptografado
     * @throws java.rmi.RemoteException
     * @throws Exception
     */
    @Override
    public String criptografarSenha(String texto) throws RemoteException, Exception {
        MessageDigest algorithm;
        String hash = null;
        byte messageDigest[] = null;
        try {
            algorithm = MessageDigest.getInstance("SHA-256");
            messageDigest = algorithm.digest(texto.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                hexString.append(String.format("%02X", 0xFF & b));
            }
            hash = hexString.toString();

        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            ex.getMessage();
        }

        return hash;
    }

    /**
     * Método recebe dados criptografados, descriptografa eles e os insere no
     * banco de dados
     *
     * @param texto
     * @return 1 se consegui inserir, 0 caso contrário
     * @throws java.rmi.RemoteException
     * @throws Exception
     */
    @Override
    public int solicitaCadastro(byte[] texto) throws RemoteException, Exception {
        //descriptografar texto
        String textodescriptografado = descriptografar(texto);
        //separar dados concatenados
        String[] dados = textodescriptografado.split("::");
        String tabela = dados[0];
        String valores = dados[1];
        OperacoesBanco ob = new OperacoesBanco();
        if (ob.inserirLinhasBD(tabela, valores) > 0) {
            return 1;
        }
        return 0;
    }

    /**
     * Método recebe usuário criptografado, descriptografa, verifica se ele
     * existe e retorna as iniciais de seu nome, cpf e senha criptografado
     *
     * @param usuarioCriptografado
     * @return iniciais::cpf::senha criptografado ou -1 criptografado
     * @throws java.rmi.RemoteException
     * @throws Exception
     */
    @Override
    public byte[] verificaUsuario(byte[] usuarioCriptografado) throws RemoteException, Exception {
        String nome = "", login = "", senha = "", saldo = "", numero_cartao = "", cpf = "", dados = "-1";
        //descriptografa o usuário
        String usuarioDescriptografado = descriptografar(usuarioCriptografado);
        //verifica se ele existe
        ResultSet rs = null;
        Conexao con = new Conexao();
        String buscaUsuario = "select nome,login,senha,cpf"
                + " from cliente where login='" + usuarioDescriptografado + "'";
        rs = con.executaBusca(buscaUsuario);
        try {
            while (rs.next()) {
                nome = rs.getString("nome");
                login = rs.getString("login");
                senha = rs.getString("senha");
                cpf = rs.getString("cpf");
            }

        } catch (SQLException ex) {
            dados = "-1";
        }
        if (!cpf.equals("")) {//encontrou
            String[] nomeCompleto = nome.split(" ");
            String iniciais = "";
            for (String nomeCompleto1 : nomeCompleto) {
                iniciais += nomeCompleto1.substring(0, 1);
            }
            dados = iniciais + "::" + cpf + "::" + senha;

        }
        return criptografar(dados); //retornar dados criptografados
    }

    /**
     * Método descriptografa o cpf e retorna os dados dele criptografado
     *
     * @param cpfCriptografado
     * @return nome::cartao::saldo ou -1 criptografados
     * @throws java.rmi.RemoteException
     * @throws Exception
     */
    @Override
    public byte[] buscaCliente(byte[] cpfCriptografado) throws RemoteException, Exception {
        String nome = "", cartao = "", cpf = "", dados = "";
        double saldo = 0;
        //descriptografa o usuário
        String cpfDescriptografado = descriptografar(cpfCriptografado);
        //verifica se ele existe
        ResultSet rs = null;
        Conexao con = new Conexao();
        String buscaUsuario = "select nome,saldo,numero_cartao,cpf"
                + " from cliente where cpf='" + cpfDescriptografado + "'";
        rs = con.executaBusca(buscaUsuario);
        try {
            while (rs.next()) {
                nome = rs.getString("nome");
                saldo = rs.getDouble("saldo");
                cartao = rs.getString("numero_cartao");
                cpf = rs.getString("cpf");
            }

        } catch (SQLException ex) {
            dados = "-1";
        }
        if (!cpf.equals("")) {//encontrou   
            dados = nome + "::" + cartao + "::" + saldo;

        }
        return criptografar(dados); //retornar dados criptografados  
    }

    /**
     * Método recebe dados da transferencia recebida criptografada,
     * descriptografa e salva no banco
     *
     * @param pgtoCriptografado
     * @return saldo criptografado ou -1
     * @throws java.rmi.RemoteException
     * @throws Exception
     */
    @Override
    public byte[] AdicionarSaldoCliente(byte[] pgtoCriptografado) throws RemoteException, Exception {
        OperacoesBanco ob = new OperacoesBanco();
        String dados = descriptografar(pgtoCriptografado);
        String[] dadosSeparados = dados.split("::");
        double valorPagamento = Double.parseDouble(dadosSeparados[0]);
        String saldo = "-1";
        String cpf = dadosSeparados[1];
        String condicao = "cpf='" + cpf + "'";
        double saldoVelho = 0;
        double saldoNovo = 0;
        //buscar saldo atual
        ResultSet rs = null;
        Conexao con = new Conexao();
        String buscaUsuario = "select saldo,cpf"
                + " from cliente where cpf='" + cpf + "'";
        rs = con.executaBusca(buscaUsuario);
        try {
            while (rs.next()) {
                saldoVelho = rs.getDouble("saldo");
                cpf = rs.getString("cpf");
            }

        } catch (SQLException ex) {
            saldo = "-1";
        }
        if (!cpf.equals("")) {//encontrou, atualiza saldo   
            saldoNovo = saldoVelho + valorPagamento;
            if (ob.atualizarLinhasBD("cliente", "saldo", saldoNovo + "", condicao) > 0) {
                saldo = saldoNovo + "";
            } else {
                saldo = "-1";
            }

        }
        //atualizar saldo

        return criptografar(saldo);
    }

    /**
     * Método recebe dados do pagamento criptografado, descriptografa e salva no
     * banco
     *
     * @param pgtoCriptografado
     * @return saldo criptografado ou -1
     * @throws java.rmi.RemoteException
     * @throws Exception
     */
    @Override
    public byte[] pagamentoCliente(byte[] pgtoCriptografado) throws RemoteException, Exception {
        OperacoesBanco ob = new OperacoesBanco();
        String dados = descriptografar(pgtoCriptografado);
        String[] dadosSeparados = dados.split("::");
        String tabela = dadosSeparados[0];
        String valores = dadosSeparados[1];
        double valorPagamento = Double.parseDouble(dadosSeparados[2]);
        String saldo = "-1";
        String cpf = dadosSeparados[3];
        String condicao = "cpf='" + cpf + "'";
        double saldoVelho = 0;
        double saldoNovo = 0;
        //buscar saldo atual
        ResultSet rs = null;
        Conexao con = new Conexao();
        String buscaUsuario = "select saldo,cpf"
                + " from cliente where cpf='" + cpf + "'";
        rs = con.executaBusca(buscaUsuario);
        try {
            while (rs.next()) {
                saldoVelho = rs.getDouble("saldo");
                cpf = rs.getString("cpf");
            }

        } catch (SQLException ex) {
            // ex.printStackTrace();
            saldo = "-1";
        }
        if (!cpf.equals("")) {//encontrou, atualiza saldo   
            saldoNovo = saldoVelho - valorPagamento;
            if (ob.atualizarLinhasBD("cliente", "saldo", saldoNovo + "", condicao) > 0) {
                if (ob.inserirLinhasBD(tabela, valores) > 0) {
                    saldo = saldoNovo + "";
                } else {//volta o saldo com o valor antigo
                    ob.atualizarLinhasBD("cliente", "saldo", saldoVelho + "", condicao);
                    saldo = "-1";
                }
            } else {
                saldo = "-1";
            }

        }
        //retorna saldo

        return criptografar(saldo);
    }

    /**
     * Método descriptografa o cpf e retorna a senha criptografada
     *
     * @param cpfCriptografado
     * @return senha criptografada ou -1
     * @throws java.rmi.RemoteException
     * @throws Exception
     */
    @Override
    public byte[] getSenhaCliente(byte[] cpfCriptografado) throws RemoteException, Exception {
        String cpfDescrip = descriptografar(cpfCriptografado);
        String senha = "";
        String dado = "-1";
        ResultSet rs = null;
        Conexao con = new Conexao();
        String buscaUsuario = "select cpf,senha"
                + " from cliente where cpf='" + cpfDescrip + "'";
        rs = con.executaBusca(buscaUsuario);
        try {
            while (rs.next()) {
                senha = rs.getString("senha");
            }

        } catch (SQLException ex) {
            dado = "-1";
        }
        if (!senha.equals("")) {//encontrou   
            dado = senha;

        }
        return criptografar(dado);
    }

    /**
     * Método descriptografa o cpf e retorna o saldo criptografado
     *
     * @param cpfCriptografado
     * @return saldo criptografado ou -1
     * @throws java.rmi.RemoteException
     * @throws Exception
     */
    @Override
    public byte[] getSaldo(byte[] cpfCriptografado) throws RemoteException, Exception {
        String cpfDescrip = descriptografar(cpfCriptografado);
        String saldo = "-1";
        ResultSet rs = null;
        Conexao con = new Conexao();
        String buscaUsuario = "select cpf,saldo"
                + " from cliente where cpf='" + cpfDescrip + "'";
        rs = con.executaBusca(buscaUsuario);
        try {
            while (rs.next()) {
                saldo = rs.getString("saldo");
            }

        } catch (SQLException ex) {
            saldo = "-1";
        }

        return criptografar(saldo);
    }

    /**
     * Método descriptografa cpf, busca dados para gerar extrato no banco e
     * retorna eles criptografado
     *
     * @param cpfCriptografado
     * @return dados ou -1 criptografados
     * @throws RemoteException
     * @throws Exception
     */
    @Override
    public byte[] buscaDadosExtrato(byte[] cpfCriptografado) throws RemoteException, Exception {
        String cpfDescrip = descriptografar(cpfCriptografado);
        ResultSet rs = null;
        Conexao con = new Conexao();
        ArrayList<Transacoes> transacoes = new ArrayList<>();
        //buscar dados tabela pagamento
        String buscaUsuario = "select cpf_cliente,data_pgto,tipo,valor,novo_saldo"
                + " from pagamento where cpf_cliente='" + cpfDescrip + "'";
        rs = con.executaBusca(buscaUsuario);
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        SimpleDateFormat formato2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
       
        try {
            while (rs.next()) {
              

                Transacoes pg = new Transacoes();
                pg.setData(rs.getString("data_pgto"));
                pg.setTipo("PAG-"+rs.getString("tipo"));
                pg.setValor(rs.getDouble("valor"));
                pg.setNovoSaldo(rs.getDouble("novo_saldo"));
                transacoes.add(pg);
            }

        } catch (SQLException ex) {
            return criptografar("-1");
        }
        //buscar dados tabela transferencia - transferencias realizadas
        con = new Conexao();
        buscaUsuario = "select cliente_origem,data_transf,valor,pessoa_destino,novo_saldo_origem"
                + " from transferencia where cliente_origem='" + cpfDescrip + "'";
        rs = con.executaBusca(buscaUsuario);

        try {
            while (rs.next()) {

                Transacoes tr = new Transacoes();
                Calendar calendar=null;
                tr.setData(rs.getString("data_transf"));
                tr.setValor(rs.getDouble("valor"));
                tr.setTipo("TRANS-P/-"+rs.getString("pessoa_destino"));
                tr.setNovoSaldo(rs.getDouble("novo_saldo_origem"));
                transacoes.add(tr);
            }

        } catch (SQLException ex) {
            return criptografar("-1");
        }
        
        //buscar dados tabela transferencia - transferencias recebidas
        con = new Conexao();
        buscaUsuario = "select pessoa_destino,data_transf,valor,cliente_origem,novo_saldo_destino"
                + " from transferencia where pessoa_destino='" + cpfDescrip + "'";
        rs = con.executaBusca(buscaUsuario);

        try {
            while (rs.next()) {

                Transacoes tr = new Transacoes();
                tr.setData(rs.getString("data_transf"));
                tr.setValor(rs.getDouble("valor"));
                tr.setTipo("TRANS-REC/-"+rs.getString("cliente_origem"));
                tr.setNovoSaldo(rs.getDouble("novo_saldo_destino"));
                transacoes.add(tr);
            }

        } catch (SQLException ex) {
            return criptografar("-1");
        }
        
        //buscar saldo
        con = new Conexao();
        buscaUsuario = "select cpf,saldo"
                + " from cliente where cpf='" + cpfDescrip + "'";
        rs = con.executaBusca(buscaUsuario);
        try {
            while (rs.next()) {
                Transacoes tr=new Transacoes();
                tr.setValor(0);
                tr.setTipo("SALDO");
                tr.setData(formato2.format(Calendar.getInstance().getTime()));
                tr.setNovoSaldo(rs.getDouble("saldo"));
                transacoes.add(tr);
            }

        } catch (SQLException ex) {
            return criptografar("-1");
        }
   
        //ordenar transacoes em ordem decrescente
        Collections.sort(transacoes, new ComparadorTransacoes());
        
        //transformar ArrayList de transacoes em String
        String trans="";
        for(Transacoes t:transacoes){
            DecimalFormat decimalFormato=new DecimalFormat("0.00");
            trans+=t.getData()+"#"+t.getTipo()+"#"+decimalFormato.format(t.getValor())+"#"+decimalFormato.format(t.getNovoSaldo())+"::";
        }
         
        return criptografar(trans);
    }

}
