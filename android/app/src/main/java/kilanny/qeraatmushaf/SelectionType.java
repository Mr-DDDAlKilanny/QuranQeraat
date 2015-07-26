package kilanny.qeraatmushaf;

/**
 * Created by ibraheem on 5/27/2015.
 */

public enum SelectionType {
    Farsh(1),
    Hamz(2),
    Edgham(3),
    Emalah(4),
    Naql(5),
    Mad(6),
    Sakt(7);

    private final int value;
    private SelectionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static SelectionType fromValue(int t) {
        switch (t) {
            case 1:
                return Farsh;
            case 2:
                return Hamz;
            case 3:
                return Edgham;
            case 4:
                return Emalah;
            case 5:
                return Naql;
            case 6:
                return Mad;
            case 7:
                return Sakt;
            default:
                throw new IllegalArgumentException();
        }
    }
}

