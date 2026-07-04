package net.swordie.webapi.routes.gms;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import net.swordie.ms.connection.db.DatabaseManager;
import net.swordie.webapi.ApiConstants;
import net.swordie.webapi.protocol.result.GmsApiResult;
import net.swordie.webapi.routes.BaseResource;

import java.util.*;

@Path("/config/v1")
public class ConfigRoute extends BaseResource {

    private static boolean tablesInitialized = false;

    static {
        initTables();
    }

    private static synchronized void initTables() {
        if (tablesInitialized) return;
        try {
            DatabaseManager.executeQuery(
                "CREATE TABLE IF NOT EXISTS gms_configs (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  configType VARCHAR(64) NOT NULL," +
                "  configSubType VARCHAR(64)," +
                "  configClazz VARCHAR(64)," +
                "  configCode VARCHAR(255) NOT NULL," +
                "  configValue TEXT," +
                "  configDesc TEXT" +
                ")"
            );
        } catch (Exception e) {
            // table may already exist
        }
        tablesInitialized = true;
    }

    @GET
    @Path("/getConfigTypeList")
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult getConfigTypeList(@HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);

        List<Object> raw = DatabaseManager.executeSelect(
                (rs, alias) -> {
                    try {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("type", rs.getString("configType"));
                        m.put("count", rs.getInt("cnt"));
                        return m;
                    } catch (Exception e) { return null; }
                },
                "SELECT configType, COUNT(*) as cnt FROM gms_configs t GROUP BY configType ORDER BY configType",
                "t"
        );
        List<Map<String, Object>> types = new ArrayList<>();
        for (Object o : raw) {
            if (o instanceof Map) types.add((Map<String, Object>) o);
        }
        return GmsApiResult.success(types);
    }

    @POST
    @Path("/getConfigList")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult getConfigList(Map<String, Object> body, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);

        String type = body != null ? (String) body.get("type") : null;
        String subType = body != null ? (String) body.get("subType") : null;
        String filter = body != null ? (String) body.get("filter") : null;
        int pageNo = body != null && body.containsKey("pageNo") ? ((Number) body.get("pageNo")).intValue() : 1;
        int pageSize = body != null && body.containsKey("pageSize") ? ((Number) body.get("pageSize")).intValue() : 20;
        pageNo = Math.max(pageNo, 1);
        pageSize = Math.min(Math.max(pageSize, 1), 100);
        int offset = (pageNo - 1) * pageSize;

        StringBuilder sql = new StringBuilder("SELECT * FROM gms_configs t WHERE 1=1");
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM gms_configs t WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (type != null && !type.isEmpty()) {
            sql.append(" AND t.configType = ?");
            countSql.append(" AND t.configType = ?");
            params.add(type);
        }
        if (subType != null && !subType.isEmpty()) {
            sql.append(" AND t.configSubType = ?");
            countSql.append(" AND t.configSubType = ?");
            params.add(subType);
        }
        if (filter != null && !filter.isEmpty()) {
            String like = "%" + filter + "%";
            sql.append(" AND (t.configCode LIKE ? OR t.configValue LIKE ?)");
            countSql.append(" AND (t.configCode LIKE ? OR t.configValue LIKE ?)");
            params.add(like);
            params.add(like);
        }

        List<Object> countResult = DatabaseManager.executeSelect(
                (rs, alias) -> { try { return rs.getLong(1); } catch (Exception e) { return 0L; } },
                countSql.toString(), "t", params.toArray()
        );
        long total = countResult.isEmpty() ? 0 : (Long) countResult.get(0);

        List<Object> sqlParams = new ArrayList<>(params);
        sql.append(" ORDER BY t.id DESC LIMIT ? OFFSET ?");
        sqlParams.add(pageSize);
        sqlParams.add(offset);

        List<Object> raw = DatabaseManager.executeSelect(
                (rs, alias) -> {
                    try {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", rs.getInt("id"));
                        m.put("configType", rs.getString("configType"));
                        m.put("configSubType", rs.getString("configSubType"));
                        m.put("configClazz", rs.getString("configClazz"));
                        m.put("configCode", rs.getString("configCode"));
                        m.put("configValue", rs.getString("configValue"));
                        m.put("configDesc", rs.getString("configDesc"));
                        return m;
                    } catch (Exception e) { return null; }
                },
                sql.toString(), "t", sqlParams.toArray()
        );
        List<Map<String, Object>> records = new ArrayList<>();
        for (Object o : raw) {
            if (o instanceof Map) records.add((Map<String, Object>) o);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("page", pageNo);
        result.put("size", pageSize);
        result.put("total", total);
        result.put("records", records);

        return GmsApiResult.success(result);
    }

    @POST
    @Path("/addConfig")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult addConfig(Map<String, Object> body, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);
        if (body == null) return GmsApiResult.error(40000, "Missing body");

        String configType = (String) body.get("configType");
        String configCode = (String) body.get("configCode");
        if (configType == null || configCode == null) {
            return GmsApiResult.error(40000, "configType and configCode required");
        }

        DatabaseManager.executeQuery(
            "INSERT INTO gms_configs (configType, configSubType, configClazz, configCode, configValue, configDesc) VALUES (?, ?, ?, ?, ?, ?)",
            configType,
            body.get("configSubType"),
            body.get("configClazz"),
            configCode,
            body.get("configValue"),
            body.get("configDesc")
        );
        return GmsApiResult.success();
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
            "UPDATE gms_configs SET configType=?, configSubType=?, configClazz=?, configCode=?, configValue=?, configDesc=? WHERE id=?",
            body.get("configType"),
            body.get("configSubType"),
            body.get("configClazz"),
            body.get("configCode"),
            body.get("configValue"),
            body.get("configDesc"),
            id
        );
        return GmsApiResult.success();
    }

    @DELETE
    @Path("/deleteConfig/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult deleteConfig(@PathParam("id") int id, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);
        DatabaseManager.executeQuery("DELETE FROM gms_configs WHERE id = ?", id);
        return GmsApiResult.success();
    }

    @POST
    @Path("/deleteConfigList")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult deleteConfigList(List<Integer> ids, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);
        if (ids != null) {
            for (int id : ids) {
                DatabaseManager.executeQuery("DELETE FROM gms_configs WHERE id = ?", id);
            }
        }
        return GmsApiResult.success();
    }

    @GET
    @Path("/exportYml")
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult exportYml(@HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);

        List<Object> raw = DatabaseManager.executeSelect(
                (rs, alias) -> {
                    try {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", rs.getInt("id"));
                        m.put("configType", rs.getString("configType"));
                        m.put("configSubType", rs.getString("configSubType"));
                        m.put("configCode", rs.getString("configCode"));
                        m.put("configValue", rs.getString("configValue"));
                        m.put("configDesc", rs.getString("configDesc"));
                        return m;
                    } catch (Exception e) { return null; }
                },
                "SELECT * FROM gms_configs t ORDER BY configType, configCode", "t"
        );

        List<Map<String, Object>> records = new ArrayList<>();
        for (Object o : raw) {
            if (o instanceof Map) records.add((Map<String, Object>) o);
        }

        return GmsApiResult.success(records);
    }
}
