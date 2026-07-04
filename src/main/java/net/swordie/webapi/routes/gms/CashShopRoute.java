package net.swordie.webapi.routes.gms;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import net.swordie.ms.connection.db.DatabaseManager;
import net.swordie.webapi.ApiConstants;
import net.swordie.webapi.protocol.result.GmsApiResult;
import net.swordie.webapi.routes.BaseResource;

import java.util.*;

@Path("/cashShop/v1")
public class CashShopRoute extends BaseResource {

    @GET
    @Path("/getAllCategoryList")
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult getAllCategoryList(@HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);

        List<Object> raw = DatabaseManager.executeSelect(
                (rs, alias) -> {
                    try {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", rs.getInt("id"));
                        m.put("name", rs.getString("name"));
                        m.put("parent", rs.getInt("parent"));
                        m.put("priority", rs.getInt("priority"));
                        return m;
                    } catch (Exception e) { return null; }
                },
                "SELECT id, name, parent, priority FROM cs_categories t ORDER BY priority",
                "t"
        );
        List<Map<String, Object>> categories = new ArrayList<>();
        for (Object o : raw) {
            if (o instanceof Map) categories.add((Map<String, Object>) o);
        }
        return GmsApiResult.success(categories);
    }

    @POST
    @Path("/getCommodityByCategory")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult getCommodityByCategory(Map<String, Object> body,
                                                @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);

        int pageNo = body != null && body.containsKey("pageNo") ? ((Number) body.get("pageNo")).intValue() : 1;
        int subId = body != null && body.containsKey("subId") ? ((Number) body.get("subId")).intValue() : 0;
        Integer itemId = body != null && body.containsKey("itemId") ? ((Number) body.get("itemId")).intValue() : null;
        Boolean onSale = body != null && body.containsKey("onSale") ? (Boolean) body.get("onSale") : null;
        pageNo = Math.max(pageNo, 1);
        int pageSize = 50;
        int offset = (pageNo - 1) * pageSize;

        StringBuilder sql = new StringBuilder("SELECT * FROM cs_items t WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (subId > 0) {
            sql.append(" AND t.category = ?");
            params.add(subId);
        }
        if (itemId != null && itemId > 0) {
            sql.append(" AND t.itemID = ?");
            params.add(itemId);
        }
        if (onSale != null) {
            sql.append(" AND t.onSale = ?");
            params.add(onSale ? 1 : 0);
        }

        sql.append(" ORDER BY t.id DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add(offset);

        List<Object> raw = DatabaseManager.executeSelect(
                (rs, alias) -> {
                    try {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", rs.getInt("id"));
                        m.put("itemID", rs.getInt("itemID"));
                        m.put("stock", rs.getInt("stock"));
                        m.put("price", rs.getInt("price"));
                        m.put("oldPrice", rs.getInt("oldPrice"));
                        m.put("newPrice", rs.getInt("newPrice"));
                        m.put("bundleQuantity", rs.getInt("bundleQuantity"));
                        m.put("availableDays", rs.getInt("availableDays"));
                        m.put("category", rs.getInt("category"));
                        m.put("shopItemFlag", rs.getInt("shopItemFlag"));
                        m.put("onSale", rs.getInt("onSale"));
                        m.put("buyableWithMaplePoints", rs.getInt("buyableWithMaplePoints"));
                        m.put("buyableWithCredit", rs.getInt("buyableWithCredit"));
                        m.put("buyableWithPrepaid", rs.getInt("buyableWithPrepaid"));
                        return m;
                    } catch (Exception e) { return null; }
                },
                sql.toString(), "t", params.toArray()
        );
        List<Map<String, Object>> items = new ArrayList<>();
        for (Object o : raw) {
            if (o instanceof Map) items.add((Map<String, Object>) o);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("page", pageNo);
        result.put("records", items);
        return GmsApiResult.success(result);
    }

    @POST
    @Path("/onSale")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult onSale(Map<String, Object> body, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);
        if (body == null || !body.containsKey("sn")) {
            return GmsApiResult.error(40000, "Missing sn");
        }
        int sn = ((Number) body.get("sn")).intValue();
        DatabaseManager.executeQuery("UPDATE cs_items SET onSale = 1 WHERE id = ?", sn);
        return GmsApiResult.success();
    }

    @POST
    @Path("/offSale")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult offSale(Map<String, Object> body, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);
        if (body == null || !body.containsKey("sn")) {
            return GmsApiResult.error(40000, "Missing sn");
        }
        int sn = ((Number) body.get("sn")).intValue();
        DatabaseManager.executeQuery("UPDATE cs_items SET onSale = 0 WHERE id = ?", sn);
        return GmsApiResult.success();
    }

    @POST
    @Path("/batchOnSale")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult batchOnSale(Map<String, Object> body, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);
        if (body == null) return GmsApiResult.error(40000, "Missing body");

        Object rawData = body.get("data");
        String type = (String) body.get("type");
        Number value = body.get("value") != null ? (Number) body.get("value") : null;

        int onSaleVal = "onSale".equals(type) ? 1 : 0;

        if (rawData instanceof List) {
            List<?> items = (List<?>) rawData;
            for (Object item : items) {
                if (item instanceof Map) {
                    Object sn = ((Map<?, ?>) item).get("sn");
                    if (sn instanceof Number) {
                        DatabaseManager.executeQuery("UPDATE cs_items SET onSale = ? WHERE id = ?", onSaleVal, ((Number) sn).intValue());
                    }
                }
            }
        }

        return GmsApiResult.success();
    }
}
