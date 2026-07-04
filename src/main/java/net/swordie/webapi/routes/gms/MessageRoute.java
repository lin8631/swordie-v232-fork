package net.swordie.webapi.routes.gms;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import net.swordie.webapi.ApiConstants;
import net.swordie.webapi.protocol.result.GmsApiResult;
import net.swordie.webapi.routes.BaseResource;

import java.util.*;

@Path("/api/message")
public class MessageRoute extends BaseResource {

    @POST
    @Path("/list")
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult queryMessageList(@HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);
        List<Map<String, Object>> messages = new ArrayList<>();
        return GmsApiResult.success(messages);
    }

    @POST
    @Path("/read")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult setMessageStatus(Map<String, Object> body, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);
        return GmsApiResult.success();
    }

    @POST
    @Path("/chat/list")
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult queryChatList(@HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);
        List<Map<String, Object>> chats = new ArrayList<>();
        return GmsApiResult.success(chats);
    }
}
