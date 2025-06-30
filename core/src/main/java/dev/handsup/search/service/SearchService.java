package dev.handsup.search.service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.handsup.auction.domain.product.product_category.PreferredProductCategory;
import dev.handsup.auction.domain.product.product_category.ProductCategory;
import dev.handsup.auction.dto.mapper.AuctionMapper;
import dev.handsup.auction.repository.auction.AuctionRepository;
import dev.handsup.auction.repository.auction.AuctionSearchRepository;
import dev.handsup.auction.repository.product.PreferredProductCategoryRepository;
import dev.handsup.auction.repository.search.RedisSearchRepository;
import dev.handsup.common.dto.CommonMapper;
import dev.handsup.common.dto.PageResponse;
import dev.handsup.recommend.dto.RecommendAuctionResponse;
import dev.handsup.search.dto.AuctionSearchCondition;
import dev.handsup.search.dto.AuctionSearchMapper;
import dev.handsup.search.dto.AuctionSearchResponse;
import dev.handsup.search.dto.PopularKeywordsResponse;
import dev.handsup.search.dto.SearchMapper;
import dev.handsup.user.domain.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchService {
	private final AuctionRepository auctionRepository;
	private final AuctionSearchRepository auctionSearchRepository;
	private final RedisSearchRepository redisSearchRepository;
	private final PreferredProductCategoryRepository preferredProductCategoryRepository;

	@Transactional(readOnly = true)
	public PageResponse<AuctionSearchResponse> searchAuctions(AuctionSearchCondition condition, Pageable pageable) {
		Slice<AuctionSearchResponse> auctionResponsePage = auctionRepository
			.searchAuctions(condition, pageable)
			.map(AuctionMapper::toAuctionSearchResponse);
		redisSearchRepository.increaseSearchCount(condition.keyword());

		return CommonMapper.toPageResponse(auctionResponsePage);
	}

	@Transactional(readOnly = true)
	public PageResponse<AuctionSearchResponse> searchAuctionsV2(AuctionSearchCondition condition, Pageable pageable) {
		Slice<AuctionSearchResponse> auctionResponsePage = auctionSearchRepository
			.searchAuctions(condition, pageable)
			.map(AuctionSearchMapper::toAuctionSearchResponse);
		redisSearchRepository.increaseSearchCount(condition.keyword());

		return CommonMapper.toPageResponse(auctionResponsePage);
	}

	@Transactional(readOnly = true)
	public PageResponse<RecommendAuctionResponse> getRecommendAuctions(String si, String gu, String dong,
		Pageable pageable) {
		Slice<RecommendAuctionResponse> auctionResponsePage = auctionSearchRepository
			.sortAuctionByCriteria(si, gu, dong, pageable)
			.map(AuctionSearchMapper::toRecommendAuctionResponse);
		return CommonMapper.toPageResponse(auctionResponsePage);
	}

	@Transactional(readOnly = true)
	public PageResponse<RecommendAuctionResponse> getUserPreferredCategoryAuctions(User user, Pageable pageable) {
		List<String> productCategories = preferredProductCategoryRepository.findByUser(user)
			.stream()
			.map(PreferredProductCategory::getProductCategory)  // ProductCategory 추출
			.map(ProductCategory::getValue)                     // String value 추출
			.toList();

		Slice<RecommendAuctionResponse> auctionResponsePage = auctionSearchRepository
			.findByProductCategories(productCategories, pageable)
			.map(AuctionSearchMapper::toRecommendAuctionResponse);

		return CommonMapper.toPageResponse(auctionResponsePage);
	}

	@Transactional(readOnly = true)
	public PopularKeywordsResponse getPopularKeywords() {
		return SearchMapper.toPopularKeywordsResponse(redisSearchRepository.getPopularKeywords(10));
	}
}
