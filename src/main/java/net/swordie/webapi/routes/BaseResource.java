package net.swordie.webapi.routes;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import net.swordie.ms.Server;
import net.swordie.ms.client.User;

public abstract class BaseResource {

    private static final String BEARER_PREFIX = "Bearer ";

    private static String extractToken(String raw) {
        if (raw == null) return null;
        if (raw.startsWith(BEARER_PREFIX)) {
            return raw.substring(BEARER_PREFIX.length());
        }
        return raw;
    }

    public User authorize(String token, int userId) {
        token = extractToken(token);
        if (token == null) {
            throw new BadRequestException("Your session has expired. Please relog in order to continue.");
        }

        var user = Server.getInstance().getUserFromAuthToken(token);
        if (user == null || userId != user.getId()) {
            throw new ForbiddenException("Invalid token");
        }

        return user;
    }

    public User authorize(String token) {
        token = extractToken(token);
        if (token == null) {
            throw new BadRequestException("Your session has expired. Please relog in order to continue.");
        }

        var user = Server.getInstance().getUserFromAuthToken(token);
        if (user == null) {
            throw new ForbiddenException("Invalid token");
        }

        return user;
    }

}
