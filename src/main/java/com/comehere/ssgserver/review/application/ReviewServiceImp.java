package com.comehere.ssgserver.review.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.comehere.ssgserver.image.application.ImageService;
import com.comehere.ssgserver.image.domain.ReviewImage;
import com.comehere.ssgserver.image.dto.ImageReqDTO;
import com.comehere.ssgserver.image.infrastructure.ReviewImageRepository;
import com.comehere.ssgserver.member.infrastructure.MemberRepository;
import com.comehere.ssgserver.review.domain.Review;
import com.comehere.ssgserver.review.vo.ReviewCreateVO;
import com.comehere.ssgserver.review.infrastructure.ReviewRepository;
import com.comehere.ssgserver.review.vo.ReviewUpdateVo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewServiceImp implements ReviewService {
	private final ReviewRepository reviewRepository;

	private final MemberRepository memberRepository;

	private final ReviewImageRepository reviewImageRepository;

	private final ImageService imageService;

	@Override
	@Transactional
	public void createReview(ReviewCreateVO reviewCreateVO) {
		reviewRepository.findByPurchaseListId(reviewCreateVO.getPurchaseListId())
				.ifPresent(review -> {
					throw new IllegalArgumentException("이미 리뷰를 작성하셨습니다.");
				});

		Review review = Review.builder()
				.member(memberRepository.findById(reviewCreateVO.getMemberId())
						.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 멤버입니다.")))
				.itemCode(reviewCreateVO.getItemCode())
				.purchaseListId(reviewCreateVO.getPurchaseListId())
				.star(reviewCreateVO.getStar())
				.content(reviewCreateVO.getContent())
				.build();

		reviewRepository.save(review);
		createReviewImages(reviewCreateVO.getImages(), review);
	}

	@Override
	public void deleteReview(Long reviewId) {
		Review review = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));

		// 리뷰의 모든 이미지 삭제
		imageService.deleteReviewImages(reviewId);

		reviewRepository.delete(review);
	}

	@Override
	@Transactional
	public void updateReview(ReviewUpdateVo reviewUpdateVo) {
		Review review = reviewRepository.findById(reviewUpdateVo.getReviewId())
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));

		Review updateReview = Review.builder()
				.id(review.getId())
				.star(reviewUpdateVo.getStar())
				.content(reviewUpdateVo.getContent())
				.build();

		reviewRepository.save(updateReview);
	}

	private void createReviewImages(List<ImageReqDTO> images, Review review) {
		if (images.size() > 3) {
			throw new IllegalArgumentException("리뷰 이미지는 최대 3개까지 등록 가능합니다.");
		}

		images.forEach(image -> {
			ReviewImage reviewImage = ReviewImage.builder()
					.review(review)
					.imageUrl(image.getImageUrl())
					.alt(image.getAlt())
					.build();

			reviewImageRepository.save(reviewImage);
		});
	}
}
