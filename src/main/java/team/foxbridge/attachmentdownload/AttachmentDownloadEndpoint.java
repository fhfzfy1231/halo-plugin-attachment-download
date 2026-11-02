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
        if (request.queryParam("check").filter("1"::equals).isPresent()) {
            return ServerResponse.noContent().build();
        }

        String attachmentName = request.pathVariable("name");
        return client.fetch(Attachment.class, attachmentName)
            .flatMap(attachment -> proxyAttachment(request, attachment))
            .switchIfEmpty(ServerResponse.notFound().build());
    }

    private Mono<ServerResponse> proxyAttachment(ServerRequest request, Attachment attachment) {
        Attachment.AttachmentStatus status = attachment.getStatus();
        if (status == null || status.getPermalink() == null || status.getPermalink().isBlank()) {
            return ServerResponse.notFound().build();
        }

        URI sourceUri;
        try {
            sourceUri = URI.create(status.getPermalink());
            if (!sourceUri.isAbsolute()) {
                sourceUri = request.uri().resolve(sourceUri);
            }
        } catch (IllegalArgumentException ignored) {
            return ServerResponse.notFound().build();
        }

        Attachment.AttachmentSpec spec = attachment.getSpec();
        String filename = spec != null ? spec.getDisplayName() : null;
        if (filename == null || filename.isBlank()) {
            filename = attachment.getMetadata().getName();
        }

        Flux<DataBuffer> body = webClient.get()
            .uri(sourceUri)
            .retrieve()
            .bodyToFlux(DataBuffer.class);

        ServerResponse.BodyBuilder response = ServerResponse.ok();
        response.header(
            CONTENT_DISPOSITION,
            ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8)
                .build()
                .toString()
        );
        response.header(CACHE_CONTROL, "private, no-store");
        response.header("X-Content-Type-Options", "nosniff");

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (spec != null && spec.getMediaType() != null && !spec.getMediaType().isBlank()) {
            try {
                mediaType = MediaType.parseMediaType(spec.getMediaType());
            } catch (IllegalArgumentException ignored) {
            }
        }
        response.contentType(mediaType);

        if (spec != null && spec.getSize() != null && spec.getSize() > 0) {
            response.contentLength(spec.getSize());
        }

        return response.body(body, DataBuffer.class);
    }
}
