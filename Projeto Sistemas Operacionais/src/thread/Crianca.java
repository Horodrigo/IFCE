/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thread;

import view.TelaPrincipal;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Esta thread foi criada através de uma adaptação do problema produtor/consumidor,
 * em que a Criança pode ser um produtor ou consumidor, dependendo da situação.
 * 
 */
public class Crianca extends Thread {
    /* Atributos */
    private TelaPrincipal telaPrincipal;
    private final long idCrianca;
    private final String nome;
    private final long tempoDeBrincar;
    private final long tempoQuieta;
    
    private final boolean iniciouComBola; //Criada apenas para mostrar na tabela de Crianças sem que seu valor seja alterado;
    private boolean tenho_bola;           //Muda seu valor constantemente, quando a Criança pega ou devolve uma bola do cesto;
    
    /* Recebe o valor atualizado dos semáforos, no dado momento quando a Criança é instanciada.
     * Obs.: os semáforos são inicializados no construtor da classe TelaPrincipal.
     */
    private Semaphore cesto_full;
    private Semaphore cesto_empty;
    private Semaphore mutex;
    
    //==========================================================================
    
    /* CONSTRUTOR */
    public Crianca(
            TelaPrincipal tela,
            long id,
            String nome,
            boolean tenho_bola,
            long tb,
            long tq,
            Semaphore cesto_full,
            Semaphore cesto_empty,
            Semaphore mutex
    ) {
        this.telaPrincipal  = tela;
        this.idCrianca      = id;
        this.nome           = nome;
        this.tenho_bola     = tenho_bola;
        this.iniciouComBola = tenho_bola;
        this.tempoDeBrincar = tb;
        this.tempoQuieta    = tq;
        
        this.cesto_full     = cesto_full;
        this.cesto_empty    = cesto_empty;
        this.mutex          = mutex;
      } //Fim Construtor de crianças;
    
    @Override
    public void run() {
        while(true) { //Loop infinito para que o método run nunca termine sua execução.
            if(this.tenho_bola){
                try { //O uso do método acquire da classe Semaphore exige um try/catch
                    
                    this.brincar(this.tempoDeBrincar); //Se a criança possui bola, então vai brincar por um determinado tempo
                    
                    this.telaPrincipal.mostrarCriancaAguardandoVagaNoCesto(this.idCrianca);
                    this.telaPrincipal.logMessage(this.nome + " aguardando para colocar bola no cesto"); //Função da classe TelaPrincipal utilizada para mostrar o texto passado como parâmetro no JTextArea da tela;
                    
                    /*
                     * semaphore.acquire() = DOWN(semaphore);
                     * Se possuir vaga no cesto para devolver uma bola, a criança consegue devolver a bola e a thread continua sua execução;
                     * Caso contrário, ela ficará bloqueada neste ponto.
                     * Como na linha 70 chamamos o método 'mostrarCriancaAguardandoVagaNoCesto()', presente na
                     * classe TelaPrincipal, então a imagem que aparecerá na tela será a da criança segurando a bola parada.
                     */
                    cesto_empty.acquire();
                    
                    /*
                     * É preciso garantir que o método de devolver uma bola para o cesto seja executado por uma única thread por vez,
                     * então por isso ele está encapsulado pelo semáforo mutex.
                     */
                    mutex.acquire();
                    this.devolverBolaParaCesto();
                    
                    /*
                     * semaphore.release() = UP(semaphores);
                     * Libera as threads travadas pelos semáforos mutex e cesto_full
                     */
                    //Após brincar e devolver a bola para o cesto, a criança ficará quieta por um determinado tempo.
                                        
                    cesto_full.release();
                    mutex.release();
                    
                    this.quieta(this.tempoQuieta);
                    
                } catch (InterruptedException ex) {
                    /*
                     * 
                     * 
                     * 
                     */
                    Logger.getLogger(Crianca.class.getName()).log(Level.SEVERE, null, ex);
                } 
            //Fim if(tenhoBola);
            } else { //se a criança não tem bola
                try { //try-catch exigido pelo método acquire()
                    
                    this.telaPrincipal.mostrarCriancaAguardandoBola(this.idCrianca, this.nome);
                    this.telaPrincipal.logMessage(this.nome + " aguardando ter bola no cesto");
                    
                    /* Se a criança não possuir bola e o cesto estiver vazio, ela ficará travada neste ponto, por conta do método acquire,
                     * pois cesto_full estará com o valor zero; ela só será destravada quando alguma thread der um release no cesto_full.
                     *
                     * Como na linha 112 chamamos o método 'mostrarCriancaAguardandoBola()', presente na
                     * classe TelaPrincipal, então a imagem que aparecerá na tela será a da criança parada e sem bola.
                     */
                    cesto_full.acquire();
                    
                    /*
                     * É preciso garantir que o método de pegar uma bola para o cesto seja executado por uma única thread por vez,
                     * então por isso ele está encapsulado pelo semáforo mutex.
                     */
                    mutex.acquire();
                    this.pegarBolaDoCesto();
                    
                } catch (InterruptedException ex) {
                    Logger.getLogger(Crianca.class.getName()).log(Level.SEVERE, null, ex);
                } finally { //Este bloco finally pode ser eliminado e seu conteúdo ficar dentro do try, logo após o método pegarBolaDoCesto() na linha 128;
                    
                    //Libera todas as threads travadas pelos semáforos cesto_empty e mutex;
                    cesto_empty.release();
                    mutex.release();
                }
            } //Fim else;
        } //Fim while(true);
    } //Fim run();
    
    //==========================================================================
    
    /**
     * Métodos da Thread Criança
     */
    
    /*
     * Recebe um tempo e converte-o para milissegundos.
     * É utilizadas nas funções brincar() e quieta().
     * Função desnecessária, pois a conversão do tempo pode ficar dentro desses dois métodos onde ela é utilizada.
     */
    private long converteParaMillis(long unidadeDeTempo){
        return unidadeDeTempo * 1000;
    } //Fim converteParaMillis();
    
    /*
     * A criança deve ficar bricando por um determinado tempo e, durante esse tempo, a criança faz algum movimento.
     * Esse movimento é feito com duas imagens que são trocadas constantemente.
     */
    public void brincar(long unidadeDeTempo) {
        long start, finish, tempo;
        
        this.telaPrincipal.logMessage(this.nome + " está brincando");
        tempo = this.converteParaMillis(unidadeDeTempo); //converte o tempo recebido em segundos para milissegundos;
        
        start = System.currentTimeMillis(); //Recebe a hora atual do sistema para referência como o início da contagem de tempo;
        
        do {
            this.telaPrincipal.mostrarCriancaBrincando(this.idCrianca, this.nome);
            finish = System.currentTimeMillis();
            this.telaPrincipal.mostrarCriancaBrincando(this.idCrianca, this.nome);
        } while((finish-start) <= tempo); //Compara a hora atual com a hora de referência inicial para saber se o tempo determinado pelo usuário foi atingido;
        
        this.telaPrincipal.logMessage(this.nome + " terminou de brincar");
    } //Fim brincar();
    
    
    /*
     * Semelhante à função brincar()
     */
    private void quieta(long unidadeDeTempo){
        long start, finish, tempo;
        
        this.telaPrincipal.logMessage("A criança " + this.nome + " está quieta");
        tempo = this.converteParaMillis(unidadeDeTempo);
        
        start = System.currentTimeMillis();
        do {
            this.telaPrincipal.mostrarCriancaQuieta(this.idCrianca, this.nome);
            finish = System.currentTimeMillis();
            this.telaPrincipal.mostrarCriancaQuieta(this.idCrianca, this.nome);
        } while((finish-start) <= tempo);
        
        this.telaPrincipal.logMessage("A criança " + this.nome + " cansou de ficar quieta");
    } //Fim quieta();
    
    
    /*
     * Atualiza o atributo tenho_bola para true.
     */
    private void pegarBolaDoCesto(){
        this.tenho_bola = true;
        this.telaPrincipal.removerBolaDoCesto();
        this.telaPrincipal.logMessage(this.nome + " pegou uma bola do cesto");
    } //Fim pegarBolaDoCesto();
    
    
    /*
     * Atualiza o atributo tenho_bola para false;
     */
    private void devolverBolaParaCesto(){
        this.tenho_bola = false;
        this.telaPrincipal.adicionarBolaNoCesto();
        this.telaPrincipal.logMessage(this.nome + " devolveu sua bola para o cesto");
    } //Fim devolverBolaParaCesto();
    
    
    /* Getters utilizados na classe TeabelaLog */
    public long getIdCrianca() {
        return idCrianca;
    }

    public String getNome() {
        return nome;
    }

    public long getTempoDeBrincar() {
        return tempoDeBrincar;
    }

    public long getTempoQuieta() {
        return tempoQuieta;
    }

    public String getIniciouComBola(){
        if(this.iniciouComBola) return "SIM";
        else return "NÃO";
    }
    
    public boolean getTenho_bola() {
        return tenho_bola;
    }
    
} //Fim classe Crianca;
