package net.swordie.webapi.routes.gms;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import net.swordie.ms.connection.db.DatabaseManager;
import net.swordie.webapi.ApiConstants;
import net.swordie.webapi.protocol.result.GmsApiResult;
import net.swordie.webapi.routes.BaseResource;

import java.util.*;

@Path("/autoban/v1")
public class AutobanRoute extends BaseResource {

    private static boolean tablesInitialized = false;

    static {
        initTables();
    }

    private static synchronized void initTables() {
        if (tablesInitialized) return;
        try {
            DatabaseManager.executeQuery(
                "CREATE TABLE IF NOT EXISTS autoban_config (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  type VARCHAR(64)," +
                "  name VARCHAR(128)," +
                "  disabled TINYINT(1) DEFAULT 0," +
                "  points INT DEFAULT 0," +
                "  expireTimeSeconds INT DEFAULT 0," +
                "  description TEXT," +
                "  defaultPoints INT DEFAULT 0," +
                "  defaultExpireTimeSeconds INT DEFAULT 0," +
                "  changePoints INT DEFAULT 0," +
                "  changeExpireTime INT DEFAULT 0" +
                ")"
            );
        } catch (Exception e) {
        }
        tablesInitialized = true;
    }

    @GET
    @Path("/getConfigList")
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult getConfigList(@HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);

        List<Object> raw = DatabaseManager.executeSelect(
                (rs, alias) -> {
                    try {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", rs.getInt("id"));
                        m.put("type", rs.getString("type"));
                        m.put("name", rs.getString("name"));
                        m.put("disabled", rs.getInt("disabled"));
                        m.put("points", rs.getObject("points"));
                        m.put("expireTimeSeconds", rs.getObject("expireTimeSeconds"));
                        m.put("description", rs.getString("description"));
                        m.put("defaultPoints", rs.getInt("defaultPoints"));
                        m.put("defaultExpireTimeSeconds", rs.getInt("defaultExpireTimeSeconds"));
                        m.put("changePoints", rs.getInt("changePoints"));
                        m.put("changeExpireTime", rs.getInt("changeExpireTime"));
                        return m;
                    } catch (Exception e) { return null; }
                },
                "SELECT * FROM autoban_config t ORDER BY t.id", "t"
        );
        List<Map<String, Object>> configs = new ArrayList<>();
        for (Object o : raw) {
            if (o instanceof Map) configs.add((Map<String, Object>) o);
        }
        return GmsApiResult.success(configs);
    }

    @POST
    @Path("/updateConfig")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult updateConfig(Map<String, Object> body, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);
        if (body == null || !body.containsKey("id")) {
            return GmsApiResult.error(40000, "Missing id");
        }
        int id = ((Number) body.get("id")).intValue();

        DatabaseManager.executeQuery(
            "UPDATE autoban_config SET type=?, name=?, disabled=?, points=?, expireTimeSeconds=?, description=?, defaultPoints=?, defaultExpireTimeSeconds=?, changePoints=?, changeExpireTime=? WHERE id=?",
            body.get("type"),
            body.get("name"),
            body.containsKey("disabled") ? ((Boolean) body.get("disabled") ? 1 : 0) : 0,
            body.get("points"),
            body.get("expireTimeSeconds"),
            body.get("description"),
            body.getOrDefault("defaultPoints", 0),
            body.getOrDefault("defaultExpireTimeSeconds", 0),
            body.getOrDefault("changePoints", 0),
            body.getOrDefault("changeExpireTime", 0),
            id
        );
        return GmsApiResult.success();
    }
}
