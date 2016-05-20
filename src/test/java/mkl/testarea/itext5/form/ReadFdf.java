// $Id$
package mkl.testarea.itext5.form;

import java.io.IOException;
import java.io.InputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.itextpdf.text.pdf.FdfReader;

/**
 * @author mkl
 */
public class ReadFdf
{
    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
    }

    /**
     * <a href="http://stackoverflow.com/questions/37161133/failed-to-add-fdf-with-attachment-annotation">
     * Failed to add fdf with attachment annotation
     * </a>
     * <br/>
     * itext-SO.fdf <i>received via mail from the OP</i>
     * 
     * <p>
     * Indeed, the exception occurs, and the reason is two-fold, both the FDF is
     * broken and the `FdfReader` works incorrectly.
     * </p>
     */
    @Test
    public void testReadFdfFabienLevalois() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("itext-SO.fdf")   )
        {
            FdfReader fdfReader = new FdfReader(resource);
            System.out.println(fdfReader.getFields().keySet());
        }
    }
}
