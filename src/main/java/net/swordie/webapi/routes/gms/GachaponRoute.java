package net.swordie.webapi.routes.gms;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import net.swordie.ms.connection.db.DatabaseManager;
import net.swordie.webapi.ApiConstants;
import net.swordie.webapi.protocol.result.GmsApiResult;
import net.swordie.webapi.routes.BaseResource;

import java.util.*;

@Path("/gachapon/v1")
public class GachaponRoute extends BaseResource {

    private static boolean tablesInitialized = false;

    static {
        initTables();
    }

    private static synchronized void initTables() {
        if (tablesInitialized) return;
        try {
            DatabaseManager.executeQuery(
                "CREATE TABLE IF NOT EXISTS gachapon_pools (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  name VARCHAR(128) NOT NULL," +
                "  type VARCHAR(64)" +
                ")"
            );
            DatabaseManager.executeQuery(
                "CREATE TABLE IF NOT EXISTS gachapon_rewards (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  poolId INT NOT NULL," +
                "  itemId INT NOT NULL," +
                "  chance INT DEFAULT 0," +
                "  minQuant INT DEFAULT 1," +
                "  maxQuant INT DEFAULT 1" +
                ")"
            );
        } catch (Exception e) {
        }
        tablesInitialized = true;
    }

    @POST
    @Path("/getPools")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult getPools(Map<String, Object> body, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);

        int pageNo = body != null && body.containsKey("pageNo") ? ((Number) body.get("pageNo")).intValue() : 1;
        int pageSize = body != null && body.containsKey("pageSize") ? ((Number) body.get("pageSize")).intValue() : 20;
        Integer gachaponId = body != null && body.containsKey("gachaponId") ? ((Number) body.get("gachaponId")).intValue() : null;
        pageNo = Math.max(pageNo, 1);
        pageSize = Math.min(Math.max(pageSize, 1), 100);
        int offset = (pageNo - 1) * pageSize;

        StringBuilder sql = new StringBuilder("SELECT * FROM gachapon_pools t WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (gachaponId != null && gachaponId > 0) {
            sql.append(" AND t.id = ?");
            params.add(gachaponId);
        }
        sql.append(" ORDER BY t.id LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add(offset);

        List<Object> raw = DatabaseManager.executeSelect(
                (rs, alias) -> {
                    try {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", rs.getInt("id"));
                        m.put("name", rs.getString("name"));
                        m.put("type", rs.getString("type"));
                        return m;
                    } catch (Exception e) { return null; }
                },
                sql.toString(), "t", params.toArray()
        );
        List<Map<String, Object>> pools = new ArrayList<>();
        for (Object o : raw) {
            if (o instanceof Map) pools.add((Map<String, Object>) o);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", pools);
        return GmsApiResult.success(result);
    }

    @POST
    @Path("/updatePool")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult updatePool(Map<String, Object> body, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);
        if (body == null || !body.containsKey("id")) {
            return GmsApiResult.error(40000, "Missing id");
        }
        int id = ((Number) body.get("id")).intValue();
        String name = (String) body.get("name");
        String type = (String) body.get("type");

        DatabaseManager.executeQuery("UPDATE gachapon_pools SET name=?, type=? WHERE id=?",
                name, type, id);
        return GmsApiResult.success();
    }

    @POST
    @Path("/deletePool")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult deletePool(Map<String, Object> body, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);
        if (body == null || !body.containsKey("id")) {
            return GmsApiResult.error(40000, "Missing id");
        }
        int id = ((Number) body.get("id")).intValue();
        DatabaseManager.executeQuery("DELETE FROM gachapon_pools WHERE id = ?", id);
        DatabaseManager.executeQuery("DELETE FROM gachapon_rewards WHERE poolId = ?", id);
        return GmsApiResult.success();
    }

    @POST
    @Path("/getRewards")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult getRewards(Map<String, Object> body, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);

        int poolId = body != null && body.containsKey("id") ? ((Number) body.get("id")).intValue() : 0;

        List<Object> raw = DatabaseManager.executeSelect(
                (rs, alias) -> {
                    try {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", rs.getInt("id"));
                        m.put("poolId", rs.getInt("poolId"));
                        m.put("itemId", rs.getInt("itemId"));
                        m.put("chance", rs.getInt("chance"));
                        m.put("minQuant", rs.getInt("minQuant"));
                        m.put("maxQuant", rs.getInt("maxQuant"));
                        return m;
                    } catch (Exception e) { return null; }
                },
                "SELECT * FROM gachapon_rewards t WHERE t.poolId = ? ORDER BY t.id",
                "t", poolId
        );
        List<Map<String, Object>> rewards = new ArrayList<>();
        for (Object o : raw) {
            if (o instanceof Map) rewards.add((Map<String, Object>) o);
        }
        return GmsApiResult.success(rewards);
    }

    @POST
    @Path("/updateReward")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult updateReward(Map<String, Object> body, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);
        if (body == null || !body.containsKey("id")) {
            return GmsApiResult.error(40000, "Missing id");
        }
        int id = ((Number) body.get("id")).intValue();
        int itemId = body.containsKey("itemId") ? ((Number) body.get("itemId")).intValue() : 0;
        int chance = body.containsKey("chance") ? ((Number) body.get("chance")).intValue() : 0;

        DatabaseManager.executeQuery("UPDATE gachapon_rewards SET itemId=?, chance=? WHERE id=?",
                itemId, chance, id);
        return GmsApiResult.success();
    }

    @POST
    @Path("/deleteReward")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult deleteReward(Map<String, Object> body, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);
        if (body == null || !body.containsKey("id")) {
            return GmsApiResult.error(40000, "Missing id");
        }
        int id = ((Number) body.get("id")).intValue();
        DatabaseManager.executeQuery("DELETE FROM gachapon_rewards WHERE id = ?", id);
        return GmsApiResult.success();
    }
}
