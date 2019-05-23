import java.io.File; 
import java.io.IOException; 
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.lang.String;
import java.util.regex.Pattern;
  
public class ImageProcessing {
    public static class Kernel {
        public String name;
        public int size;
        public double divisor;
        public int [] data; // single dimensionnal array instead of 2D array
        public int radius;

        public Kernel (String name, int size, double divisor, int [] data){
            this.name = name;
            this.size = size;
            this.divisor = divisor;
            this.data = data;
            this.radius = size >>> 1;
        }

        public int getData(int x, int y){ return this.data[y * this.size + x]; }
    }

    // function to apply extension to the border pixels
    public static int boundsExtension(int axisValue, int imageAxisSize){
        if(axisValue < 0) return 0;
        else if(axisValue >= imageAxisSize) return imageAxisSize-1;
        return axisValue;
    }

    // function to keep the value within the rgb limit
    public static int boundRGB(int value, int limit){
        if(value < 0) return 0;
        if(value > limit) return limit;
        return value;
    }

    public static BufferedImage imageToGrey(BufferedImage img){
        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();
        for(int y=0; y < imgHeight; y++){
            for(int x=0; x < imgWidth; x++){
                int pixel = img.getRGB(x,y);
                int r = (pixel >>> 16) & 0xFF;
                int g = (pixel >>> 8) & 0xFF;
                int b = pixel & 0xFF;
                int grey = (r+g+b/3);
                img.setRGB(x, y, (grey << 16) | (grey << 8) | grey | -0x01000000);
            }
        }
        return img;
    }

    public static BufferedImage applyKernel(BufferedImage inputImage, Kernel kernel){
        int imageInWidth = inputImage.getWidth();
        int imageInHeight = inputImage.getHeight();
        BufferedImage outputImage = new BufferedImage(imageInWidth, imageInHeight,
                                    BufferedImage.TYPE_3BYTE_BGR);
        for(int y=0; y < imageInHeight; y++){
            for(int x=0; x < imageInWidth; x++){
                double[] rgbValues = new double[3];
                for(int ky=0; ky < kernel.size; ky++){
                    for(int kx=0; kx < kernel.size; kx++){
                        int pixel = inputImage.getRGB(
                            boundsExtension( x + kx - kernel.radius, imageInWidth),
                            boundsExtension( y + ky - kernel.radius, imageInHeight)
                        );
                        int kernelValue = kernel.getData(kx,ky);
                        rgbValues[0] += ((pixel >>> 16) & 0xFF) * kernelValue;
                        rgbValues[1] += ((pixel >>> 8) & 0xFF) * kernelValue;
                        rgbValues[2] += (pixel & 0xFF) * kernelValue;
                    }
                }
                int red   = boundRGB((int)Math.round(rgbValues[0] / kernel.divisor), 256);
                int green = boundRGB((int)Math.round(rgbValues[1] / kernel.divisor), 256);
                int blue  = boundRGB((int)Math.round(rgbValues[2] / kernel.divisor), 256);
                outputImage.setRGB(x, y, (red << 16) | (green << 8) | blue | -0x01000000);
            }
        }
        return outputImage;
    }

    public static void main(String args[]) throws IOException {

        String inputFilename = args[0];
        File inputFile = new File(inputFilename);

        BufferedImage inputImage = ImageIO.read(inputFile);
        Kernel kernel = new Kernel("contour-grey", 3, 1, new int[]{-1,-1,-1,-1,8,-1,-1,-1,-1});

        BufferedImage outputImage = imageToGrey(applyKernel(inputImage,kernel));

        String[] tmp =inputFilename.split(Pattern.quote("."));
        String outputFilename = tmp[0] + "-" + kernel.name + ".jpg";
        ImageIO.write(outputImage, "jpg", new File(outputFilename));

        return;
    }

}