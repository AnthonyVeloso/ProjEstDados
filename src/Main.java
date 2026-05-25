//Integrantes do grupo:
// Anthony Veloso 10737481
// André Cintra 10738062
// Luca Saboia 10736834

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
    
    // Variável global dinâmica que armazena qual arquivo está em uso no momento
    private static String caminhoArquivoAtual = "dados/netflix_titles.csv";
    
    // Sinalizador para liberar as opções do menu apenas após o carregamento de uma base
    private static boolean arquivoCarregado = false;

    public static void main(String[] args) {
        ABB arvore = new ABB();
        Scanner leitor = new Scanner(System.in);
        
        System.out.println("=================================================");
        System.out.println("       SISTEMA DE GERENCIAMENTO MACKENZIE        ");
        System.out.println("=================================================");
        System.out.println("Aviso: A base de dados está vazia.");
        System.out.println("Por favor, utilize a Opção 0 para carregar um arquivo CSV.");

        int opcao = -1;
        do {
            exibirMenu();
            try {
                opcao = leitor.nextInt();
                leitor.nextLine(); // Limpa o buffer do teclado
            } catch (InputMismatchException e) {
                System.out.println("\n[ERRO] Por favor, digite um número inteiro válido.");
                leitor.nextLine(); 
                continue;
            }

            // TRAVA DE SEGURANÇA: Impede o uso de operações de consulta/remoção sem dados na memória
            if (!arquivoCarregado && ((opcao >= 1 && opcao <= 7) || opcao == 9)) {
                System.out.println("\n[BLOQUEADO] Operação negada! Carregue uma base de dados primeiro usando a Opção 0.");
                continue;
            }

            switch (opcao) {
                case 0:
                    System.out.print("Digite o nome ou caminho do arquivo CSV (Ex: dados/netflix_titles.csv ou dados/teste.csv): ");
                    String nomeArquivo = leitor.nextLine().trim();
                    
                    // Sincroniza a variável global com a escolha do usuário
                    caminhoArquivoAtual = nomeArquivo;
                    
                    // Reinicializa a árvore e limpa as listas estáticas para evitar acúmulo de lixo de memória
                    arvore = new ABB(); 
                    todosOsPaises.clear();
                    todosOsGeneros.clear();
                    generosDeFilmes.clear();
                    carregarDadosCSV(arvore, caminhoArquivoAtual);
                    break;
                    
                case 1: 
                    tratarObjetivo1(arvore, leitor); 
                    break;
                    
                case 2: 
                    tratarObjetivo2(arvore, leitor); 
                    break;
                    
                case 3: 
                    tratarObjetivo3(arvore, leitor); 
                    break; 
                    
                case 4: 
                    tratarObjetivo4(arvore, leitor); 
                    break;
                    
                case 5: 
                    tratarObjetivo5(arvore, leitor); 
                    break;
                    
                case 6: 
                    System.out.print("Digite o ID do título para buscar (Ex: s1, s10): ");
                    String idBusca = leitor.nextLine().trim();
                    int[] comp = new int[1];
                    long tempoInicio = System.nanoTime();
                    ProgramaNetFlix enc = arvore.buscar(idBusca, comp);
                    long tempoFim = System.nanoTime();
                    
                    if (enc != null) {
                        System.out.println("\n[LOCALIZADO] Título: " + enc.getTitulo() + " | Tipo: " + enc.getTipoShow() + " | Ano: " + enc.getAnoLancamento());
                        System.out.println("Descrição: " + enc.getDescricao());
                    } else {
                        System.out.println("\n[AVISO] Título não localizado na estrutura da árvore BST.");
                    }
                    System.out.println("-> Comparações realizadas na árvore (Nós visitados): " + comp[0]);
                    System.out.println("-> Tempo gasto na busca: " + (tempoFim - tempoInicio) + " nanosegundos.");
                    break;
                    
                case 7:
                    System.out.println("\nA altura atual da Árvore Binária de Busca (BST) é: " + arvore.calcularAltura());
                    break;
                    
                case 8:
                    tratarInsercaoNovoPrograma(arvore, leitor);
                    break;
                    
                case 9:
                    tratarRemocaoPrograma(arvore, leitor);
                    break;
                    
                case 10: 
                    System.out.println("\nSaindo do sistema Mackenzie e fechando buffers. Até breve!"); 
                    break;
                    
                default: 
                    System.out.println("\n[AVISO] Opção inválida! Selecione um número de 0 a 10."); 
                    break;
            }
        } while (opcao != 10);

        leitor.close();
    }

    private static void exibirMenu() {
        System.out.println("\n===== MENU PRINCIPAL MACKENZIE =====");
        System.out.println("0 - Carregar / Trocar Arquivo CSV de Dados");
        System.out.println("1 - Filme mais curtido por gênero (Escolher 5 gêneros)");
        System.out.println("2 - Quantidade de filmes por país dentro de um período (1950-2022)");
        System.out.println("3 - Filme mais famoso de um país específico");
        System.out.println("4 - Título com maior número de votos em um período (1950-2022)");
        System.out.println("5 - Ano com mais lançamentos para N países (1 a 3 países de forma separada)");
        System.out.println("6 - Buscar programa individualizado por ID");
        System.out.println("7 - Exibir a Altura da Árvore");
        System.out.println("8 - Inserir Novo Programa (Gravação Automática)");
        System.out.println("9 - Remover Programa por ID (Exclusão Automática)");
        System.out.println("10 - Sair do programa");
        System.out.print("Escolha uma opção: ");
    }

    private static void carregarDadosCSV(ABB arvore, String caminhoArquivo) {
        String linha = ""; 
        int inseridos = 0; 
        int ignoradosPorErroConversao = 0;
        List<ProgramaNetFlix> listaTemporaria = new ArrayList<>();

        System.out.println("\nTentando estabelecer conexão com o arquivo: " + caminhoArquivo + " ...");

        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
            br.readLine(); // Salta a linha de cabeçalho
            
            // Mapeamento estático e preciso das colunas baseadas na amostra real do arquivo de 15 colunas:
            int idxId = 0;
            int idxTitulo = 1;
            int idxTipo = 2;
            int idxDescricao = 3;
            int idxAno = 4;
            int idxGeneros = 7;
            int idxPaises = 8;

            while ((linha = br.readLine()) != null) {
                // Remove aspas duplicadas que quebram o interpretador Regex de splits
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

                    // Adiciona na lista intermediária temporária
                    ProgramaNetFlix pg = new ProgramaNetFlix(id, titulo, tipoShow, descricao, ano, "R", 90, g, p, 0.0, "tt000", score, votos, 10.0, score);
                    listaTemporaria.add(pg);
                    
                } catch (Exception e) { 
                    ignoradosPorErroConversao++;
                }
            }
            
            // EMBARALHAMENTO DOS DADOS: Evita o efeito de árvore degenerada ("fio de telefone")
            Collections.shuffle(listaTemporaria);
            
            // Descarrega os dados ramificados dentro da BST
            for (ProgramaNetFlix pg : listaTemporaria) {
                arvore.inserir(pg);
                inseridos++;
            }
            
            arquivoCarregado = true; // Libera o acesso às demais funções do menu

            System.out.println("\n===== RELATÓRIO DE CARREGAMENTO MANUAL =====");
            System.out.println("-> Arquivo mapeado em disco: " + caminhoArquivo);
            System.out.println("-> Registros inseridos com SUCESSO na Árvore: " + inseridos);
            System.out.println("-> Linhas corrompidas ou ignoradas: " + ignoradosPorErroConversao);
            System.out.println("-> Altura atual calculada para a BST: " + arvore.calcularAltura());
            System.out.println("============================================\n");

        } catch (IOException e) { 
            System.out.println("\n[ERRO CRÍTICO] Falha ao tentar localizar ou ler o arquivo: \"" + caminhoArquivo + "\"");
            System.out.println("Certifique-se de que digitou o nome correto, a extensão '.csv' e que ele está na pasta certa.");
            arquivoCarregado = false;
        }
    }

    private static void salvarDadosCSV(ABB arvore, String caminhoArquivo) {
        List<ProgramaNetFlix> todosOsProgramas = arvore.exportarParaLista();
        String cabecalho = "id,title,type,description,release_year,age_certification,runtime,genres,production_countries,seasons,imdb_id,imdb_score,imdb_votes,tmdb_popularity,tmdb_score";

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(caminhoArquivo))) {
            bw.write(cabecalho);
            bw.newLine();

            for (ProgramaNetFlix p : todosOsProgramas) {
                String generosFormatados = "\"" + p.getGeneros().toString().replace(", ", "','").replace("[", "['").replace("]", "']") + "\"";
                String paisesFormatados = "\"" + p.getPaisesProducao().toString().replace(", ", "','").replace("[", "['").replace("]", "']") + "\"";
                
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
            System.out.println("[OK] Sincronização em disco rígido realizada no arquivo: " + caminhoArquivo);
        } catch (IOException e) {
            System.out.println("[ERRO] Falha ao reescrever dados de persistência: " + e.getMessage());
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
            System.out.print("O programa é um MOVIE ou um SHOW? (M/S): ");
            String entradaTipo = leitor.nextLine().trim().toUpperCase();
            if (entradaTipo.equals("M")) { tipoShow = "Movie"; break; }
            else if (entradaTipo.equals("S")) { tipoShow = "Show"; break; }
            System.out.println("[!] Entrada inválida.");
        }
        System.out.print("Digite o Título: "); String titulo = leitor.nextLine().trim();
        System.out.print("Digite a Descrição: "); String descricao = leitor.nextLine().trim();
        System.out.print("Digite o Ano de Lançamento: "); int ano = leitor.nextInt(); leitor.nextLine();
        System.out.print("Digite os Gêneros (Separados por vírgula): "); String gT = leitor.nextLine().trim();
        List<String> listaGeneros = Arrays.asList(gT.split("\\s*,\\s*"));
        System.out.print("Digite os Países (Separados por vírgula): "); String pT = leitor.nextLine().trim();
        List<String> listaPaises = Arrays.asList(pT.split("\\s*,\\s*"));
        System.out.print("Digite a Nota IMDB: "); double nota = leitor.nextDouble();
        System.out.print("Digite os Votos IMDB: "); int votos = leitor.nextInt(); leitor.nextLine();

        int proximoNumeroUnico = arvore.obterMaiorIdNumerico() + 1;
        String prefixo = tipoShow.equals("Movie") ? "tm" : "ts";
        String idGerado = prefixo + proximoNumeroUnico;

        ProgramaNetFlix novo = new ProgramaNetFlix(idGerado, titulo, tipoShow, descricao, ano, "R", 120, listaGeneros, listaPaises, 0.0, "tt000", nota, votos, 10.0, nota);
        arvore.inserir(novo);
        
        todosOsGeneros.addAll(listaGeneros); 
        todosOsPaises.addAll(listaPaises);
        if (tipoShow.equals("Movie")) generosDeFilmes.addAll(listaGeneros);

        arquivoCarregado = true; // Se a árvore estava vazia e o usuário começou a preencher na mão, libera as opções
        System.out.println("\n[SUCESSO] Registro inserido em memória com o ID gerado: " + idGerado);
        
        // PERSISTÊNCIA DINÂMICA
        salvarDadosCSV(arvore, caminhoArquivoAtual);
    }

    private static void tratarRemocaoPrograma(ABB arvore, Scanner leitor) {
        System.out.println("\n--- Remover Programa da BST ---");
        System.out.print("Digite o ID do programa que deseja deletar da árvore: ");
        String idParaRemover = leitor.nextLine().trim();

        int[] comp = new int[1];
        ProgramaNetFlix alvo = arvore.buscar(idParaRemover, comp);

        if (alvo != null) {
            System.out.println("[AVISO] Registro localizado: \"" + alvo.getTitulo() + "\" (" + alvo.getTipoShow() + ")");
            System.out.print("Tem certeza que deseja deletar permanentemente? (S/N): ");
            String confirmacao = leitor.nextLine().trim().toUpperCase();

            if (confirmacao.equals("S")) {
                arvore.remover(idParaRemover);
                System.out.println("[SUCESSO] O nó foi removido das ramificações da BST.");
                
                // PERSISTÊNCIA DINÂMICA
                salvarDadosCSV(arvore, caminhoArquivoAtual);
            } else {
                System.out.println("[CANCELADO] Operação abortada. Nenhuma modificação foi salva.");
            }
        } else {
            System.out.println("[ERRO] Falha na remoção. O ID \"" + idParaRemover + "\" não foi localizado.");
        }
    }

    private static void exibirOpcoesDisponiveis(boolean exibirPaises, Set<String> conjuntoGeneros) {
        System.out.println("\n---------------------------------------------------------");
        if (exibirPaises) { System.out.println("PAÍSES NA BASE: " + todosOsPaises); }
        if (conjuntoGeneros != null) { System.out.println("GÊNEROS NA BASE: " + conjuntoGeneros); }
        System.out.println("---------------------------------------------------------");
    }

    private static void tratarObjetivo1(ABB arvore, Scanner leitor) {
        exibirOpcoesDisponiveis(false, generosDeFilmes);
        System.out.println("\n--- Objetivo 1: Filme mais curtido por gênero ---");
        List<String> escolhas = new ArrayList<>(); //arraylist pra guardar as opções do usuario
        System.out.println("Digite exatamente 5 gêneros das opções acima:");
        
        for (int i = 1; i <= 5; i++) {
            System.out.print("Gênero " + i + ": "); 
            escolhas.add(leitor.nextLine().trim()); //método trim() remove qualquer espaço das pontas da string pra não dar erro na hora de buscar na árore(com o equals)
        }
        
        //o resultado da busca é armazenado no dicionário 'resultados', onde String é a chave/gênero e ProgramaNetFlix é o valor/objeto completo
        Map<String, ProgramaNetFlix> resultado = arvore.obterFilmesMaisCurtidosPorGeneros(escolhas);
        System.out.println("\nResultado da consulta:");
        //para cada item no dicionario, se usa o ternário pra indicar o resutado ou a ausência dele 
        resultado.forEach((g, p) -> System.out.println("[" + g + "] " + (p != null ? p.getTitulo() + " (Nota IMDB: " + p.getNotaImdb() + ")" : "Nenhum filme encontrado")));
    }

    
    private static void tratarObjetivo2(ABB arvore, Scanner leitor) {
        exibirOpcoesDisponiveis(true, null);
        System.out.print("Ano inicial do intervalo: "); 
        int ini = leitor.nextInt();
        System.out.print("Ano final do intervalo: "); 
        int fim = leitor.nextInt(); 
        leitor.nextLine(); //lida com o bug de espaço no buffer
        //chama o método de contagem e armazena o resultado em um dicionario de chave=pais e valor=contagem
        Map<String, Integer> contagem = arvore.contarFilmesPorPaisEmPeriodo(ini, fim);
        System.out.println("\nResultado do volume de produções por país:");
        //varre o dicionário e printa os resultados respectivos a cada país 
        contagem.forEach((p, q) -> System.out.println("-> " + p + ": " + q + " filmes lançados."));
    }

    private static void tratarObjetivo3(ABB arvore, Scanner leitor) {
        exibirOpcoesDisponiveis(true, null);
        System.out.print("Digite o nome exato do País de origem: "); String pais = leitor.nextLine().trim();
        ProgramaNetFlix p = arvore.obterFilmeMaisFamosoPorPais(pais); 
        if (p != null) System.out.println("\n[CAMPEÃO DE VOTOS] Filme mais famoso de " + pais + ": " + p.getTitulo() + " (Votos IMDB: " + p.getVotosImdb() + ")");
        else System.out.println("\nNenhum registro encontrado para este país.");
    }

    private static void tratarObjetivo4(ABB arvore, Scanner leitor) {
        System.out.print("Ano inicial do intervalo: "); int ini = leitor.nextInt();
        System.out.print("Ano final do intervalo: "); int fim = leitor.nextInt(); leitor.nextLine();
        ProgramaNetFlix p = arvore.obterTituloMaisVotadoEmPeriodo(ini, fim);
        if (p != null) System.out.println("\nTítulo com maior engajamento no período: " + p.getTitulo() + " (" + p.getTipoShow() + ") com " + p.getVotosImdb() + " votos.");
        else System.out.println("\nNenhum registro localizado no intervalo de anos informado.");
    }

    private static void tratarObjetivo5(ABB arvore, Scanner leitor) {
        exibirOpcoesDisponiveis(true, null);
        int n = 0;
        while (true) {
            System.out.print("Quantos países deseja analisar de forma separada? (1 a 3): ");
            try {
                n = leitor.nextInt(); leitor.nextLine();
                if (n >= 1 && n <= 3) break;
                System.out.println("[!] Limite estrito do projeto: selecione de 1 a 3.");
            } catch (Exception e) { leitor.nextLine(); }
        }
        List<String> paises = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            System.out.print("Nome do País " + i + ": "); 
            paises.add(leitor.nextLine().trim());
        }
        System.out.println("\n===== ANOS COM MAIS LANÇAMENTOS POR PAÍS =====");
        for (String pais : paises) {
            int anoCampeao = arvore.obterAnoComMaisLancamentosParaPais(pais);
            if (anoCampeao != -1) System.out.println("-> [" + pais + "]: Ano com maior pico de estreias: " + anoCampeao);
            else System.out.println("-> [" + pais + "]: Sem registros na base informada.");
        }
        System.out.println("==============================================");
    }
} 