package mkl.testarea.itext5.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

/**
 * @author mkl
 */
public class AddInlineImage {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/58224394/add-inline-image-in-pdf-document-using-itext-library">
     * add Inline image in pdf document using itext library
     * </a>
     * <p>
     * The example image has properties that iText cannot properly translate
     * into an inlined image. Unfortunately it does not recognize this and
     * outputs an erroneous result PDF.
     * 
     * In particular the image file uses transparency. Inline images don't
     * allow for Mask or SMask entries (which images in PDFs use to represent
     * transparency). Thus, your image as is cannot be used as inline image.
     * 
     * As a result the inline image created by iText only consists of a black
     * rectangle while the transparency information (which contains the line
     * drawing) is dropped.
     * 
     * Furthermore, the image uses a calibrated RGB color space. Such
     * calibrated RGB color spaces cannot be inlined themselves, so the color
     * space definition has to be put into the page resources. iText, though,
     * when creating an inlined image, fails to reference that non-inlined
     * part properly.
     * 
     * As a result the inline image created by iText references a color space
     * by the wrong name, causing the "An error exists on this page" error
     * message by Adobe Reader. Fixing that reference one gets a valid result
     * PDF showing the black rectangle mentioned above.
     * </p>
     */
    @Test
    public void testAddInlineImageLikeSainse() throws IOException, DocumentException {
        try (   InputStream resource = getClass().getResourceAsStream("test3.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "test3-withImage.pdf"))  ) {
            PdfReader resultantPdfReader = new PdfReader(resource);
            PdfStamper resultandPdfStamper = new PdfStamper(resultantPdfReader, result);

            String encodedSignature = "iVBORw0KGgoAAAANSUhEUgAAAU4AAACWCAYAAACxSWGfAAAAAXNSR0IArs4c6QAAFJFJREFUeAHtnQmwZFV9hw37DgoiaJgZsGBAFgkSIEJgjAZRAliFikrEoOgoGkQlEFJKiXFhr5RCAKM" + 
                    "jJhBETAoRImjAB4KAKBgEDBBhQM2IyEDYh2XM90Gf8k7T3a/fe9093X1//6rv3XOXvsvX9/773HNP93vBCxIxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEA" + 
                    "MxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxE" + 
                    "AMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAMxEAPNBl7MhEObJ2Y8BmIgBmKgtYG9mTwBv2vAIBEDMRADMdDKwCuY" + 
                    "eCmUhFmGrZbNtBiIgRhoa2AN5uwDZ8L3oCST6Q6vYR3nwnyYC8MQ67MTp8JT4HEthsMbZccTMRADMTCpgU1Z4kPwbXgCppsku3ndIta/vBLpymz7w2CidF9NnCZQE6lR9v+5sfyNgRiIgYq" + 
                    "BlSjvASfALVAShsNn4Br4OPwRzDSsZc4Hk+UiqG7LstOcNx/mQj9iM1Z6LFwNZfveonurXmJVCmVemZZhDMRAzQ1swPG/E74GD0BJEg4dPw8OAp8u9zNMjibJc2ERVPfDstOcNx/mwnRjLV" + 
                    "54MFwBS6Fsx3X7MKg5jmeCyxzXPCPjnQ38QefZmRsDI2dge/bYJCE7wwpQ4mcULm5wFcOny4wBD02O8yps1LR9b6sfhvsa/GaS8k7M/yvYH9YE41H4NzgLJsAEWQ3dWBs1Xg0/fLaUPzEQA" + 
                    "7UysAdH+2UotSyHtl1eAn8Nm8Kwhol0PlgzvA6qxzCVsrVMa5sHg7XPdrEqM24F153aZjtLHaanxtlBTmaNhAFvs0+EdzX29iaGJp+L4DKw5jVqYa1xQ/DYpF3ZeS77A/gJfBXuhMnCW/Qj" + 
                    "wRq4bbpLIBEDMVADA37ovwfuB2tOj8MnYBVItDfgLbpNFLJT+8UyJwZiYNwMbM0BfR/KbaxPi18+bgfZ4+Pxg+bNcCPkFr3HcrO63hlYl1VtA3vBe+FYsA3uO3Ar/Aouh9PhjbAaJDobWIP" + 
                    "Zn4MnwYt/EbwNEp0NvI7Z10P5oDmasu2ciRgYGgOvYk9OgnKSdju0Le6bcAhsDIllDezN6F2gz2fgNPDDKdHewI7M+i6Uc9AP6/eBfVoTMTAUBjwZj4HylTbb3qxZWsNcANY4rXlaA7Um6k" + 
                    "W/A/gaawNLoZzglp3mvF50yGY1IxsvY8+/AcXNDZT/eGSPZjA7vgWb+TqUc+oBykfB6pCIgaExsCV7Yj84L25P1lNgqrfe1jJNrNY6rX2WROHwHNgX6hQrcrAfhodABw79brXTE60NvJTJZ" + 
                    "0L58H6M8nHwQkjEwNAYsMH9MPAE9eJeCPNgpmHS9db0DLCGVZLoBGVvv8Y9rFFWj9uO3NY8E60NmBhNkOU8NHGaQE2kiRgYKgObsDeXQUlqCyiv04c9tAngQ3AfuC1rtGfDLBi3sPniVLAN" + 
                    "02O9C/wASbQ24K23t+CLoZwb3qJ7q56IgaEzcBB79CB4st4L+0K/w6RireJxcLsOfcLcj2TNagceB7DF/wWPzafmHqtP0RPPN+CHqQ95fNijL/ku1OFuhMNMjJqBF7PD/w7lZLXstEHGbDZ" + 
                    "mm6c1T/fDmugHwYtpFMP+l5dCcXoVZR+eJZY1YLPQLnAyXAfF1/WUXweJGBhKA9YqrV16wlrbtNa5PMPaxRVQLqD/przf8tyhKW57FZb3Z9xKDdpeCIeACSLxnIFqsrybSeW9dngevAXiCw" + 
                    "mJ4TPgrfACKCet7Zq2bw5LmCxvg7J/E5SH/ZZtJ/bRC7/s81cpD7rmziaHMjoly3vY41PAmmcS5lC+fdkpDcyDheAF7hPLw2AYT1hv00fhAZLfVrHt0u9J6/QCmAd1jyTLup8BY3L8q3Ecf" + 
                    "rKXdkT7aM4dgWNr9QDJRGWteXmHtcxbwYRp4jweTKR1jSTLur7zY3rca3FcJ4IXuP3hjgFrdKMUs9jZYXmA1FzLNHnuPEoye7iv1WTpbbfnWCG34T0UnVUN3sD5jZN5gqHfOx/laH6AdCUH" + 
                    "czRsNKCDSi3zuaad8jQ8yXJAJ142M1gDR7I5awA+Nd98sJvu69Z8gGTH6FK7sSbtt3H2hH602da1lrk6Pv2wejf8A1wOd0Dx7tDkaZeiPOBBQmL0DbyWQ7DtzXbNfUf/cJ53BCbI14MJ08R" + 
                    "ZLuY7KfeyFlqHWqYuNwU/kD4B3qXYs6F846m4LUMTaJIlEhLjZWAWh3MfeKJ/arwOreXReKv+d2DSLBf3TGuh41rL9MHarvABOB2uhvLDI8VdGfqNp5+C7ctHwRvgZZCIgbEz4BP068GT/z" + 
                    "9gBahLdKqFmli7bQsdh1qmv7o0F+xU/vfgr1TdBSUpNg/9euglcCK8E14JdupPxEAtDHyZo/Si+DnU+ee3plMLHdVa5ga8138Gh8MC+BE8Bs3J0XG/2eR8l3N5X+frEzFQWwPv58i9OB6F7" + 
                    "WprYdkDb1cLbZVUyrRh7ZdpDdD39S/hBLCGaE2x7HfzcCHzLoRPw1thS7AmmoiBGGgY2IXhEvDieUdjWgbLGii1UB8oNSeZMm5H9mHol2lb4l5wJJwNN4FtjmU/q0PbKG2rtM3yUNgNbMtM" + 
                    "xEBXBqxd1DFewkH/GLzY7DbyEUiMhgFrkVuBbYoF30drh82xlAn/AybRKgsZN5EmYmBaBuqYOP0W0GWwO1wJpRsSxcSQGdiQ/fFWuyRIhybNlaE5bmeCt+HVBHkL47ZdJmKgpwZG7auEvTj" + 
                    "4k1iJSfNXYDuW7XOJ5WvA83ALqCZIyxu32C1rkfaZ/K8mftli2UyKgb4YqFuN07ZM+9jZ9rUHXAuJwRqw50JzLXIbpvmEvjlsi7QGWU2SNzOeWmSzqYwP1ECdapxerP/UsHsYwyTN/p5q9o" + 
                    "d9OTTXIme12KztjXdCNUFaXghpi0RCYrgM1KXGaS3HvnibwQJ4DyR6Z2BtVrUtVJOk42u22IS1Rb9dU02S1iofbrFsJsXAUBqoQ+K05nMR+PU3k6ddT+yGlJiegTm8rJogLfuB1Opc+gXTq" + 
                    "wnSsk+5badMxMDIGqjDrfoneXdMmr+F/SFJEwldxOosY9tjNUna3LFui9fq1CfYzUnygRbLZlIMjLyBVrWEkT+oygHsS/kCsIazJ1wOiecbsB9kNUFa9im3tfXm+DUTmhOkT7nTO6HZVMZj" + 
                    "YAQNeOE/CD5c+JsR3P9+77Lft9bLhaCjZux5YNvjv8AR8OewISRiIAbG1IA16c+DycAf8E383sCrKf4zPAElWfoDJ34p4BR4F2wPq0AiBmKgRgZ251hNCn6tcq0aHXe7Q9XB++AnUJLlM5R" + 
                    "9aPZGaHVLzuREDMRAnQycycGaID5Tp4NucaxbM+0L8H9QEua9lD8LsyERAzEQA88a8BZzMZgoXvHslHr9WZnDPQCugJIsHX4f3g65BUdCIgZiYFkDb2LURHHjspPHfmwWR2gN26feJWE+RP" + 
                    "k0sFtRIgZiIAbaGjifOSaOj7VdYnxm2Da5F/hk3DbLkjDtLvR+SPsuEhIxEAOdDazL7MfBJGLfxHENuxIdCT+Hkix9Sn427AqJGIiBGOjawLtZ0kRyWdevGK0F7Upkv8pqV6I7GT8KTKaJG" + 
                    "IiBGJiyAROmifPgKb9yeF/g7fZ8aO5K9C2mpSvR8L5v2bMYGAkD3pp7i+6t+jojscedd9KuRKdCuhJ19pS5MRADMzBwBK+1tjnK3xSyq9DboLkr0ZVMS1ciJCRiIAZ6a6Dcyu7X29UOZG2z" + 
                    "2YpdieycbvKX0pXImmciBmIgBnpuwORisrkfRqWDt12J/Lm7Vl2JbNNMVyIkJGIgBvpn4LOs2sR5Rv820dM1b8La/EGNUrssXYl8ap6IgRiIgYEYuIitmIT+dCBbm9lGtuPl/kdG9/eHYH/" + 
                    "MdCVCQiIGYmCwBi5hcyaiXQa72Slv7bW8ojwln6C83pTXkBfMxIA/N+g5cjJ4ziRioNYGvBBMnA6HNQ5kx5aA+/k1aPXvcJmc6LGBarK8m3Xrv7BVj7eV1cXASBmwFuHFcA94oQxb+M2epe" + 
                    "A+ngTDuI/s1thEp2TpOWL7sudM3oexectzINMx4AXgBWFi8oIYlvDJub9O5H7ZOf8wSPTHQDVZlnNB75Jk2R/nWesYGBi22/XVcXoBeOH6bSb/w2aitwaSLHvrM2uroYFyu/4bjv1zsM1yd" + 
                    "OBT8mvApHk/5BeLkNCDMFH6vn4QzoPypYdqzdIP0NyGIyERA90Y8KLaB8pF5PAm+FuYDYOKzdjQ7eD274ItITE9AyvyslfBR8Haux9Ceq0ywXiSJRISMTBdAyZP+3KeDr+FcoH5YMZ/HfEB" + 
                    "6GefyR1Z/73gdm+AjSDRvQG/9eWXAI6Gb4NfOy3vYRnezTR/Wu+9MBcSMRADPTTg/975C/hXeBTKhfcU5YvgHbAm9Cr8ibdHwO1cAmtBorMB24FfA5+Ey+ExKO9TGd7BtC/BQTAHEjEQAwM" + 
                    "yYIK0H+XFYOIsF6WJ7hzYG0y0041DeOHT4Hq/AitB4vkG1mGS/+rDNuir4Uko74VD7wxuBnsiHAAbQyIGYmAIDGzAPhwKV4EXarlwvbX/R9gNvOXvNo5lwbKOT3X7opostz7H6a9V2W/yR1" + 
                    "A+XIovx3/cmP8mhi6fiIGhNzCVBDH0BzONHZzNa7xll20qr19E+T5Y2IYHmG6t8otwMNhH03+O9iWoc1hDtI15D9gdtobqOWZt3wR6BVwJ1jptx0zEwEgZqJ7UI7XjfdjZbVmnCXQH2HOS9" + 
                    "Xux625tMBmcBT7MWNjAxFqH8IPHBFkS5eZNB/0449dBSZTXUrYdMxEDI20gibP12/ciJs/pgAmzU5hYF3ZgVBPrFhxTNVHOYrwaDzPyAyiJ8nrKT1YXSDkGxsFAEufU3kX7ZFqznAN+je84" + 
                    "8MnwnArWwnwI0ikmS6zOXxVWaQy7LXe7XLv1dnq9TRPOr8ZiRmwrLonyRso2WyRiYKwNJHF2//buyqIXwovAW859wAdKrcJl5rShm8Taap3DMM3fD70bSqL0CfjvhmHHsg8xMEgDSZzd2d6" + 
                    "fxc6G1eCb8Haw/W660SmxrstK7ThvzW1JA293S9lhdXyq5Zm83vbcRAzEQAxMauAwljCJWbOyf+EKkIiBGIiBGGhhwNr4iWDCXAp+5z0RAzEQAzHQxoAPT84Fk6a3wgdCIgZiIAZioI2B9Z" + 
                    "g+ASZN/z+Q/ycoEQMxEAMx0MaAXYu+ACbNX8J2kIiBGIiBGOhgwO+qmzS/A5t0WC6zYiAGYiAGMGC/TJPmE/BKSMRADMRADHQwYL9Jf9TDxHl4h+UyKwZiIAZiAAN2O7oUTJoO86UAJCRiI" + 
                    "AZioJOBjzLTpOk/ebPmmYiBGIiBGOhgwLZM2zRNnLZxJmIgBmIgBjoYsOvRLWDS9KuUiRiIgRiIgUkMlK5HJk+TaCIGYiAGYqCDgXQ96iAns2IgBmKg2UC6HjUbyXgMxEAMdDCQrkcd5GRW" + 
                    "DMRADLQy8BEmputRKzOZFgMxEAMtDKTrUQspmRQDMRAD7Qyk61E7M5keAzEQA20MpOtRGzGZHAMxEAOtDKTrUSsrmRYDMRADbQxUux75YCgRA3U2sBMHf1ydBeTYJzeQrkeTO8oS9TCwKod" + 
                    "pwnwa8rsM9XjPp32U6Xo0bXV54RgZsJZ5K5gwTZzHg4k0MQMD4/rbk3Y9ug48QfaFb0FitAz4n0bXqrB2pTzZ9FbLei74z/fE/yf1i6Zhmeb8cQiP91g4AlaEn8HB4HWRmKGBlWb4+mF8uR" + 
                    "8G88ETx6fpSZpI6HN0k+RaJTMTYKvpTlu5D/u8Jut8KWzVYd2PMK9dUh325DqXfZ/XYHuGW8IzcAIcA0sg0QMD41jj3AUv18CNsCs8DompG/BDdUPwAVth40q5TDPJvQR6HU+xQpPYw42h5" + 
                    "UKrad0suwbr+MMG/jM+y81Dk/lk4baGJblamzwQ9oa3QjXOYOQsSC2zaqUH5XGscb6l4eV7DJM0n3+SvJBJJek1D6uJcQOW6/aD9VGWfRJaJbRupzUnPtfX63CdD8LNHVa8LvOak2nzuMnV" + 
                    "Wmunmut/Mv8mOB9MXLYx9ipKspzHCr39LuFPJP4UJhrcxjDRBwPdXhh92HRfVunx3A2e6H8C10IdYjUO0lpfNfE1J8UybhNGN/EMC/kP7H7dhkWV6Q9RrlN0Sq7WYtcHa+sl7qHwDZhpEi0" + 
                    "J05qlNcwSX6EwAeeA71uizwbGLXHugi9v072Nmg29/JRndQONFdiatb6S8By2S4zrTWHPTHImw2ria5Uc/R9MS6ew3iz6ewNeVzuDCe7N4Ad5Cc9NE+hUkmhJmB/ndZs3VnQxw69DkmVDyC" + 
                    "AH45Y4T0ae/3ztFPjYIEVOYVu2CVaTYbVcTYzWWLxguglvQe+FagJslxjTfNGN0d4tM90kOpddmNdgW4Zbg3EHfBqSMLWxnGLcEucleHw9nAufh+th1SZ8Atw8rTrez/m2KbutbsLa8v1QT" + 
                    "YbV8qLKvMXdrDDLLHcDJYnaDi/VmuhpjN8FO8IWsANUw9qltcwkzKqV5VQet8RpY/2ty8llt5v1FvgRqCa+akIsZWuQPllOjKeBuRzWfPB23vbp5ge1NzDtdphocBvDxJAYGLfEWf1E99bm" + 
                    "NfA0LAFvZx22Y1Dzkwx5E2oYJsp5sB/sBjbZNMdFTJAJSKJEQiIGYqC+Bmx2aYVPwK1VngTbQSIGYiAGYqBhoJo0TZQ+Ud8f1mnMzyAGYiAGYiAGYiAGYiAGYiAGYiAGYiAGYiAGYiAGYiA" + 
                    "GYiAGYiAGYiAGYiAGYiAGYiAGYiAGYiAGYiAGYiAGYiAGYiAGYiAGYiAGYiAGYiAGYiAGYiAGRs3A/wMpp3D77pc5JQAAAABJRU5ErkJggg==";

            Image image = null;
            try {
                byte[] decoded = Base64.getDecoder().decode(encodedSignature);
                image = Image.getInstance(decoded);
            } catch (Exception e) {
                e.printStackTrace();
            }
            image.scaleAbsoluteHeight(200);
            image.scaleAbsoluteWidth(200);
            image.scaleToFit(new Rectangle(200, 200));
            image.setAbsolutePosition(100, 600);
            PdfContentByte canvas = resultandPdfStamper.getOverContent(1);
            canvas.addImage(image, Boolean.TRUE);
            resultandPdfStamper.close();
            resultantPdfReader.close();
        }
    }
}
