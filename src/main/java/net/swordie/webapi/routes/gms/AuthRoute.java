package net.swordie.webapi.routes.gms;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import net.swordie.ms.Server;
import net.swordie.ms.ServerConstants;
import net.swordie.ms.client.User;
import net.swordie.orm.dao.SworDaoFactory;
import net.swordie.orm.dao.UserDao;
import net.swordie.webapi.ApiConstants;
import net.swordie.webapi.protocol.result.GmsApiResult;
import net.swordie.webapi.routes.BaseResource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Path("/auth/v1")
public class AuthRoute extends BaseResource {

    private static final UserDao userDao = (UserDao) SworDaoFactory.getByClass(User.class);

    @POST
    @Path("/login")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult login(Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || password == null) {
            return GmsApiResult.error(40000, "Missing username or password");
        }

        User user = userDao.getByName(username);
        if (user == null || !user.isCorrectPassword(password)) {
            return GmsApiResult.error(40000, "Invalid username or password");
        }

        byte[] token = UUID.randomUUID().toString().getBytes();
        Server.getInstance().addAuthToken(token, user.getId(), user.isPlayer());

        Map<String, Object> data = new HashMap<>();
        data.put("token", new String(token));
        data.put("username", user.getName());
        data.put("accountType", user.getAccountType().toString());
        data.put("webadmin", user.getAccountType() == net.swordie.ms.enums.AccountType.Admin);
        data.put("id", user.getId());

        return GmsApiResult.success(data);
    }

    @DELETE
    @Path("/logout")
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult logout(@HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        if (token != null) {
            Server.getInstance().removeUserFromAuthToken(token);
        }
        return GmsApiResult.success();
    }

    @GET
    @Path("/refreshToken")
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult refreshToken(@HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        User user = authorize(token);

        Server.getInstance().removeUserFromAuthToken(token);

        byte[] newToken = UUID.randomUUID().toString().getBytes();
        Server.getInstance().addAuthToken(newToken, user.getId(), user.isPlayer());

        Map<String, Object> data = new HashMap<>();
        data.put("token", new String(newToken));

        return GmsApiResult.success(data);
    }
}
