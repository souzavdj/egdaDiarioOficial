package br.uff.utils;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DiarioOficialReader {

    private static List<RenderedImage> getImagesFromResources(PDResources resources) throws IOException {
        List<RenderedImage> images = new ArrayList<>();

        for (COSName xObjectName : resources.getXObjectNames()) {
            PDXObject xObject = resources.getXObject(xObjectName);

            if (xObject instanceof PDFormXObject) {
                PDFormXObject xObject1 = (PDFormXObject) xObject;
                images.addAll(getImagesFromResources(xObject1.getResources()));
            } else if (xObject instanceof PDImageXObject) {
                PDImageXObject xObject1 = (PDImageXObject) xObject;
                images.add(xObject1.getImage());
            }
        }

        return images;
    }

    public static List<RenderedImage> getImagesFromPDF(PDDocument document) throws IOException {
        List<RenderedImage> images = new ArrayList<>();
        for (PDPage page : document.getPages()) {
            images.addAll(getImagesFromResources(page.getResources()));
        }

        return images;
    }


    public static String readFile(String path) throws IOException {
        PDFParser parser = new PDFParser(new RandomAccessFile(new File(path), "r"));
        parser.parse();
        COSDocument cosDoc = parser.getDocument();
        PDFTextStripper pdfStripper = new PDFTextStripper();
        PDDocument pdDoc = new PDDocument(cosDoc);

        List<RenderedImage> images = getImagesFromPDF(pdDoc);
        for (RenderedImage image : images) {
            System.out.println("Imagem: "+image);
        }

        return pdfStripper.getText(pdDoc);

    }

}
