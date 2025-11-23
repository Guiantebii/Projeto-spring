package br.com.alura.screenmatch.service;

import br.com.alura.screenmatch.model.Linguagem;
import br.com.alura.screenmatch.principal.Principal;
import br.com.alura.screenmatch.model.MyMemoryResponseValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class Tradutor {
    public static String traduzirInglesParaPortugues(String text) {
        ObjectMapper mapper = new ObjectMapper();

        String json = Principal.MyMemoryApiConnector.get(text, Linguagem.INGLES, Linguagem.PORTUGUES);

        MyMemoryResponseValue traducao;
        try {
            traducao = mapper.readValue(json, MyMemoryResponseValue.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        traducao.dadosResposta.textoTraduzido = URLDecoder.decode(traducao.dadosResposta.textoTraduzido, StandardCharsets.UTF_8);

        return traducao.dadosResposta.textoTraduzido;
    }
}