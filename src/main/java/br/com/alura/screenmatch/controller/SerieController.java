package br.com.alura.screenmatch.controller;

import br.com.alura.screenmatch.dto.SerieDTO;
import br.com.alura.screenmatch.model.EpisodioDTO;
import br.com.alura.screenmatch.service.SerieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/series")
public class SerieController {


    @Autowired
    private SerieService serieService;

    @GetMapping
    public List<SerieDTO> obterSeries(){
        return serieService.obterTodasSeries();
    }

    @GetMapping("/top5")
    public List<SerieDTO> obterTop5(){
        return serieService.obterTop5series();
    }

    @GetMapping("/lancamentos")
    public List<SerieDTO> obterLancamentos(){
        return serieService.obterLancamentos();
    }
    @GetMapping("/{id}")
    public SerieDTO obterPorId(@PathVariable Long id){
        return serieService.obterPorId(id);
    }

    @GetMapping("/{id}/temporadas/todas")
    public List<EpisodioDTO> obterTodasTemporadas(@PathVariable Long id){
        return serieService.obterTodasTemporadas(id);
    }
    @GetMapping("/{id}/temporadas/{numero}")
    public List<EpisodioDTO> obterTodasTemporadasPorNumero(@PathVariable Long id, @PathVariable Long numero){
        return serieService.obterTodasTemporadasPorNumero(id,numero);
    }
    @GetMapping("/categoria/{nomeGenero}")
    public List<SerieDTO> obterTodasCategorias(@PathVariable String nomeGenero){
        return serieService.obterSeriesPorCategoria(nomeGenero);
    }



}
