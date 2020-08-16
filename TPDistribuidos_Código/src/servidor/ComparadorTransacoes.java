/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor;

import java.sql.Date;
import java.util.Comparator;

/**
 * @author Fabiana Barreto e Yasmine de Melo
 * Curso: Sistemas de Informação
 * Disciplina: Sistemas Distribuídos
 */
public class ComparadorTransacoes implements Comparator<Transacoes>{

    @Override
    public int compare(Transacoes o1, Transacoes o2) {
        
        String data1=o1.getData().replace("-", "");
        data1=data1.replace(":", "");
        data1=data1.replace(" ", "");
        
        Long d1=Long.parseLong(data1);
        String data2=o2.getData().replace("-", "");
        data2=data2.replace(":", "");
        data2=data2.replace(" ", "");
        Long d2=Long.parseLong(data2);
        if(d1>d2){
            return -1;
        }else if(d1<d2){
            return +1;
            
        }else
            return 0;
    }
    
}
