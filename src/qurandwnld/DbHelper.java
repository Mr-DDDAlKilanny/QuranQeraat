/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qurandwnld;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author Yasser
 */
public final class DbHelper {

    public static final Connection CONNECTION;

    static {
        Connection tmp1 = null;
        try {
            Class.forName("org.sqlite.JDBC");
            tmp1 = DriverManager.getConnection("jdbc:sqlite:mushaf_alqeraat.db");
            tmp1.setAutoCommit(false);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            System.exit(0);
        }
        CONNECTION = tmp1;
    }

    public static void close() {
        try {
            CONNECTION.close();
        } catch (SQLException ex) {
            Logger.getLogger(DbHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static long getLastInsertRowId() {
        String sql = "SELECT last_insert_rowid()";
        long ret = -1;
        try {
            try (Statement stmnt = CONNECTION.createStatement();
                    ResultSet res = stmnt.executeQuery(sql)) {
                if (res.next()) {
                    ret = res.getLong(1);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DbHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    private static void commit() throws SQLException {
        CONNECTION.commit();
    }

    private static void rollback() {
        try {
            CONNECTION.rollback();
        } catch (SQLException ex) {
            Logger.getLogger(DbHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static Point getAyah(int ayahIndex) {
        try {
            try (PreparedStatement stmnt = CONNECTION.prepareStatement(
                    "SELECT sura, ayah FROM combined_mushaf WHERE ayah_index=?",
                    Statement.RETURN_GENERATED_KEYS)) {
                stmnt.setInt(1, ayahIndex);
                try (ResultSet set = stmnt.executeQuery()) {
                    if (set.next()) {
                        return new Point(set.getInt(1), set.getInt(2));
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    static int getAyahIndex(int surah, int ayah) {
        try {
            try (PreparedStatement stmnt = CONNECTION.prepareStatement(
                    "SELECT ayah_index FROM combined_mushaf WHERE sura=? AND ayah=?",
                    Statement.RETURN_GENERATED_KEYS)) {
                stmnt.setInt(1, surah);
                stmnt.setInt(2, ayah);
                try (ResultSet set = stmnt.executeQuery()) {
                    if (set.next()) {
                        return (int) set.getLong(1);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    static boolean updateSelection(Selection s, PaintSurface surface) {
        Selection original = new Selection();
        original.id = s.id;
        fillSelection(original);
        try {
            String fields[] = (WriteJFrame1.isMadinaMushaf
                    ? "x1, y1, x2, y2" : "x1_shmrl, y1_shmrl, x2_shmrl, y2_shmrl")
                    .split(",");
            try (PreparedStatement stmnt = CONNECTION.prepareStatement(
                    String.format("UPDATE MawdeaKhlaf SET AyahIndex=?, "
                            + "%s=?, %s=?, %s=?, %s=?, AyahWord=? WHERE _ID=?",
                            fields[0], fields[1], fields[2], fields[3]))) {
                stmnt.setInt(1, s.ayahIndex);
                Rectangle2D.Float rect = surface.getImageRectFromScaled(
                        WriteJFrame1.pageSize,
                        (Rectangle2D.Float) s.scaledRect);
                stmnt.setDouble(2, rect.getX());
                stmnt.setDouble(3, rect.getY());
                stmnt.setDouble(4, rect.getWidth());
                stmnt.setDouble(5, rect.getHeight());
                stmnt.setString(6, s.ayahWord);
                stmnt.setLong(7, s.id);
                stmnt.executeUpdate();
            }
            //1) First, delete deleted details
            List<SelectionDetail> deleted = original.details.stream()
                    .filter(k -> !s.details.stream().anyMatch(j -> j.id == k.id))
                    .collect(Collectors.toList());
            for (SelectionDetail d : deleted) {
                try (Statement stmnt = CONNECTION.createStatement()) {
                    stmnt.executeUpdate(String.format("DELETE FROM DorrahShahed \n"
                            + "WHERE MawdeaKhlafGroupID = %d", d.id));
                    stmnt.executeUpdate(String.format("DELETE FROM ShatibiyyahShahed \n"
                            + "WHERE MawdeaKhlafGroupID = %d", d.id));
                    stmnt.executeUpdate("DELETE FROM MawdeaKhlafGroup WHERE _ID = " + d.id);
                }
            }
            //2) add new details
            List<SelectionDetail> added = s.details.stream()
                    .filter(k -> !original.details.stream().anyMatch(j -> j.id == k.id))
                    .collect(Collectors.toList());
            for (SelectionDetail d : added) {
                insertSelectionDetail(d, s.id);
            }
            //3) Update the rest
            List<SelectionDetail> updated = s.details.stream()
                    .filter(k -> original.details.stream().anyMatch(j -> j.id == k.id))
                    .collect(Collectors.toList());
            for (SelectionDetail d : updated) {
                try (PreparedStatement stmnt = CONNECTION.prepareStatement(
                        "UPDATE MawdeaKhlafGroup SET KhlafType=?, Descr=?, Rewayat=? WHERE _ID=?",
                        Statement.RETURN_GENERATED_KEYS)) {
                    stmnt.setLong(1, d.type.getValue());
                    stmnt.setString(2, d.descr);
                    stmnt.setString(3, d.rewayaat.toString());
                    stmnt.setLong(4, d.id);
                    stmnt.execute();
                }
                try (Statement stmnt = CONNECTION.createStatement()) {
                    stmnt.executeUpdate(String.format("DELETE FROM DorrahShahed \n"
                            + "WHERE MawdeaKhlafGroupID = %d", d.id));
                    stmnt.executeUpdate(String.format("DELETE FROM ShatibiyyahShahed \n"
                            + "WHERE MawdeaKhlafGroupID = %d", d.id));
                }
                insertShaheds(d);
            }
            commit();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(DbHelper.class.getName()).log(Level.SEVERE, null, ex);
            rollback();
            return false;
        }
    }
    
    static long insertSelection(Selection s, PaintSurface surface) {
        try {
            String fields = WriteJFrame1.isMadinaMushaf
                    ? "x1, y1, x2, y2" : "x1_shmrl, y1_shmrl, x2_shmrl, y2_shmrl";
            try (PreparedStatement stmnt = CONNECTION.prepareStatement(
                    "INSERT INTO MawdeaKhlaf (AyahIndex, " + fields + ", AyahWord) VALUES (?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                stmnt.setInt(1, s.ayahIndex);
                Rectangle2D.Float rect = surface.getImageRectFromScaled(WriteJFrame1.pageSize,
                        (Rectangle2D.Float) s.scaledRect);
                stmnt.setDouble(2, rect.getX());
                stmnt.setDouble(3, rect.getY());
                stmnt.setDouble(4, rect.getWidth());
                stmnt.setDouble(5, rect.getHeight());
                stmnt.setString(6, s.ayahWord);
                stmnt.executeUpdate();
                commit();
                try (ResultSet generatedKeys = stmnt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        s.id = generatedKeys.getLong(1);
                    }
                }
            }
            for (SelectionDetail d : s.details) {
                insertSelectionDetail(d, s.id);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DbHelper.class.getName()).log(Level.SEVERE, null, ex);
            rollback();
        }
        return s.id;
    }
    
    private static void insertShaheds(SelectionDetail d) throws SQLException {
        ArrayList<Shahed> all = new ArrayList<>(d.dorrah);
        all.addAll(d.shatibiyyah);
        for (int i = 0; i < all.size(); ++i) {
            String tableName = i >= d.dorrah.size() ? "ShatibiyyahShahed" : "DorrahShahed";
            Shahed sh = all.get(i);
            try (PreparedStatement stmnt = CONNECTION.prepareStatement(
                    "INSERT INTO " + tableName
                    + "(MawdeaKhlafGroupID, BeitID, BeitPart) VALUES (?,?,?)")) {
                stmnt.setLong(1, d.id);
                stmnt.setLong(2, sh.id);
                stmnt.setLong(3, sh.part1 != null && sh.part2 != null ? 0
                        : sh.part1 != null ? 1 : 2);
                stmnt.execute();
            }
        }
    }
    
    private static void insertSelectionDetail(SelectionDetail d, long selectionId) throws SQLException {
        try (PreparedStatement stmnt = CONNECTION.prepareStatement(
                "INSERT INTO MawdeaKhlafGroup (MawdeaKhlaf, KhlafType, Descr, Rewayat) VALUES (?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            stmnt.setLong(1, selectionId);
            stmnt.setLong(2, d.type.getValue());
            stmnt.setString(3, d.descr);
            stmnt.setString(4, d.rewayaat.toString());
            stmnt.execute();
            commit();
            try (ResultSet generatedKeys = stmnt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    d.id = generatedKeys.getLong(1);
                }
            }
        }
        insertShaheds(d);
    }

    static boolean deleteSelection(Selection s) {
        try (Statement stmnt = CONNECTION.createStatement()) {
            stmnt.executeUpdate(String.format("DELETE FROM DorrahShahed \n"
                    + "WHERE MawdeaKhlafGroupID IN (\n"
                    + "	SELECT _ID\n"
                    + "	FROM MawdeaKhlafGroup\n"
                    + "	WHERE MawdeaKhlaf = %d\n"
                    + ")", s.id));
            stmnt.executeUpdate(String.format("DELETE FROM ShatibiyyahShahed \n"
                    + "WHERE MawdeaKhlafGroupID IN (\n"
                    + "	SELECT _ID\n"
                    + "	FROM MawdeaKhlafGroup\n"
                    + "	WHERE MawdeaKhlaf = %d\n"
                    + ")", s.id));
            stmnt.executeUpdate("DELETE FROM MawdeaKhlafGroup WHERE MawdeaKhlaf = " + s.id);
            stmnt.executeUpdate("DELETE FROM MawdeaKhlaf WHERE _ID = " + s.id);
            commit();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(DbHelper.class.getName()).log(Level.SEVERE, null, ex);
            rollback();
            return false;
        }
    }

    static ArrayList<Ayah> getPageAyat(int page) {
        ArrayList<Ayah> ayaat = new ArrayList<>();
        try {
            try (Statement stmnt = CONNECTION.createStatement();
                    ResultSet res = stmnt.executeQuery(String.format("SELECT * \n"
                                    + "	FROM combined_mushaf \n"
                                    + "	WHERE " + (WriteJFrame1.isMadinaMushaf ? "madina_page" : "shmrl_page") + "= %d\n"
                                    + "ORDER BY ayah_index", page))) {
                String x = (WriteJFrame1.isMadinaMushaf ? "x_madina" : "x_shmrl"),
                        y = (WriteJFrame1.isMadinaMushaf ? "y_madina" : "y_shmrl");
                int px, py, brx, blx;
                if (page <= 3) {
                    px = 749;
                    py = 380;
                    blx = 113;
                } else {
                    px = 870;
                    py = 36;
                    blx = 13;
                }
                brx = px;
                int lineHeight = 80;
                while (res.next()) {
                    Ayah a = new Ayah();
                    int xx = res.getInt(x), yy = res.getInt(y);
                    a.ayahIndex = res.getInt("ayah_index");
                    a.rects = new ArrayList<>();
                    int h = py - 10;
                    int w = px;
                    while (true) {
                        if (h + lineHeight < yy) {
                            if (w - blx > 35) // prevent ayah at line end small rect
                                a.rects.add(new Rectangle2D.Float(blx, h, w, h + lineHeight));
                            h += lineHeight;
                            w = brx;
                        } else {
                            a.rects.add(new Rectangle2D.Float(xx, h, w, yy + 55));
                            break;
                        }
                    }
                    if (a.rects.size() > 1) {
                        Rectangle2D r = a.rects.get(a.rects.size() - 2);
                        r.setRect(r.getX(), r.getY(), r.getMaxX(), yy - lineHeight / 3);
                        r = a.rects.get(a.rects.size() - 1);
                        r.setRect(r.getX(), yy - lineHeight / 3, r.getMaxX(), r.getMaxY());
                    }
                    ayaat.add(a);
                    px = xx;
                    py = yy;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DbHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ayaat;
    }

    static ArrayList<Selection> getPageSelections(int page, PaintSurface surface) {
        ArrayList<Selection> sel = new ArrayList<>();
        try {
            //TODO: Performance: 
            //Add madina_page, shmrl_page columns to MawdeaKhlaf, and create index on them
            try (Statement stmnt = CONNECTION.createStatement();
                    ResultSet res = stmnt.executeQuery(String.format("SELECT *\n"
                                    + "FROM MawdeaKhlaf\n"
                                    + "WHERE AyahIndex IN (\n"
                                    + "	SELECT ayah_index \n"
                                    + "	FROM combined_mushaf \n"
                                    + "	WHERE " + (WriteJFrame1.isMadinaMushaf ? "madina_page" : "shmrl_page") + "= %d\n"
                                    + ")\n"
                                    + "ORDER BY AyahIndex", page))) {
                String x1 = (WriteJFrame1.isMadinaMushaf ? "x1" : "x1_shmrl"),
                        y1 = (WriteJFrame1.isMadinaMushaf ? "y1" : "y1_shmrl"),
                        x2 = (WriteJFrame1.isMadinaMushaf ? "x2" : "x2_shmrl"),
                        y2 = (WriteJFrame1.isMadinaMushaf ? "y2" : "y2_shmrl");
                while (res.next()) {
                    Selection s = new Selection();
                    s.naturalRect = new Rectangle2D.Float(res.getFloat(x1),
                            res.getFloat(y1), res.getFloat(x2), res.getFloat(y2));
                    s.scaledRect = surface.getScaledRectFromImageRect(WriteJFrame1.pageSize,
                            (Rectangle2D.Float) s.naturalRect);
                    s.id = res.getLong("_ID");
                    s.ayahIndex = res.getInt("AyahIndex");
                    s.ayahWord = res.getString("AyahWord");
                    sel.add(s);
                }
            }
            for (Selection selection : sel) {
                fillSelection(selection);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DbHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sel;
    }

    static ArrayList<Shahed> getShahedList(List<Integer> ids, boolean isDorrah) {
        StringBuilder b = new StringBuilder().append(ids.get(0));
        for (int i = 1; i < ids.size(); ++i) {
            b.append(", ").append(ids.get(i));
        }
        String sql = String.format("SELECT _ID, Part1, Part2 "
                + "	FROM %s "
                + "	WHERE _ID IN (%s) "
                + " ORDER BY _ID", isDorrah ? "Dorrah" : "Shatibiyyah", b);
        ArrayList<Shahed> ret = new ArrayList<>();
        try {
            try (Statement stmnt = CONNECTION.createStatement()) {
                try (ResultSet res = stmnt.executeQuery(sql)) {
                    while (res.next()) {
                        Shahed h = new Shahed();
                        h.id = res.getInt("_ID");
                        h.part1 = res.getString("Part1");
                        h.part2 = res.getString("Part2");
                        ret.add(h);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DbHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    static ArrayList<Shahed> getMatnPage(int page, boolean isDorrah) {
        String table = isDorrah ? "Dorrah" : "Shatibiyyah";
        ArrayList<Shahed> res = new ArrayList<>();
        String pageTbl = table + "Page";
        String titleTbl = table + "Title";
        int total = getMatnTotalPages(isDorrah);
        String comment = total < page + 1 ? "--" : "";
        String sql = String.format("SELECT *\n"
                + "FROM (SELECT *\n"
                + "	FROM %s\n"
                + "	WHERE %s _ID < (SELECT BeitID FROM %s WHERE Page = %d) AND \n"
                + "		_ID >= (SELECT BeitID FROM %s WHERE Page = %d)\n"
                + "	UNION\n"
                + "	SELECT BeitID, Title, NULL\n"
                + "	FROM %s\n"
                + "	WHERE %s BeitID < (SELECT BeitID FROM %s WHERE Page = %d) AND \n"
                + "		BeitID >= (SELECT BeitID FROM %s WHERE Page = %d)\n"
                + ") tmp\n"
                + "ORDER BY _ID", table, comment, pageTbl, page + 1, pageTbl, page,
                titleTbl, comment, pageTbl, page + 1, pageTbl, page);
        try (Statement stmnt = CONNECTION.createStatement();
                ResultSet set = stmnt.executeQuery(sql)) {
            while (set.next()) {
                Shahed s = new Shahed();
                s.part1 = set.getString("Part1");
                s.part2 = set.getString("Part2");
                if (set.wasNull()) {
                    s.part2 = null;
                }
                s.id = set.getInt("_ID");
                res.add(s);
            }
            res.sort((Shahed o1, Shahed o2) -> {
                if (o1.id != o2.id) {
                    return Integer.compare(o1.id, o2.id);
                }
                return o1.part2 == null ? -1 : 1;
            });
        } catch (SQLException ex) {
            Logger.getLogger(DbHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }

    static Map.Entry<Integer, Integer> getMatnPageBeitStartEnd(int page, boolean isDorrah) {
        String table = isDorrah ? "Dorrah" : "Shatibiyyah";
        String pageTbl = table + "Page";
        String titleTbl = table + "Title";
        int total = getMatnTotalPages(isDorrah);
        String comment = total < page + 1 ? "--" : "";
        String sql = String.format("SELECT *\n"
                + "FROM (SELECT *\n"
                + "	FROM %s\n"
                + "	WHERE %s _ID < (SELECT BeitID FROM %s WHERE Page = %d) AND \n"
                + "		_ID >= (SELECT BeitID FROM %s WHERE Page = %d)\n"
                + ") tmp\n"
                + "ORDER BY _ID", table, comment, pageTbl, page + 1, pageTbl, page, titleTbl, pageTbl, page + 1, pageTbl, page);
        int start = -1, end = -1;
        try (Statement stmnt = CONNECTION.createStatement();
                ResultSet set = stmnt.executeQuery(sql)) {
            while (set.next()) {
                int num = set.getInt("_ID");
                if (start == -1) {
                    start = num;
                }
                end = Math.max(end, num);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DbHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new AbstractMap.SimpleEntry<>(start, end);
    }

    static int getMatnPageByBeit(int beit, boolean isDorrah) {
        String table = isDorrah ? "DorrahPage" : "ShatibiyyahPage";
        String sql = String.format("SELECT *\n"
                + "FROM %s\n"
                + "WHERE BeitID <= %d\n"
                + "ORDER BY BeitID DESC\n"
                + "LIMIT 1", table, beit);
        try (Statement stmnt = CONNECTION.createStatement();
                ResultSet set = stmnt.executeQuery(sql)) {
            if (set.next()) {
                return set.getInt("Page");
            }
        } catch (SQLException ex) {
            Logger.getLogger(DbHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    static int getMatnTotalPages(boolean isDorrah) {
        int res = 0;
        String table = isDorrah ? "DorrahPage" : "ShatibiyyahPage";
        String sql = String.format("SELECT MAX(Page) FROM %s", table);
        try (Statement stmnt = CONNECTION.createStatement();
                ResultSet set = stmnt.executeQuery(sql)) {
            if (set.next()) {
                res = set.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DbHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }

    static int getMatnTotalAbyat(boolean isDorrah) {
        int res = 0;
        String table = isDorrah ? "Dorrah" : "Shatibiyyah";
        String sql = String.format("SELECT COUNT(*) FROM %s", table);
        try (Statement stmnt = CONNECTION.createStatement();
                ResultSet set = stmnt.executeQuery(sql)) {
            if (set.next()) {
                res = set.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DbHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }

    static int getBeitNumberByPage(int page, boolean isDorrah) {
        String table = isDorrah ? "DorrahPage" : "ShatibiyyahPage";
        String sql = String.format("SELECT *\n"
                + "FROM %s\n"
                + "WHERE Page = %d", table, page);
        try (Statement stmnt = CONNECTION.createStatement();
                ResultSet set = stmnt.executeQuery(sql)) {
            if (set.next()) {
                return set.getInt("BeitID");
            }
        } catch (SQLException ex) {
            Logger.getLogger(DbHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    static ArrayList<MatnBab> getMatnAbwab(boolean isDorrah) {
        ArrayList<MatnBab> ret = new ArrayList<>();
        String table = isDorrah ? "Dorrah" : "Shatibiyyah";
        String sql = "SELECT * FROM %sTitle";
        try {
            try (Statement stmnt = CONNECTION.createStatement()) {
                try (ResultSet res = stmnt.executeQuery(String.format(sql, table))) {
                    while (res.next()) {
                        MatnBab b = new MatnBab();
                        b.beit = res.getInt("BeitID");
                        b.bab = res.getString("Title");
                        ret.add(b);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DbHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    static Map.Entry<Integer, Integer> getMatnBabStartEnd(int beit, boolean isDorrah) {
        String table = isDorrah ? "Dorrah" : "Shatibiyyah";
        int start = -1, end = -1;
        String sql = String.format("SELECT * FROM "
                + "(SELECT * FROM (SELECT * \n"
                + "	FROM %sTitle\n"
                + "	WHERE BeitID <= %d\n"
                + "	ORDER BY BeitID DESC\n"
                + "	LIMIT 1\n"
                + ") tmp1\n"
                + "UNION \n"
                + "SELECT * FROM (SELECT * \n"
                + "	FROM %sTitle\n"
                + "	WHERE BeitID > %d\n"
                + "	ORDER BY BeitID\n"
                + "	LIMIT 1\n"
                + ") tmp2\n"
                + "UNION\n"
                + "SELECT COUNT(*) + 1, NULL FROM %s"
                + ") tmp3 "
                + "ORDER BY BeitID", table, beit, table, beit, table);
        try (Statement stmnt = CONNECTION.createStatement();
                ResultSet set = stmnt.executeQuery(sql)) {
            if (set.next()) {
                start = set.getInt("BeitID");
                if (set.next()) {
                    end = set.getInt("BeitID") - 1;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DbHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new AbstractMap.SimpleEntry<>(start, end);
    }

    static Map<Integer, String> searchMatn(String search, boolean isDorrah, int maxResults) {
        String sql = "SELECT * \n"
                + "	FROM " + (isDorrah ? "Dorrah" : "Shatibiyyah") + "Search\n"
                + "	WHERE BeitText LIKE '%" + search + "%'\n"
                + "	ORDER BY BeitID";
        Map<Integer, String> res = new LinkedHashMap<>();
        try (Statement stmnt = CONNECTION.createStatement();
                ResultSet set = stmnt.executeQuery(sql)) {
            while (set.next()) {
                if (res.size() > maxResults) {
                    return null;
                }
                res.put(set.getInt("BeitID"), set.getString("BeitText"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DbHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }

    static boolean updateSelectionLocation(Selection s, PaintSurface surface) {
        try (Statement stmnt = CONNECTION.createStatement()) {
            double x1, y1, x2, y2;
            Rectangle2D.Float rect = surface.getImageRectFromScaled(WriteJFrame1.pageSize,
                    (Rectangle2D.Float) s.scaledRect);
            x1 = rect.getX();
            y1 = rect.getY();
            x2 = rect.getWidth();
            y2 = rect.getHeight();
            String fields = WriteJFrame1.isMadinaMushaf
                    ? "x1=%f, y1=%f, x2=%f, y2=%f" : "x1_shmrl=%f, y1_shmrl=%f, x2_shmrl=%f, y2_shmrl=%f";
            String sql = String.format("UPDATE MawdeaKhlaf SET " + fields + " WHERE _ID=%d",
                    x1, y1, x2, y2, s.id);
            stmnt.executeUpdate(sql);
            commit();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(DbHelper.class.getName()).log(Level.SEVERE, null, ex);
            rollback();
            return false;
        }
    }

    static void fillSelection(Selection s) {
        Selection selection = s;
        try (Statement stmnt = CONNECTION.createStatement();
                ResultSet res = stmnt.executeQuery("SELECT * FROM MawdeaKhlafGroup WHERE MawdeaKhlaf = "
                        + selection.id)) {
            selection.details = new ArrayList<>();
            while (res.next()) {
                SelectionDetail d = new SelectionDetail();
                d.id = res.getLong("_ID");
                d.type = SelectionType.fromValue(res.getInt("KhlafType"));
                d.descr = res.getString("Descr");
                d.rewayaat = RewayahSelectionList.fromString(res.getString("Rewayat"));
                selection.details.add(d);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DbHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (SelectionDetail d : selection.details) {
            try (Statement stmnt = CONNECTION.createStatement();
                    ResultSet res = stmnt.executeQuery(String.format("SELECT * "
                                    + "FROM (SELECT BeitID, BeitPart, Part1, Part2, 0 AS isDorrah "
                                    + "	FROM ShatibiyyahShahed s "
                                    + "		JOIN Shatibiyyah t ON t._ID = s.BeitID "
                                    + "	WHERE s.MawdeaKhlafGroupID = %d "
                                    + "	UNION "
                                    + "	SELECT BeitID, BeitPart, Part1, Part2, 1 AS isDorrah "
                                    + "	FROM DorrahShahed ss "
                                    + "		JOIN Dorrah r ON r._ID = ss.BeitID "
                                    + "	WHERE ss.MawdeaKhlafGroupID = %d "
                                    + ") tmp "
                                    + "ORDER BY isDorrah, BeitID", d.id, d.id))) {
                d.dorrah = new ArrayList<>();
                d.shatibiyyah = new ArrayList<>();
                while (res.next()) {
                    Shahed h = new Shahed();
                    h.id = res.getInt("BeitID");
                    switch (res.getInt("BeitPart")) {
                        case 0:
                            h.part1 = res.getString("Part1");
                            h.part2 = res.getString("Part2");
                            break;
                        case 1:
                            h.part1 = res.getString("Part1");
                            break;
                        case 2:
                            h.part2 = res.getString("Part2");
                            break;
                    }
                    if (res.getInt("isDorrah") == 0) {
                        d.shatibiyyah.add(h);
                    } else {
                        d.dorrah.add(h);
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(DbHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private DbHelper() {
    }
}
