package net.swordie.webapi.routes.gms;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import net.swordie.ms.connection.db.DatabaseManager;
import net.swordie.webapi.ApiConstants;
import net.swordie.webapi.protocol.result.GmsApiResult;
import net.swordie.webapi.routes.BaseResource;

import java.util.*;

@Path("/inventory/v1")
public class InventoryRoute extends BaseResource {

    @GET
    @Path("/getInventoryTypeList")
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult getInventoryTypeList(@HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);

        String[][] types = {
            {"1", "EQUIP"},
            {"2", "CONSUME"},
            {"3", "INSTALL"},
            {"4", "ETC"},
            {"5", "CASH"}
        };
        List<Map<String, Object>> list = new ArrayList<>();
        for (String[] t : types) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", Integer.parseInt(t[0]));
            m.put("name", t[1]);
            list.add(m);
        }
        return GmsApiResult.success(list);
    }

    @POST
    @Path("/getCharacterList")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult getCharacterList(Map<String, Object> body, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);

        String charName = body != null ? (String) body.get("characterName") : null;
        Object accountId = body != null ? body.get("accountId") : null;

        StringBuilder sql = new StringBuilder("SELECT t.id, t.name, t.level, t.job, t.accId FROM characters t WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (charName != null && !charName.isEmpty()) {
            sql.append(" AND t.name LIKE ?");
            params.add("%" + charName + "%");
        }
        if (accountId != null && ((Number) accountId).intValue() > 0) {
            sql.append(" AND t.accId = ?");
            params.add(((Number) accountId).intValue());
        }
        sql.append(" LIMIT 100");

        List<Object> raw = DatabaseManager.executeSelect(
                (rs, alias) -> {
                    try {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", rs.getInt("id"));
                        m.put("name", rs.getString("name"));
                        m.put("level", rs.getInt("level"));
                        m.put("job", rs.getInt("job"));
                        m.put("accId", rs.getInt("accId"));
                        return m;
                    } catch (Exception e) { return null; }
                },
                sql.toString(), "t", params.toArray()
        );
        List<Map<String, Object>> chars = new ArrayList<>();
        for (Object o : raw) {
            if (o instanceof Map) chars.add((Map<String, Object>) o);
        }
        return GmsApiResult.success(chars);
    }

    @POST
    @Path("/getInventoryList")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult getInventoryList(Map<String, Object> body, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);

        Integer charId = body != null && body.containsKey("characterId") ? ((Number) body.get("characterId")).intValue() : null;
        Integer invType = body != null && body.containsKey("inventoryType") ? ((Number) body.get("inventoryType")).intValue() : null;
        int pageNo = body != null && body.containsKey("pageNo") ? ((Number) body.get("pageNo")).intValue() : 1;
        int pageSize = body != null && body.containsKey("pageSize") ? ((Number) body.get("pageSize")).intValue() : 20;
        pageNo = Math.max(pageNo, 1);
        pageSize = Math.min(Math.max(pageSize, 1), 100);
        int offset = (pageNo - 1) * pageSize;

        StringBuilder sql = new StringBuilder(
            "SELECT t.id, t.itemId, t.bagIndex, t.invType, t.quantity, t.owner, t.dateExpire " +
            "FROM items t LEFT JOIN inventories inv ON t.inventoryId = inv.id WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (charId != null) {
            sql.append(" AND inv.characterId = ?");
            params.add(charId);
        }
        if (invType != null) {
            sql.append(" AND t.invType = ?");
            params.add(invType);
        }
        sql.append(" ORDER BY t.id DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add(offset);

        List<Object> raw = DatabaseManager.executeSelect(
                (rs, alias) -> {
                    try {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", rs.getInt("id"));
                        m.put("itemId", rs.getInt("itemId"));
                        m.put("bagIndex", rs.getInt("bagIndex"));
                        m.put("invType", rs.getInt("invType"));
                        m.put("quantity", rs.getInt("quantity"));
                        m.put("owner", rs.getString("owner"));
                        m.put("dateExpire", rs.getString("dateExpire"));
                        return m;
                    } catch (Exception e) { return null; }
                },
                sql.toString(), "t", params.toArray()
        );
        List<Map<String, Object>> records = new ArrayList<>();
        for (Object o : raw) {
            if (o instanceof Map) records.add((Map<String, Object>) o);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("page", pageNo);
        result.put("size", pageSize);
        result.put("records", records);
        return GmsApiResult.success(result);
    }

    @POST
    @Path("/updateInventory")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult updateInventory(Map<String, Object> body, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);
        if (body == null || !body.containsKey("id")) {
            return GmsApiResult.error(40000, "Missing id");
        }
        int id = ((Number) body.get("id")).intValue();
        int quantity = body.containsKey("quantity") ? ((Number) body.get("quantity")).intValue() : 0;

        DatabaseManager.executeQuery("UPDATE items SET quantity = ? WHERE id = ?", quantity, id);
        return GmsApiResult.success();
    }

    @POST
    @Path("/deleteInventory")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult deleteInventory(Map<String, Object> body, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);
        if (body == null || !body.containsKey("id")) {
            return GmsApiResult.error(40000, "Missing id");
        }
        int id = ((Number) body.get("id")).intValue();
        DatabaseManager.executeQuery("DELETE FROM items WHERE id = ?", id);
        return GmsApiResult.success();
    }
}
