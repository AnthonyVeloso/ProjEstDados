//Integrantes do grupo:
// Anthony Veloso 10737481
// André Cintra 10738062
// Luca Saboia 10736834

import java.util.*;

public class ABB {
    private NoABB raiz;

    public ABB() {
        this.raiz = null; 
    }

    public NoABB getRaiz() { return this.raiz; }

    //usa recursão chamando o método que realmente faz a lógica, e depois só retorna o resultado
    public void inserir(ProgramaNetFlix program) {
        raiz = inserirRec(raiz, program);
    }

    private NoABB inserirRec(NoABB raiz, ProgramaNetFlix program) {
        if (raiz == null) {
            raiz = new NoABB(program);
            return raiz;
        }
        
        //faz uma comaração lexicografica(alfabeto representado numericamente) entre os dados e a raiz
        if (program.getId().compareTo(raiz.dados.getId()) < 0) {
            raiz.esquerda = inserirRec(raiz.esquerda, program);
        } else if (program.getId().compareTo(raiz.dados.getId()) > 0) {
            raiz.direita = inserirRec(raiz.direita, program);
        }
        return raiz;
    } //parei aqui

    public ProgramaNetFlix buscar(String id, int[] comparacoes) {
        //usa a classe wrapper interna pra controlar o ponteiro de busca -> armazena o nó
        NoABBBox atual = new NoABBBox(raiz);
        
        //percorre a árvore até achar ou ser null 
        while (atual.no != null) {
            comparacoes[0]++; //contador que guarda o número de comparações
            if (id.equals(atual.no.dados.getId())) return atual.no.dados;
            if (id.compareTo(atual.no.dados.getId()) < 0) atual.no = atual.no.esquerda;
            else { atual.no = atual.no.direita; }
        }
        return null;
    }
    
    //classe wrapper interna que serve para encapsular a referência do nó
    private static class NoABBBox {
        NoABB no;
        NoABBBox(NoABB no) { this.no = no; }
    }

    //igual o método de inserir, o privado faz a lógica e aqui apenas chama
    public int calcularAltura() {
    return calcularAlturaRec(raiz);
    }

private int calcularAlturaRec(NoABB no) {
    if (no == null) {
        return -1; // Árvore vazia tem altura -1 
    }
    //calcula a altura das subárvores usando recursão
    int alturaEsq = calcularAlturaRec(no.esquerda);
    int alturaDir = calcularAlturaRec(no.direita);
    
    return 1 + Math.max(alturaEsq, alturaDir); //calculo que devolve a altura do no atual: soma do caminho mais longo entre os filhos e 1
}

    public Map<String, ProgramaNetFlix> obterFilmesMaisCurtidosPorGeneros(List<String> generosEscolhidos) {
        //usamos HashMap pela complexidade 0(1)
        Map<String, ProgramaNetFlix> topFilmes = new HashMap<>();
        //já guarda no dicionario os generos, mas sem nenhum filme 
        for (String g : generosEscolhidos) 
            topFilmes.put(g, null);
        //chama o método responsável por achar o filme mais curtido
        percorrerParaFilmesMaisCurtidos(raiz, generosEscolhidos, topFilmes);
        return topFilmes;
    }

     //vai percorrer Em-Ordem cada lado da árore garantindo que todos os níos sejam verificados
    private void percorrerParaFilmesMaisCurtidos(NoABB no, List<String> generos, Map<String, ProgramaNetFlix> topFilmes) {
        if (no != null) { //verifica a raiz pra ver se o nó existe
            //desce recursivamente por toda a subarvore esquerda
            percorrerParaFilmesMaisCurtidos(no.esquerda, generos, topFilmes);
            
            //usa o equals para pegar apenas os filmes(Movie)
            if ("Movie".equalsIgnoreCase(no.dados.getTipoShow())) {
                //for é pra entrar em cada um dos 5 generos
                for (String generoBuscado : generos) {
                    //ajustando a formatação pra não dar erro na leitura 
                    String buscaMinusculo = generoBuscado.toLowerCase().trim();
                    //como, cada filme tem mais de um gênero, a gente tem que percorrer a lista de cada objeto do filme pra ver se tem o gênero buscado 
                    for (String generoFilme : no.dados.getGeneros()) {
                        if (generoFilme.toLowerCase().trim().contains(buscaMinusculo)) {
                            ProgramaNetFlix topAtual = topFilmes.get(generoBuscado);
                            if (topAtual == null || no.dados.getNotaImdb() > topAtual.getNotaImdb()) {
                                topFilmes.put(generoBuscado, no.dados);
                            }
                        }
                    }
                }
            }
            percorrerParaFilmesMaisCurtidos(no.direita, generos, topFilmes);
        }
    }

    public Map<String, Integer> contarFilmesPorPaisEmPeriodo(int anoInicio, int anoFim) {
        //de novo usando HashMap, é criado vazio(vai guardar o pais + contagem)
        Map<String, Integer> contagemPaises = new HashMap<>();
        percorrerParaContarFilmes(raiz, anoInicio, anoFim, contagemPaises);
        return contagemPaises;
    }

    //de novo uso do percurso Em Ordem já que o dataset da netflix está organizado por id e não por ano, então tem que varrer tudo pra garantir que nenhum filme foi pulado
    private void percorrerParaContarFilmes(NoABB no, int anoInicio, int anoFim, Map<String, Integer> contagemPaises) {
        if (no != null) {
            //começa percorrendo recursivamente a sub arvore esquerda
            percorrerParaContarFilmes(no.esquerda, anoInicio, anoFim, contagemPaises);
            //verificação pra ver se atende ao intervalo
            if ("Movie".equalsIgnoreCase(no.dados.getTipoShow()) && no.dados.getAnoLancamento() >= anoInicio && no.dados.getAnoLancamento() <= anoFim) {
                //assim como os generos, cada filme pode ter mais de um pais(co produtor), então tem que varrer a lista dos países de cada um
                for (String pais : no.dados.getPaisesProducao()) {
                    //usa .put pra guardar o pais no dicionario e 
                    //usa o  método .getOrDefault como contador -> esse método verifica a quantidade de aparições do filme e soma o novo
                    contagemPaises.put(pais, contagemPaises.getOrDefault(pais, 0) + 1);
                }
            }
            percorrerParaContarFilmes(no.direita, anoInicio, anoFim, contagemPaises);
        }
    }

    public ProgramaNetFlix obterFilmeMaisFamosoPorPais(String pais) {
        ProgramaNetFlix[] maisFamoso = new ProgramaNetFlix[1];
        percorrerParaMaisFamoso(raiz, pais, maisFamoso);
        return maisFamoso[0];
    }

    private void percorrerParaMaisFamoso(NoABB no, String pais, ProgramaNetFlix[] maisFamoso) {
        if (no != null) {
            percorrerParaMaisFamoso(no.esquerda, pais, maisFamoso);
            if ("Movie".equalsIgnoreCase(no.dados.getTipoShow())) {
                for (String p : no.dados.getPaisesProducao()) {
                    if (p.equalsIgnoreCase(pais)) {
                        if (maisFamoso[0] == null || no.dados.getVotosImdb() > maisFamoso[0].getVotosImdb()) {
                            maisFamoso[0] = no.dados;
                        }
                    }
                }
            }
            percorrerParaMaisFamoso(no.direita, pais, maisFamoso);
        }
    }

    public ProgramaNetFlix obterTituloMaisVotadoEmPeriodo(int anoInicio, int anoFim) {
        ProgramaNetFlix[] maisVotado = new ProgramaNetFlix[1];
        percorrerParaMaisVotado(raiz, anoInicio, anoFim, maisVotado);
        return maisVotado[0];
    }

    private void percorrerParaMaisVotado(NoABB no, int anoInicio, int anoFim, ProgramaNetFlix[] maisVotado) {
        if (no != null) {
            percorrerParaMaisVotado(no.esquerda, anoInicio, anoFim, maisVotado);
            if (no.dados.getAnoLancamento() >= anoInicio && no.dados.getAnoLancamento() <= anoFim) {
                if (maisVotado[0] == null || no.dados.getVotosImdb() > maisVotado[0].getVotosImdb()) {
                    maisVotado[0] = no.dados;
                }
            }
            percorrerParaMaisVotado(no.direita, anoInicio, anoFim, maisVotado);
        }
    }

    // Método refinado para obter o ano com mais lançamentos de UM país específico
    public int obterAnoComMaisLancamentosParaPais(String pais) {
        Map<Integer, Integer> contagemAnos = new HashMap<>();
        percorrerParaAnoComMaisLancamentos(raiz, pais, contagemAnos);
        
        int anoMaximo = -1;
        int contagemMaxima = -1;
        
        for (Map.Entry<Integer, Integer> entrada : contagemAnos.entrySet()) {
            if (entrada.getValue() > contagemMaxima) {
                contagemMaxima = entrada.getValue();
                anoMaximo = entrada.getKey();
            }
        }
        return anoMaximo;
    }

    private void percorrerParaAnoComMaisLancamentos(NoABB no, String pais, Map<Integer, Integer> contagemAnos) {
        if (no != null) {
            percorrerParaAnoComMaisLancamentos(no.esquerda, pais, contagemAnos);
            
            // Verifica se o filme/show atual possui o país buscado (ignorando maiúsculas/minúsculas)
            for (String pFilme : no.dados.getPaisesProducao()) {
                if (pFilme.equalsIgnoreCase(pais)) {
                    int ano = no.dados.getAnoLancamento();
                    contagemAnos.put(ano, contagemAnos.getOrDefault(ano, 0) + 1);
                    break; // Evita duplicar caso o país apareça duas vezes na mesma string por erro do CSV
                }
            }
            
            percorrerParaAnoComMaisLancamentos(no.direita, pais, contagemAnos);
        }
    }

    // Descobre o maior ID numérico dentro da árvore BST para evitar duplicados
    public int obterMaiorIdNumerico() {
        int[] maior = new int[]{0};
        percorrerParaMaiorId(raiz, maior);
        return maior[0];
    }

    private void percorrerParaMaiorId(NoABB no, int[] maior) {
        if (no != null) {
            percorrerParaMaiorId(no.esquerda, maior);
            
            // Remove as letras 'tm' ou 'ts' e captura apenas a parte numérica
            try {
                String idLimpo = no.dados.getId().replaceAll("[^0-9]", "");
                if (!idLimpo.isEmpty()) {
                    int idNum = Integer.parseInt(idLimpo);
                    if (idNum > maior[0]) {
                        maior[0] = idNum;
                    }
                }
            } catch (Exception e) {
                // Ignora falhas de conversão caso o ID tenha um formato corrompido
            }
            
            percorrerParaMaiorId(no.direita, maior);
        }
    }

    // Método público para iniciar a remoção pelo ID
    public void remover(String id) {
        raiz = removerRec(raiz, id);
    }

    private NoABB removerRec(NoABB raiz, String id) {
        // Caso Base: Árvore vazia ou ID não encontrado
        if (raiz == null) {
            return null;
        }

        // Navega pela árvore para encontrar o nó
        if (id.compareTo(raiz.dados.getId()) < 0) {
            raiz.esquerda = removerRec(raiz.esquerda, id);
        } else if (id.compareTo(raiz.dados.getId()) > 0) {
            raiz.direita = removerRec(raiz.direita, id);
        } else {
            // ENCONTROU O NÓ! Agora aplica as regras de remoção:

            // Caso 1 e 2: Nó com apenas um filho ou nenhum filho
            if (raiz.esquerda == null) {
                return raiz.direita;
            } else if (raiz.direita == null) {
                return raiz.esquerda;
            }

            // Caso 3: Nó com dois filhos
            // Obtém o sucessor em ordem (menor valor da subárvore direita)
            raiz.dados = obterSubarvoreMinima(raiz.direita);

            // Remove o sucessor em ordem da subárvore direita
            raiz.direita = removerRec(raiz.direita, raiz.dados.getId());
        }

        return raiz;
    }

    // Auxiliar para encontrar o menor nó de uma determinada subárvore (mais à esquerda possível)
    private ProgramaNetFlix obterSubarvoreMinima(NoABB raiz) {
        ProgramaNetFlix minDados = raiz.dados;
        while (raiz.esquerda != null) {
            minDados = raiz.esquerda.dados;
            raiz = raiz.esquerda;
        }
        return minDados;
    }

    // Retorna todos os programas da árvore em uma lista para fins de salvamento
    public List<ProgramaNetFlix> exportarParaLista() {
        List<ProgramaNetFlix> lista = new ArrayList<>();
        percorrerParaExportar(raiz, lista);
        return lista;
    }

    private void percorrerParaExportar(NoABB no, List<ProgramaNetFlix> lista) {
        if (no != null) {
            percorrerParaExportar(no.esquerda, lista);
            lista.add(no.dados); // Adiciona o programa atual à lista
            percorrerParaExportar(no.direita, lista);
        }
    }
}