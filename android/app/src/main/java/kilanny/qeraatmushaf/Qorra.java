package kilanny.qeraatmushaf;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by ibraheem on 7/22/2015.
 */
final class Qeraah {
    public static final Qeraah NAFE3 = new Qeraah("نافع", "1");
    public static final Qeraah IBN_KATHEER = new Qeraah("ابن كثير", "2");
    public static final Qeraah ABO_AMRE = new Qeraah("أبو عمرو", "3");
    public static final Qeraah IBN_AMER = new Qeraah("ابن عامر", "4");
    public static final Qeraah AASEM = new Qeraah("عاصم", "5");
    public static final Qeraah HAMZAH = new Qeraah("حمزة", "6");
    public static final Qeraah ALKESA2E = new Qeraah("الكسائي", "7");
    public static final Qeraah ABO_JA3FAR = new Qeraah("أبو جعفر", "8");
    public static final Qeraah YA3QOB = new Qeraah("يعقوب", "9");
    public static final Qeraah KHALAF = new Qeraah("خلف العاشر", "10");

    public static final Qeraah[] array = {Qeraah.NAFE3,Qeraah.IBN_KATHEER,Qeraah.ABO_AMRE,Qeraah.IBN_AMER,
            Qeraah.AASEM,Qeraah.HAMZAH,Qeraah.ALKESA2E,Qeraah.ABO_JA3FAR,Qeraah.YA3QOB,Qeraah.KHALAF};

    public final String qeraah, code;

    private Qeraah(String q, String code) {
        this.qeraah = q;
        this.code = code;
    }

    @Override
    public String toString() {
        return qeraah;
    }
}

final class Rewayah {
    public static final Rewayah QALOON = new Rewayah(Qeraah.NAFE3, "قالون", "1");
    public static final Rewayah WARSH = new Rewayah(Qeraah.NAFE3, "ورش", "2");
    public static final Rewayah BAZZI = new Rewayah(Qeraah.IBN_KATHEER, "البزي", "1");
    public static final Rewayah QONBOL = new Rewayah(Qeraah.IBN_KATHEER, "قنبل", "2");
    public static final Rewayah DORI_ABO_AMR = new Rewayah(Qeraah.ABO_AMRE, "الدوري", "1");
    public static final Rewayah SOSI = new Rewayah(Qeraah.ABO_AMRE, "السوسي", "2");
    public static final Rewayah IBN_THAKWAN = new Rewayah(Qeraah.IBN_AMER, "ابن ذكوان", "1");
    public static final Rewayah HESHAM = new Rewayah(Qeraah.IBN_AMER, "هشام", "2");
    public static final Rewayah SHO3BAH = new Rewayah(Qeraah.AASEM, "شعبة", "1");
    public static final Rewayah HAFS = new Rewayah(Qeraah.AASEM, "حفص", "2");
    public static final Rewayah KHALAF = new Rewayah(Qeraah.HAMZAH, "خلف", "1");
    public static final Rewayah KHALLAD = new Rewayah(Qeraah.HAMZAH, "خلاد", "2");
    public static final Rewayah ABO_ALHARETH = new Rewayah(Qeraah.ALKESA2E, "أبو الحارث", "1");
    public static final Rewayah DORI_KESA2E = new Rewayah(Qeraah.ALKESA2E, "الدوري", "2");
    public static final Rewayah IBN_WARDAN = new Rewayah(Qeraah.ABO_JA3FAR, "ابن وردان", "1");
    public static final Rewayah IBN_JAMMAZ = new Rewayah(Qeraah.ABO_JA3FAR, "ابن جماز", "2");
    public static final Rewayah ROWISE = new Rewayah(Qeraah.YA3QOB, "رويس", "1");
    public static final Rewayah ROW7 = new Rewayah(Qeraah.YA3QOB, "روح", "2");
    public static final Rewayah ES7AQ = new Rewayah(Qeraah.KHALAF, "إسحاق", "1");
    public static final Rewayah EDREES = new Rewayah(Qeraah.KHALAF, "إدريس", "2");

    public static final Rewayah[] array = {
            QALOON,WARSH,BAZZI,QONBOL,DORI_ABO_AMR,SOSI,HESHAM,IBN_THAKWAN,
            SHO3BAH,HAFS,KHALAF,KHALLAD,ABO_ALHARETH,DORI_KESA2E,
            IBN_WARDAN,IBN_JAMMAZ,ROWISE,ROW7,ES7AQ,EDREES
    };

    public static boolean toStringUsesCode = false;

    public static Rewayah getByCombinedCode(String c) {
        for (Rewayah r : array) {
            if (r.getCombinedCode().equals(c))
                return r;
        }
        throw new IllegalArgumentException();
    }

    public final String rewayah, code;
    public final Qeraah qeraah;

    private Rewayah(Qeraah q, String rewayah, String code) {
        this.qeraah = q;
        this.rewayah = rewayah;
        this.code = code;
    }

    public String getCombinedCode() {
        return qeraah.code + "." + code;
    }

    public String getCombinedName() {
        return rewayah + " عن " + (qeraah.qeraah.startsWith("أبو") ?
                "أبي" + qeraah.qeraah.substring(3) : qeraah.qeraah);
    }

    @Override
    public String toString() {
        return toStringUsesCode ? getCombinedCode() : getCombinedName();
    }
}

final class QeraahGroup {
    public static final QeraahGroup SAMA = new QeraahGroup("سما", new Qeraah[]{Qeraah.NAFE3,Qeraah.IBN_KATHEER,Qeraah.ABO_AMRE});
    public static final QeraahGroup NAFAR = new QeraahGroup("نفر", new Qeraah[]{Qeraah.IBN_KATHEER,Qeraah.ABO_AMRE,Qeraah.IBN_AMER});
    public static final QeraahGroup HAQQ = new QeraahGroup("حق", new Qeraah[]{Qeraah.IBN_KATHEER,Qeraah.ABO_AMRE});
    public static final QeraahGroup AMMA = new QeraahGroup("عم", new Qeraah[]{Qeraah.NAFE3,Qeraah.IBN_AMER});
    public static final QeraahGroup THAL = new QeraahGroup("ذ", new Qeraah[]{Qeraah.AASEM,Qeraah.HAMZAH,Qeraah.ALKESA2E,Qeraah.IBN_AMER});
    public static final QeraahGroup ZHAA = new QeraahGroup("ظ", new Qeraah[]{Qeraah.AASEM,Qeraah.HAMZAH,Qeraah.ALKESA2E,Qeraah.IBN_KATHEER});
    public static final QeraahGroup KHAA = new QeraahGroup("خ", new Qeraah[]{Qeraah.IBN_KATHEER,Qeraah.ABO_AMRE,Qeraah.IBN_AMER,Qeraah.AASEM,Qeraah.HAMZAH,Qeraah.ALKESA2E});
    public static final QeraahGroup KOFI = new QeraahGroup("الكوفي", new Qeraah[]{Qeraah.AASEM,Qeraah.HAMZAH,Qeraah.ALKESA2E});
    public static final QeraahGroup GHINE = new QeraahGroup("غ", new Qeraah[]{Qeraah.AASEM,Qeraah.HAMZAH,Qeraah.ALKESA2E,Qeraah.ABO_AMRE});

    public static final QeraahGroup[] array = {SAMA,NAFAR,HAQQ,AMMA,THAL,ZHAA,KHAA,KOFI,GHINE};

    public final String name;
    public final Qeraah[] qeraat;

    private QeraahGroup(String name, Qeraah[] qeraat) {
        this.name = name;
        this.qeraat = qeraat;
    }
}

class RewayahSelection {
    Rewayah r;
    boolean kholf;

    @Override
    public String toString() {
        return r.getCombinedName() + (kholf ? " ؟" : "");
    }
}

class RewayahSelectionGroup {
    List<RewayahSelection> rewayaat;
    String descr;

    private boolean applies(List<RewayahSelection> l, Rewayah r) {
        for (RewayahSelection s : l) {
            if (s.r == r && !s.kholf)
                return true;
        }
        return false;
    }

    private void remove(List<RewayahSelection> l, Rewayah r) {
        for (int i = l.size() - 1; i >= 0; --i) {
            if (l.get(i).r == r) {
                l.remove(i);
            }
        }
    }

    private boolean applies(List<RewayahSelection> l, Qeraah q) {
        for (Rewayah t : Rewayah.array) {
            if (t.qeraah == q) {
                if (!applies(l, t))
                    return false;
            }
        }
        return true;
    }

    private void remove(List<RewayahSelection> l, Qeraah r) {
        for (int i = l.size() - 1; i >= 0; --i) {
            if (l.get(i).r.qeraah == r) {
                l.remove(i);
            }
        }
    }

    private boolean applies(List<RewayahSelection> l, QeraahGroup g) {
        for (Qeraah q : g.qeraat)
            if (!applies(l, q))
                return false;
        return true;
    }

    private void remove(List<RewayahSelection> l, QeraahGroup r) {
        for (Qeraah q : r.qeraat) {
            remove(l, q);
        }
    }

    private StringBuilder tryGroup(boolean useGroups, boolean displayGroupMembers) {
        // 1. try group by ramz, then qeraah (if no kholf)
        // 2. sort qeraat, rewayaat
        StringBuilder ret = new StringBuilder();
        List<RewayahSelection> l = new LinkedList<>(rewayaat);
        if (useGroups) {
            for (QeraahGroup g : QeraahGroup.array) {
                if (applies(l, g)) {
                    remove(l, g);
                    if (ret.length() > 0)
                        ret.append(" و");
                    if (g == QeraahGroup.KOFI)
                        ret.append("الكوفيون");
                    else
                        ret.append("أهل ").append(g.name);
                    if (displayGroupMembers) {
                        ret.append("(").append(g.qeraat[0].toString());
                        for (int i = 1; i < g.qeraat.length; ++i) {
                            ret.append(" و").append(g.qeraat[i].toString());
                        }
                        ret.append(")");
                    }
                }
            }
        }
        for (Qeraah g : Qeraah.array) {
            if (applies(l, g)) {
                remove(l, g);
                if (ret.length() > 0)
                    ret.append(" و");
                ret.append(g.toString());
            }
        }
        for (Rewayah g : Rewayah.array) {
            if (applies(l, g)) {
                remove(l, g);
                if (ret.length() > 0)
                    ret.append(" و");
                ret.append(g.toString());
            }
        }
        for (RewayahSelection s : l) {
            if (ret.length() > 0)
                ret.append(" و");
            ret.append(s.r.toString());
            if (s.kholf)
                ret.append(" بخلف عنه");
        }
        return ret;
    }

    public String getReadableDescr(boolean useGroups, boolean displayGroupMembers) {
        return "• قرأ " + tryGroup(useGroups, displayGroupMembers) + " بـ " + descr;
    }
}