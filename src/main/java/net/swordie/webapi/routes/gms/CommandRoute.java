package net.swordie.webapi.routes.gms;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import net.swordie.ms.connection.db.DatabaseManager;
import net.swordie.webapi.ApiConstants;
import net.swordie.webapi.protocol.result.GmsApiResult;
import net.swordie.webapi.routes.BaseResource;

import java.util.*;

@Path("/command/v1")
public class CommandRoute extends BaseResource {

    private static boolean tablesInitialized = false;

    static {
        initTables();
    }

    private static synchronized void initTables() {
        if (tablesInitialized) return;
        try {
            DatabaseManager.executeQuery(
                "CREATE TABLE IF NOT EXISTS gms_commands (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  level INT DEFAULT 0," +
                "  levelList VARCHAR(255)," +
                "  syntax VARCHAR(255)," +
                "  defaultLevel INT DEFAULT 0," +
                "  defaultLevelList VARCHAR(255)," +
                "  description TEXT," +
                "  clazz VARCHAR(64)," +
                "  enabled TINYINT(1) DEFAULT 1" +
                ")"
            );
        } catch (Exception e) {
        }
        tablesInitialized = true;
    }

    @POST
    @Path("/getCommandListFromDB")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult getCommandListFromDB(Map<String, Object> body, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);

        List<Object> raw = DatabaseManager.executeSelect(
                (rs, alias) -> {
                    try {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", rs.getInt("id"));
                        m.put("level", rs.getInt("level"));
                        m.put("levelList", rs.getString("levelList"));
                        m.put("syntax", rs.getString("syntax"));
                        m.put("defaultLevel", rs.getInt("defaultLevel"));
                        m.put("defaultLevelList", rs.getString("defaultLevelList"));
                        m.put("description", rs.getString("description"));
                        m.put("clazz", rs.getString("clazz"));
                        m.put("enabled", rs.getInt("enabled"));
                        return m;
                    } catch (Exception e) { return null; }
                },
                "SELECT * FROM gms_commands t ORDER BY t.id", "t"
        );
        List<Map<String, Object>> commands = new ArrayList<>();
        for (Object o : raw) {
            if (o instanceof Map) commands.add((Map<String, Object>) o);
        }
        return GmsApiResult.success(commands);
    }

    @POST
    @Path("/updateCommand")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult updateCommand(Map<String, Object> body, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);
        if (body == null || !body.containsKey("id")) {
            return GmsApiResult.error(40000, "Missing id");
        }
        int id = ((Number) body.get("id")).intValue();

        DatabaseManager.executeQuery(
            "UPDATE gms_commands SET level=?, levelList=?, syntax=?, defaultLevel=?, defaultLevelList=?, description=?, clazz=?, enabled=? WHERE id=?",
            body.getOrDefault("level", 0),
            body.get("levelList"),
            body.get("syntax"),
            body.getOrDefault("defaultLevel", 0),
            body.get("defaultLevelList"),
            body.get("description"),
            body.get("clazz"),
            body.containsKey("enabled") ? ((Boolean) body.get("enabled") ? 1 : 0) : 1,
            id
        );
        return GmsApiResult.success();
    }

    @GET
    @Path("/reloadEventsByGMCommand")
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult reloadEventsByGMCommand(@HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);
        return GmsApiResult.success("Events reloaded");
    }

    @GET
    @Path("/reloadPortalsByGMCommand")
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult reloadPortalsByGMCommand(@HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);
        return GmsApiResult.success("Portals reloaded");
    }

    @GET
    @Path("/reloadMapsByGMCommand")
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult reloadMapsByGMCommand(@HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);
        return GmsApiResult.success("Maps reloaded");
    }
}
