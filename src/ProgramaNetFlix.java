import java.util.List;

public class ProgramaNetFlix {
    // Todos os 15 atributos definidos como privados (Conforme especificação do PDF)
    private String id;
    private String titulo;
    private String tipoShow; 
    private String descricao;
    private int anoLancamento;
    private String classificacaoIdade;
    private int duracao;
    private List<String> generos; 
    private List<String> paisesProducao; 
    private double temporadas;
    private String imdbId;
    private double notaImdb;
    private int votosImdb;
    private double popularidadeTmdb;
    private double notaTmdb;

    // Construtor completo com a assinatura exata de 15 parâmetros
    public ProgramaNetFlix(String id, String titulo, String tipoShow, String descricao, int anoLancamento, 
                           String classificacaoIdade, int duracao, List<String> generos, List<String> paisesProducao, 
                           double temporadas, String imdbId, double notaImdb, int votosImdb, 
                           double popularidadeTmdb, double notaTmdb) {
        this.id = id;
        this.titulo = titulo;
        this.tipoShow = tipoShow;
        this.descricao = descricao;
        this.anoLancamento = anoLancamento;
        this.classificacaoIdade = classificacaoIdade;
        this.duracao = duracao;
        this.generos = generos;
        this.paisesProducao = paisesProducao;
        this.temporadas = temporadas;
        this.imdbId = imdbId;
        this.notaImdb = notaImdb;
        this.votosImdb = votosImdb;
        this.popularidadeTmdb = popularidadeTmdb;
        this.notaTmdb = notaTmdb;
    }

    // Métodos Getters públicos exigidos para análise de dados
    public String getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getTipoShow() { return tipoShow; }
    public String getDescricao() { return descricao; }
    public int getAnoLancamento() { return anoLancamento; }
    public String getClassificacaoIdade() { return classificacaoIdade; }
    public int getDuracao() { return duracao; }
    public List<String> getGeneros() { return generos; }
    public List<String> getPaisesProducao() { return paisesProducao; }
    public double getTemporadas() { return temporadas; }
    public String getImdbId() { return imdbId; }
    public double getNotaImdb() { return notaImdb; }
    public int getVotosImdb() { return votosImdb; }
    public double getPopularidadeTmdb() { return popularidadeTmdb; }
    public double getNotaTmdb() { return notaTmdb; }
}