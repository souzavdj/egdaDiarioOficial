package br.uff;

import br.uff.utils.DiarioOficialReader;
import br.uff.utils.GetPdfImage;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        String path = "src/main/resources/diariosOficiais/pdf/05_julho.pdf";

        // Lendo o conteudo do arquivo.
        String fileContent = null;
        try {
            fileContent = DiarioOficialReader.readFile(path);
        } catch (IOException e) {
            System.err.println("Erro ao ler conteudo do pdf: " + e.getMessage());
        }
        System.out.println(fileContent);

        // Recuperando as imagens do arquivo.
        String destination = "src/main/resources/diariosOficiais/images/";
        GetPdfImage.getPdfImages(path, destination);

    }

}
