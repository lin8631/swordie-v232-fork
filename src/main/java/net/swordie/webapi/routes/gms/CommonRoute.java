package net.swordie.webapi.routes.gms;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import net.swordie.ms.connection.db.DatabaseManager;
import net.swordie.ms.loaders.StringData;
import net.swordie.ms.loaders.containerclasses.SkillStringInfo;
import net.swordie.webapi.ApiConstants;
import net.swordie.webapi.protocol.result.GmsApiResult;
import net.swordie.webapi.routes.BaseResource;

import java.util.*;

@Path("/common/v1")
public class CommonRoute extends BaseResource {

    private static final int LIMIT_PER_TYPE = 30;

    @POST
    @Path("/informationSearch")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult informationSearch(Map<String, Object> body, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);

        List<String> types = body != null ? (List<String>) body.get("types") : null;
        String filter = body != null ? (String) body.get("filter") : null;

        List<Map<String, Object>> results = new ArrayList<>();
        boolean hasFilter = filter != null && !filter.isEmpty();
        String lowerFilter = hasFilter ? filter.toLowerCase() : "";

        if (types == null || types.isEmpty()) {
            types = new ArrayList<>(Arrays.asList("mob", "map", "npc", "skill", "consume", "eqp", "etc", "ins", "cash"));
        }

        Set<String> typeSet = new HashSet<>(types);

        for (String type : typeSet) {
            if (results.size() >= LIMIT_PER_TYPE * 5) break;
            searchByType(type, lowerFilter, hasFilter, results);
        }

        if (typeSet.contains("character")) {
            searchCharacters(filter, hasFilter, results);
        }

        return GmsApiResult.success(results);
    }

    private void searchByType(String type, String lowerFilter, boolean hasFilter, List<Map<String, Object>> results) {
        switch (type) {
            case "mob":
                searchStringMap(StringData.getMobStrings(), "mob", lowerFilter, results);
                break;
            case "map":
                searchStringMap(StringData.getMapStrings(), "map", lowerFilter, results);
                break;
            case "npc":
                searchStringMap(StringData.getNpcStrings(), "npc", lowerFilter, results);
                break;
            case "skill":
                searchSkillMap(lowerFilter, results);
                break;
            case "pet":
                searchItemIdRange(StringData.getItemStrings(), "pet", lowerFilter, 5000000, 5999999, results);
                break;
            case "cash":
                searchItemIdRange(StringData.getItemStrings(), "cash", lowerFilter, 5000000, Integer.MAX_VALUE, results);
                break;
            case "consume":
                searchItemIdRange(StringData.getItemStrings(), "consume", lowerFilter, 2000000, 2999999, results);
                break;
            case "eqp":
                searchItemIdRange(StringData.getItemStrings(), "eqp", lowerFilter, 1000000, 1999999, results);
                break;
            case "etc":
                searchItemIdRange(StringData.getItemStrings(), "etc", lowerFilter, 4000000, 4999999, results);
                break;
            case "ins":
                searchItemIdRange(StringData.getItemStrings(), "ins", lowerFilter, 3000000, 3999999, results);
                break;
        }
    }

    private void searchStringMap(Map<Integer, String> stringMap, String type, String lowerFilter, List<Map<String, Object>> results) {
        if (stringMap == null) return;
        int count = 0;
        for (Map.Entry<Integer, String> entry : stringMap.entrySet()) {
            if (count >= LIMIT_PER_TYPE) break;
            String name = entry.getValue();
            if (name == null) continue;
            if (!lowerFilter.isEmpty() && !name.toLowerCase().contains(lowerFilter)) continue;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("type", type);
            m.put("id", entry.getKey());
            m.put("name", name);
            m.put("desc", type + " #" + entry.getKey());
            results.add(m);
            count++;
        }
    }

    private void searchSkillMap(String lowerFilter, List<Map<String, Object>> results) {
        Map<Integer, SkillStringInfo> skillMap = StringData.getSkillString();
        if (skillMap == null) return;
        int count = 0;
        for (Map.Entry<Integer, SkillStringInfo> entry : skillMap.entrySet()) {
            if (count >= LIMIT_PER_TYPE) break;
            String name = entry.getValue() != null ? entry.getValue().getName() : null;
            if (name == null) continue;
            if (!lowerFilter.isEmpty() && !name.toLowerCase().contains(lowerFilter)) continue;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("type", "skill");
            m.put("id", entry.getKey());
            m.put("name", name);
            m.put("desc", "skill #" + entry.getKey());
            results.add(m);
            count++;
        }
    }

    private void searchItemIdRange(Map<Integer, String> itemMap, String type, String lowerFilter, int minId, int maxId, List<Map<String, Object>> results) {
        if (itemMap == null) return;
        int count = 0;
        for (Map.Entry<Integer, String> entry : itemMap.entrySet()) {
            if (count >= LIMIT_PER_TYPE) break;
            int id = entry.getKey();
            if (id < minId || id > maxId) continue;
            String name = entry.getValue();
            if (name == null) continue;
            if (!lowerFilter.isEmpty() && !name.toLowerCase().contains(lowerFilter)) continue;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("type", type);
            m.put("id", id);
            m.put("name", name);
            m.put("desc", type + " #" + id);
            results.add(m);
            count++;
        }
    }

    private void searchCharacters(String filter, boolean hasFilter, List<Map<String, Object>> results) {
        String sql = "SELECT id, name, level, job FROM characters t WHERE 1=1";
        List<Object> params = new ArrayList<>();
        if (hasFilter) {
            sql += " AND t.name LIKE ?";
            params.add("%" + filter + "%");
        }
        sql += " LIMIT " + LIMIT_PER_TYPE;

        List<Object> raw = DatabaseManager.executeSelect(
                (rs, alias) -> {
                    try {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("type", "character");
                        m.put("id", rs.getInt("id"));
                        m.put("name", rs.getString("name"));
                        m.put("desc", "Lv." + rs.getInt("level") + " Job:" + rs.getInt("job"));
                        return m;
                    } catch (Exception e) { return null; }
                },
                sql, "t", params.toArray()
        );
        for (Object o : raw) {
            if (o instanceof Map) results.add((Map<String, Object>) o);
        }
    }

    @POST
    @Path("/getEquipmentInfoByItemId")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult getEquipmentInfoByItemId(Map<String, Object> body,
                                                  @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);

        int itemId = body != null && body.containsKey("id") ? ((Number) body.get("id")).intValue() : 0;

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("itemId", itemId);
        info.put("name", "Item #" + itemId);

        List<Object> raw = DatabaseManager.executeSelect(
                (rs, alias) -> {
                    try {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", rs.getInt("id"));
                        m.put("itemid", rs.getInt("itemid"));
                        m.put("type", rs.getInt("type"));
                        m.put("inventoryType", rs.getInt("inventorytype"));
                        return m;
                    } catch (Exception e) { return null; }
                },
                "SELECT id, itemid, type, inventorytype FROM items t WHERE t.itemid = ? LIMIT 1",
                "t", itemId
        );

        if (!raw.isEmpty() && raw.get(0) instanceof Map) {
            info.put("exampleItem", raw.get(0));
        }

        return GmsApiResult.success(info);
    }
}
