/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qurandwnld;

import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

class Ayah {
    int ayahIndex;
    ArrayList<Rectangle2D> rects;
}

enum SelectionType {
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

class Shahed {
    String part1, part2;
    int id;
}

class RewayahSelectionList extends ArrayList<Rewayah> {

    @Override
    public boolean add(Rewayah e) {
        for (int i = 0; i < size(); ++i)
            if (get(i) == e)
                return false;
        return super.add(e); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void add(int index, Rewayah element) {
        for (int i = 0; i < size(); ++i)
            if (get(i) == element)
                return;
        super.add(index, element); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean addAll(Collection<? extends Rewayah> c) {
        for (Rewayah r : c)
            for (int i = 0; i < size(); ++i)
                if (get(i) == r)
                    return false;
        return super.addAll(c); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean addAll(int index, Collection<? extends Rewayah> c) {
        for (Rewayah r : c)
            for (int i = 0; i < size(); ++i)
                if (get(i) == r)
                    return false;
        return super.addAll(index, c); //To change body of generated methods, choose Tools | Templates.
    }
    
    public static RewayahSelectionList fromString(String s) {
        RewayahSelectionList list = new RewayahSelectionList();
        String arr[] = s.split(";");
        Pattern p = Pattern.compile("\\d+");
        for (String string : arr)
            if (p.matcher(string).matches()) {
                list.add(Rewayah.getByCombinedCode(string + ".1"));
                list.add(Rewayah.getByCombinedCode(string + ".2"));
            } else if (!string.isEmpty())
                list.add(Rewayah.getByCombinedCode(string));
        return list;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        Collections.sort(this, (Rewayah o1, Rewayah o2) ->
                o1.getCombinedCode().compareTo(o2.getCombinedCode()));
        for (int i = 0; i < size(); ++i) {
            if (get(i).code.equals("1") && i + 1 < size() &&
                    get(i).qeraah == get(i + 1).qeraah)
                b.append(get(i++).qeraah.code).append(";");
            else
                b.append(get(i).getCombinedCode()).append(";");
        }
        return b.toString();
    }
}

/**
 * MawdeaKhlafGroup
 * @author Yasser
 */
class SelectionDetail {
    long id;
    ArrayList<Shahed> shatibiyyah, dorrah;
    SelectionType type;
    
    RewayahSelectionList rewayaat;
    String descr;
    final ArrayList<Object> lastGroupingResult = new ArrayList<>();
    
    private boolean applies(List<Rewayah> l, Rewayah r) {
        return l.stream().anyMatch((Rewayah s) -> {
           return s == r; 
        });
    }
    
    private void remove(List<Rewayah> l, Rewayah r) {
        Object[] rem = l.stream().filter((s) -> (s == r)).toArray();
        l.removeAll(Arrays.asList(rem));
    }
    
    private boolean applies(List<Rewayah> l, Qeraah q) {
        Object[] o = Arrays.stream(Rewayah.array).filter((Rewayah t) -> {
            return t.qeraah == q;
        }).toArray();
        return Arrays.stream(o).allMatch((Object s) -> {
            return applies(l, (Rewayah) s);
        });
    }
    
    private void remove(List<Rewayah> l, Qeraah r) {
        Object[] rem = l.stream().filter((s) -> (s.qeraah == r)).toArray();
        l.removeAll(Arrays.asList(rem));
    }
    
    private boolean applies(List<Rewayah> l, QeraahGroup g) {
        return Arrays.stream(g.qeraat).allMatch((Qeraah q) -> {
            return applies(l, q);
        });
    }
    
    private void remove(List<Rewayah> l, QeraahGroup r) {
        for (Qeraah q : r.qeraat) {
            remove(l, q);
        }
    }
    
    private StringBuilder tryGroup() {
        // 1. try group by ramz, then qeraah (if no kholf)
        // 2. sort qeraat, rewayaat
        StringBuilder ret = new StringBuilder();
        lastGroupingResult.clear();
        if (rewayaat == null)
            return ret;
        List<Rewayah> l = new LinkedList<>(rewayaat);
        for (QeraahGroup g : QeraahGroup.array) {
            if (applies(l, g)) {
                lastGroupingResult.add(g);
                remove(l, g);
                if (ret.length() > 0)
                    ret.append(" و");
                if (g == QeraahGroup.KOFI)
                    ret.append("الكوفيون");
                else
                    ret.append("أهل ").append(g.name);
            }
        }
        for (Qeraah g : Qeraah.array) {
            if (applies(l, g)) {
                remove(l, g);
                lastGroupingResult.add(g);
                if (ret.length() > 0)
                    ret.append(" و");
                ret.append(g.toString());
            }
        }
        for (Rewayah g : Rewayah.array) {
            if (applies(l, g)) {
                remove(l, g);
                lastGroupingResult.add(g);
                if (ret.length() > 0)
                    ret.append(" و");
                ret.append(g.getDisplayName());
            }
        }
        for (Rewayah s : l) {
            if (ret.length() > 0)
                ret.append(" و");
            lastGroupingResult.add(s);
            ret.append(s.getDisplayName());
        }
        return ret;
    }

    @Override
    public String toString() {
        return "قرأ " + tryGroup() + " بـ " + descr;
    }
}

/**
 * MawdeaKhlaf
 * @author Yasser
 */
class Selection {
    long id;
    String ayahWord;
    int ayahIndex;
    ArrayList<SelectionDetail> details;
    Rectangle2D naturalRect, scaledRect;

    public Shape getShape() {
        return scaledRect;
    }
}

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
    public static final Rewayah QALOON = new Rewayah(Qeraah.NAFE3, "قالون", "1", true);
    public static final Rewayah WARSH = new Rewayah(Qeraah.NAFE3, "ورش", "2", true);
    public static final Rewayah BAZZI = new Rewayah(Qeraah.IBN_KATHEER, "البزي", "1", true);
    public static final Rewayah QONBOL = new Rewayah(Qeraah.IBN_KATHEER, "قنبل", "2", true);
    public static final Rewayah DORI_ABO_AMR = new Rewayah(Qeraah.ABO_AMRE, "الدوري", "1", false);
    public static final Rewayah SOSI = new Rewayah(Qeraah.ABO_AMRE, "السوسي", "2", true);
    public static final Rewayah IBN_THAKWAN = new Rewayah(Qeraah.IBN_AMER, "ابن ذكوان", "1", true);
    public static final Rewayah HESHAM = new Rewayah(Qeraah.IBN_AMER, "هشام", "2", true);
    public static final Rewayah SHO3BAH = new Rewayah(Qeraah.AASEM, "شعبة", "1", true);
    public static final Rewayah HAFS = new Rewayah(Qeraah.AASEM, "حفص", "2", true);
    public static final Rewayah KHALAF = new Rewayah(Qeraah.HAMZAH, "خلف", "1", false);
    public static final Rewayah KHALLAD = new Rewayah(Qeraah.HAMZAH, "خلاد", "2", true);
    public static final Rewayah ABO_ALHARETH = new Rewayah(Qeraah.ALKESA2E, "أبو الحارث", "1", true);
    public static final Rewayah DORI_KESA2E = new Rewayah(Qeraah.ALKESA2E, "الدوري", "2", false);
    public static final Rewayah IBN_WARDAN = new Rewayah(Qeraah.ABO_JA3FAR, "ابن وردان", "1", true);
    public static final Rewayah IBN_JAMMAZ = new Rewayah(Qeraah.ABO_JA3FAR, "ابن جماز", "2", true);
    public static final Rewayah ROWISE = new Rewayah(Qeraah.YA3QOB, "رويس", "1", true);
    public static final Rewayah ROW7 = new Rewayah(Qeraah.YA3QOB, "روح", "2", true);
    public static final Rewayah ES7AQ = new Rewayah(Qeraah.KHALAF, "إسحاق", "1", true);
    public static final Rewayah EDREES = new Rewayah(Qeraah.KHALAF, "إدريس", "2", true);
    
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
        throw new IllegalArgumentException(c);
    }
    
    public final String rewayah, code;
    public final Qeraah qeraah;
    public final boolean isRewayahEnough;
    
    private Rewayah(Qeraah q, String rewayah, String code, boolean isRewayahEnough) {
        this.qeraah = q;
        this.isRewayahEnough = isRewayahEnough;
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
    
    public String getDisplayName() {
        return isRewayahEnough ? rewayah : getCombinedName();
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

    @Override
    public String toString() {
        return "(" + name + ")";
    }
    
}

class MatnBab {
    int beit;
    String bab;

    @Override
    public String toString() {
        return bab;
    }
}