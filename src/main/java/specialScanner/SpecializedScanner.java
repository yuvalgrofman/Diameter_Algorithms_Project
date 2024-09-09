package specialScanner;

import java.util.Scanner;

public abstract class SpecializedScanner<T> {
    protected Scanner scanner;

//    public SpecializedScanner(Scanner scanner) {
//        this.scanner = scanner;
//    }
    public SpecializedScanner() {
        this.scanner = null;
    }

    public void setScanner(Scanner scanner) {
        this.scanner = scanner;
    }

    public abstract boolean hasNext();
    public abstract T next();
}
