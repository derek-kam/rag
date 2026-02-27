package com.example.rag.api;

import com.example.rag.model.QueryRequest;
import com.example.rag.model.QueryResponse;
import com.example.rag.model.UploadResponse;
import com.example.rag.service.RagService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.InputStream;

@Path("/rag")
@Produces(MediaType.APPLICATION_JSON)
public class RagResource {

    @Inject
    RagService ragService;

    @POST
    @Path("/documents")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadDocument(@FormDataParam("file") InputStream file,
                                   @FormDataParam("file") FormDataContentDisposition fileMeta,
                                   @FormDataParam("file") org.glassfish.jersey.media.multipart.FormDataBodyPart bodyPart) {
        if (file == null || fileMeta == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Missing multipart field named 'file'")
                    .build();
        }

        UploadResponse response = ragService.ingest(
                fileMeta.getFileName(),
                bodyPart.getMediaType().toString(),
                file);

        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @POST
    @Path("/query")
    @Consumes(MediaType.APPLICATION_JSON)
    public QueryResponse query(@Valid QueryRequest request) {
        int maxResults = request.maxResults() <= 0 ? 5 : request.maxResults();
        return ragService.query(request.query(), maxResults);
    }
}
