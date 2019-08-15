package com.wootecobook.turkey.comment.controller.api;

import com.wootecobook.turkey.comment.service.CommentService;
import com.wootecobook.turkey.comment.service.dto.CommentCreate;
import com.wootecobook.turkey.comment.service.dto.CommentResponse;
import com.wootecobook.turkey.comment.service.dto.CommentUpdate;
import com.wootecobook.turkey.commons.resolver.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentApiController {
    private static final Logger log = LoggerFactory.getLogger(CommentApiController.class);

    private final CommentService commentService;

    public CommentApiController(final CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public ResponseEntity<Page<CommentResponse>> list(@PageableDefault(size = 5, sort = "createdAt") Pageable pageable,
                                                      @PathVariable Long postId) {
        log.info("postId : {}, page : {}, pageSize : {}, sort : {}", postId, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        final Page<CommentResponse> commentResponses = commentService.findCommentResponsesByPostId(postId, pageable);

        return ResponseEntity.ok(commentResponses);
    }

    //TODO /{id}는 comment 하나만 조회인데 자식List 조회해도 괜찮은지?
    @GetMapping("/{id}")
    public ResponseEntity<Page<CommentResponse>> childCommentList(@PageableDefault(size = 5, sort = "createdAt") Pageable pageable,
                                                                  @PathVariable Long id) {
        log.info("id : {}, page : {}, pageSize : {}, sort : {}", id, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        final Page<CommentResponse> commentResponses = commentService.findCommentResponsesByParentId(id, pageable);

        return ResponseEntity.ok(commentResponses);
    }

    @PostMapping
    public ResponseEntity<CommentResponse> create(@RequestBody CommentCreate commentCreate,
                                                  @PathVariable Long postId,
                                                  UserSession userSession) {
        log.info("postId : {}", postId);

        final CommentResponse commentResponse = commentService.save(commentCreate, userSession.getId(), postId);
        final URI uri = linkTo(CommentApiController.class, postId).toUri();
        return ResponseEntity.created(uri).body(commentResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity delete(@PathVariable Long id,
                                 @PathVariable Long postId,
                                 UserSession userSession) {
        log.info("id : {}", id);

        commentService.delete(id, userSession.getId());

        final URI uri = linkTo(CommentApiController.class, postId).toUri();
        return ResponseEntity.noContent().location(uri).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentResponse> update(@RequestBody CommentUpdate commentUpdate,
                                                  @PathVariable Long id,
                                                  UserSession userSession) {
        log.info("id : {}", id);

        CommentResponse commentResponse = commentService.update(commentUpdate, id, userSession.getId());

        return ResponseEntity.ok(commentResponse);
    }
}

