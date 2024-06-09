package specialScanner;

import java.util.Scanner;

public class SpecializedStringScanner extends SpecializedScanner<String>{
//    public SpecializedStringScanner(Scanner s) {
//        super(s);
//    }

    @Override
    public boolean hasNext() {
        return scanner.hasNext();
    }

    @Override
    public String next() {
        return scanner.next();
    }
}
