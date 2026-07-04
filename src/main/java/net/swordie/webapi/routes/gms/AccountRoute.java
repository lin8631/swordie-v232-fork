package net.swordie.webapi.routes.gms;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import net.swordie.ms.Server;
import net.swordie.ms.client.User;
import net.swordie.ms.connection.db.DatabaseManager;
import net.swordie.ms.enums.AccountType;
import net.swordie.orm.dao.SworDaoFactory;
import net.swordie.orm.dao.UserDao;
import net.swordie.webapi.ApiConstants;
import net.swordie.webapi.protocol.result.GmsApiResult;
import net.swordie.webapi.routes.BaseResource;

import java.util.*;

@Path("/account/v1")
public class AccountRoute extends BaseResource {

    private static final UserDao userDao = (UserDao) SworDaoFactory.getByClass(User.class);

    @GET
    @Path("")
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult getAccountList(
            @QueryParam("page") int page,
            @QueryParam("size") int size,
            @QueryParam("id") Integer id,
            @QueryParam("name") String name,
            @QueryParam("lastLoginStart") String lastLoginStart,
            @QueryParam("lastLoginEnd") String lastLoginEnd,
            @QueryParam("createdAtStart") String createdAtStart,
            @QueryParam("createdAtEnd") String createdAtEnd,
            @HeaderParam(ApiConstants.TOKEN_HEADER) String token
    ) {
        authorize(token);

        page = Math.max(page, 1);
        size = size <= 0 ? 20 : Math.min(size, 100);
        int offset = (page - 1) * size;

        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM users t WHERE 1=1");
        StringBuilder dataSql = new StringBuilder("SELECT * FROM users t WHERE 1=1");

        List<Object> params = new ArrayList<>();
        List<Object> countParams = new ArrayList<>();

        if (id != null) {
            countSql.append(" AND t.id = ?");
            dataSql.append(" AND t.id = ?");
            params.add(id);
            countParams.add(id);
        }
        if (name != null && !name.isEmpty()) {
            countSql.append(" AND t.name LIKE ?");
            dataSql.append(" AND t.name LIKE ?");
            params.add("%" + name + "%");
            countParams.add("%" + name + "%");
        }

        dataSql.append(" ORDER BY t.id DESC LIMIT ? OFFSET ?");
        params.add(size);
        params.add(offset);

        long total = 0;
        List<Object> countResult = DatabaseManager.executeSelect(
            (java.sql.ResultSet rs, String alias) -> {
                try { return rs.getLong(1); } catch (Exception e) { return 0L; }
            }, countSql.toString(), "t", countParams.toArray());

        if (!countResult.isEmpty()) {
            total = (Long) countResult.get(0);
        }

        List<Map<String, Object>> records = new ArrayList<>();
        List<Object> dataResult = DatabaseManager.executeSelect(
                (rs, alias) -> {
                    try {
                        Map<String, Object> map = new LinkedHashMap<>();
                        map.put("id", rs.getInt("id"));
                        map.put("name", rs.getString("name"));
                        map.put("email", rs.getString("email"));
                        map.put("accountType", rs.getString("accounttype"));
                        map.put("banExpireDate", rs.getObject("banExpireDate"));
                        map.put("creationDate", String.valueOf(rs.getTimestamp("creationdate")));
                        map.put("votePoints", rs.getInt("votepoints"));
                        map.put("donationPoints", rs.getInt("donationpoints"));
                        map.put("characterSlots", rs.getInt("characterslots"));
                        return map;
                    } catch (Exception e) {
                        return null;
                    }
                },
                dataSql.toString(), "t",
                params.toArray()
        );

        for (Object obj : dataResult) {
            if (obj instanceof Map) {
                records.add((Map<String, Object>) obj);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("page", page);
        result.put("size", size);
        result.put("total", total);
        result.put("records", records);

        return GmsApiResult.success(result);
    }

    @POST
    @Path("")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult addAccount(Map<String, Object> body, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);

        String name = (String) body.get("name");
        String password = (String) body.get("password");

        if (name == null || password == null) {
            return GmsApiResult.error(40000, "Missing required fields");
        }

        User existing = userDao.getByName(name);
        if (existing != null) {
            return GmsApiResult.error(40000, "Username already exists");
        }

        User user = new User(name);
        user.setPasswordAndHash(password);
        if (body.containsKey("email")) {
            user.setEmail((String) body.get("email"));
        }
        user.setCharacterSlots(12);
        userDao.saveOrUpdate(user, null);

        return GmsApiResult.success();
    }

    @PUT
    @Path("/{id}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult updateAccountByGM(
            @PathParam("id") int id,
            Map<String, Object> body,
            @HeaderParam(ApiConstants.TOKEN_HEADER) String token
    ) {
        User admin = authorize(token);
        if (admin.getAccountType() != AccountType.Admin) {
            return GmsApiResult.error(40300, "Admin only");
        }

        User user = userDao.getById(id);
        if (user == null) {
            return GmsApiResult.error(40400, "User not found");
        }

        if (body.containsKey("email")) {
            user.setEmail((String) body.get("email"));
        }
        if (body.containsKey("newPwd")) {
            user.setPasswordAndHash((String) body.get("newPwd"));
        }
        if (body.containsKey("donationPoints")) {
            int dp = ((Number) body.get("donationPoints")).intValue();
            user.setDonationPoints(dp);
        }
        if (body.containsKey("characterSlots")) {
            int slots = ((Number) body.get("characterSlots")).intValue();
            user.setCharacterSlots(slots);
        }
        if (body.containsKey("accountType")) {
            user.setAccountType(AccountType.valueOf((String) body.get("accountType")));
        }

        userDao.saveProperties(user);
        return GmsApiResult.success();
    }

    @DELETE
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult deleteAccount(@PathParam("id") int id, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        User admin = authorize(token);
        if (admin.getAccountType() != AccountType.Admin) {
            return GmsApiResult.error(40300, "Admin only");
        }

        DatabaseManager.executeQuery("DELETE FROM users WHERE id = ?", id);
        return GmsApiResult.success();
    }

    @PUT
    @Path("/{id}/ban")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult banAccount(@PathParam("id") int id, Map<String, Object> body,
                                   @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);
        String reason = body != null ? (String) body.get("reason") : null;
        DatabaseManager.executeQuery("UPDATE users SET banExpireDate = DATE_ADD(NOW(), INTERVAL 365 DAY) WHERE id = ?", id);
        return GmsApiResult.success();
    }

    @PUT
    @Path("/{id}/unban")
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult unbanAccount(@PathParam("id") int id, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);
        DatabaseManager.executeQuery("UPDATE users SET banExpireDate = NULL WHERE id = ?", id);
        return GmsApiResult.success();
    }

    @PUT
    @Path("/{id}/reset/logged")
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult resetLoggedIn(@PathParam("id") int id, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);
        // loggedIn column removed in later schema - no-op for now
        return GmsApiResult.success();
    }

    @GET
    @Path("/info")
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult getUserInfo(@HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        User user = authorize(token);

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("id", user.getId());
        info.put("username", user.getName());
        info.put("email", user.getEmail());
        info.put("accountType", user.getAccountType().toString());
        info.put("webadmin", user.getAccountType() == net.swordie.ms.enums.AccountType.Admin);
        info.put("donationPoints", user.getDonationPoints());
        info.put("votePoints", user.getVotePoints());
        info.put("characterSlots", user.getCharacterSlots());

        return GmsApiResult.success(info);
    }

    @GET
    @Path("/menu")
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult getMenuList(@HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);

        List<Map<String, Object>> menus = new ArrayList<>();
        String[][] menuDefs = {
            {"dashboard", "Dashboard", "icon-dashboard"},
            {"account", "Accounts", "icon-user"},
            {"game", "Game", "icon-game"}
        };

        Map<String, List<Map<String, Object>>> children = new LinkedHashMap<>();
        
        List<Map<String, Object>> dashboardChildren = new ArrayList<>();
        Map<String, Object> workplace = new LinkedHashMap<>();
        workplace.put("name", "workplace");
        workplace.put("title", "Server Control");
        workplace.put("path", "/dashboard/workplace");
        dashboardChildren.add(workplace);
        children.put("dashboard", dashboardChildren);

        List<Map<String, Object>> accountChildren = new ArrayList<>();
        Map<String, Object> accList = new LinkedHashMap<>();
        accList.put("name", "list");
        accList.put("title", "Account List");
        accList.put("path", "/account/list");
        accountChildren.add(accList);
        Map<String, Object> playerList = new LinkedHashMap<>();
        playerList.put("name", "player");
        playerList.put("title", "Player List");
        playerList.put("path", "/account/player");
        accountChildren.add(playerList);
        children.put("account", accountChildren);

        List<Map<String, Object>> gameChildren = new ArrayList<>();
        String[][] gameItems = {
            {"config", "Server Config", "/game/config"},
            {"cashShop", "Cash Shop", "/game/cashShop"},
            {"npcShop", "NPC Shop", "/game/npcShop"},
            {"drop", "Drop Data", "/game/drop"},
            {"globalDrop", "Global Drop", "/game/globalDrop"},
            {"inventory", "Inventory", "/game/inventory"},
            {"gachapon", "Gachapon", "/game/gachapon"},
            {"commandInfo", "Commands", "/game/commandInfo"},
            {"file", "File Editor", "/game/file"},
            {"autoban", "Autoban", "/game/autoban"},
            {"informationSearch", "Information Search", "/dashboard/informationSearch"}
        };
        for (String[] item : gameItems) {
            Map<String, Object> mi = new LinkedHashMap<>();
            mi.put("name", item[0]);
            mi.put("title", item[1]);
            mi.put("path", item[2]);
            gameChildren.add(mi);
        }
        children.put("game", gameChildren);

        for (String[] m : menuDefs) {
            Map<String, Object> menu = new LinkedHashMap<>();
            menu.put("name", m[0]);
            menu.put("title", m[1]);
            menu.put("icon", m[2]);
            menu.put("children", children.getOrDefault(m[0], new ArrayList<>()));
            menus.add(menu);
        }

        return GmsApiResult.success(menus);
    }
}
