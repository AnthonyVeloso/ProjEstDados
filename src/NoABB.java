public class NoABB {
    ProgramaNetFlix dados;
    NoABB esquerda, direita;

    public NoABB(ProgramaNetFlix dados) {
        this.dados = dados;
        this.esquerda = null;
        this.direita = null;
    }
}