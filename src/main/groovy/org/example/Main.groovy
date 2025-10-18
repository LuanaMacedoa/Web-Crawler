package org.example

import java.nio.file.Paths

import static groovyx.net.http.HttpBuilder.configure
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.nio.file.Files
import java.nio.file.StandardOpenOption


class Main {
    static void main(String[] args) {
        String url = "https://www.gov.br/ans/pt-br"

        def paginaInicialDoc = Jsoup.connect(url).get()
        def linksPaginaInicial = paginaInicialDoc.select("a[href]")

        Element espacoPrestadorLink = linksPaginaInicial.find { it.text().contains("Espaço do Prestador") }

        if (espacoPrestadorLink != null) {
            String urlEspacoPrestador = espacoPrestadorLink.attr("abs:href")
            println("Link encontrado: $urlEspacoPrestador")

            def espacoPrestadorDoc = Jsoup.connect(urlEspacoPrestador).get()
            def linksEspacoPrestador = espacoPrestadorDoc.select("a[href]")

            Element link = linksEspacoPrestador.find { it.text().contains("Padrão para Troca de Informação") }
            String urlPadraoTrocaInformacao = link.attr("abs:href")
            if (urlEspacoPrestador != null) {
                println("link2 encontrado: $urlPadraoTrocaInformacao")
                def padraoTisdoc = Jsoup.connect(urlPadraoTrocaInformacao).get()
                def linksPadraoTis = padraoTisdoc.select("a[href]")
                Element link3 = linksPadraoTis.find { it.text().contains("Clique aqui para acessar a versão Setembro/2025") }
                String urlPadraotis = link3.attr("abs:href")
                println("link 3 encontrado: $urlPadraotis")


                def componenteComunicacaoDoc = Jsoup.connect(urlPadraotis).get()
                def linhas = componenteComunicacaoDoc.select("tr")


                def urlZipComunicacao = ''
                for (Element linha : linhas) {
                    if (linha.text().contains("Componente de Comunicação")) {
                        Element linkZipComunicacao = linha.selectFirst("a[href\$='.zip']")
                        if (linkZipComunicacao != null) {
                            String urlZip = linkZipComunicacao.attr("abs:href")
                            println("Link do zip encontrado: $urlZip")
                            urlZipComunicacao = urlZip
                            break

                        }
                    }
                }
                def pastaDestino = "./DownloadsTiss"
                def nomeArquivo = urlZipComunicacao.substring(urlZipComunicacao.lastIndexOf('/') + 1)
                def caminhoArquivo = Paths.get(pastaDestino, nomeArquivo)


                try {
                    configure {
                        request.uri = urlZipComunicacao
                    }.get { byte[] body ->
                        Files.write(caminhoArquivo, body, StandardOpenOption.CREATE)
                        println("Arquivo baixado e salvo em: $caminhoArquivo")
                    }
                } catch (Exception e) {
                    println("Erro ao baixar arquivo: ${e.message}")
                }



            } else{
                println "Link2 não encontrado"
            }

        } else {
            println("Link não encontrado.")
        }


    }
}
