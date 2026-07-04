package net.swordie.webapi.routes.gms;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import net.swordie.ms.Server;
import net.swordie.ms.client.User;
import net.swordie.ms.client.character.Char;
import net.swordie.ms.connection.db.DatabaseManager;
import net.swordie.ms.world.Channel;
import net.swordie.ms.world.World;
import net.swordie.webapi.ApiConstants;
import net.swordie.webapi.protocol.result.GmsApiResult;
import net.swordie.webapi.routes.BaseResource;

import java.util.*;

@Path("/character/v1")
public class CharacterRoute extends BaseResource {

    @POST
    @Path("/online/list")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult getOnlineList(Map<String, Object> body, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);

        int pageNo = body != null && body.containsKey("pageNo") ? ((Number) body.get("pageNo")).intValue() : 1;
        int pageSize = body != null && body.containsKey("pageSize") ? ((Number) body.get("pageSize")).intValue() : 20;
        Object filterId = body != null ? body.get("id") : null;
        Object filterName = body != null ? body.get("name") : null;
        Object filterMap = body != null ? body.get("map") : null;
        pageNo = Math.max(pageNo, 1);
        pageSize = Math.min(Math.max(pageSize, 1), 100);

        List<Map<String, Object>> onlineChars = new ArrayList<>();
        for (World world : Server.getInstance().getWorlds()) {
            for (Channel channel : world.getChannels()) {
                for (Char chr : channel.getChars().values()) {
                    if (filterId != null && chr.getId() != ((Number) filterId).intValue()) continue;
                    if (filterName != null && !chr.getName().toLowerCase().contains(((String) filterName).toLowerCase())) continue;

                    Map<String, Object> info = new LinkedHashMap<>();
                    info.put("id", chr.getId());
                    info.put("name", chr.getName());
                    info.put("level", chr.getLevel());
                    info.put("job", chr.getJob());
                    info.put("map", chr.getField() != null ? chr.getField().getId() : 0);
                    info.put("channel", channel.getChannelId());
                    info.put("world", world.getWorldId().getVal());
                    info.put("accountId", chr.getAccount() != null ? chr.getAccount().getId() : 0);
                    onlineChars.add(info);
                }
            }
        }

        int total = onlineChars.size();
        int fromIdx = (pageNo - 1) * pageSize;
        int toIdx = Math.min(fromIdx + pageSize, total);
        List<Map<String, Object>> page = fromIdx < total ? onlineChars.subList(fromIdx, toIdx) : new ArrayList<>();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("page", pageNo);
        result.put("size", pageSize);
        result.put("total", total);
        result.put("records", page);

        return GmsApiResult.success(result);
    }
}
