package br.com.alura.screenmatch.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MyMemoryResponseValue {

    @JsonAlias("responseData")
    public DadosResposta dadosResposta;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DadosResposta {
        @JsonAlias("translatedText")
        public String textoTraduzido;
    }
}