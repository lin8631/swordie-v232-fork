package net.swordie.webapi.routes.gms;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import net.swordie.ms.ServerConstants;
import net.swordie.webapi.ApiConstants;
import net.swordie.webapi.protocol.result.GmsApiResult;
import net.swordie.webapi.routes.BaseResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import net.swordie.ms.client.User;
import net.swordie.ms.enums.AccountType;

@Path("/file/v1")
public class FileRoute extends BaseResource {

    private static final String BASE_PATH = ServerConstants.DIR + "/src";

    @POST
    @Path("/tree")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult treeFile(Map<String, Object> body, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);

        String currentKey = body != null ? (String) body.get("currentKey") : null;
        File dir;
        if (currentKey != null && !currentKey.isEmpty()) {
            dir = new File(BASE_PATH, currentKey);
        } else {
            dir = new File(BASE_PATH);
        }

        if (!dir.exists() || !dir.isDirectory()) {
            return GmsApiResult.error(40400, "Directory not found");
        }

        List<Map<String, Object>> children = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files != null) {
            Arrays.sort(files, (a, b) -> {
                if (a.isDirectory() && !b.isDirectory()) return -1;
                if (!a.isDirectory() && b.isDirectory()) return 1;
                return a.getName().compareToIgnoreCase(b.getName());
            });
            for (File f : files) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("key", f.getName());
                entry.put("title", f.getName());
                entry.put("isLeaf", !f.isDirectory());
                entry.put("isDirectory", f.isDirectory());
                children.add(entry);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("key", currentKey != null ? currentKey : "");
        result.put("title", dir.getName());
        result.put("children", children);
        return GmsApiResult.success(result);
    }

    @POST
    @Path("/tree/read")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult readFile(Map<String, Object> body, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        authorize(token);

        String currentKey = body != null ? (String) body.get("currentKey") : null;
        String title = body != null ? (String) body.get("title") : null;

        if (currentKey == null && title == null) {
            return GmsApiResult.error(40000, "Missing file path");
        }

        String filePath = currentKey != null ? currentKey : title;
        var path = Paths.get(BASE_PATH, filePath);

        if (!Files.exists(path) || Files.isDirectory(path)) {
            return GmsApiResult.error(40400, "File not found");
        }

        try {
            String content = Files.readString(path);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("content", content);
            result.put("path", filePath);
            return GmsApiResult.success(result);
        } catch (IOException e) {
            return GmsApiResult.error(50000, "Failed to read file: " + e.getMessage());
        }
    }

    @POST
    @Path("/tree/write")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public GmsApiResult writeFile(Map<String, Object> body, @HeaderParam(ApiConstants.TOKEN_HEADER) String token) {
        User user = authorize(token);
        if (user.getAccountType() != AccountType.Admin) {
            return GmsApiResult.error(40300, "Admin only");
        }

        String currentKey = body != null ? (String) body.get("currentKey") : null;
        String title = body != null ? (String) body.get("title") : null;
        String content = body != null ? (String) body.get("content") : null;

        if (currentKey == null && title == null) {
            return GmsApiResult.error(40000, "Missing file path");
        }
        if (content == null) {
            return GmsApiResult.error(40000, "Missing content");
        }

        String filePath = currentKey != null ? currentKey : title;
        var path = Paths.get(BASE_PATH, filePath);

        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, content);
            return GmsApiResult.success("File saved successfully");
        } catch (IOException e) {
            return GmsApiResult.error(50000, "Failed to write file: " + e.getMessage());
        }
    }
}
