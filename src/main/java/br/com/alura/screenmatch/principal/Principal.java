package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.DadosEpisodio;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;
import org.springframework.core.annotation.MergedAnnotationPredicates;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {

        Scanner leitura = new Scanner(System.in);

        private ConsumoApi consumo  = new ConsumoApi();
        private ConverteDados conversor = new ConverteDados();

        private final  String ENDERECO = "http://www.omdbapi.com/?t=";

        private final String API_KEY =  "&apikey=4c8c328c";



    public void exibeMenu() {
        System.out.println("Digite o nome da serie :");

        var nomeSerie = leitura.nextLine();

        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+" ) + API_KEY);

        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);

        System.out.println(dados);

        	List<DadosTemporada> temporadas = new ArrayList<>();

		for (int i = 1; i<=dados.totalTemporadas(); i++){
			json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+" ) + "&season=" + i + API_KEY);
			DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
			temporadas.add(dadosTemporada);
		}
		temporadas.forEach(System.out::println);

        temporadas.forEach(t-> t.episodios().forEach(e-> System.out.println(e.titulo())));

        List<DadosEpisodio> dadosEpisodios = temporadas.stream()
                .flatMap(t-> t.episodios().stream())
                .collect(Collectors.toList());

        System.out.println("\n********* TOP 5 EPISODIOS **********\n");
        dadosEpisodios.stream()
                .filter(e-> !e.avaliacao().equalsIgnoreCase("N/A"))
                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
                .limit(5)
                .forEach(System.out::println);

        List<Episodio> episodios = temporadas.stream()
                .flatMap(t-> t.episodios().stream()
                .map(d-> new Episodio(t.numero(), d ))
                ).collect(Collectors.toList());

        episodios.forEach(System.out::println);
/*
        System.out.println("\n******* PESQUISA POR ANO ************\n");
        System.out.println("Informe desde que ano quer exibir os episodios");

        var ano = leitura.nextInt();

        LocalDate dataBusca = LocalDate.of(ano,1 ,1);
        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        episodios.stream()
                .filter(e-> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
                .forEach(e-> System.out.println(
                        "Temporada : " + e.getTemporada() +
                                "Episodio : " + e.getTitulo() +
                                " Data de Lancamento : " + e.getDataLancamento().format(formatador)
                ));
*/
        System.out.println("\n*********** PESQUISA POR NOME ***********\n");
        System.out.println("Digite o titulo ou parte do titulo : ");
        var trechoTitulo = leitura.nextLine();
        Optional<Episodio> episodioBuscado = episodios.stream()
                .filter(e -> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase()))
                .findFirst();

        if (episodioBuscado.isPresent()){
            System.out.println("Episodio encontrado : " + episodioBuscado.get().getTitulo());
            System.out.println("Temporada : " + episodioBuscado.get().getTemporada());

        }else{
            System.out.println("Não foi encontrado episodio com esse nome : " + trechoTitulo);
        }

        System.out.println("\n*********** AVALIACAO POR TEMPORADA *********");
        Map<Integer, Double> avalicaoPorTemporada = episodios.stream()
                .filter(e-> e.getAvaliacao() > 0.0)
                .collect(Collectors.groupingBy(Episodio::getTemporada,
                        Collectors.averagingDouble(Episodio::getAvaliacao)));
        System.out.println( avalicaoPorTemporada + );

        System.out.println("\b******** RESUMO DE AVALIACAO SERIE ************\n");
        DoubleSummaryStatistics est = episodios.stream()
                .filter(e-> e.getAvaliacao()>0.0)
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));
        System.out.println("Media : " + est.getAverage() +
                            "\nMelhor Avaliacao : " + est.getMax() +
                            "\nPior Avaliacao : " + est.getMin() +
                            "\nTotal Episodios Avaliados : " + est.getCount());


    }



    }

