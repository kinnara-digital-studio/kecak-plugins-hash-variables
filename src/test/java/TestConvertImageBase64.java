import com.kinnarastudio.kecakplugins.hashvariables.ConvertImageBase64;
import org.junit.Assert;
import org.junit.Test;

public class TestConvertImageBase64 {
    ConvertImageBase64 plugin = new ConvertImageBase64();

    @Test
    public void testEmptyInput() {
        String result = plugin.processHashVariable("");
        Assert.assertEquals("", result);

        result = plugin.processHashVariable(null);
        Assert.assertEquals("", result);
    }

    @Test
    public void testPrefixAndAvailableSyntax() {
        Assert.assertEquals("convertImageBase64", plugin.getPrefix());
        Assert.assertTrue(plugin.availableSyntax().contains("convertImageBase64.IMAGE_URL"));
    }
}

