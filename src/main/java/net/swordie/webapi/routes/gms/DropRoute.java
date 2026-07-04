package net.swordie.webapi.routes.gms;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import net.swordie.ms.connection.db.DatabaseManager;
import net.swordie.webapi.ApiConstants;
import net.swordie.webapi.protocol.result.GmsApiResult;
import net.swordie.webapi.routes.BaseResource;

import java.util.*;

@Path("/drop/v1")
public class DropRoute extends BaseResource {

    private static boolean tablesInitialized = false;

    static {
        initTables();
    }

    private static synchronized void initTables() {
        if (tablesInitialized) return;
        try {
            DatabaseManager.executeQuery(
                "CREATE TABLE IF NOT EXISTS global_drops (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  itemID INT NOT NULL DEFAULT 0," +
                "  chance INT NOT NULL DEFAULT 0," +
                "  minQuant INT NOT NULL DEFAULT 1," +
                "  maxQuant INT NOT NULL DEFAULT 1," +
                "  continent VARCHAR(32)" +
                ")"
            );
        } catch (Exception e) {
        }
        tablesInitialized = true;
    }

    private List<Map<String, Object>> queryDrops(String table, Map<String, Object> condition) {
        StringBuilder sql = new StringBuilder("SELECT * FROM " + table + " t WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (condition != null) {
            if (condition.containsKey("dropperId") && condition.get("dropperId") != null) {
                sql.append(" AND t.mobId = ?");
                params.add(((Number) condition.get("dropperId")).intValue());
            }
            if (condition.containsKey("itemId") && condition.get("itemId") != null) {
                sql.append(" AND t.itemID = ?");
                params.add(((Number) condition.get("itemId")).intValue());
            }
            if (condition.containsKey("itemName") && condition.get("itemName") != null) {
                sql.append(" AND t.itemID LIKE ?");
                params.add("%" + condition.get("itemName") + "%");
            }
        }

        Object pageNoObj = condition != null ? condition.get("pageNo") : null;
        Object pageSizeObj = condition != null ? condition.get("pageSize") : null;
        Object onlyTotal = condition != null ? condition.get("onlyTotal") : null;
        Object notPage = condition != null ? condition.get("notPage") : null;

        int pageNo = pageNoObj != null ? ((Number) pageNoObj).intValue() : 1;
        int pageSize = pageSizeObj != null ? ((Number) pageSizeObj).intValue() : 20;
        pageNo = Math.max(pageNo, 1);
        pageSize = Math.min(Math.max(pageSize, 1), 200);

        sql.append(" ORDER BY t.id DESC");

        if (!Boolean.TRUE.equals(notPage)) {
            sql.append(" LIMIT ? OFFSET ?");
            params.add(pageSize);
            params.add((pageNo - 1) * pageSize);
        }

        List<Object> raw = DatabaseManager.executeSelect(
                (rs, alias) -> {
                    try {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", rs.getInt("id"));
                        m.put("mobId", rs.getInt("mobId"));
                        m.put("itemID", rs.getInt("itemID"));
                        m.put("chance", rs.getInt("chance"));
                        m.put("minQuant", rs.getInt("minQuant"));
                        m.put("maxQuant", rs.getInt("maxQuant"));
                        return m;
                    } catch (Exception e) { return null; }
                },
                sql.toString(), "t", params.toArray()
        );
        List<Map<String, Object>> records = new ArrayList<>();
        for (Object o : raw) {
            if (o instanceof Map) records.add((Map<String, Object>) o);
        }
        return records;
    }

    @POST
    @Path("/getDropList")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult getDropList(Map<String, Object> body, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);
        List<Map<String, Object>> records = queryDrops("mob_drops", body);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", records);
        return GmsApiResult.success(result);
    }

    @POST
    @Path("/updateDropData")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult updateDropData(Map<String, Object> body, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);
        if (body == null || !body.containsKey("id")) {
            return GmsApiResult.error(40000, "Missing id");
        }
        int id = ((Number) body.get("id")).intValue();
        int mobId = body.containsKey("mobId") ? ((Number) body.get("mobId")).intValue() : 0;
        int itemId = body.containsKey("itemID") ? ((Number) body.get("itemID")).intValue() : 0;
        int chance = body.containsKey("chance") ? ((Number) body.get("chance")).intValue() : 0;
        int minQ = body.containsKey("minQuant") ? ((Number) body.get("minQuant")).intValue() : 1;
        int maxQ = body.containsKey("maxQuant") ? ((Number) body.get("maxQuant")).intValue() : 1;

        DatabaseManager.executeQuery(
            "UPDATE mob_drops SET mobId=?, itemID=?, chance=?, minQuant=?, maxQuant=? WHERE id=?",
            mobId, itemId, chance, minQ, maxQ, id
        );
        return GmsApiResult.success();
    }

    @PUT
    @Path("/addDropData")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult addDropData(Map<String, Object> body, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);
        if (body == null) return GmsApiResult.error(40000, "Missing body");

        int mobId = body.containsKey("mobId") ? ((Number) body.get("mobId")).intValue() : 0;
        int itemId = body.containsKey("itemID") ? ((Number) body.get("itemID")).intValue() : 0;
        int chance = body.containsKey("chance") ? ((Number) body.get("chance")).intValue() : 0;
        int minQ = body.containsKey("minQuant") ? ((Number) body.get("minQuant")).intValue() : 1;
        int maxQ = body.containsKey("maxQuant") ? ((Number) body.get("maxQuant")).intValue() : 1;

        DatabaseManager.executeQuery(
            "INSERT INTO mob_drops (mobId, itemID, chance, minQuant, maxQuant) VALUES (?, ?, ?, ?, ?)",
            mobId, itemId, chance, minQ, maxQ
        );
        return GmsApiResult.success();
    }

    @DELETE
    @Path("/deleteDropData/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult deleteDropData(@PathParam("id") int id, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);
        DatabaseManager.executeQuery("DELETE FROM mob_drops WHERE id = ?", id);
        return GmsApiResult.success();
    }

    @POST
    @Path("/getGlobalDropList")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult getGlobalDropList(Map<String, Object> body, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);
        List<Map<String, Object>> records = queryDrops("global_drops", body);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", records);
        return GmsApiResult.success(result);
    }

    @POST
    @Path("/updateGlobalDropData")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult updateGlobalDropData(Map<String, Object> body, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);
        if (body == null || !body.containsKey("id")) {
            return GmsApiResult.error(40000, "Missing id");
        }
        int id = ((Number) body.get("id")).intValue();
        int itemId = body.containsKey("itemID") ? ((Number) body.get("itemID")).intValue() : 0;
        int chance = body.containsKey("chance") ? ((Number) body.get("chance")).intValue() : 0;
        int minQ = body.containsKey("minQuant") ? ((Number) body.get("minQuant")).intValue() : 1;
        int maxQ = body.containsKey("maxQuant") ? ((Number) body.get("maxQuant")).intValue() : 1;

        DatabaseManager.executeQuery(
            "UPDATE global_drops SET itemID=?, chance=?, minQuant=?, maxQuant=? WHERE id=?",
            itemId, chance, minQ, maxQ, id
        );
        return GmsApiResult.success();
    }

    @PUT
    @Path("/addGlobalDropData")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult addGlobalDropData(Map<String, Object> body, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);
        if (body == null) return GmsApiResult.error(40000, "Missing body");

        int itemId = body.containsKey("itemID") ? ((Number) body.get("itemID")).intValue() : 0;
        int chance = body.containsKey("chance") ? ((Number) body.get("chance")).intValue() : 0;
        int minQ = body.containsKey("minQuant") ? ((Number) body.get("minQuant")).intValue() : 1;
        int maxQ = body.containsKey("maxQuant") ? ((Number) body.get("maxQuant")).intValue() : 1;

        DatabaseManager.executeQuery(
            "INSERT INTO global_drops (itemID, chance, minQuant, maxQuant) VALUES (?, ?, ?, ?)",
            itemId, chance, minQ, maxQ
        );
        return GmsApiResult.success();
    }

    @DELETE
    @Path("/deleteGlobalDropData/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult deleteGlobalDropData(@PathParam("id") int id, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);
        DatabaseManager.executeQuery("DELETE FROM global_drops WHERE id = ?", id);
        return GmsApiResult.success();
    }
}
