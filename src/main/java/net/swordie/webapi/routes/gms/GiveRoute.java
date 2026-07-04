package net.swordie.webapi.routes.gms;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import net.swordie.ms.Server;
import net.swordie.ms.client.User;
import net.swordie.ms.client.character.Char;
import net.swordie.ms.client.character.items.Item;
import net.swordie.ms.connection.db.DatabaseManager;
import net.swordie.orm.dao.CharDao;
import net.swordie.orm.dao.SworDaoFactory;
import net.swordie.webapi.ApiConstants;
import net.swordie.webapi.protocol.result.GmsApiResult;
import net.swordie.webapi.routes.BaseResource;

import java.util.List;
import java.util.Map;

@Path("/give/v1")
public class GiveRoute extends BaseResource {

    @POST
    @Path("/resource")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult giveResource(Map<String, Object> body, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        User user = authorize(token);

        String type = (String) body.get("type");
        if (type == null) {
            return GmsApiResult.error(40000, "Missing type field");
        }

        if (body.containsKey("player")) {
            String playerName = (String) body.get("player");
            CharDao charDao = (CharDao) SworDaoFactory.getByClass(Char.class);

            List<Object> results = DatabaseManager.executeSelect(
                    (rs, alias) -> {
                        try {
                            Map<String, Object> chr = new java.util.LinkedHashMap<>();
                            chr.put("id", rs.getInt("id"));
                            chr.put("name", rs.getString("name"));
                            chr.put("level", rs.getInt("level"));
                            chr.put("job", rs.getInt("job"));
                            return chr;
                        } catch (Exception e) {
                            return null;
                        }
                    },
                    "SELECT id, name, level, job FROM characters t WHERE t.name = ?",
                    "t", playerName
            );

            if (results.isEmpty()) {
                return GmsApiResult.error(40400, "Player not found");
            }
        }

        return GmsApiResult.success("Resource given successfully");
    }
}
