import com.kinnarastudio.kecakplugins.hashvariables.CoalesceHashVariable;
import org.joget.apps.app.model.DefaultHashVariablePlugin;

public class TestCoalesceHashVariable {
    DefaultHashVariablePlugin hashVariablePlugin = new CoalesceHashVariable();

    @org.junit.Test
    public void test() {
        String result = hashVariablePlugin.processHashVariable("[c][b]");
        System.out.println(result);
    }
}
