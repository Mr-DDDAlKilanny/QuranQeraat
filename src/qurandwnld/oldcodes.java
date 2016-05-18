//    static long insertSelection(Selection s, SelectionType ty, PaintSurface surface, Dimension pageSize) {
//        long id = 0;
//        try {
//            try (PreparedStatement stmnt = CONNECTION.prepareStatement(
//                    "INSERT INTO MawdeaKhlafDetail (KhlafType) VALUES (?)",
//                    Statement.RETURN_GENERATED_KEYS)) {
//                stmnt.setInt(1, ty.getValue());
//                stmnt.executeUpdate();
//                commit();
//                try (ResultSet generatedKeys = stmnt.getGeneratedKeys()) {
//                    if (generatedKeys.next()) {
//                        id = generatedKeys.getLong(1);
//                    }
//                }
//            }
//            SelectionDetail d = new SelectionDetail();
//            d.id = id;
//            d.type = ty;
//            s.detail = d;
//            return insertSelection(s, d, surface, pageSize);
//        } catch (SQLException ex) {
//            Logger.getLogger(DbHelper.class.getName()).log(Level.SEVERE, null, ex);
//            rollback();
//            return -1;
//        }
//    }
//    
//    static long insertSelection(Selection s, SelectionDetail detail, PaintSurface surface, Dimension pageSize) {
//        long id = 0;
//        try {
//            try (PreparedStatement stmnt = CONNECTION.prepareStatement(
//                    "INSERT INTO MawdeaKhlaf (PageNumber, DetailID, x1, y1, x2, y2) VALUES (?,?,?,?,?,?)",
//                    Statement.RETURN_GENERATED_KEYS)) {
//                stmnt.setInt(1, s.page);
//                stmnt.setLong(2, detail.id);
//                if (s instanceof RectSelection) {
//                    RectSelection ss = (RectSelection) s;
//                    Rectangle2D.Float rect = surface.getImageRectFromScaled(pageSize,
//                            (Rectangle2D.Float) ss.scaledRect);
//                    stmnt.setDouble(3, rect.getX());
//                    stmnt.setDouble(4, rect.getY());
//                    stmnt.setDouble(5, rect.getWidth());
//                    stmnt.setDouble(6, rect.getHeight());
//                } else {
//                    LineSelection ss = (LineSelection) s;
//                    Line2D.Float line = surface.getLineFromScaled(pageSize, (Line2D.Float) ss.line);
//                    stmnt.setDouble(3, line.getX1());
//                    stmnt.setDouble(4, line.getY1());
//                    stmnt.setDouble(5, line.getX2());
//                    stmnt.setDouble(6, line.getY2());
//                }
//                
//                stmnt.executeUpdate();
//                commit();
//                try (ResultSet generatedKeys = stmnt.getGeneratedKeys()) {
//                    if (generatedKeys.next()) {
//                        id = generatedKeys.getLong(1);
//                    }
//                }
//            }
//            // update OwnerMawdeaKhlaf
//            try (Statement stmnt = CONNECTION.createStatement()) {
//                boolean yes = false;
//                try (ResultSet res = 
//                        stmnt.executeQuery("SELECT OwnerMawdeaKhlaf FROM MawdeaKhlafDetail WHERE _ID="
//                        + detail.id)) {
//                    if (res.next()) {
//                        res.getInt(1);
//                        if (res.wasNull()) {
//                            yes = true;
//                        }
//                    }
//                }
//                if (yes) {
//                    stmnt.executeUpdate(String.format(
//                                "UPDATE MawdeaKhlafDetail SET OwnerMawdeaKhlaf = %d WHERE _ID = %d",
//                                id, detail.id));
//                    detail.ownerMawdeaId = id;
//                    commit();
//                }
//            }
//        } catch (SQLException ex) {
//            Logger.getLogger(DbHelper.class.getName()).log(Level.SEVERE, null, ex);
//            rollback();
//        }
//        return s.id = id;
//    }
//    

//    static boolean updateSelectionDetailKhlafat(SelectionDetail d) {
//        try (Statement stmnt = CONNECTION.createStatement()) {
//            // check groups changes (add/delete)
//            ArrayList<RewayahSelectionGroup> work = new ArrayList<>(d.khelafat);
//            ArrayList<Long> add = new ArrayList<>(work.stream().map(k -> k.id).collect(Collectors.toList())),
//                    delete = new ArrayList<>();
//            try (ResultSet res = stmnt.executeQuery(
//                    "SELECT * FROM KhlafGroup WHERE MawdeaKhlafDetailID = " + d.id)) {
//                while (res.next()) {
//                    long id = res.getLong("_ID");
//                    if (!work.stream().anyMatch(dd -> dd.id == id))
//                        delete.add(id);
//                    add.removeIf(k -> k == id);
//                }
//            }
//            if (delete.size() > 0) {
//                StringBuilder b = new StringBuilder("(").append(delete.get(0));
//                for (int i = 1; i < delete.size(); ++i) {
//                    b.append(", ").append(delete.get(i));
//                }
//                b.append(")");
//                stmnt.executeUpdate("DELETE FROM Khlaf WHERE KhlafGroupID IN " + b);
//                stmnt.executeUpdate("DELETE FROM KhlafGroup WHERE _ID IN " + b);
//            }
//            if (!add.isEmpty()) {
//                List<RewayahSelectionGroup> ad = work.stream()
//                        .filter(k -> add.stream().anyMatch(j -> j == k.id))
//                        .collect(Collectors.toList());
//                for (RewayahSelectionGroup a : ad) {
//                    stmnt.executeUpdate(String.format(
//                            "INSERT INTO KhlafGroup (Description, MawdeaKhlafDetailID) VALUES('%s', %d)",
//                            a.descr, d.id));
//                    long gid = getLastInsertRowId();
//                    a.id = gid;
//                    for (RewayahSelection rs : a.rewayaat) {
//                        stmnt.executeUpdate(String.format(
//                                "INSERT INTO Khlaf(RewayahID, HasKholf, KhlafGroupID) VALUES(%d, %d, %d)", 
//                                getRewayahId(rs.r.getCombinedCode()),
//                                rs.kholf ? 1 : 0,
//                                gid));
//                    }
//                }
//                work.removeIf(k -> ad.stream().anyMatch(j -> j.id == k.id));
//            }
//            
//            //update the rest of items
//            for (RewayahSelectionGroup g : work) {
//                stmnt.executeUpdate(String.format(
//                        "UPDATE KhlafGroup SET Description='%s', MawdeaKhlafDetailID=%d WHERE _ID=%d",
//                        g.descr, d.id, g.id));
//                stmnt.executeUpdate("DELETE FROM Khlaf WHERE KhlafGroupID = " + g.id);
//                for (RewayahSelection rs : g.rewayaat) {
//                    stmnt.executeUpdate(String.format(
//                            "INSERT INTO Khlaf(RewayahID, HasKholf, KhlafGroupID) VALUES(%d, %d, %d)", 
//                            getRewayahId(rs.r.getCombinedCode()),
//                            rs.kholf ? 1 : 0,
//                            g.id));
//                }
//            }
//            commit();
//        } catch (SQLException ex) {
//            Logger.getLogger(DbHelper.class.getName()).log(Level.SEVERE, null, ex);
//            rollback();
//            return false;
//        }
//        return true;
//    }
//    
//    static boolean updateSelectionDetail(SelectionDetail d) {
//        return updateSelectionDetailKhlafat(d) && updateSelectionDetailShahed(d);
//    }
//
//    
//    static boolean updateSelectionDetailShahed(SelectionDetail d) {
//        try (Statement st = CONNECTION.createStatement()) {
//            st.executeUpdate("DELETE FROM DorrahShahed WHERE DetailID = " + d.id);
//        } catch (SQLException ex) {
//            Logger.getLogger(DbHelper.class.getName()).log(Level.SEVERE, null, ex);
//            rollback();
//            return false;
//        }
//        try (PreparedStatement stmnt = CONNECTION.prepareStatement(
//                "INSERT INTO DorrahShahed (DetailID, BeitID, BeitPart) VALUES (?,?,?)")) {
//            for (Shahed dor : d.dorrah) {
//                stmnt.setLong(1, d.id);
//                stmnt.setLong(2, dor.id);
//                stmnt.setInt(3, dor.part1 == null ? 2 : dor.part2 == null ? 1 : 0);
//                stmnt.addBatch();
//            }
//            stmnt.executeBatch();
//            commit();
//        } catch (SQLException ex) {
//            Logger.getLogger(DbHelper.class.getName()).log(Level.SEVERE, null, ex);
//            rollback();
//            return false;
//        }
//        try (Statement st = CONNECTION.createStatement()) {
//            st.executeUpdate("DELETE FROM ShatibiyyahShahed WHERE DetailID = " + d.id);
//        } catch (SQLException ex) {
//            Logger.getLogger(DbHelper.class.getName()).log(Level.SEVERE, null, ex);
//            rollback();
//            return false;
//        }
//        try (PreparedStatement stmnt = CONNECTION.prepareStatement(
//                "INSERT INTO ShatibiyyahShahed (DetailID, BeitID, BeitPart) VALUES (?,?,?)")) {
//            for (Shahed dor : d.shatibiyyah) {
//                stmnt.setLong(1, d.id);
//                stmnt.setLong(2, dor.id);
//                stmnt.setInt(3, dor.part1 == null ? 2 : dor.part2 == null ? 1 : 0);
//                stmnt.addBatch();
//            }
//            stmnt.executeBatch();
//            commit();
//        } catch (SQLException ex) {
//            Logger.getLogger(DbHelper.class.getName()).log(Level.SEVERE, null, ex);
//            rollback();
//            return false;
//        }
//        return true;
//    }
//    static boolean deleteSelection(Selection s) {
//        String sql;
//        if (s.id == s.detail.ownerMawdeaId) {
//            long newOwnerMawdea = 0;
//            // get any mawade3 that uses this detail (excepting me)
//            sql = String.format("SELECT *\n" +
//    "FROM MawdeaKhlaf\n" +
//    "WHERE DetailID = %d AND _ID <> (SELECT OwnerMawdeaKhlaf FROM MawdeaKhlafDetail WHERE _ID = %d)\n" +
//    "LIMIT 1", s.detail.id, s.detail.id);
//            try (Statement stmnt = CONNECTION.createStatement();
//                    ResultSet res = stmnt.executeQuery(sql)) {
//                if (res.next()) {
//                    newOwnerMawdea = res.getLong("_ID");
//                }
//            } catch (SQLException ex) {
//                Logger.getLogger(DbHelper.class.getName()).log(Level.SEVERE, null, ex);
//                return false;
//            }
//            // if exists, make it the owner
//            if (newOwnerMawdea > 0) {
//                try (Statement stmnt = CONNECTION.createStatement()) {
//                    stmnt.executeUpdate(String.format(
//                                "UPDATE MawdeaKhlafDetail SET OwnerMawdeaKhlaf = %d WHERE _ID = %d",
//                                newOwnerMawdea, s.detail.id));
//                    s.detail.ownerMawdeaId = newOwnerMawdea;
//                } catch (SQLException ex) {
//                    Logger.getLogger(DbHelper.class.getName()).log(Level.SEVERE, null, ex);
//                    rollback();
//                    return false;
//                }
//            } else { // otherwise, delete the detail also
//                try (Statement stmnt = CONNECTION.createStatement()) {
//                    // first delete khlafGroups 
//                    sql = String.format("DELETE FROM Khlaf "
//                        + "WHERE KhlafGroupID IN "
//                        + "(SELECT DISTINCT _ID FROM KhlafGroup WHERE MawdeaKhlafDetailID = %d)", s.detail.id);
//                    stmnt.executeUpdate(sql);
//                    sql = String.format("DELETE FROM KhlafGroup WHERE MawdeaKhlafDetailID = %d", s.detail.id);
//                    stmnt.executeUpdate(sql);
//                    // secondly delete shaheds
//                    sql = String.format("DELETE FROM DorrahShahed WHERE DetailID = %d", s.detail.id);
//                    stmnt.executeUpdate(sql);
//                    sql = String.format("DELETE FROM ShatibiyyahShahed WHERE DetailID = %d", s.detail.id);
//                    stmnt.executeUpdate(sql);
//                    // thirdly delete the detail
//                    sql = String.format("DELETE FROM MawdeaKhlafDetail WHERE _ID = %d", s.detail.id);
//                    stmnt.executeUpdate(sql);
//                    s.detail = null;
//                } catch (SQLException ex) {
//                    Logger.getLogger(DbHelper.class.getName()).log(Level.SEVERE, null, ex);
//                    rollback();
//                    return false;
//                }
//            }
//        }
//        //finally delete the mawdea
//        try (Statement stmnt = CONNECTION.createStatement()) {
//            sql = String.format("DELETE FROM MawdeaKhlaf WHERE _ID = %d", s.id);
//            stmnt.executeUpdate(sql);
//            commit();
//            return true;
//        } catch (SQLException ex) {
//            Logger.getLogger(DbHelper.class.getName()).log(Level.SEVERE, null, ex);
//            rollback();
//            return false;
//        }
//    }
//
//    
//    static ArrayList<Selection> getPageSelections(int page, PaintSurface surface, Dimension pageSize) {
//        ArrayList<Selection> sel = new ArrayList<>();
//        ArrayList<SelectionDetail> details = new ArrayList<>();
//        try {
//            String sql = String.format("SELECT *\n" +
//"FROM MawdeaKhlafDetail\n" +
//"WHERE _ID IN (SELECT DISTINCT DetailID\n" +
//"	FROM MawdeaKhlaf\n" +
//"	WHERE PageNumber = %d)", page);
//            try (Statement stmnt = CONNECTION.createStatement();
//                    ResultSet res = stmnt.executeQuery(sql)) {
//                while (res.next()) {
//                    SelectionDetail d = new SelectionDetail();
//                    d.id = res.getLong("_ID");
//                    d.ownerMawdeaId = res.getLong("OwnerMawdeaKhlaf");
//                    d.type = SelectionType.fromValue(res.getInt("KhlafType"));
//                    details.add(d);
//                }
//            }
//            sql = String.format("SELECT m._ID, DetailID, x1, x2, y1, y2 "
//                    + "FROM MawdeaKhlaf AS m "
//                    + "WHERE PageNumber = %d", page);
//            try (Statement stmnt = CONNECTION.createStatement();
//                    ResultSet res = stmnt.executeQuery(sql)) {
//                while (res.next()) {
//                    Selection s;
//                    long detail = res.getLong("DetailID");
//                    SelectionDetail d = details.stream().filter(k -> k.id == detail).findAny().get();
//                    if (d.isRect()) {
//                        RectSelection ss = new RectSelection();
//                        ss.naturalRect = new Rectangle2D.Float(res.getFloat("x1"),
//                                res.getFloat("y1"), res.getFloat("x2"), res.getFloat("y2"));
//                        ss.scaledRect = surface.getScaledRectFromImageRect(pageSize,
//                                (Rectangle2D.Float) ss.naturalRect);
//                        s = ss;
//                    } else {
//                        LineSelection ss = new LineSelection();
//                        ss.origLine = new Line2D.Float(res.getFloat("x1"),
//                                res.getFloat("y1"), res.getFloat("x2"), res.getFloat("y2"));
//                        ss.line = surface.getScaledLine(pageSize, (Line2D.Float) ss.origLine);
//                        s = ss;
//                    }
//                    s.id = res.getLong("_ID");
//                    s.detail = d;
//                    s.page = page;
//                    sel.add(s);
//                }
//            }
//        } catch (SQLException ex) {
//            Logger.getLogger(DbHelper.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return sel;
//    }
//

//    private static int getRewayahId(String combinedCode) {
//        int pos = combinedCode.indexOf(".");
//        return Integer.parseInt(combinedCode.substring(0, pos)) * 2
//                + Integer.parseInt(combinedCode.substring(pos + 1)) - 2;
//    }
    
//    static void fillShaheds(SelectionDetail s) {
//        String sql = "SELECT * "
//                + "FROM (SELECT BeitID, BeitPart, Part1, Part2, 0 AS isDorrah "
//                + "	FROM ShatibiyyahShahed s "
//                + "		JOIN Shatibiyyah t ON t._ID = s.BeitID "
//                + "	WHERE s.DetailID = %d "
//                + "	UNION "
//                + "	SELECT BeitID, BeitPart, Part1, Part2, 1 AS isDorrah "
//                + "	FROM DorrahShahed ss "
//                + "		JOIN Dorrah r ON r._ID = ss.BeitID "
//                + "	WHERE ss.DetailID = %d "
//                + ") tmp "
//                + "ORDER BY isDorrah, BeitID";
//        try {
//            try (Statement stmnt = CONNECTION.createStatement()) {
//                s.dorrah = new ArrayList<>();
//                s.shatibiyyah = new ArrayList<>();
//                try (ResultSet res = stmnt.executeQuery(String.format(sql, s.id, s.id))) {
//                    while (res.next()) {
//                        Shahed h = new Shahed();
//                        h.id = res.getInt("BeitID");
//                        switch (res.getInt("BeitPart")) {
//                            case 0:
//                                h.part1 = res.getString("Part1");
//                                h.part2 = res.getString("Part2");
//                                break;
//                            case 1:
//                                h.part1 = res.getString("Part1");
//                                break;
//                            case 2:
//                                h.part2 = res.getString("Part2");
//                                break;
//                        }
//                        if (res.getInt("isDorrah") == 0) {
//                            s.shatibiyyah.add(h);
//                        } else {
//                            s.dorrah.add(h);
//                        }
//                    }
//                }
//            }
//        } catch (SQLException ex) {
//            Logger.getLogger(DbHelper.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
