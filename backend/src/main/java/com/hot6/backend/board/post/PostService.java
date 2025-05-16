package com.hot6.backend.board.post;

import com.hot6.backend.board.comment.CommentService;
import com.hot6.backend.board.post.images.PostImageService;
import com.hot6.backend.board.post.model.BoardType;
import com.hot6.backend.board.post.model.Post;
import com.hot6.backend.board.post.model.PostDto;
import com.hot6.backend.board.post.model.PostListResponse;
import com.hot6.backend.category.model.Category;
import com.hot6.backend.category.model.CategoryRepository;
import com.hot6.backend.common.BaseResponseStatus;
import com.hot6.backend.common.exception.BaseException;
import com.hot6.backend.pet.PetRepository;
import com.hot6.backend.pet.model.Pet;
import com.hot6.backend.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {
    private final PostImageService postImageService;
    private final CommentService commentService;
    private final PostRepository postRepository;
    private final BoardTypeRepository boardTypeRepository;
    private final CategoryRepository categoryRepository;
    private final PetRepository petRepository;

    @Transactional(readOnly = false)
    public void create(User user, PostDto.PostRequest dto, List<MultipartFile> images) {
        BoardType boardType = boardTypeRepository.findByBoardName(dto.getBoardType())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.POST_BOARD_NOT_FOUND));

        Category category = categoryRepository.findById(dto.getCategoryIdx())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.CATEGORY_NOT_FOUND));

        Post post = Post.builder()
                .user(user)
                .title(dto.getTitle())
                .content(dto.getContent())
                .category(category)
                .boardType(boardType)
                .build();

        try {
            postRepository.save(post);
            if (dto.getPetIdxList() != null) {
                List<Pet> pets = petRepository.findAllById(dto.getPetIdxList());
                pets.forEach(p -> p.setPost(post));
                petRepository.saveAll(pets);
            }
            if (images != null && !images.isEmpty()) {
                postImageService.saveImages(images, post);
            }
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.POST_CREATE_FAILED);
        }
    }

    public PostListResponse list(String boardName, int page, int size) {
        BoardType boardType = boardTypeRepository.findByBoardName(boardName)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.POST_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> result = postRepository.findByBoardType(boardType, pageable);
        List<PostDto.PostResponse> posts = result.map(PostDto.PostResponse::from).toList();

        int totalPages = result.getTotalPages();
        int currentPage = result.getNumber() + 1; // 1-based 페이지 번호
        int pageGroupSize = 10;
        int pageGroupStart = ((currentPage - 1) / pageGroupSize) * pageGroupSize + 1;
        int pageGroupEnd = Math.min(pageGroupStart + pageGroupSize - 1, totalPages);

        List<Integer> visiblePages = new java.util.ArrayList<>();
        for (int i = pageGroupStart; i <= pageGroupEnd; i++) {
            visiblePages.add(i);
        }

        return new PostListResponse(posts, currentPage, totalPages, pageGroupStart, pageGroupEnd, visiblePages);
    }

    public PostDto.PostResponse read(Long idx) {
        Post post = postRepository.findWithImagesById(idx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.POST_NOT_FOUND));
        return PostDto.PostResponse.from(post);
    }

    public PostListResponse search(String boardName, String categoryName, String keyword, int page, int size) {
        BoardType boardType = boardTypeRepository.findByBoardName(boardName)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.POST_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> result;

        if (keyword != null && !keyword.isBlank()) {
            if (categoryName == null || categoryName.isBlank()) {
                result = postRepository.searchByKeywordOnly(boardType, keyword, pageable);
            } else {
                result = postRepository.searchByCategoryAndKeyword(boardType, categoryName, keyword, pageable);
            }
        } else {
            if (categoryName == null || categoryName.isBlank()) {
                result = postRepository.findByBoardType(boardType, pageable);
            } else {
                result = postRepository.findByBoardTypeAndCategoryName(boardType, categoryName, pageable);
            }
        }

        List<PostDto.PostResponse> posts = result.map(PostDto.PostResponse::from).toList();

        int totalPages = result.getTotalPages();
        int currentPage = result.getNumber() + 1;
        int pageGroupSize = 10;
        int pageGroupStart = ((currentPage - 1) / pageGroupSize) * pageGroupSize + 1;
        int pageGroupEnd = Math.min(pageGroupStart + pageGroupSize - 1, totalPages);

        List<Integer> visiblePages = new java.util.ArrayList<>();
        for (int i = pageGroupStart; i <= pageGroupEnd; i++) {
            visiblePages.add(i);
        }

        return new PostListResponse(posts, currentPage, totalPages, pageGroupStart, pageGroupEnd, visiblePages);
    }


    @Transactional(readOnly = false)
    public void delete(Long idx) {
        Post post = postRepository.findById(idx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.POST_NOT_FOUND));

        try {
            List<Pet> pets = petRepository.findAllByPost(post);
            pets.forEach(p -> p.setPost(null));
            petRepository.saveAll(pets);

            postRepository.delete(post);
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.POST_DELETE_FAILED);
        }
    }

    @Transactional(readOnly = false)
    public void update(Long idx, PostDto.PostRequest dto, List<MultipartFile> images) {
        Post post = postRepository.findById(idx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.POST_NOT_FOUND));

        BoardType boardType = boardTypeRepository.findByBoardName(dto.getBoardType())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.POST_UPDATE_FAILED));
        Category category = categoryRepository.findById(dto.getCategoryIdx())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.POST_UPDATE_FAILED));

        try {
            post.setTitle(dto.getTitle());
            post.setContent(dto.getContent());
            post.setBoardType(boardType);
            post.setCategory(category);
            postRepository.save(post);

            List<Pet> oldPets = petRepository.findAllByPost(post);
            oldPets.forEach(p -> p.setPost(null));
            petRepository.saveAll(oldPets);

            if (dto.getPetIdxList() != null) {
                List<Pet> newPets = petRepository.findAllById(dto.getPetIdxList());
                newPets.forEach(p -> p.setPost(post));
                petRepository.saveAll(newPets);
            }

            if (dto.getRemovedImageUrls() != null) {
                postImageService.deleteImagesByUrls(dto.getRemovedImageUrls());
            }

            if (images != null && !images.isEmpty()) {
                postImageService.saveImages(images, post);
            }
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.POST_UPDATE_FAILED);
        }
    }

    @Transactional(readOnly = true)
    public List<PostDto.UserPostResponse> findUserPosts(Long userId) {
        return postRepository.findByUserIdxAndIsDeletedFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(PostDto.UserPostResponse::from)
                .toList();
    }
}
