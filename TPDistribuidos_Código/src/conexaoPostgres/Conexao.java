/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package conexaoPostgres;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * @author Fabiana Barreto e Yasmine de Melo
 * Curso: Sistemas de Informação
 * Disciplina: Sistemas Distribuídos
 */
public class Conexao {
    private String url;
    private String usuario;
    private String senha;
    private Connection con;
    
    public Conexao(){
        url="jdbc:postgresql://localhost:5432/bdbanking";
        usuario="bdbanking";
        senha="123";
        try{
            Class.forName("org.postgresql.Driver");
            con=(Connection) DriverManager.getConnection(url, usuario, senha);
        }catch(Exception e){
           System.out.println("Erro de conexão.");
        }
    }
    
    /**
     *Método retorna a conexão
     * ideal quando não deseja fechar a conexão, útil em tabelas dinâmicas
     * @return conexão
     */
    public Connection getConexao(){
        return con;
    }
    
   /**
     *Método atualiza dados do banco de dados e fecha conexao
     * @param sql
     * @return numero inteiro referente a quantas linhas foram atingidas, se valor negativo, nao encontrou nenhum
     */
    public int executaSQL(String sql){
        try{
            Statement stm=con.createStatement();
            int resultado=stm.executeUpdate(sql);
            con.close();
            return resultado;
        }catch(Exception ex){
            ex.printStackTrace();
            return 0;
        }
    }
    
    /**
     *Método busca resultado do banco de dados e fecha conexão
     * Ideal para buscar conjunto de valores
     * @param sql
     * @return conjunto de resultados
     */
    public ResultSet executaBusca(String sql){
         try{
            Statement stm=con.createStatement();
            ResultSet resultado=stm.executeQuery(sql);
            con.close();
            return resultado;
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }
}

