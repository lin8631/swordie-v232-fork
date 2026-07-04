package net.swordie.webapi.routes.gms;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import net.swordie.ms.Server;
import net.swordie.ms.ServerConstants;
import net.swordie.ms.client.User;
import net.swordie.ms.enums.AccountType;
import net.swordie.webapi.ApiConstants;
import net.swordie.webapi.protocol.result.GmsApiResult;
import net.swordie.webapi.routes.BaseResource;

import java.util.HashMap;
import java.util.Map;

@Path("/server/v1")
public class ServerRoute extends BaseResource {

    @GET
    @Path("/online")
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult getServerOnline(@HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);
        Server server = Server.getInstance();
        boolean online = server.getWorlds() != null && !server.getWorlds().isEmpty()
                && server.getPlayerCount() >= 0;
        return GmsApiResult.success(online);
    }

    @GET
    @Path("/startServer")
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult startServer(@HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        User user = authorize(token);
        if (user.getAccountType() != AccountType.Admin) {
            return GmsApiResult.error(40300, "Admin only");
        }
        return GmsApiResult.success("Server is already running");
    }

    @POST
    @Path("/stopServerWithMsgAndInternal")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult stopServer(Map<String, Object> body, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        User user = authorize(token);
        if (user.getAccountType() != AccountType.Admin) {
            return GmsApiResult.error(40300, "Admin only");
        }

        int minutes = body != null && body.containsKey("minutes") ? ((Number) body.get("minutes")).intValue() : 5;
        Server.getInstance().shutdown(minutes);
        return GmsApiResult.success("Server shutting down in " + minutes + " minutes");
    }

    @GET
    @Path("/restartServer")
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult restartServer(@HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        User user = authorize(token);
        if (user.getAccountType() != AccountType.Admin) {
            return GmsApiResult.error(40300, "Admin only");
        }
        Server.getInstance().shutdown(1);
        return GmsApiResult.success("Server restarting in 1 minute");
    }

    @GET
    @Path("/shutdown")
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult shutdown(@HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        User user = authorize(token);
        if (user.getAccountType() != AccountType.Admin) {
            return GmsApiResult.error(40300, "Admin only");
        }
        Server.getInstance().shutdown(0);
        return GmsApiResult.success("Server shutting down now");
    }

    @GET
    @Path("/version")
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult getVersion(@HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);
        Map<String, Object> versionInfo = new HashMap<>();
        versionInfo.put("version", ServerConstants.VERSION);
        versionInfo.put("minorVersion", ServerConstants.MINOR_VERSION);
        return GmsApiResult.success(versionInfo);
    }

    @GET
    @Path("/stats")
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult getServerStats(@HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);
        Server server = Server.getInstance();
        int playerCount = server.getPlayerCount();
        int worldCount = server.getWorlds() != null ? server.getWorlds().size() : 0;
        int channelCount = 0;
        if (server.getWorlds() != null && !server.getWorlds().isEmpty()) {
            channelCount = server.getWorlds().get(0).getChannels().size();
        }
        Map<String, Object> stats = new HashMap<>();
        stats.put("playerCount", playerCount);
        stats.put("worldCount", worldCount);
        stats.put("channelCount", channelCount);
        stats.put("serverTime", System.currentTimeMillis());
        stats.put("serverVersion", ServerConstants.VERSION + "." + ServerConstants.MINOR_VERSION);
        return GmsApiResult.success(stats);
    }
}
