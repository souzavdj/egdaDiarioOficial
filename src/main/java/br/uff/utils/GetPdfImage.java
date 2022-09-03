package br.uff.utils;

import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.DrawObject;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.state.*;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is an example on how to get the x/y coordinates of image location and size of image.
 */
public class GetPdfImage extends PDFStreamEngine {

    private Map<RenderedImage,Matrix> imageMatrix = new HashMap<>();

    private static String destination;

    /**
     * @throws IOException If there is an error loading text stripper properties.
     */
    public GetPdfImage() throws IOException {
        // preparing PDFStreamEngine
        addOperator(new Concatenate());
        addOperator(new DrawObject());
        addOperator(new SetGraphicsStateParameters());
        addOperator(new Save());
        addOperator(new Restore());
        addOperator(new SetMatrix());
    }

    public static void getPdfImages(String path, String destination) {
        PDDocument document = null;
        GetPdfImage.destination = destination;
        try {
            document = PDDocument.load(new File(path));
            GetPdfImage printer = new GetPdfImage();
            int pageNum = 0;
            for (PDPage page : document.getPages()) {
                pageNum++;
                System.out.println("\n\nProcessing page: " + pageNum + "\n---------------------------------");
                printer.processPage(page);

            }
        } catch (IOException e) {
            System.err.println("Erro ao recuperar as imagens do arquivo: "+ e.getMessage());
        } finally {
            if (document != null) {
                try {
                    document.close();
                } catch (IOException e) {
                    System.err.println("Erro ao fechar o arquivo: "+ e.getMessage());
                }
            }
        }
    }

    /**
     * @param operator The operation to perform.
     * @param operands The list of arguments.
     * @throws IOException If there is an error processing the operation.
     */
    @Override
    protected void processOperator(Operator operator, List<COSBase> operands) throws IOException {
        String operation = operator.getName();
        if ("Do".equals(operation)) {
            COSName objectName = (COSName) operands.get(0);
            // get the PDF object
            PDXObject xobject = getResources().getXObject(objectName);
            // check if the object is an image object
            if (xobject instanceof PDImageXObject) {
                PDImageXObject image = (PDImageXObject) xobject;
                int imageWidth = image.getWidth();
                int imageHeight = image.getHeight();

                System.out.println("\nImage [" + objectName.getName() + "]");

                Matrix ctmNew = getGraphicsState().getCurrentTransformationMatrix();
                this.imageMatrix.put(image.getImage(), ctmNew);

                File file = new File(destination + System.nanoTime() + ".jpg");
                ImageIO.write(image.getImage(), "jpg", file);

                float imageXScale = ctmNew.getScalingFactorX();
                float imageYScale = ctmNew.getScalingFactorY();

                // position of image in the pdf in terms of user space units
                System.out.println("position in PDF = " + ctmNew.getTranslateX() + ", " + ctmNew.getTranslateY() + " in user space units");
                // raw size in pixels
                System.out.println("raw image size  = " + imageWidth + ", " + imageHeight + " in pixels");
                // displayed size in user space units
                System.out.println("displayed size  = " + imageXScale + ", " + imageYScale + " in user space units");
            } else if (xobject instanceof PDFormXObject) {
                PDFormXObject form = (PDFormXObject) xobject;
                showForm(form);
            }
        } else {
            super.processOperator(operator, operands);
        }
    }

    public Map<RenderedImage, Matrix> getImageMatrix() {
        return imageMatrix;
    }
}