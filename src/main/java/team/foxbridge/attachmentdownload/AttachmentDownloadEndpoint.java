package team.foxbridge.attachmentdownload;

import static org.springframework.http.HttpHeaders.CACHE_CONTROL;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * Serves attachment bytes only after Halo's RBAC layer authorizes the request.
 */
@Component
public class AttachmentDownloadEndpoint implements CustomEndpoint {

    private static final GroupVersion GROUP_VERSION =
        new GroupVersion("api.attachment-download.foxbridge.team", "v1alpha1");

    private final ReactiveExtensionClient client;
    private final WebClient webClient;

    public AttachmentDownloadEndpoint(ReactiveExtensionClient client) {
        this.client = client;
        this.webClient = WebClient.builder().build();
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        return RouterFunctions.route()
            .GET("/downloads/{name}", this::download)
            .build();
    }

    @Override
    public GroupVersion groupVersion() {
        return GROUP_VERSION;
    }

    private Mono<ServerResponse> download(ServerRequest request) {
        // The front-end performs this lightweight request first. Reaching this
        // handler already means Halo RBAC granted the download permission.
        if (request.queryParam("check").filter("1"::equals).isPresent()) {
            return ServerResponse.noContent().build();
        }

        var attachmentName = request.pathVariable("name");
        return client.fetch(Attachment.class, attachmentName)
            .flatMap(attachment -> proxyAttachment(request, attachment))
            .switchIfEmpty(ServerResponse.notFound().build());
    }

    private Mono<ServerResponse> proxyAttachment(ServerRequest request, Attachment attachment) {
        var status = attachment.getStatus();
        if (status == null || status.getPermalink() == null || status.getPermalink().isBlank()) {
            return ServerResponse.notFound().build();
        }

        URI sourceUri;
        try {
            sourceUri = URI.create(status.getPermalink());
            if (!sourceUri.isAbsolute()) {
                sourceUri = request.uri().resolve(sourceUri);
            }
        } catch (IllegalArgumentException ex) {
            return ServerResponse.notFound().build();
        }

        var spec = attachment.getSpec();
        var filename = spec != null && spec.getDisplayName() != null && !spec.getDisplayName().isBlank()
            ? spec.getDisplayName()
            : attachment.getMetadata().getName();

        Flux<DataBuffer> body = webClient.get()
            .uri(sourceUri)
            .retrieve()
            .bodyToFlux(DataBuffer.class);

        var response = ServerResponse.ok()
            .header(CONTENT_DISPOSITION,
                ContentDisposition.attachment()
                    .filename(filename, StandardCharsets.UTF_8)
                    .build()
                    .toString())
            .header(CACHE_CONTROL, "private, no-store")
            .header("X-Content-Type-Options", "nosniff");

        if (spec != null && spec.getMediaType() != null && !spec.getMediaType().isBlank()) {
            try {
                response.contentType(MediaType.parseMediaType(spec.getMediaType()));
            } catch (IllegalArgumentException ignored) {
                response.contentType(MediaType.APPLICATION_OCTET_STREAM);
            }
        } else {
            response.contentType(MediaType.APPLICATION_OCTET_STREAM);
        }

        if (spec != null && spec.getSize() != null && spec.getSize() > 0) {
            response.contentLength(spec.getSize());
        }

        return response.body(body, DataBuffer.class);
    }
}
