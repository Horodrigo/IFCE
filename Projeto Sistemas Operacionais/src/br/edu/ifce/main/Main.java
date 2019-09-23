/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.ifce.main;

import br.edu.ifce.view.TelaPrincipal;

/**
 *
 * Neste projeto, esta classe é completamente desnecessária, pois na classe TelaPrincipal já possui o método main.
 * De qualquer modo, ela foi criada e configurada para ser a classe principal desta aplicação apenas para organização do código,
 * já que não é muito intuitivo que uma outra pessoa saiba que na TelaPrincipal há o método main.
 * 
 * Se quiser utilizar o método main da Tela principal, basta apagar este arquivo e o pacote 'br.edu.ifce.main'.
 * 
 */
public class Main {
    
    public static void main(String[] args){        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TelaPrincipal().setVisible(true);
            }
        });
    }
}
