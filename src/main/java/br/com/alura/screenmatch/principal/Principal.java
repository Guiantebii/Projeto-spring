package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;


import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";
    private List<DadosSerie> dadosSeries = new ArrayList<>();
    private SerieRepository serieRepository;
    private List<Serie> series = new ArrayList<>();


    public Principal(SerieRepository serieRepository) {
        this.serieRepository = serieRepository;
    }


    public void exibeMenu() {
        var opcao= -1;
        while (opcao != 0) {
            var menu = """
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar Séries Buscadas
                    4 - Buscar Série por Titulo
                    5 - Buscar Serie por Ator
                    6-  Buscar Top 5 Serie
                    7 - Buscar Serie por genero
                    8 - Buscar Serie por número de temporadas e avaliação
                    0 - Sair
                    """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                    case 4:
                        buscarSeriePorTitulo();
                        break;
                        case 5:
                            buscarSeriePorAtor();
                            break;
                case 6:
                    buscarTop5Series();
                    break;
                case 7:
                    buscarSeriesPorCategoria();
                    break;
                case 8:
                    buscarSeriesPorTemporadasEAvaliacao();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;

                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void buscarSeriesPorTemporadasEAvaliacao() {
        System.out.println("Escolha o máximo de temporadas da serie");
        var temporadas = leitura.nextInt();
        System.out.println("Avaliações a partir de qual valor? ");
        var avaliacao = leitura.nextDouble();
        List<Serie> seriesPorTemporadasEAvaliacao = serieRepository.findByTotalTemporadasLessThanEqualAndAvaliacaoGreaterThanEqual(temporadas,avaliacao);

        seriesPorTemporadasEAvaliacao.forEach(s -> System.out.println(s.getTitulo()
                + " avaliacao: " + s.getAvaliacao() ));
    }

    private void buscarSeriesPorCategoria() {
        System.out.println("Deseja buscar séries por categoria/ gênero? ");
        var nomeGenero = leitura.nextLine();
        Categoria categoria = Categoria.fromPortugues(nomeGenero);
        List<Serie> seriePorCategoria = serieRepository.findByGenero(categoria);
        System.out.println("serie por categoria: " + nomeGenero);
        seriePorCategoria.forEach(System.out::println);
    }

    private void buscarTop5Series() {
        List<Serie> seriesTop5 = serieRepository.findTop5ByOrderByAvaliacaoDesc();
        seriesTop5.forEach(s ->
                System.out.println(s.getTitulo() + " avaliacao:  " + s.getAvaliacao()));
    }

    private void buscarSeriePorAtor() {
        System.out.println("Digite um nome de um Ator");
        var nomeAtor = leitura.nextLine();
        System.out.println("Avaliações a partir de qual valor? ");
        var avaliacao = leitura.nextDouble();
        List<Serie> seriesEncontradas = serieRepository.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor, avaliacao);
        System.out.println("Séries em que: " + nomeAtor + " trabalhou: ");
        seriesEncontradas.forEach(s -> System.out.println(s.getTitulo()
        + " avaliacao: " + s.getAvaliacao() ));
    }

    private void buscarSeriePorTitulo() {
        System.out.println("Escolha uma série pelo nome");
        var nomeSerie = leitura.nextLine();
        Optional<Serie> serieBuscada = serieRepository.findByTituloContainingIgnoreCase(nomeSerie);
        if (serieBuscada.isPresent()) {
            System.out.println("Dados da serie: " + serieBuscada.get());
        }else {
            System.out.println("Serie não encontrada!");
        }
    }

    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        Serie serie = new Serie(dados);
        serieRepository.save(serie);
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie() {

        listarSeriesBuscadas();
        System.out.println("Escolha uma série pelo nome");
        var nomeSerie = leitura.nextLine();

        Optional<Serie> serie = series.stream()
                .filter(s -> s.getTitulo().toLowerCase().contains(nomeSerie.toLowerCase()))
                .findFirst();

        if (serie.isPresent()) {

            var serieEncontrada = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(t -> t.episodios()
                            .stream()
                            .map(e -> new Episodio(t.numero(), e)))
                    .collect(Collectors.toList());

            serieEncontrada.setEpisodios(episodios);
            serieRepository.save(serieEncontrada);
        } else {
            System.out.println("Série não encontrada!");
        }
    }

    private void listarSeriesBuscadas() {
        series = serieRepository.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    public class MyMemoryURLGenerator {
        public static String urlEncodeQuery(String text, Linguagem lingua1, Linguagem lingua2) {
            String texto = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String langpair = URLEncoder.encode(lingua1.siglaLinguagem+"|"+lingua2.siglaLinguagem, StandardCharsets.UTF_8);

            String url = "https://api.mymemory.translated.net/get?q="+texto+"&langpair="+langpair;

            return url;
        }
    }

    public class MyMemoryApiConnector {
        public static String get(String texto, Linguagem lingua1, Linguagem lingua2) {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(MyMemoryURLGenerator.urlEncodeQuery(texto, lingua1, lingua2)))
                    .build();

            try {
                HttpResponse<String> resposta = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (resposta.statusCode() == 200) {
                    return resposta.body();
                } else {
                    throw new IOException("Resposta mal-sucedida da MyMemory API");
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


}