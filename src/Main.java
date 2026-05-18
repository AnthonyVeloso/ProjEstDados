import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Main {

    private static Set<String> todosOsPaises = new TreeSet<>();
    private static Set<String> todosOsGeneros = new TreeSet<>();
    private static Set<String> generosDeFilmes = new TreeSet<>();
    // Armazena o caminho global do arquivo para uso na leitura e escrita
    private static final String CAMINHO_ARQUIVO = "dados/netflix_titles.csv"; 

    public static void main(String[] args) {
        ABB arvore = new ABB();
        Scanner leitor = new Scanner(System.in);
        System.out.println(CAMINHO_ARQUIVO);
        System.out.println("Iniciando o carregamento completo dos dados...");
        carregarDadosCSV(arvore, CAMINHO_ARQUIVO);

        int opcao = 0;
        do {
            exibirMenu();
            try {
                opcao = leitor.nextInt();
                leitor.nextLine(); // Limpa buffer
            } catch (InputMismatchException e) {
                System.out.println("Erro: Por favor, digite um número inteiro válido.");
                leitor.nextLine(); 
                continue;
            }

            switch (opcao) {
                case 1: tratarObjetivo1(arvore, leitor); break;
                case 2: tratarObjetivo2(arvore, leitor); break;
                case 3: tratarObjetivo3(arvore, leitor); break; 
                case 4: tratarObjetivo4(arvore, leitor); break;
                case 5: tratarObjetivo5(arvore, leitor); break;
                case 6: 
                    System.out.print("Digite o ID do título para buscar: ");
                    String idBusca = leitor.nextLine().trim();
                    int[] comp = new int[1];
                    long tempoInicio = System.nanoTime();
                    ProgramaNetFlix enc = arvore.buscar(idBusca, comp);
                    long tempoFim = System.nanoTime();
                    if (enc != null) {
                        System.out.println("Título Encontrado: " + enc.getTitulo() + " | Tipo: " + enc.getTipoShow());
                    } else {
                        System.out.println("Título não localizado na ABB.");
                    }
                    System.out.println("Comparações realizadas na ABB: " + comp[0]);
                    System.out.println("Tempo de execução: " + (tempoFim - tempoInicio) + " nanosegundos.");
                    break;
                case 7:
                    System.out.println("A altura atual da Árvore Binária de Busca é: " + arvore.calcularAltura());
                    break;
                case 8:
                    tratarInsercaoNovoPrograma(arvore, leitor);
                    break;
                case 9:
                    tratarRemocaoPrograma(arvore, leitor);
                    break;
                case 10: 
                    System.out.println("Encerrando a aplicação. Até breve!"); 
                    break;
                default: 
                    System.out.println("Opção inválida!"); 
                    break;
            }
        } while (opcao != 10);

        leitor.close();
    }

    private static void exibirMenu() {
        System.out.println("\n===== MENU PRINCIPAL MACKENZIE =====");
        System.out.println("1 - Filme mais curtido por gênero (Escolher 5 gêneros)");
        System.out.println("2 - Quantidade de filmes por país dentro de um período (1950-2022)");
        System.out.println("3 - Filme mais famoso de um país específico");
        System.out.println("4 - Título com maior número de votos em um período (1950-2022)");
        System.out.println("5 - Ano com mais lançamentos para N países (1 a 3 países de forma separada)");
        System.out.println("6 - Buscar programa individualizado por ID");
        System.out.println("7 - Exibir a Altura da Árvore");
        System.out.println("8 - Inserir Novo Programa (Geração de ID Automática)");
        System.out.println("9 - Remover Programa por ID");
        System.out.println("10 - Sair do programa");
        System.out.print("Escolha uma opção: ");
    }

    // FUNÇÃO SOLICITADA: Salva o estado atual da árvore de volta no arquivo CSV
    private static void salvarDadosCSV(ABB arvore, String caminhoArquivo) {
        List<ProgramaNetFlix> todosOsProgramas = arvore.exportarParaLista();
        
        // Cabeçalho oficial do dataset padrão da Mackenzie
        String cabecalho = "id,title,type,description,release_year,age_certification,runtime,genres,production_countries,seasons,imdb_id,imdb_score,imdb_votes,tmdb_popularity,tmdb_score";

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(caminhoArquivo))) {
            bw.write(cabecalho);
            bw.newLine();

            for (ProgramaNetFlix p : todosOsProgramas) {
                // Formata de volta as listas para o padrão CSV de colchetes: ['Genre1', 'Genre2']
                String generosFormatados = "\"" + p.getGeneros().toString().replace(", ", "','").replace("[", "['").replace("]", "']") + "\"";
                String paisesFormatados = "\"" + p.getPaisesProducao().toString().replace(", ", "','").replace("[", "['").replace("]", "']") + "\"";
                
                // Monta a linha com delimitadores por vírgula
                String linha = String.format("%s,%s,%s,%s,%d,%s,%d,%s,%s,%.1f,%s,%.1f,%d,%.1f,%.1f",
                    p.getId(),
                    p.getTitulo().contains(",") ? "\"" + p.getTitulo() + "\"" : p.getTitulo(),
                    p.getTipoShow(),
                    p.getDescricao().contains(",") ? "\"" + p.getDescricao() + "\"" : p.getDescricao(),
                    p.getAnoLancamento(),
                    p.getClassificacaoIdade(),
                    p.getDuracao(),
                    generosFormatados,
                    paisesFormatados,
                    p.getTemporadas(),
                    p.getImdbId(),
                    p.getNotaImdb(),
                    p.getVotosImdb(),
                    p.getPopularidadeTmdb(),
                    p.getNotaTmdb()
                );
                
                bw.write(linha);
                bw.newLine();
            }
            System.out.println("[OK] Arquivo CSV atualizado e sincronizado em disco rígido com sucesso!");
        } catch (IOException e) {
            System.out.println("[ERRO] Falha crítica ao persistir dados no CSV: " + e.getMessage());
        }
    }

    private static void carregarDadosCSV(ABB arvore, String caminhoArquivo) {
    String linha = ""; 
    int inseridos = 0; 
    int ignoradosPorErroConversao = 0;
    List<ProgramaNetFlix> listaTemporaria = new ArrayList<>();

    try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
        br.readLine(); // Pula o cabeçalho
        
        int idxId = 0, idxTitulo = 1, idxTipo = 2, idxDescricao = 3, idxAno = 4, idxGeneros = 7, idxPaises = 8;

        while ((linha = br.readLine()) != null) {
            String linhaTratada = linha.replace("\"\"", "\"");
            String[] colunas = linhaTratada.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            
            if (colunas.length < 9) {
                ignoradosPorErroConversao++;
                continue;
            }
            
            try {
                String id = colunas[idxId].trim(); 
                String titulo = colunas[idxTitulo].trim().replace("\"", ""); 
                String tipoShow = colunas[idxTipo].trim(); 
                String descricao = colunas[idxDescricao].trim().replace("\"", "");
                
                String anoTexto = colunas[idxAno].replaceAll("[^0-9]", "").trim();
                int ano = Integer.parseInt(anoTexto);
                
                List<String> g = processarListaCSV(colunas[idxGeneros]); 
                List<String> p = processarListaCSV(colunas[idxPaises]); 
                
                double score = 7.0; 
                int votos = 500;
                
                if (colunas.length >= 15) {
                    try {
                        score = Double.parseDouble(colunas[11].trim());
                        votos = Integer.parseInt(colunas[12].trim().replaceAll("[^0-9]", ""));
                    } catch (Exception e) {}
                }

                if (!g.isEmpty()) todosOsGeneros.addAll(g);
                if (!p.isEmpty()) todosOsPaises.addAll(p);
                if ("Movie".equalsIgnoreCase(tipoShow)) generosDeFilmes.addAll(g);

                // Armazena na lista antes de pôr na árvore
                ProgramaNetFlix pg = new ProgramaNetFlix(id, titulo, tipoShow, descricao, ano, "R", 90, g, p, 0.0, "tt000", score, votos, 10.0, score);
                listaTemporaria.add(pg);
                
            } catch (Exception e) { 
                ignoradosPorErroConversao++;
            }
        }
        
        // O TRUQUE: Embaralha os dados para que a árvore monte de forma ramificada e saudável
        Collections.shuffle(listaTemporaria);
        
        // Agora insere na árvore de fato
        for (ProgramaNetFlix pg : listaTemporaria) {
            arvore.inserir(pg);
            inseridos++;
        }
        
        System.out.println("\n===== RELATÓRIO DEFINITIVO MACKENZIE =====");
        System.out.println("-> Registros carregados do arquivo: " + (inseridos + ignoradosPorErroConversao));
        System.out.println("-> Registros inseridos com SUCESSO na Árvore: " + inseridos);
        System.out.println("==========================================\n");

    } catch (IOException e) { 
        System.out.println("Erro crítico ao abrir o arquivo: " + e.getMessage()); 
    }
}

    private static List<String> processarListaCSV(String texto) {
        String limpo = texto.replace("[", "").replace("]", "").replace("'", "").replace("\"", "").trim();
        if (limpo.isEmpty()) return new ArrayList<>();
        String[] itens = limpo.split(",");
        List<String> res = new ArrayList<>();
        for (String i : itens) { if (!i.trim().isEmpty()) res.add(i.trim()); }
        return res;
    }

    private static void tratarInsercaoNovoPrograma(ABB arvore, Scanner leitor) {
        System.out.println("\n--- Inserir Novo Programa na BST ---");
        String tipoShow = "";
        while (true) {
            System.out.print("O programa é um MOVIE ou um SHOW? (Digite M ou S): ");
            String entradaTipo = leitor.nextLine().trim().toUpperCase();
            if (entradaTipo.equals("M")) { tipoShow = "Movie"; break; }
            else if (entradaTipo.equals("S")) { tipoShow = "Show"; break; }
            System.out.println("Opção inválida!");
        }
        System.out.print("Digite o Título: "); String titulo = leitor.nextLine().trim();
        System.out.print("Digite a Descrição: "); String descricao = leitor.nextLine().trim();
        System.out.print("Digite o Ano de Lançamento: "); int ano = leitor.nextInt(); leitor.nextLine();
        System.out.print("Digite os Gêneros (Ex: Drama, Action): "); String gT = leitor.nextLine().trim();
        List<String> listaGeneros = Arrays.asList(gT.split("\\s*,\\s*"));
        System.out.print("Digite os Países (Ex: Brazil): "); String pT = leitor.nextLine().trim();
        List<String> listaPaises = Arrays.asList(pT.split("\\s*,\\s*"));
        System.out.print("Digite a Nota IMDB: "); double nota = leitor.nextDouble();
        System.out.print("Digite os Votos IMDB: "); int votos = leitor.nextInt(); leitor.nextLine();

        int proximoNumeroUnico = arvore.obterMaiorIdNumerico() + 1;
        String prefixo = tipoShow.equals("Movie") ? "tm" : "ts";
        String idGerado = prefixo + proximoNumeroUnico;

        ProgramaNetFlix novo = new ProgramaNetFlix(idGerado, titulo, tipoShow, descricao, ano, "R", 120, listaGeneros, listaPaises, 0.0, "tt000", nota, votos, 10.0, nota);
        arvore.inserir(novo);
        todosOsGeneros.addAll(listaGeneros); todosOsPaises.addAll(listaPaises);
        if (tipoShow.equals("Movie")) generosDeFilmes.addAll(listaGeneros);

        System.out.println("\n[SUCESSO] Cadastrado com ID: " + idGerado);
        
        // DISCO: Salva automaticamente no CSV após inserir
        salvarDadosCSV(arvore, CAMINHO_ARQUIVO);
    }

    private static void tratarRemocaoPrograma(ABB arvore, Scanner leitor) {
        System.out.println("\n--- Remover Programa da BST ---");
        System.out.print("Digite o ID do programa que deseja excluir: ");
        String idParaRemover = leitor.nextLine().trim();

        int[] comp = new int[1];
        ProgramaNetFlix alvo = arvore.buscar(idParaRemover, comp);

        if (alvo != null) {
            System.out.println("[AVISO] Registro localizado: \"" + alvo.getTitulo() + "\"");
            System.out.print("Tem certeza que deseja deletar permanentemente? (S/N): ");
            String confirmacao = leitor.nextLine().trim().toUpperCase();

            if (confirmacao.equals("S")) {
                arvore.remover(idParaRemover);
                System.out.println("[SUCESSO] O nó foi removido da BST!");
                
                // DISCO: Salva automaticamente no CSV após remover
                salvarDadosCSV(arvore, CAMINHO_ARQUIVO);
            } else {
                System.out.println("[CANCELADO] Operação abortada.");
            }
        } else {
            System.out.println("[ERRO] O ID \"" + idParaRemover + "\" não existe.");
        }
    }

    private static void exibirOpcoesDisponiveis(boolean exibirPaises, Set<String> conjuntoGeneros) {
        System.out.println("\n---------------------------------------------------------");
        if (exibirPaises) { System.out.println("PAÍSES ENCONTRADOS:"); System.out.println(todosOsPaises); }
        if (conjuntoGeneros != null) {
            if (exibirPaises) System.out.println();
            System.out.println("=== LISTA DE GÊNEROS DISPONÍVEIS ===");
            int contador = 1;
            for (String genero : conjuntoGeneros) { System.out.printf("[%02d] %s\n", contador, genero); contador++; }
        }
        System.out.println("---------------------------------------------------------");
    }

    private static void tratarObjetivo1(ABB arvore, Scanner leitor) {
        exibirOpcoesDisponiveis(false, generosDeFilmes);
        System.out.println("\n--- Objetivo 1: Filme mais curtido por gênero ---");
        List<String> escolhas = new ArrayList<>();
        System.out.println("Digite exatamente 5 gêneros:");
        for (int i = 1; i <= 5; i++) { System.out.print("Gênero " + i + ": "); escolhas.add(leitor.nextLine().trim()); }
        Map<String, ProgramaNetFlix> resultado = arvore.obterFilmesMaisCurtidosPorGeneros(escolhas);
        System.out.println("\nResultado:");
        resultado.forEach((g, p) -> System.out.println("[" + g + "] " + (p != null ? p.getTitulo() + " (Nota IMDB: " + p.getNotaImdb() + ")" : "Nenhum filme encontrado")));
    }

    private static void tratarTratarObjetivo2(ABB arvore, Scanner leitor) { /* mantido */ }
    private static void tratarObjetivo2(ABB arvore, Scanner leitor) {
        exibirOpcoesDisponiveis(true, null);
        System.out.print("Ano inicial: "); int ini = leitor.nextInt();
        System.out.print("Ano final: "); int fim = leitor.nextInt(); leitor.nextLine();
        Map<String, Integer> contagem = arvore.contarFilmesPorPaisEmPeriodo(ini, fim);
        contagem.forEach((p, q) -> System.out.println("País: " + p + " -> Qtd: " + q));
    }
    private static void tratarObjetivo3(ABB arvore, Scanner leitor) {
        exibirOpcoesDisponiveis(true, null);
        System.out.print("Nome do País: "); String pais = leitor.nextLine().trim();
        ProgramaNetFlix p = arvore.obterFilmeMaisFamosoPorPais(pais); 
        if (p != null) System.out.println("Mais famoso de [" + pais + "]: " + p.getTitulo());
        else System.out.println("Nenhum localizado.");
    }
    private static void tratarObjetivo4(ABB arvore, Scanner leitor) {
        exibirOpcoesDisponiveis(true, todosOsGeneros);
        System.out.print("Ano inicial: "); int ini = leitor.nextInt();
        System.out.print("Ano final: "); int fim = leitor.nextInt(); leitor.nextLine();
        ProgramaNetFlix p = arvore.obterTituloMaisVotadoEmPeriodo(ini, fim);
        if (p != null) System.out.println("Mais votado: " + p.getTitulo());
        else System.out.println("Nenhum encontrado.");
    }
    private static void tratarObjetivo5(ABB arvore, Scanner leitor) {
        exibirOpcoesDisponiveis(true, null);
        int n = 0;
        while (true) {
            System.out.print("Quantos países? (1 a 3): ");
            try { n = leitor.nextInt(); leitor.nextLine(); if (n >= 1 && n <= 3) break; } catch (Exception e) { leitor.nextLine(); }
        }
        List<String> paises = new ArrayList<>();
        for (int i = 1; i <= n; i++) { System.out.print("País " + i + ": "); paises.add(leitor.nextLine().trim()); }
        for (String pais : paises) {
            int ano = arvore.obterAnoComMaisLancamentosParaPais(pais);
            System.out.println("-> [" + pais + "]: Ano campeão: " + (ano != -1 ? ano : "Nenhum"));
        }
    }
}