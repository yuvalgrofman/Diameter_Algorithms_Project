package specialScanner;

import java.util.Scanner;

public class SpecializedIntegerScanner extends SpecializedScanner<Integer>{
//    public SpecializedIntegerScanner(Scanner s) {
//        super(s);
//    }
    @Override
    public boolean hasNext() {
        return scanner.hasNextInt();
    }

    @Override
    public Integer next() {
        return scanner.nextInt();
    }
}
