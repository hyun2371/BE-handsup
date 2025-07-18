package dev.handsup.bidding.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.handsup.auction.domain.Auction;
import dev.handsup.auction.exception.AuctionErrorCode;
import dev.handsup.auction.repository.auction.AuctionRepository;
import dev.handsup.bidding.domain.Bidding;
import dev.handsup.bidding.dto.BiddingMapper;
import dev.handsup.bidding.dto.request.RegisterBiddingRequest;
import dev.handsup.bidding.dto.response.BiddingResponse;
import dev.handsup.bidding.exception.BiddingErrorCode;
import dev.handsup.bidding.repository.BiddingRepository;
import dev.handsup.common.exception.NotFoundException;
import dev.handsup.common.exception.ValidationException;
import dev.handsup.notification.domain.NotificationType;
import dev.handsup.notification.service.NotificationSender;
import dev.handsup.user.domain.User;
import lombok.RequiredArgsConstructor;

/**
 * 입찰 생성 알림 비동기/동기 성능 테스트
 */
@Service
@RequiredArgsConstructor
public class BiddingNotificationService {
	private final BiddingRepository biddingRepository;
	private final AuctionRepository auctionRepository;
	private final ApplicationEventPublisher eventPublisher;
	private final NotificationSender notificationSender;

	@Transactional
	public BiddingResponse registerBidding(RegisterBiddingRequest request, Long auctionId, User bidder) {
		Auction auction = auctionRepository
			.findByIdWithPessimisticLock(auctionId)
			.orElseThrow(() -> new NotFoundException(AuctionErrorCode.NOT_FOUND_AUCTION));

		validateBiddingPrice(request.biddingPrice(), auction);
		updateAuctionOnNewBidding(request, auction);
		Bidding bidding = BiddingMapper.toBidding(request.biddingPrice(), auction, bidder);
		notificationSender.sendNotification(
			bidder.getId(),
			auction.getSeller().getId(),
			auction.getSeller().getNickname(),
			auction.getId(),
			NotificationType.BIDDING_CREATED
		);

		return BiddingMapper.toBiddingResponse(biddingRepository.save(bidding));
	}

	@Transactional
	public BiddingResponse registerBiddingWithAsync(RegisterBiddingRequest request, Long auctionId, User bidder) {
		Auction auction = auctionRepository
			.findByIdWithPessimisticLock(auctionId)
			.orElseThrow(() -> new NotFoundException(AuctionErrorCode.NOT_FOUND_AUCTION));

		validateBiddingPrice(request.biddingPrice(), auction);
		updateAuctionOnNewBidding(request, auction);
		Bidding bidding = BiddingMapper.toBidding(request.biddingPrice(), auction, bidder);

		eventPublisher.publishEvent(new NotificationEvent(
			bidder.getId(),
			auction.getSeller().getId(),
			auction.getSeller().getNickname(),
			auction.getId(),
			NotificationType.BIDDING_CREATED
		));

		return BiddingMapper.toBiddingResponse(biddingRepository.save(bidding));
	}

	public void validateBiddingPrice(int biddingPrice, Auction auction) {
		Integer maxBiddingPrice = biddingRepository.findMaxBiddingPriceByAuctionId(auction.getId());

		if (maxBiddingPrice == null) {
			// 입찰 내역이 없는 경우, 최소 입찰가부터 입찰 가능
			if (biddingPrice < auction.getInitPrice()) {
				throw new ValidationException(BiddingErrorCode.BIDDING_PRICE_LESS_THAN_INIT_PRICE);
			}
		} else {
			// 최고 입찰가보다 1000원 이상일 때만 입찰 가능
			if (biddingPrice < (maxBiddingPrice + 1000)) {
				throw new ValidationException(BiddingErrorCode.BIDDING_PRICE_NOT_HIGH_ENOUGH);
			}
		}
	}

	private void updateAuctionOnNewBidding(RegisterBiddingRequest request, Auction auction) {
		auction.updateCurrentBiddingPrice(request.biddingPrice()); // 경매 입찰 최고가 갱신
		auction.increaseBiddingCount(); // 경매 입찰 수 + 1
	}
}
