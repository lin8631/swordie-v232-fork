package net.swordie.webapi.routes.gms;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import net.swordie.ms.connection.db.DatabaseManager;
import net.swordie.webapi.ApiConstants;
import net.swordie.webapi.protocol.result.GmsApiResult;
import net.swordie.webapi.routes.BaseResource;

import java.util.*;

@Path("/shop/v1")
public class NpcShopRoute extends BaseResource {

    @POST
    @Path("/getShopList")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult getShopList(Map<String, Object> body, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);

        int pageNo = body != null && body.containsKey("pageNo") ? ((Number) body.get("pageNo")).intValue() : 1;
        int pageSize = body != null && body.containsKey("pageSize") ? ((Number) body.get("pageSize")).intValue() : 20;
        Integer shopId = body != null && body.containsKey("shopId") ? ((Number) body.get("shopId")).intValue() : null;
        pageNo = Math.max(pageNo, 1);
        pageSize = Math.min(Math.max(pageSize, 1), 100);

        StringBuilder sql = new StringBuilder("SELECT DISTINCT t.shopID FROM shopitems t WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (shopId != null) {
            sql.append(" AND t.shopID = ?");
            params.add(shopId);
        }
        sql.append(" ORDER BY t.shopID");

        List<Object> raw = DatabaseManager.executeSelect(
                (rs, alias) -> { try { return rs.getInt("shopID"); } catch (Exception e) { return 0; } },
                sql.toString(), "t", params.toArray()
        );
        Set<Integer> shopIds = new LinkedHashSet<>();
        for (Object o : raw) shopIds.add((Integer) o);

        List<Map<String, Object>> records = new ArrayList<>();
        for (Integer sid : shopIds) {
            Map<String, Object> s = new LinkedHashMap<>();
            s.put("shopID", sid);
            s.put("name", "Shop #" + sid);
            records.add(s);
        }

        int total = records.size();
        int fromIdx = (pageNo - 1) * pageSize;
        int toIdx = Math.min(fromIdx + pageSize, total);
        List<Map<String, Object>> page = fromIdx < total ? records.subList(fromIdx, toIdx) : new ArrayList<>();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("page", pageNo);
        result.put("size", pageSize);
        result.put("total", total);
        result.put("records", page);

        return GmsApiResult.success(result);
    }

    @POST
    @Path("/getShopItemList")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult getShopItemList(Map<String, Object> body, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);

        int shopId = body != null && body.containsKey("shopId") ? ((Number) body.get("shopId")).intValue() : 0;
        int pageNo = body != null && body.containsKey("pageNo") ? ((Number) body.get("pageNo")).intValue() : 1;
        int pageSize = body != null && body.containsKey("pageSize") ? ((Number) body.get("pageSize")).intValue() : 20;
        boolean onlyTotal = body != null && Boolean.TRUE.equals(body.get("onlyTotal"));
        boolean notPage = body != null && Boolean.TRUE.equals(body.get("notPage"));
        pageNo = Math.max(pageNo, 1);
        pageSize = Math.min(Math.max(pageSize, 1), 200);
        int offset = (pageNo - 1) * pageSize;

        StringBuilder sql = new StringBuilder("SELECT * FROM shopitems t WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (shopId > 0) {
            sql.append(" AND t.shopID = ?");
            params.add(shopId);
        }
        sql.append(" ORDER BY t.id");

        if (!notPage) {
            sql.append(" LIMIT ? OFFSET ?");
            params.add(pageSize);
            params.add(offset);
        }

        List<Object> raw = DatabaseManager.executeSelect(
                (rs, alias) -> {
                    try {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", rs.getInt("id"));
                        m.put("shopID", rs.getInt("shopID"));
                        m.put("itemID", rs.getInt("itemID"));
                        m.put("price", rs.getInt("price"));
                        m.put("tokenItemID", rs.getInt("tokenItemID"));
                        m.put("tokenPrice", rs.getInt("tokenPrice"));
                        m.put("quantity", rs.getInt("quantity"));
                        m.put("itemPeriod", rs.getInt("itemPeriod"));
                        m.put("maxPerSlot", rs.getInt("maxPerSlot"));
                        m.put("tabIndex", rs.getInt("tabIndex"));
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

    @DELETE
    @Path("/deleteShopItem/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult deleteShopItem(@PathParam("id") int id, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);
        DatabaseManager.executeQuery("DELETE FROM shopitems WHERE id = ?", id);
        return GmsApiResult.success();
    }

    @PUT
    @Path("/addShopItem")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult addShopItem(Map<String, Object> body, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);
        if (body == null) return GmsApiResult.error(40000, "Missing body");

        int shopId = body.containsKey("shopID") ? ((Number) body.get("shopID")).intValue() : 0;
        int itemId = body.containsKey("itemID") ? ((Number) body.get("itemID")).intValue() : 0;
        int price = body.containsKey("price") ? ((Number) body.get("price")).intValue() : 0;

        DatabaseManager.executeQuery(
            "INSERT INTO shopitems (shopID, itemID, price) VALUES (?, ?, ?)",
            shopId, itemId, price
        );
        return GmsApiResult.success();
    }

    @POST
    @Path("/updateShopItem")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult updateShopItem(Map<String, Object> body, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);
        if (body == null || !body.containsKey("id")) {
            return GmsApiResult.error(40000, "Missing id");
        }
        int id = ((Number) body.get("id")).intValue();
        int shopId = body.containsKey("shopID") ? ((Number) body.get("shopID")).intValue() : 0;
        int itemId = body.containsKey("itemID") ? ((Number) body.get("itemID")).intValue() : 0;
        int price = body.containsKey("price") ? ((Number) body.get("price")).intValue() : 0;
        int quantity = body.containsKey("quantity") ? ((Number) body.get("quantity")).intValue() : 1;
        int itemPeriod = body.containsKey("itemPeriod") ? ((Number) body.get("itemPeriod")).intValue() : 0;

        DatabaseManager.executeQuery(
            "UPDATE shopitems SET shopID=?, itemID=?, price=?, quantity=?, itemPeriod=? WHERE id=?",
            shopId, itemId, price, quantity, itemPeriod, id
        );
        return GmsApiResult.success();
    }
}
