package com.ssafy.dangdang.controller;

import com.ssafy.dangdang.config.security.CurrentUser;
import com.ssafy.dangdang.config.security.auth.PrincipalDetails;
import com.ssafy.dangdang.domain.Study;
import com.ssafy.dangdang.domain.User;
import com.ssafy.dangdang.domain.dto.CommentDto;
import com.ssafy.dangdang.domain.dto.MakeStudy;
import com.ssafy.dangdang.domain.dto.StudyDto;
import com.ssafy.dangdang.domain.dto.WriteComment;
import com.ssafy.dangdang.domain.types.CommentType;
import com.ssafy.dangdang.domain.types.UserRoleType;
import com.ssafy.dangdang.service.CommentService;
import com.ssafy.dangdang.service.StorageService;
import com.ssafy.dangdang.service.StudyService;
import com.ssafy.dangdang.util.ApiUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.ssafy.dangdang.util.ApiUtils.*;

@RestController
@RequestMapping("/study")
//@CrossOrigin(origins = {"http://localhost:3000"}, allowedHeaders = "*")
@RequiredArgsConstructor
@Slf4j
public class StudyController {

    private final StudyService studyService;
    private final CommentService commentService;

    private final StorageService storageService;

    @Operation(summary = "????????? ??????", description = "????????? ?????? ???????????? ????????? ????????? ?????? ??????, ?????? ????????? ????????? ?????? ????????? ???????????? ????????????.")
    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "????????? ?????? ??????")
    })
    @GetMapping()
    public ApiResult<Page<StudyDto>> getAllStudies(@RequestParam(required = false)
                                                       @Parameter(description = "??????????????? ???????????? ????????? ??? ??????")
                                                               List<String> hashtags,
                                                   @ParameterObject Pageable pageable){
        Page<StudyDto> allStudies = studyService.getAllStudies(hashtags, pageable);

        return  success(allStudies);
    }

    @Operation(summary = "????????? ?????? ??????")
    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "????????? ?????? ?????? ??????")
    })
    @GetMapping("/{studyId}")
    public ApiResult<StudyDto> getStudy(@Parameter(description = "????????? ????????? id", example = "1") @PathVariable Long studyId,
                                        @ParameterObject Pageable pageable){

        StudyDto studyWithUsers = studyService.findStudyWithUsers(studyId);
        Page<CommentDto> commentDtos = commentService.findCommentByReferenceIdWithPage(studyId, CommentType.STUDY, pageable);
        studyWithUsers.setCommentDtos(commentDtos);
        return  success(studyWithUsers);
    }


    @Operation(summary = "????????? ?????????")
    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "????????? ????????? ??????")
    })
    @PostMapping()
    @PreAuthorize("hasRole('USER')")
    public ApiResult<StudyDto> createStudy(@CurrentUser PrincipalDetails userPrincipal
            ,@RequestBody @Valid MakeStudy makeStudy){

        User user = userPrincipal.getUser();
        log.info(user.toString());
        StudyDto study = studyService.createStudy(user, StudyDto.of(makeStudy));
        return success(study);

    }


    @Operation(summary = "????????? ?????? ??????")
    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "????????? ?????? ??????")
    })
//    @CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", methods = {RequestMethod.PUT})
    @PatchMapping("/{studyId}")
    @PreAuthorize("hasRole('USER')")
    public ApiResult<StudyDto> updateStudy(@CurrentUser PrincipalDetails userPrincipal,
            @Parameter(description = "????????? ????????? id", example = "1")  @PathVariable Long studyId,
            @RequestBody @Valid MakeStudy makeStudy){
        log.info("=======================================");
        log.info("????????? ??????");
        log.info("=======================================");
        User user = userPrincipal.getUser();
        log.info(user.toString());
        StudyDto studyDto = StudyDto.of(makeStudy);
        studyDto.setId(studyId);
        StudyDto study = studyService.updateStudy(user, studyDto);
        return success(study);
    }

    @Operation(summary = "????????? ??????", description = "?????????????????? ???????????? ????????? ??? ????????????.")
    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "????????? ?????? ??????")
    })
    @DeleteMapping("/{studyId}")
    @PreAuthorize("hasRole('USER')")
    public ApiResult<String> deleteStudy(@CurrentUser PrincipalDetails userPrincipal
            ,@Parameter(description = "????????? ????????? id", example = "1")  @PathVariable Long studyId){
        User user = userPrincipal.getUser();
        return studyService.deleteStudy(user, studyId);

    }

    @Operation(summary = "????????? ?????? ??????", description = "parentId??? ?????? ??????????????? ??????")
    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "????????? ?????? ?????? ??????")
    })
    @PostMapping("/{studyId}/comment")
    public ApiResult<CommentDto> writeComment(@CurrentUser PrincipalDetails userPrincipal,
                                              @Parameter(description = "????????? ????????? ????????? id", example = "1") @PathVariable Long studyId,
                                              @RequestBody WriteComment writeComment){
        CommentDto commentDto = CommentDto.of(writeComment);
        commentDto.setReferenceId(studyId);
        commentDto.setCommentType(CommentType.STUDY);
        return success(commentService.writeComment(userPrincipal.getUser(), commentDto));
    }

    @Operation(summary = "????????? ?????? ??????")
    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "????????? ?????? ?????? ??????")
    })
    @DeleteMapping("/{studyId}/comment/{commentId}")
    public ApiResult<String> deleteComment(@CurrentUser PrincipalDetails userPrincipal,
                                           @Parameter(description = "????????? ?????? id") @PathVariable String commentId){
        return commentService.deleteComment(userPrincipal.getUser(), commentId);
    }

    @Operation(summary = "????????? ?????? ??????")
    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "????????? ?????? ?????? ??????")
    })
    @PatchMapping("/{studyId}/comment/{commentId}")
    public ApiResult<CommentDto> updateComment(@CurrentUser PrincipalDetails userPrincipal,
                                               @Parameter(description = "????????? ?????? id") @PathVariable String commentId,
                                               @RequestBody WriteComment writeComment){
        CommentDto commentDto = CommentDto.of(writeComment);
        commentDto.setId(commentId);
        commentDto.setCommentType(CommentType.STUDY);
        return commentService.updateComment(userPrincipal.getUser(), commentDto);
    }

    @Operation(summary = "????????? ????????? ??????")
    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "????????? ????????? ?????? ??????")
    })
    @PostMapping(value = "/{studyId}/image", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasRole('USER')")
    public ApiResult<String> uploadImage(@CurrentUser PrincipalDetails userPrincipal,
                                         @PathVariable Long studyId,
                                         @Parameter(
                                                 description = "???????????? ?????????",
                                                 content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)  // Won't work without OCTET_STREAM as the mediaType.
                                         )@RequestParam("image")  MultipartFile image) throws IOException {
        log.info("????????? image ?????? {}", image.getOriginalFilename());
        UUID uuid = UUID.randomUUID();
        storageService.imageStore(uuid.toString(), image);
        studyService.uploadImage(userPrincipal.getUser(), studyId,  uuid.toString(), image);
        return success("?????? ??????");
    }

    @Operation(summary = "????????? ????????? ??????")
    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "????????? ????????? ?????? ??????")
    })
    @PatchMapping(value = "/{studyId}/image", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasRole('USER')")
    public ApiResult<String> updateImage(@CurrentUser PrincipalDetails userPrincipal,
                                         @PathVariable Long studyId,
                                         @Parameter(
                                                 description = "????????? ?????????",
                                                 content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)  // Won't work without OCTET_STREAM as the mediaType.
                                         ) MultipartFile image) throws IOException {

        String studyImageUrl = studyService.getImageUrl(studyId);
        if(studyImageUrl != null) storageService.deleteImage(studyImageUrl);

        log.info("????????? image ?????? {}", image.getOriginalFilename());
        UUID uuid = UUID.randomUUID();
        storageService.imageStore(uuid.toString(), image);
        studyService.uploadImage(userPrincipal.getUser(), studyId, uuid.toString(), image);
        return success("?????? ??????");
    }
}
