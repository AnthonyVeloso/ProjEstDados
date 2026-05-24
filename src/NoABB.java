//Integrantes do grupo:
// Anthony Veloso 10737481
// André Cintra 10738062
// Luca Saboia 10736834

public class NoABB {
    //objeto com todos os atributos
    ProgramaNetFlix dados;  
    
    NoABB esquerda, direita;
    
    //construtor inicializa cada nó como uma folha
    public NoABB(ProgramaNetFlix dados) { 
        this.dados = dados;
        this.esquerda = null;
        this.direita = null;
    }
}