import org.junit.Test;

public class TestMatcher {
    @Test
    public void matcher() {
        System.out.println("d-123-e".matches("d-\\d+-e"));
    }
}
