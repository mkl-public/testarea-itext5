package mkl.testarea.itext5.meta;

import java.lang.reflect.Field;

import org.junit.Test;

import com.itextpdf.text.log.DefaultCounter;

/**
 * This test shows the {@link DefaultCounter} message.
 * 
 * @author mkl
 */
public class ShowDefaultCounterMessage
{
    @Test
    public void retrieveAndShowDefaultCounterMessage() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
    {
        Field messageField = DefaultCounter.class.getDeclaredField("message");
        messageField.setAccessible(true);
        byte[] messageBytes = (byte[]) messageField.get(null);
        System.out.println(new String(messageBytes));
    }
}
