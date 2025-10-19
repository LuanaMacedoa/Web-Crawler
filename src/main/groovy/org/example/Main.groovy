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
        def padraoTisdoc = ''


        Element espacoPrestadorLink = linksPaginaInicial.find { it.text().contains("Espaço do Prestador") }

        if (espacoPrestadorLink != null) {
            String urlEspacoPrestador = espacoPrestadorLink.attr("abs:href")


            def espacoPrestadorDoc = Jsoup.connect(urlEspacoPrestador).get()
            def linksEspacoPrestador = espacoPrestadorDoc.select("a[href]")

            Element link = linksEspacoPrestador.find { it.text().contains("Padrão para Troca de Informação") }
            String urlPadraoTrocaInformacao = link.attr("abs:href")
            if (urlEspacoPrestador != null) {

                padraoTisdoc = Jsoup.connect(urlPadraoTrocaInformacao).get()


                def linksPadraoTis = padraoTisdoc.select("a[href]")
                Element link3 = linksPadraoTis.find { it.text().contains("Clique aqui para acessar a versão Setembro/2025") }
                String urlPadraotis = link3.attr("abs:href")



                def componenteComunicacaoDoc = Jsoup.connect(urlPadraotis).get()
                def linhas = componenteComunicacaoDoc.select("tr")


                def urlZipComunicacao = ''
                for (Element linha : linhas) {
                    if (linha.text().contains("Componente de Comunicação")) {
                        Element linkZipComunicacao = linha.selectFirst("a[href\$='.zip']")
                        if (linkZipComunicacao != null) {
                            String urlZip = linkZipComunicacao.attr("abs:href")
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


            } else {
                println "Link2 não encontrado"
            }

        } else {
            println("Link não encontrado.")
        }

        // implementação parte 2
        def historicoTiss = padraoTisdoc.select("a[href]")
        Element linkHistorico = historicoTiss.find { it.text().contains("Clique aqui para acessar todas as versões dos Componentes") }

        String linkAbsHistorico = ''
        if(linkHistorico != null){
            linkAbsHistorico = linkHistorico.attr('abs:href')
        }else {
            println("Link 4 não encontrado")
        }

        Document historicoTisDoc = Jsoup.connect(linkAbsHistorico).get()
        Element tabela = historicoTisDoc.select("div#parent-fieldname-text table").first()
        Element cabecalhoTr = tabela.select("thead tr").first()

        def colunas = cabecalhoTr.select("th").take(3)
        def linhasDados = tabela.select("tbody tr")
        def linhasInvertidas = linhasDados.reverse()

        List<String> LinhaCsv = []

        def cabecalhoCsv = colunas.collect {it.text().trim()}.join(",")
        LinhaCsv.add(cabecalhoCsv)
        println(LinhaCsv)

        /*
            colunas.each{th ->
                print(th.text() + " ")
            }
            println(" ")
        */
        Boolean coletar = false

        for (def linha: linhasInvertidas) {
            def coluna = linha.select("td")
            if (coluna.size() >= 3) {
                def competencia = coluna[0].text().trim()
                if (competencia == "Jan/2016"){
                    coletar = true
                }
                if (coletar) {
                    def publicacao = coluna[1].text().trim()
                    def inicioVigencia = coluna[2].text().trim()

                    LinhaCsv.add([competencia,publicacao,inicioVigencia].join(","))
                }
            }
        }
        def pastaDestino = "./DownloadsTiss"
        def nomeArq = "HistoricoVersoes.csv"
        def caminhoArquivo = Paths.get(pastaDestino,nomeArq)

        try {
            Files.write(caminhoArquivo, LinhaCsv, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
            println("Arquivo salvo em: $caminhoArquivo")
        } catch (Exception e) {
            println("Erro ao salvar arquivo: ${e.message}")
        }

        //implementação task 3

        def tabelasRelacionadas = padraoTisdoc.select("a[href]")
        def linkTabelasRelacionadas  = tabelasRelacionadas.find { it.text().contains("Clique aqui para acessar as planilhas") }
        def urlTabelasRelacionadas = linkTabelasRelacionadas.attr("abs:href")

        def tabelaErro = Jsoup.connect(urlTabelasRelacionadas).get()

        def linkTabelaErro = tabelaErro.select("a[href]")
        def linkArqTabelaErro = linkTabelaErro.find { it.text().contains("Clique aqui para baixar a tabela de erros no envio para a ANS ") }

        if (linkArqTabelaErro) {
            def urlArqdTabelaErro = linkArqTabelaErro.attr("abs:href")


            def nomeArquivo = urlArqdTabelaErro.substring(urlArqdTabelaErro.lastIndexOf('/')+1)
            def caminhoArquivoE = Paths.get(pastaDestino,nomeArquivo)

            try {
                configure {
                    request.uri = urlArqdTabelaErro
                }.get {byte[] body ->
                    Files.write(caminhoArquivoE, body, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
                    println("Arquivo salvo em: $caminhoArquivoE")
                }
            } catch (Exception e) {
                println("Erro ao baixar o arquivo: ${e.message}")
            }
        } else {
            println("Link da tabela de erros não encontrado.")
        }


    }
}